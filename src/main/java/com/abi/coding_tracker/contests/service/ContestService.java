package com.abi.coding_tracker.contests.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.abi.coding_tracker.contests.dto.ContestResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ContestService {
    public List<ContestResponse> getUpcomingContests(){
        log.info("Fetching upcoming contests (Skeleton implementation)");

        List<ContestResponse> upcoming = new ArrayList<>();

        upcoming.add(new ContestResponse("1", "Codeforces Round 1005 (Div. 2)", "Codeforces", System.currentTimeMillis() / 1000 + 86400, 7200));
        upcoming.add(new ContestResponse("2", "Weekly Contest 450", "LeetCode", System.currentTimeMillis() / 1000 + 172800, 5400));
        
        return upcoming;
    }
}
