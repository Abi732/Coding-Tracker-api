package com.abi.coding_tracker.github.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectGithubRequest {
    
    @NotBlank(message = "Github username cannot be blank")
    private String username;
}
