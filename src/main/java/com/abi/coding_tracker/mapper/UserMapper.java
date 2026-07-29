package com.abi.coding_tracker.mapper;

import org.springframework.stereotype.Component;

import com.abi.coding_tracker.dto.UserRequest;
import com.abi.coding_tracker.dto.UserResponse;
import com.abi.coding_tracker.entity.User;

@Component
public class UserMapper {
    
    //DTO->Entity
    public User toEntity(UserRequest request){
        if(request == null) return null;

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        return user;
    }

    //Entity->DTO
    public UserResponse toResponse(User user){
        if(user==null) return null;

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    
}
