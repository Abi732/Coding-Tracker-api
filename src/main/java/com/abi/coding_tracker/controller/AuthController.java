package com.abi.coding_tracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.dto.LoginRequest;
import com.abi.coding_tracker.dto.LoginResponse;
import com.abi.coding_tracker.dto.UserRequest;
import com.abi.coding_tracker.dto.UserResponse;
import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.mapper.UserMapper;
import com.abi.coding_tracker.security.JwtService;
import com.abi.coding_tracker.service.UserService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserMapper userMapper;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService,UserMapper userMapper){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest userRequest){
        log.info("Received registration request for Email: {}", userRequest.getEmail());
        User user = userMapper.toEntity(userRequest) ;
        User savedUser = userService.saveUser(user);
        UserResponse response = userMapper.toResponse(savedUser);
        log.info("Succesfully Registered user with ID: {}", savedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        log.info("Received login request from email : {}", loginRequest.getEmail());
        //checks whether the password matches
        try{

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
    
            String token = jwtService.generateToken(loginRequest.getEmail());
            log.info("Succesfully Authenticated user : {}", loginRequest.getEmail());
            return ResponseEntity.ok(new LoginResponse(token));
        }catch(Exception e){
            log.warn("Failed login Attempt for email : {}", loginRequest.getEmail());
            throw e;
        }
        
    }
}
