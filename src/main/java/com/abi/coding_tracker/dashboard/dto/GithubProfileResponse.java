package com.abi.coding_tracker.dashboard.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubProfileResponse {
    private String username;
    private int publicRepos;
    private int followers;
    private int totalCommits;

    private boolean cached;
    private LocalDateTime lastUpdated;
}
