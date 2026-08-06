package com.abi.coding_tracker.codeforces.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CodeforcesProfileResponse {
    private String handle;

    private Integer rating;
    private Integer maxRating;

    private String rank;
    private String maxRank;

    private Integer contribution;
    private Integer friendOfCount;

    private boolean cached;
    private LocalDateTime lastUpdated;
}
