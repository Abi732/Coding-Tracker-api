package com.abi.coding_tracker.leetcode.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectLeetcodeRequest {
    
    @NotBlank(message = "Leetcode username cannot be blank")
    private String username;
}

