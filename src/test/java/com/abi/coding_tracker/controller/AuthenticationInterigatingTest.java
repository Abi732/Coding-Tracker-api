package com.abi.coding_tracker.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.abi.coding_tracker.dto.LoginRequest;
import com.abi.coding_tracker.dto.UserRequest;
import com.abi.coding_tracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "app.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970" 
})
public class AuthenticationInterigatingTest {
    
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    private UserRequest userRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp(){

        userRequest = new UserRequest();
        userRequest.setName("Integration Tester");
        userRequest.setEmail("integration@test.com");
        userRequest.setPassword("securePassword123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("integration@test.com");
        loginRequest.setPassword("securePassword123");
    }

    @Test
    void testRegister_ShouldReturn201Created() throws Exception{

        mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("integration@test.com"));

    }

    @Test
    void testAccessProtectedEndpoint_WithoutToken_ShouldReturn401() throws Exception{
        mockMvc.perform(get("/users")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_ShouldReturnJwtToken() throws Exception{
        //1. first register the user first
        mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isCreated());
        
        //2.now login with the login request
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testAccessProtectedEndpoint_WithValidToken_ShouldSucceed() throws Exception{
        //1.registerionInterigatingTest': Uns
        mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)));

        //2. Login and extract token from the response
        MvcResult result = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andReturn();
        
        String responseString = result.getResponse().getContentAsString();
        String token = JsonPath.parse(responseString).read("$.token");

        //3. try to hit the protected endpoint with token
        mockMvc.perform(get("/users")
            .header("Authorization", "Bearer"+token)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());   
    }
}
