package com.abi.coding_tracker.codeforces.dto;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.abi.coding_tracker.codeforces.entity.CodeforcesProfile;

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
@Table(name = "codeforces_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class CodeforcesSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private CodeforcesProfile profile;

    private Integer rating;
    private Integer maxRating;
    private String rank;

    @CreationTimestamp
    private LocalDateTime fetchedAt;
}
