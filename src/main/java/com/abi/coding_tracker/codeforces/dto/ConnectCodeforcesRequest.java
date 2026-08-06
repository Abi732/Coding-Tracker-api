package com.abi.coding_tracker.codeforces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectCodeforcesRequest {
    
    @NotBlank(message = "Codeforces handle cannot be blank")
    private String handle;
}
