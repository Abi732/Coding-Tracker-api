package com.abi.coding_tracker.github.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.github.dto.ConnectGithubRequest;
import com.abi.coding_tracker.github.dto.GithubProfileResponse;
import com.abi.coding_tracker.github.dto.RepositoryResponse;
import com.abi.coding_tracker.github.service.GithubProfileService;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/github")
public class GithubController {
    
    private final GithubProfileService githubProfileService;

    public GithubController(GithubProfileService githubProfileService){
        this.githubProfileService = githubProfileService;
    }

    @PostMapping("/connect")
    public ResponseEntity<String> connectGithub(@Valid @RequestBody ConnectGithubRequest request, Authentication authentication){
        String email = authentication.getName();

        log.info("User [{}] requesting to connect GitHub handle: {}", email, request.getUsername());

        githubProfileService.connectAccount(request.getUsername(), email);

        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully connected Github account: "+request.getUsername());
    }

    @GetMapping("/me")
    public ResponseEntity<GithubProfileResponse> getMyGithubStats(Authentication authentication){
        String email = authentication.getName();
        return ResponseEntity.ok(githubProfileService.fetchAndSaveMyProfile(email));
    }

    @GetMapping("/repos")
    public ResponseEntity<List<RepositoryResponse>> getMyRepositories(Authentication authentication){
        String email = authentication.getName();
        return ResponseEntity.ok(githubProfileService.fetchMyRepositories(email));
    }

    @GetMapping("/languages")
    public ResponseEntity<Map<String, Double>> getMyLanguageStats(Authentication authentication){
        String email = authentication.getName();
        return ResponseEntity.ok(githubProfileService.getLanguageDistribution(email));
    }
}
