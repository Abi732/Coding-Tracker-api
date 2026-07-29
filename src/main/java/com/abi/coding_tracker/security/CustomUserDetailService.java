package com.abi.coding_tracker.security;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.repository.UserRepository;


@Service
public class CustomUserDetailService implements UserDetailsService{
    private final UserRepository userRepository;

    public CustomUserDetailService (UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException{
        User user = userRepository.findByEmail(mail)
        .orElseThrow(()->new UsernameNotFoundException("user not found with mail :"+mail));

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            new ArrayList<>()
        );
    }
}
