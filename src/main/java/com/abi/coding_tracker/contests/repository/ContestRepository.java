package com.abi.coding_tracker.contests.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abi.coding_tracker.contests.entity.Contest;
import com.abi.coding_tracker.contests.entity.ContestStatus;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {
    List<Contest> findAllByStatusOrderByStartTimeAsc(ContestStatus status);

    List<Contest> findAllByStartTimeBeforeAndStatus(LocalDateTime time, ContestStatus status);

    Optional<Contest> findByContestId(String contestId);
    
} 
