package com.abi.coding_tracker.contests.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestResponse {
    private String platform;
    private String contestId;
    private String name;
    private LocalDateTime startTime;
    private int durationSeconds;
    private String url;
    private String status;
    private Long startsInMinutes;
}
