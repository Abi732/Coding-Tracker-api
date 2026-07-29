package com.abi.coding_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Must be a valid Email access")
    private String email;

    @NotBlank(message = "password cannot be blank")
    private String password;
}
