package com.abi.coding_tracker.codeforces.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abi.coding_tracker.codeforces.entity.CodeforcesProfile;
import com.abi.coding_tracker.entity.User;

@Repository
public interface CodeForcesProfileRepository extends JpaRepository<CodeforcesProfile, Long> {
    Optional<CodeforcesProfile> findByUser(User user);

    boolean existsByHandle(String handle);

    boolean existsByUser(User user);
}
