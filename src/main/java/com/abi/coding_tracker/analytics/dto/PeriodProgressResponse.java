package com.abi.coding_tracker.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodProgressResponse {
    private int startSolved;
    private int endSolved;
    private int growth;
}
