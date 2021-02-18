
package com.mlewicki12.melofyapi.controllers;

import com.google.gson.Gson;
import com.mlewicki12.melofyapi.SpotifyRepository;
import com.mlewicki12.melofyapi.models.SpotifyAuthResponse;
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

@RestController
@RequestMapping("/spotify")
public class SpotifyController {
    private final WebClient webClient;

    @Autowired
    private SpotifyRepository spotifyRepository;

    @Autowired
    private Gson gson;

    @Value("${MELOFY_ID:not set}")
    private String client_id;

    @Value("${MELOFY_SECRET:not set}")
    private String client_secret;

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
        SpotifyAuthResponse user = gson.fromJson(userJson, SpotifyAuthResponse.class);

        spotifyRepository.save(user);

        attributes.addAttribute("access_token", user.getAccessToken());
        return new RedirectView("http://localhost:4200");
    }
}
