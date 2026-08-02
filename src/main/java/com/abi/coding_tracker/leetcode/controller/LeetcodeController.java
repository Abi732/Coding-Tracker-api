package com.abi.coding_tracker.leetcode.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.leetcode.dto.ConnectLeetcodeRequest;
import com.abi.coding_tracker.leetcode.dto.LeetcodeProfileResponse;
import com.abi.coding_tracker.leetcode.dto.LeetcodeStatsResponse;
import com.abi.coding_tracker.leetcode.entity.LeetcodeProfile;
import com.abi.coding_tracker.leetcode.service.LeetcodeProfileService;
import com.abi.coding_tracker.leetcode.service.LeetcodeService;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/leetcode")
public class LeetcodeController {
    
    private final LeetcodeService leetcodeService;
    private final LeetcodeProfileService leetcodeProfileService;
    
    public LeetcodeController(LeetcodeService leetcodeService, LeetcodeProfileService leetcodeProfileService){
        this.leetcodeService = leetcodeService;
        this.leetcodeProfileService = leetcodeProfileService;
    }

    @PostMapping("/connect")
    public ResponseEntity<LeetcodeProfileResponse> connectLeetcode(@Valid @RequestBody ConnectLeetcodeRequest request, Authentication authentication){
        String email = authentication.getName();

        log.info("Authenticated user [{}] request to connect Leetcode profile: {}",email,request.getUsername());

        LeetcodeProfile savedProfile = leetcodeProfileService.connectAccount(request.getUsername(), email);

        LeetcodeProfileResponse response = new LeetcodeProfileResponse(
            savedProfile.getUsername(),
            savedProfile.getConnectedAt(),
            savedProfile.getLastSyncedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
    @GetMapping("/{username}")
    public ResponseEntity<LeetcodeStatsResponse> getLeetcodeProfile(@PathVariable String username){
        log.info("REST request to fetch leetcode profile for: {}", username);

        LeetcodeStatsResponse response = leetcodeService.fetchUserProfile(username);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<LeetcodeStatsResponse> getMyLeetcodeStats(Authentication authentication){
        String mail = authentication.getName();

        log.info("Authenticated User [{}] requested their leetcode stats", mail);

        LeetcodeStatsResponse response = leetcodeProfileService.fetchAndSaveMyStats(mail);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/refresh")
    public ResponseEntity<LeetcodeStatsResponse> refreshMyLeetcodeStats(Authentication authentication){
        String email = authentication.getName();
        log.info("Authenticated user [{}] requested a FORCED refresh of their leetcode stats", email);

        LeetcodeStatsResponse stats = leetcodeProfileService.fetchAndSaveMyStats(email, true);

        return ResponseEntity.ok(stats);
    }
}
