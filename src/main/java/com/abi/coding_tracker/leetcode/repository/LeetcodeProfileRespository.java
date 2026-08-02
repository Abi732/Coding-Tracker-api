package com.abi.coding_tracker.leetcode.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.leetcode.entity.LeetcodeProfile;

@Repository
public interface LeetcodeProfileRespository extends JpaRepository<LeetcodeProfile, Long> {
    
    Optional<LeetcodeProfile> findByUser(User user);

    boolean existsByUsername(String username);

    boolean existsByUser(User user);
}
