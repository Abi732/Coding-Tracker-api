package com.abi.coding_tracker.github.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


import com.abi.coding_tracker.exception.ExternalApiException;
import com.abi.coding_tracker.github.dto.GithubProfileResponse;
import com.abi.coding_tracker.github.dto.RepositoryResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GithubClient {
    
    private final WebClient webClient;

    public GithubClient(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder
                    .baseUrl("https://api.github.com/")
                    .defaultHeader("User-Agent", "Coding-Tracker-Application")
                    .build();
    }

    public GithubProfileResponse fetchProfile(String username){
        log.info("GithubClient: Fetching profile data for {}", username);

        return webClient.get()
                .uri("users/{username}", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response->
                    Mono.error(new ExternalApiException("Github User not found: "+username, response.statusCode().value())))
                .onStatus(HttpStatusCode::is5xxServerError, response->
                    Mono.error(new ExternalApiException("Github Api is currently down.", response.statusCode().value())))
                .bodyToMono(GithubProfileResponse.class)
                .block();
    }

    public List<RepositoryResponse> fetchRepository(String username){
        log.info("GithubClient: Fetching repositories for {}", username);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/{username}/repos")
                        .queryParam("sort", "updated")
                        .queryParam("per_page", "100")
                        .build(username))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response->
                    Mono.error(new ExternalApiException("Github User not found : "+username, response.statusCode().value())))
                .onStatus(HttpStatusCode::is5xxServerError, response->
                    Mono.error(new ExternalApiException("Github API is currently down.", response.statusCode().value())))
                .bodyToMono(new ParameterizedTypeReference<List<RepositoryResponse>>() {})
                .block();
    }
}
