package com.abi.coding_tracker.github.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abi.coding_tracker.github.entity.GithubProfile;
import com.abi.coding_tracker.github.entity.GithubSnapshot;

@Repository
public interface GithubSnapshotRepository extends JpaRepository<GithubSnapshot, Long> {
    Optional<GithubSnapshot> findTopByProfileOrderByFetchedAtDesc(GithubProfile profile);
    
}
