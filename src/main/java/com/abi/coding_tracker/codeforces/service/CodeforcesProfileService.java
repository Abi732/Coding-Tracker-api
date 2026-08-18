package com.abi.coding_tracker.codeforces.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abi.coding_tracker.codeforces.client.CodeforcesClient;
import com.abi.coding_tracker.codeforces.dto.CodeforcesProfileResponse;
import com.abi.coding_tracker.codeforces.dto.CodeforcesSnapshot;
import com.abi.coding_tracker.codeforces.dto.ContestHistoryResponse;
import com.abi.coding_tracker.codeforces.entity.CodeforcesProfile;
import com.abi.coding_tracker.codeforces.repository.CodeForcesProfileRepository;
import com.abi.coding_tracker.codeforces.repository.CodeforcesSnapshotRepository;
import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.exception.DuplicateResourceException;
import com.abi.coding_tracker.exception.ExternalApiException;
import com.abi.coding_tracker.exception.ResourceNotFoundException;
import com.abi.coding_tracker.platform.CodingPlatformService;
import com.abi.coding_tracker.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CodeforcesProfileService implements CodingPlatformService{
    private final UserRepository userRepository;
    private final CodeForcesProfileRepository profileRepository;
    private final CodeforcesSnapshotRepository snapshotRepository;
    private final CodeforcesClient codeforcesClient;

    public CodeforcesProfileService(UserRepository userRepository,
                                    CodeForcesProfileRepository profileRepository,
                                    CodeforcesSnapshotRepository snapshotRepository,
                                    CodeforcesClient codeforcesClient){
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.snapshotRepository = snapshotRepository;
        this.codeforcesClient = codeforcesClient;
    }

    @Override
    public String getPlatformName() {
        return "codeforces";
    }

    @Override
    public LocalDateTime getLastSync(User user) {
        return profileRepository.findByUser(user)
                .map(CodeforcesProfile::getLastSyncedAt)
                .orElse(null);
    }

    @Override
    public void refresh(User user) {
        profileRepository.findByUser(user).ifPresent(profile -> {
            log.info("Interface triggered refresh for Codeforces user: {}", user.getEmail());
            fetchAndSaveMyStats(user.getEmail()); 
        });
    }

    @Transactional
    public CodeforcesProfile connectAccount(String handle, String userEmail){
        log.info("Connecting handle '{}' for user '{}'", handle, userEmail);
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new ResourceNotFoundException("user not found"));

        if(profileRepository.existsByUser(user)){
            throw new DuplicateResourceException("You have already connected a codeforce account");
        }

        if(profileRepository.existsByHandle(handle)){
            throw new DuplicateResourceException("The handle"+handle+"is already connected to another user");
        }

        codeforcesClient.getUserInfo(handle);

        CodeforcesProfile profile = new CodeforcesProfile();
        profile.setHandle(handle);
        profile.setUser(user);
        profile.setLastSyncedAt(LocalDateTime.now());

        return profileRepository.save(profile);
    }

    @Cacheable(value = "codeforcesStats", key = "#userEmail")
    @Transactional
    public CodeforcesProfileResponse fetchAndSaveMyStats(String userEmail){
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new ResourceNotFoundException("User not Found"));

        CodeforcesProfile profile = profileRepository.findByUser(user).orElseThrow(()->new ResourceNotFoundException("No Codeforces account connected."));

        LocalDateTime lastSynced = profile.getLastSyncedAt();
        LocalDateTime now = LocalDateTime.now();

        boolean hasSnapshot = snapshotRepository.findTopByProfileOrderByFetchedAtDesc(profile).isPresent();

        if(hasSnapshot && lastSynced != null && Duration.between(lastSynced, now).toMinutes() < 30){
            log.info("Returning CACHED codeforces stats for user '{}'", profile.getHandle());
            return buildCachedResponse(profile);
        }

        log.info("Fetching FRESH codeforce stats for user '{}'", profile.getHandle());
        try{
            CodeforcesProfileResponse currentStats = codeforcesClient.getUserInfo(profile.getHandle());
            Optional<CodeforcesSnapshot> latestSnapshotOpt = snapshotRepository.findTopByProfileOrderByFetchedAtDesc(profile);

            boolean isDuplicate = latestSnapshotOpt.map(latest->
                Objects.equals(latest.getRating(), currentStats.getRating()) &&
                Objects.equals(latest.getMaxRating(), currentStats.getMaxRating()) &&
                Objects.equals(latest.getRank(), currentStats.getRank()) 
            ).orElse(false);

            if(!isDuplicate){
                log.info("Refreshing snapshot for handle '{}'", profile.getHandle());
                CodeforcesSnapshot snapshot = new CodeforcesSnapshot();
                snapshot.setProfile(profile);
                snapshot.setRating(currentStats.getRating());
                snapshot.setMaxRating(currentStats.getMaxRating());
                snapshot.setRank(currentStats.getRank());
                snapshotRepository.save(snapshot);
            }

            profile.setLastSyncedAt(now);
            profileRepository.save(profile);

            currentStats.setCached(false);
            currentStats.setLastUpdated(now);

            return currentStats;
        }catch(ExternalApiException e){
            if(e.getStatusCode() >= 500){
                log.warn("Codeforces API is down. Falling back to cached for {}", profile.getHandle());
                return buildCachedResponse(profile);
            }
            throw e;
        }

    }

    public List<ContestHistoryResponse> findMyContestHistory(String userEmail){
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new ResourceNotFoundException("User Not found"));

        CodeforcesProfile profile = profileRepository.findByUser(user).orElseThrow(()-> new ResourceNotFoundException("No codeforces account connected"));

        log.info("Fetching contest history for handle '{}'", profile.getHandle());
        List<ContestHistoryResponse> history = codeforcesClient.getUserRating(profile.getHandle());

        log.info("Contest history updated for handle '{}'", profile.getHandle());
        return history;
    }

    private CodeforcesProfileResponse buildCachedResponse(CodeforcesProfile  profile){
        CodeforcesSnapshot latestSnapshot = snapshotRepository.findTopByProfileOrderByFetchedAtDesc(profile)
                .orElseThrow(()->new ExternalApiException("Codeforces is currently unavailable and not historical data exists", 503));

        CodeforcesProfileResponse response = new CodeforcesProfileResponse();
        response.setHandle(profile.getHandle());
        response.setRating(latestSnapshot.getRating());
        response.setMaxRating(latestSnapshot.getMaxRating());
        response.setRank(latestSnapshot.getRank());
        response.setCached(true);
        response.setLastUpdated(profile.getLastSyncedAt());

        return response;
    }
}
