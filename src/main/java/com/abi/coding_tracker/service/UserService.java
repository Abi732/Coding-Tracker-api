package com.abi.coding_tracker.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.exception.DuplicateResourceException;
import com.abi.coding_tracker.exception.ResourceNotFoundException;
import com.abi.coding_tracker.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User saveUser(User user){

        if(userRepository.existsByEmail(user.getEmail())){
            throw new DuplicateResourceException("An account with this mail already exist");
        }
        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);
        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
        .orElseThrow(()->new ResourceNotFoundException("User not Found with id: "+id));
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
        .orElseThrow(()->new ResourceNotFoundException("user not found with email: "+email));
    }
}
