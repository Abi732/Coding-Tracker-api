package com.abi.coding_tracker.github.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.exception.DuplicateResourceException;
import com.abi.coding_tracker.exception.ExternalApiException;
import com.abi.coding_tracker.exception.ResourceNotFoundException;
import com.abi.coding_tracker.github.client.GithubClient;
import com.abi.coding_tracker.github.dto.GithubProfileResponse;
import com.abi.coding_tracker.github.dto.RepositoryResponse;
import com.abi.coding_tracker.github.entity.GithubProfile;
import com.abi.coding_tracker.github.entity.GithubSnapshot;
import com.abi.coding_tracker.github.repository.GithubProfileRepository;
import com.abi.coding_tracker.github.repository.GithubSnapshotRepository;
import com.abi.coding_tracker.platform.CodingPlatformService;
import com.abi.coding_tracker.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GithubProfileService implements CodingPlatformService {
    
    private final UserRepository userRepository;
    private final GithubProfileRepository githubProfileRepository;
    private final GithubSnapshotRepository githubSnapshotRepository;
    private final GithubClient githubClient;

    public GithubProfileService(UserRepository userRepository,
                                GithubProfileRepository githubProfileRepository,
                                GithubSnapshotRepository githubSnapshotRepository,
                                GithubClient githubClient
    ){
        this.userRepository = userRepository;
        this.githubProfileRepository = githubProfileRepository;
        this.githubSnapshotRepository= githubSnapshotRepository;
        this.githubClient = githubClient;
    }

    @Override
    public String getPlatformName(){
        return "github";
    }

    @Override
    public LocalDateTime getLastSync(User user){
        return githubProfileRepository.findByUser(user)
            .map(GithubProfile::getLastSyncedAt)
            .orElse(null);
    }

    @Override
    public void refresh(User user){
        githubProfileRepository.findByUser(user).ifPresent(profile->{
            log.info("Interface triggered refresh for Github user: {}", user.getEmail());
            fetchAndSaveMyProfile(user.getEmail());
        });
    }

    @Transactional
    public GithubProfile connectAccount(String username, String userEmail){
        log.info("Connect Github username '{}' for user '{}'",username, userEmail);

        User user = userRepository.findByEmail(userEmail).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        if(githubProfileRepository.existsByUser(user)){
            throw new DuplicateResourceException("You have already connected agithub account");
        }

        if(githubProfileRepository.existsByUsername(username)){
            throw new DuplicateResourceException("The Github username '"+username+"' is already claimed");
        }

        githubClient.fetchProfile(username);

        GithubProfile profile = new GithubProfile();
        profile.setUsername(username);
        profile.setUser(user);
        profile.setConnectedAt(LocalDateTime.now());
        profile.setLastSyncedAt(LocalDateTime.now());

        return githubProfileRepository.save(profile);
    }

    @Cacheable(value = "githubProfile", key = "#userEmail")
    @Transactional
    public GithubProfileResponse fetchAndSaveMyProfile(String userEmail){
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new ResourceNotFoundException("User Not found"));

        GithubProfile profile = githubProfileRepository.findByUser(user).orElseThrow(()-> new ResourceNotFoundException("No github Account connected"));

        LocalDateTime lastSynced = profile.getLastSyncedAt();
        LocalDateTime now = LocalDateTime.now();

        boolean hasSnapshot = githubSnapshotRepository.findTopByProfileOrderByFetchedAtDesc(profile).isPresent();

        if(hasSnapshot && lastSynced != null && Duration.between(lastSynced, now).toMinutes() < 30){
            log.info("Returning cached Github profile for '{}'", profile.getUsername());
            return buildCachedResponse(profile);
        }

        log.info("Fetching fresh GitHub profile for '{}'", profile.getUsername());
        try{
            GithubProfileResponse currentStats = githubClient.fetchProfile(profile.getUsername());
            Optional<GithubSnapshot> latestSnapshotOpt = githubSnapshotRepository.findTopByProfileOrderByFetchedAtDesc(profile);

            List<RepositoryResponse> repos = githubClient.fetchRepository(profile.getUsername());
            int totalStars = repos.stream().mapToInt(repo -> repo.getStars() != null ? repo.getStars() : 0).sum();

            boolean isDuplicate = latestSnapshotOpt.map(latest->
                latest.getFollowers() == currentStats.getFollowers() &&
                latest.getPublic_repos() == currentStats.getPublicRepos() &&
                latest.getTotalStars() == totalStars
            ).orElse(false);

            if(!isDuplicate){
                log.info("Refreshing GitHub snapshot for '{}'", profile.getUsername());
                GithubSnapshot snapshot = new GithubSnapshot();
                snapshot.setProfile(profile);
                snapshot.setFollowers(currentStats.getFollowers());
                snapshot.setFollowing(currentStats.getFollowing());
                snapshot.setPublic_repos(currentStats.getPublicRepos());
                snapshot.setTotalStars(totalStars);
                githubSnapshotRepository.save(snapshot);
            }

            profile.setLastSyncedAt(now);
            githubProfileRepository.save(profile);

            currentStats.setCached(false);
            currentStats.setLastUpdated(now);
            return currentStats;
        }catch(ExternalApiException e){
            if(e.getStatusCode() >= 500 && hasSnapshot){
                log.warn("GitHub API is down. Using cache for '{}'", profile.getUsername());
                return buildCachedResponse(profile);
            }
            throw e;
        }
    }

    private GithubProfileResponse buildCachedResponse(GithubProfile profile){
        GithubSnapshot snapshot = githubSnapshotRepository.findTopByProfileOrderByFetchedAtDesc(profile).orElseThrow(()-> new ExternalApiException("Github is currently unavailable and no historical data exist", 503));

        GithubProfileResponse response = new GithubProfileResponse();
        response.setUsername(profile.getUsername());
        response.setFollowers(snapshot.getFollowers());
        response.setFollowing(snapshot.getFollowing());
        response.setPublicRepos(snapshot.getPublic_repos());
        response.setCached(true);
        response.setLastUpdated(profile.getLastSyncedAt());

        return response;
    }

    @Cacheable(value = "githubRepositories", key = "#userEmail")
    public List<RepositoryResponse> fetchMyRepositories(String userEmail){
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new ResourceNotFoundException("User not found"));
        GithubProfile profile = githubProfileRepository.findByUser(user).orElseThrow(()-> new ResourceNotFoundException("Github not connected"));

        log.info("fetching repositories for Github user '{}'", profile.getUsername());
        return githubClient.fetchRepository(profile.getUsername());
    }

    public Map<String, Double> getLanguageDistribution(String userEmail){
        log.info("Aggregating language statistics for user '{}'", userEmail);
        List<RepositoryResponse> repos = fetchMyRepositories(userEmail);

        Map<String, Long> languageCount = repos.stream()
                .filter(repo-> repo.getLanguage() != null && !repo.getLanguage().isEmpty())
                .collect(Collectors.groupingBy(RepositoryResponse::getLanguage, Collectors.counting()));
        
        long totalReposWithLanguage = languageCount.values().stream().mapToLong(Long::longValue).sum();
        
        if(totalReposWithLanguage == 0) return new HashMap<>();

        return languageCount.entrySet().stream()
                .sorted(Map.Entry.<String,Long> comparingByValue().reversed())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e->Math.round((e.getValue()*100.0/ totalReposWithLanguage)*10.0)/10.0,
                    (e1,e2)->e1,
                    LinkedHashMap::new
                ));
    }
}
