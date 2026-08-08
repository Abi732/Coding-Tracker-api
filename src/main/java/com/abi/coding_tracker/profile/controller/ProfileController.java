package com.abi.coding_tracker.profile.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.profile.dto.ResumeStatsResponse;
import com.abi.coding_tracker.profile.dto.UserProfileResponse;
import com.abi.coding_tracker.profile.service.ProfileService;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService){
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication){
        return ResponseEntity.ok(profileService.getUserProfile(authentication.getName()));
    }

    @GetMapping("/resume-stats")
    public ResponseEntity<ResumeStatsResponse> getResumeStats(Authentication authentication){
        return ResponseEntity.ok(profileService.getResumeStats(authentication.getName()));
    }
}
