package com.abi.coding_tracker.contests.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestResponse {
    private String id;
    private String name;
    private String platform;
    private long StartTime;
    private int durationSeconds;
}
