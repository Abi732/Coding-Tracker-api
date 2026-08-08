package com.abi.coding_tracker.profile.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.abi.coding_tracker.analytics.service.AnalyticsService;
import com.abi.coding_tracker.codeforces.dto.CodeforcesSnapshot;
import com.abi.coding_tracker.codeforces.entity.CodeforcesProfile;
import com.abi.coding_tracker.codeforces.repository.CodeForcesProfileRepository;
import com.abi.coding_tracker.codeforces.repository.CodeforcesSnapshotRepository;
import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.exception.ResourceNotFoundException;
import com.abi.coding_tracker.leetcode.entity.LeetcodeProfile;
import com.abi.coding_tracker.leetcode.entity.LeetcodeStatsSnapshot;
import com.abi.coding_tracker.leetcode.repository.LeetcodeProfileRespository;
import com.abi.coding_tracker.leetcode.repository.LeetcodeStatsSnapshotsRepository;
import com.abi.coding_tracker.profile.dto.ResumeStatsResponse;
import com.abi.coding_tracker.profile.dto.UserProfileResponse;
import com.abi.coding_tracker.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProfileService {
    
    private final UserRepository userRepository;
    private final LeetcodeProfileRespository leetcodeProfileRespository;
    private final CodeForcesProfileRepository codeForcesProfileRepository;
    private final LeetcodeStatsSnapshotsRepository leetcodeStatsSnapshotsRepository;
    private final CodeforcesSnapshotRepository codeforcesSnapshotRepository;
    private final AnalyticsService analyticsService;

    public ProfileService(UserRepository userRepository,
                            LeetcodeProfileRespository leetcodeProfileRespository,
                            CodeForcesProfileRepository codeForcesProfileRepository,
                            LeetcodeStatsSnapshotsRepository leetcodeStatsSnapshotsRepository,
                            CodeforcesSnapshotRepository codeforcesSnapshotRepository,
                            AnalyticsService analyticsService
    ){
        this.userRepository = userRepository;
        this.leetcodeProfileRespository = leetcodeProfileRespository;
        this.codeForcesProfileRepository = codeForcesProfileRepository;
        this.leetcodeStatsSnapshotsRepository = leetcodeStatsSnapshotsRepository;
        this.codeforcesSnapshotRepository = codeforcesSnapshotRepository;
        this.analyticsService = analyticsService;
    }

    public UserProfileResponse getUserProfile(String email){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        String leetcodeHandle = leetcodeProfileRespository.findByUser(user).map(LeetcodeProfile::getUsername).orElse(null);
        String cfHandle = codeForcesProfileRepository.findByUser(user).map(CodeforcesProfile::getHandle).orElse(null);

        int streak = analyticsService.getStreak(email).getCurrentStreak();

        return UserProfileResponse.builder()
                .name(user.getName())
                .leetcode(leetcodeHandle)
                .codeforces(cfHandle)
                .streak(streak)
                .joined(user.getCreatedAt() != null ? user.getCreatedAt().getYear() + "":  "2026")
                .build();
    }

    public ResumeStatsResponse getResumeStats(String email){
        User user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        int totalSolved = leetcodeProfileRespository.findByUser(user)
                        .flatMap(leetcodeStatsSnapshotsRepository::findTopByProfileOrderByFetchedAtDesc)
                        .map(LeetcodeStatsSnapshot::getTotalSolved)
                        .orElse(0);

        Integer maxRating = codeForcesProfileRepository.findByUser(user)
                            .flatMap(codeforcesSnapshotRepository::findTopByProfileOrderByFetchedAtDesc)
                            .map(CodeforcesSnapshot::getMaxRating)
                            .orElse(0);

        long daysActive = user.getCreatedAt() != null?
                        ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now())
                        :0;
        
        int platforms = 0;
        if(leetcodeProfileRespository.existsByUser(user)) platforms++;
        if(codeForcesProfileRepository.existsByUser(user)) platforms++;

        return ResumeStatsResponse.builder()
                .totalSolved(totalSolved)
                .maxRating(maxRating)
                .daysActive(daysActive)
                .currentStreak(analyticsService.getStreak(email).getCurrentStreak())
                .platformsConnected(platforms)
                .build();
    }
    
}
