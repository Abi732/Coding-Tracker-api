package com.abi.coding_tracker.leetcode.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeetcodeProfileResponse {
    
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime lastSyncedAt;
}
