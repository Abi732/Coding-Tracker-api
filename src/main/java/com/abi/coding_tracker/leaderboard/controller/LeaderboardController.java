package com.abi.coding_tracker.leaderboard.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.leaderboard.dto.LeaderBoardResponse;
import com.abi.coding_tracker.leaderboard.service.LeaderBoardService;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {
    
    private final LeaderBoardService leaderBoardService;

    public LeaderboardController(LeaderBoardService leaderBoardService){
        this.leaderBoardService = leaderBoardService;
    }

    @GetMapping
    public ResponseEntity<List<LeaderBoardResponse>> getLeaderboard(){
        return ResponseEntity.ok(leaderBoardService.getGlobalLeaderboard());
    }
}
