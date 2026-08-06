package com.abi.coding_tracker.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryResponse {
    private int leetcodeSolved;
    private int leetcodeGrowth;          //will represent 7 day growth
    private Integer codeforcesRating;
    private int currentStreak;
    private int bestDay;
}
