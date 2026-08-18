package com.abi.coding_tracker.leetcode.service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List; 
import java.util.Optional;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.exception.DuplicateResourceException;
import com.abi.coding_tracker.exception.ExternalApiException;
import com.abi.coding_tracker.exception.ResourceNotFoundException;
import com.abi.coding_tracker.leetcode.dto.LeetcodeStatsResponse; 
import com.abi.coding_tracker.leetcode.entity.LeetcodeProfile;
import com.abi.coding_tracker.leetcode.entity.LeetcodeStatsSnapshot;
import com.abi.coding_tracker.leetcode.repository.LeetcodeProfileRespository;
import com.abi.coding_tracker.leetcode.repository.LeetcodeStatsSnapshotsRepository;
import com.abi.coding_tracker.platform.CodingPlatformService;
import com.abi.coding_tracker.repository.UserRepository;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LeetcodeProfileService implements CodingPlatformService {

    private final UserRepository userRepository;
    private final LeetcodeProfileRespository profileRepository;
    private final LeetcodeStatsSnapshotsRepository snapshotRepository; 
    private final LeetcodeService leetcodeService;

    public LeetcodeProfileService(UserRepository userRepository, 
                                  LeetcodeProfileRespository profileRepository, 
                                  LeetcodeStatsSnapshotsRepository snapshotRepository,
                                  LeetcodeService leetcodeService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.snapshotRepository = snapshotRepository;
        this.leetcodeService = leetcodeService;
    }

    @Override
    public String getPlatformName(){
        return "leetcode";
    }

    @Override
    public LocalDateTime getLastSync(User user){
        return profileRepository.findByUser(user).map(LeetcodeProfile :: getLastSyncedAt).orElse(null);
    }

    @Override
    public void refresh(User user){
        profileRepository.findByUser(user).ifPresent(profile->{
            log.info("Interface triggered refresh for leetcode user: {} ", user.getEmail());
            syncProfileStats(profile, true);
        });
    }

    @Transactional 
    public LeetcodeProfile connectAccount(String leetcodeUsername, String userEmail) {
        log.info("Attempting to connect LeetCode account '{}' to user '{}'", leetcodeUsername, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (profileRepository.existsByUser(user)) {
            throw new DuplicateResourceException("You have already connected a LeetCode account. Only one account is allowed.");
        }

        if (profileRepository.existsByUsername(leetcodeUsername)) {
            throw new DuplicateResourceException("The LeetCode username '" + leetcodeUsername + "' is already connected to another user.");
        }

        
        try {
            leetcodeService.fetchUserProfile(leetcodeUsername);
        } catch (ExternalApiException e) {
            
            throw e; 
        } catch (Exception e) {
            log.error("Error verifying LeetCode profile", e);
            throw new ExternalApiException("Failed to verify LeetCode account.", 500);
        }

    
        LeetcodeProfile profile = new LeetcodeProfile();
        profile.setUsername(leetcodeUsername);
        profile.setUser(user);
        profile.setLastSyncedAt(LocalDateTime.now()); 

        LeetcodeProfile savedProfile = profileRepository.save(profile);
        log.info("Successfully connected LeetCode account '{}' to user '{}'", leetcodeUsername, userEmail);
        
        return savedProfile;
    }

    @Transactional
    public LeetcodeStatsResponse fetchAndSaveMyStats(String userEmail, boolean forceRefresh) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LeetcodeProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("No LeetCode account connected. Please connect an account first."));

  
        return syncProfileStats(profile, forceRefresh);
    }

    @Transactional
    public LeetcodeStatsResponse syncProfileStats(LeetcodeProfile profile, boolean forceRefresh) {
        LocalDateTime lastSynced = profile.getLastSyncedAt();
        LocalDateTime now = LocalDateTime.now();

        if (!forceRefresh && lastSynced != null && Duration.between(lastSynced, now).toMinutes() < 30) {
            log.info("Returning cached LeetCode stats for user '{}' (Last synced: {} minutes ago)", 
                     profile.getUsername(), Duration.between(lastSynced, now).toMinutes());
            return buildCachedResponse(profile);
        }

        log.info("Fetching fresh stats from LeetCode API for linked account: {}", profile.getUsername());

        try {
    
            LeetcodeStatsResponse currentStats = leetcodeService.fetchUserProfile(profile.getUsername());

        
            Optional<LeetcodeStatsSnapshot> latestSnapshotOpt = snapshotRepository.findTopByProfileOrderByFetchedAtDesc(profile);
            
            boolean isDuplicate = latestSnapshotOpt.map(latest -> 
                latest.getTotalSolved() == currentStats.getTotalSolved() &&
                latest.getEasySolved() == currentStats.getEasySolved() &&
                latest.getMediumSolved() == currentStats.getMediumSolved() &&
                latest.getHardSolved() == currentStats.getHardSolved() &&
                latest.getRanking() == currentStats.getRanking()
            ).orElse(false);

            if (isDuplicate) {
                log.info("Fresh stats match latest snapshot for {}. Skipping duplicate save.", profile.getUsername());
            } else {
                LeetcodeStatsSnapshot snapshot = new LeetcodeStatsSnapshot();
                snapshot.setProfile(profile);
                snapshot.setTotalSolved(currentStats.getTotalSolved());
                snapshot.setEasySolved(currentStats.getEasySolved());
                snapshot.setMediumSolved(currentStats.getMediumSolved());
                snapshot.setHardSolved(currentStats.getHardSolved());
                snapshot.setRanking(currentStats.getRanking());
                
                snapshotRepository.save(snapshot);
                log.info("Successfully saved new stats snapshot for {}", profile.getUsername());
            }

            profile.setLastSyncedAt(now);
            profileRepository.save(profile);


            currentStats.setCached(false);
            currentStats.setLastUpdated(now);
            return currentStats;

        } catch (ExternalApiException e) {
     
            if (e.getStatusCode() >= 500) {
                log.warn("LeetCode API is down (Status {}). Falling back to cached snapshot for {}", e.getStatusCode(), profile.getUsername());
                return buildCachedResponse(profile);
            }
     
            throw e; 
        }
    }


    private LeetcodeStatsResponse buildCachedResponse(LeetcodeProfile profile) {
        LeetcodeStatsSnapshot latestSnapshot = snapshotRepository.findTopByProfileOrderByFetchedAtDesc(profile)
                .orElseThrow(() -> new ExternalApiException("LeetCode is unavailable and no historical data exists.", 503));
        
        LeetcodeStatsResponse response = mapSnapshotToResponse(latestSnapshot, profile.getUsername());
        response.setCached(true);
        response.setLastUpdated(profile.getLastSyncedAt());
        return response;
    }

  
    @Scheduled(cron = "0 0 2 * * ?") 
    public void nightlyLeetcodeSync() {
        log.info("Starting nightly LeetCode stats sync...");
        List<LeetcodeProfile> profiles = profileRepository.findAll();
        
        for (LeetcodeProfile profile : profiles) {
            try {
                log.info("Scheduled sync for: {}", profile.getUsername());
                syncProfileStats(profile, true); 
            } catch (Exception e) {
                log.error("Failed to sync profile {} during nightly job: {}", profile.getUsername(), e.getMessage());
            }
        }
        log.info("Finished nightly LeetCode stats sync.");
    }

    @Cacheable(value = "leetcodeStats" , key = "#userEmail")
    @Transactional
    public LeetcodeStatsResponse fetchAndSaveMyStats(String userEmail) {
        return fetchAndSaveMyStats(userEmail, false);
    }

    @CachePut(value = "leetcodeStats", key = "#userEmail")
    @Transactional
    public LeetcodeStatsResponse refreshMyStats(String userEmail){
        log.info("Forcing cache update via @CachePut for {}", userEmail);
        return fetchAndSaveMyStats(userEmail, true);
    }

    private LeetcodeStatsResponse mapSnapshotToResponse(LeetcodeStatsSnapshot snapshot, String username) {
        LeetcodeStatsResponse response = new LeetcodeStatsResponse();
        response.setUsername(username);
        response.setRanking(snapshot.getRanking());
        response.setTotalSolved(snapshot.getTotalSolved());
        response.setEasySolved(snapshot.getEasySolved());
        response.setMediumSolved(snapshot.getMediumSolved());
        response.setHardSolved(snapshot.getHardSolved());
        response.setReputation(0); 
        return response;
    }
}