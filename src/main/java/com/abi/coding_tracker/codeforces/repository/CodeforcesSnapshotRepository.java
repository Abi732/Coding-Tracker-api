package com.abi.coding_tracker.codeforces.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abi.coding_tracker.codeforces.dto.CodeforcesSnapshot;
import com.abi.coding_tracker.codeforces.entity.CodeforcesProfile;

@Repository
public interface CodeforcesSnapshotRepository extends JpaRepository<CodeforcesSnapshot, Long> {
    
    Optional<CodeforcesSnapshot> findTopByProfileOrderByFetchedAtDesc(CodeforcesProfile profile);

    List<CodeforcesSnapshot> findAllByProfileOrderByFetchedAtAsc(CodeforcesProfile profile);
}
