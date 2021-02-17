
package com.mlewicki12.melofyapi.controllers;

import com.google.gson.Gson;
import com.mlewicki12.melofyapi.MelofyApiApplication;
import com.mlewicki12.melofyapi.SpotifyRepository;
import com.mlewicki12.melofyapi.models.SpotifyAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import reactor.core.publisher.Mono;

import javax.print.attribute.standard.Media;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {
    private final WebClient webClient;

    @Autowired
    private SpotifyRepository spotifyRepository;

    @Autowired
    private Gson gson;

    public SpotifyController() {
        webClient = WebClient.create("http://localhost:8080");
    }

    @GetMapping("/authorize")
    public RedirectView authorize(RedirectAttributes attributes) {
        attributes.addAttribute("client_id", "11d45ba62abd4480bea0d54ad7e9c685");
        attributes.addAttribute("response_type", "code");
        attributes.addAttribute("redirect_uri", "http://localhost:8080/spotify/callback");
        return new RedirectView("https://accounts.spotify.com/authorize");
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam(value = "code") String code, RedirectAttributes attributes) {
        final String uri = "https://accounts.spotify.com/api/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", "http://localhost:8080/spotify/callback");
        params.add("client_id", "11d45ba62abd4480bea0d54ad7e9c685");
        params.add("client_secret", "c5bdb38cbcb041efad81d5c5e47b15de");

        var request = this.webClient.post()
            .uri(uri)
            .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("code", code)
                    .with("redirect_uri", "http://localhost:8080/spotify/callback")
                    .with("client_id", "11d45ba62abd4480bea0d54ad7e9c685")
                    .with("client_secret", "c5bdb38cbcb041efad81d5c5e47b15de"))
            .headers(httpHeaders -> {
                httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            }).retrieve().bodyToMono(String.class);

        String userJson = request.block();
        SpotifyAuthResponse user = gson.fromJson(userJson, SpotifyAuthResponse.class);

        spotifyRepository.save(user);

        attributes.addAttribute("access_token", user.getAccessToken());
        return new RedirectView("http://localhost:4200");
        /*
        if(response.getStatusCode() == HttpStatus.OK) {
            System.out.println(response.getBody());
            var user = response.getBody();
            System.out.println(user);
            spotifyRepository.save(user);
            attributes.addAttribute("access_token", user.getAccessToken());
            attributes.addAttribute("user", user.getUuid());
        } else {
            System.out.println("POST Request for Access Token failed!");
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Error body:");
            System.out.println(response.getBody());

            attributes.addAttribute("error", true);
        }
        */
    }
}
