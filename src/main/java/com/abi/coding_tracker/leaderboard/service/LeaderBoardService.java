package com.abi.coding_tracker.leaderboard.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.abi.coding_tracker.leaderboard.dto.LeaderBoardResponse;
import com.abi.coding_tracker.leaderboard.dto.LeaderboardProjection;
import com.abi.coding_tracker.leetcode.repository.LeetcodeStatsSnapshotsRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LeaderBoardService {
    private final LeetcodeStatsSnapshotsRepository leetcodeStatsSnapshotsRepository;

    public LeaderBoardService(LeetcodeStatsSnapshotsRepository leetcodeStatsSnapshotsRepository){
        this.leetcodeStatsSnapshotsRepository = leetcodeStatsSnapshotsRepository;
    }

    public List<LeaderBoardResponse> getGlobalLeaderboard(){
        log.info("Fetching Global leaderboard ....");

        List<LeaderboardProjection> rawData = leetcodeStatsSnapshotsRepository.getGlobalLeaderboard();

        List<LeaderBoardResponse> leaderboard = new ArrayList<>();
        int currentRank = 1;
        for(LeaderboardProjection proj : rawData){
            LeaderBoardResponse entry = new LeaderBoardResponse(currentRank++, proj.getName(), proj.getScore());
            leaderboard.add(entry);
        }

        return leaderboard;
    }
}
