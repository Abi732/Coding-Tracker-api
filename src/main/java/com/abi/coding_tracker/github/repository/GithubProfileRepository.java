package com.abi.coding_tracker.github.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.github.entity.GithubProfile;

@Repository
public interface GithubProfileRepository extends JpaRepository<GithubProfile, Long> {

    Optional<GithubProfile> findByUser(User user);

    boolean existsByUsername(String username);

    boolean existsByUser(User user);

    
}
