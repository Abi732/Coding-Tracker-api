package com.abi.coding_tracker.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeStatsResponse {
    private int totalSolved;
    private Integer maxRating;
    private long daysActive;
    private int currentStreak;
    private int platformsConnected;
}
