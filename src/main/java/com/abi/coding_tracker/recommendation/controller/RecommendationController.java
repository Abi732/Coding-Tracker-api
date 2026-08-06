package com.abi.coding_tracker.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.recommendation.dto.RecommendationResponse;
import com.abi.coding_tracker.recommendation.service.RecommendationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService){
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<RecommendationResponse> getMyRecommendation(Authentication authentication){
        String email = authentication.getName();
        log.info("User [{}] requested personalized recommendations", email);

        RecommendationResponse response =recommendationService.generateRecommendation(email);
        return ResponseEntity.ok(response);
    }
}
