package com.abi.coding_tracker.leetcode.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.abi.coding_tracker.leaderboard.dto.LeaderBoardResponse;
import com.abi.coding_tracker.leetcode.entity.LeetcodeProfile;
import com.abi.coding_tracker.leetcode.entity.LeetcodeStatsSnapshot;

@Repository
public interface LeetcodeStatsSnapshotsRepository extends JpaRepository<LeetcodeStatsSnapshot, Long>{
    Optional<LeetcodeStatsSnapshot> findTopByProfileOrderByFetchedAtDesc(LeetcodeProfile profile);

    List<LeetcodeStatsSnapshot> findAllByProfileOrderByFetchedAtAsc(LeetcodeProfile profile);

    List<LeetcodeStatsSnapshot> findTop2ByProfileOrderByFetchedAtDesc(LeetcodeProfile profile);

    @Query("SELECT new com.abi.coding_tracker.leaderboard.dto.LeaderboardResponse(0, u.name, s.totalSolved)"+
            "FROM LeetcodeStatsSnapshot s"+
            "JOIN s.profile p"+
            "JOIN p.user u"+
            "WHERE s.id IN (SELECT MAX(s2.id) FROM LeetcodeStatsSnapshot s2 GROUP BY s2.profile)"+
            "ORDER BY s.totalSolved DESC")
    List<LeaderBoardResponse> getGlobalLeaderBoard();
}
