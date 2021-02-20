
package com.mlewicki12.melofyapi.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mlewicki12.melofyapi.SpotifyRepository;
import com.mlewicki12.melofyapi.models.SpotifyAuth;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.User;
import io.netty.util.internal.StringUtil;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {
    private class ApiError {
        private final String message;
        private final Exception exception;

        public ApiError(String message, Exception exception) {
            this.message = message;
            this.exception = exception;
        }
    }

    private final WebClient webClient;

    @Autowired
    private SpotifyRepository spotifyRepository;

    @Autowired
    private Gson gson;

    @Value("${MELOFY_ID:not set}")
    private String client_id;

    @Value("${MELOFY_SECRET:not set}")
    private String client_secret;

    private SpotifyApi spotifyApi;

    public SpotifyController() {
        webClient = WebClient.create("http://localhost:8080");
    }

    @GetMapping("/authorize")
    public RedirectView authorize(RedirectAttributes attributes) {
        attributes.addAttribute("client_id", this.client_id);
        attributes.addAttribute("response_type", "code");
        attributes.addAttribute("redirect_uri", "http://localhost:8080/spotify/callback");
        return new RedirectView("https://accounts.spotify.com/authorize");
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam(value = "code") String code, RedirectAttributes attributes) {
        final String uri = "https://accounts.spotify.com/api/token";

        var request = this.webClient.post()
            .uri(uri)
            .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("code", code)
                    .with("redirect_uri", "http://localhost:8080/spotify/callback")
                    .with("client_id", this.client_id)
                    .with("client_secret", this.client_secret))
            .headers(httpHeaders -> {
                httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            }).retrieve().bodyToMono(String.class);

        String userJson = request.block();
        SpotifyAuth user = gson.fromJson(userJson, SpotifyAuth.class);

        spotifyRepository.save(user);

        // todo figure out how this handles with more than one user
        // i think i need a user map here or smth idk i dont have the social skills for networking
        spotifyApi = new SpotifyApi.Builder()
            .setAccessToken(user.getAccessToken())
            .build();

        attributes.addAttribute("user_id", user.getUuid());
        return new RedirectView("http://localhost:4200");
    }

    @GetMapping("/user")
    public ResponseEntity<Object> userInfo(@RequestParam(required = false) UUID user_id) {
        final String uri = "https://api.spotify.com/v1/me";

        if(user_id == null) {
            if(StringUtil.isNullOrEmpty(spotifyApi.getAccessToken())) {
                return new ResponseEntity<>(
                    new ApiError("Melofy: no user_id provided and access token not found", null),
                    HttpStatus.BAD_REQUEST
                );
            }
        } else {
            try {
                SpotifyAuth userOpt = spotifyRepository.findById(user_id).get();
                spotifyApi.setAccessToken(userOpt.getAccessToken());
            } catch(NoSuchElementException exception) {
                // todo write a test for non-existent user
                return new ResponseEntity<>(
                    new ApiError(String.format("Melofy: no user found with user_id %s", user_id), null),
                    HttpStatus.BAD_REQUEST
                );
            }
        }

        try {
            User profile = spotifyApi.getCurrentUsersProfile().build().execute();
            return new ResponseEntity<>(
                profile,
                HttpStatus.OK
            );
        } catch(IOException | SpotifyWebApiException | ParseException exception) {
            return new ResponseEntity<>(
                    new ApiError("Melofy: encountered exception on request", exception),
                    HttpStatus.BAD_GATEWAY
            );
        }
    }
}
