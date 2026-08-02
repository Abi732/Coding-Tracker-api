package com.abi.coding_tracker.leetcode.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abi.coding_tracker.leetcode.entity.LeetcodeProfile;
import com.abi.coding_tracker.leetcode.entity.LeetcodeStatsSnapshot;

@Repository
public interface LeetcodeStatsSnapshotsRepository extends JpaRepository<LeetcodeStatsSnapshot, Long>{
    Optional<LeetcodeStatsSnapshot> findTopByProfileOrderByFetchedAtDesc(LeetcodeProfile profile);
}
