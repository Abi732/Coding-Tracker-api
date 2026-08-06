package com.abi.coding_tracker.codeforces.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.codeforces.dto.CodeforcesProfileResponse;
import com.abi.coding_tracker.codeforces.dto.ConnectCodeforcesRequest;
import com.abi.coding_tracker.codeforces.dto.ContestHistoryResponse;
import com.abi.coding_tracker.codeforces.service.CodeforcesProfileService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/codeforces")
public class CodeforcesController {
    
    private final CodeforcesProfileService codeforcesProfileService;

    public CodeforcesController(CodeforcesProfileService codeforcesProfileService){
        this.codeforcesProfileService = codeforcesProfileService;
    }

    @PostMapping("/connect")
    public ResponseEntity<String> connectCodeforces(@Valid @RequestBody ConnectCodeforcesRequest request, Authentication authentication){
        String email = authentication.getName();
        log.info("User [{}] requesting to connect Codeforces handle: {}", email, request.getHandle());

        codeforcesProfileService.connectAccount(request.getHandle(), email);

        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully connected Codeforces account: "+request.getHandle());
    }

    @GetMapping("/me")
    public ResponseEntity<CodeforcesProfileResponse> getMyCodeforcesStats(Authentication authentication){
        String email = authentication.getName();
        log.info("User [{}] requested their Codeforces stats", email);

        CodeforcesProfileResponse stats = codeforcesProfileService.fetchAndSaveMyStats(email);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ContestHistoryResponse>> getMyContestHistory(Authentication authentication){
        String email = authentication.getName();
        log.info("User [{}] requested their codeforces contest history.", email);

        List<ContestHistoryResponse> history = codeforcesProfileService.findMyContestHistory(email);

        return ResponseEntity.ok(history);
    } 
}
