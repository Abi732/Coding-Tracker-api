package com.abi.coding_tracker.leetcode.dto;


import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LeetcodeStatsResponse {
    private String username;
    private int ranking;
    private int reputation;

    private int totalSolved;
    private int easySolved;
    private int mediumSolved;
    private int hardSolved;

    private boolean cached;
    private LocalDateTime lastUpdated;
}
