package com.abi.coding_tracker.leetcode.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "leetcode_stats_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class LeetcodeStatsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name="profile_id", nullable = false)
    private LeetcodeProfile profile;

    @Column(nullable = false)
    private int totalSolved;

    @Column(nullable = false)
    private int easySolved;

    @Column(nullable = false)
    private int mediumSolved;

    @Column(nullable = false)
    private int hardSolved;

    private int ranking;

    @CreationTimestamp
    private LocalDateTime fetchedAt;
}
