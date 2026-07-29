package com.abi.coding_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private String type;

    public LoginResponse(String token){
        this.token = token;
        this.type = "Bearer";
    }
}
