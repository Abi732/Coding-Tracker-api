package com.abi.coding_tracker.dashboard.service;

import com.abi.coding_tracker.github.service.GithubProfileService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.abi.coding_tracker.codeforces.dto.CodeforcesProfileResponse;
import com.abi.coding_tracker.codeforces.service.CodeforcesProfileService;
import com.abi.coding_tracker.dashboard.dto.DashboardResponse;
import com.abi.coding_tracker.dashboard.dto.DashboardStats;
import com.abi.coding_tracker.dashboard.dto.UserSummary;
import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.exception.ExternalApiException;
import com.abi.coding_tracker.exception.ResourceNotFoundException;
import com.abi.coding_tracker.github.dto.GithubProfileResponse;
import com.abi.coding_tracker.leetcode.dto.LeetcodeStatsResponse;
import com.abi.coding_tracker.leetcode.service.LeetcodeProfileService;
import com.abi.coding_tracker.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DashboardService {
    private final GithubProfileService githubProfileService;
    private final UserRepository userRepository;
    private final LeetcodeProfileService leetcodeProfileService;
    private final CodeforcesProfileService codeforcesProfileService;

    public DashboardService(UserRepository userRepository, LeetcodeProfileService leetcodeProfileService, CodeforcesProfileService codeforcesProfileService, GithubProfileService githubProfileService){
        this.userRepository = userRepository;
        this.leetcodeProfileService = leetcodeProfileService;
        this.codeforcesProfileService = codeforcesProfileService;
        this.githubProfileService = githubProfileService;
    }

    public DashboardResponse getDashboardData(String email){
        log.info("Generating dashboard for user: {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found"));

        UserSummary userSummary = UserSummary.builder().name(user.getName()).email(user.getEmail()).joinedAt(user.getCreatedAt()).build();

        List<String> errors = new ArrayList<>();
        Map<String, String> serviceStatus = new HashMap<>();
        boolean allCached = true;
        int totalSolvedAggregate = 0; 

        LeetcodeStatsResponse leetcodeStats = null;
        try{
            log.info("Fetching leetcode for Dashboard");
            leetcodeStats = leetcodeProfileService.fetchAndSaveMyStats(email);
            serviceStatus.put("leetcode", "UP");
            if(!leetcodeStats.isCached()) allCached = false;
            totalSolvedAggregate = leetcodeStats.getTotalSolved();
        }catch(ResourceNotFoundException e){
            serviceStatus.put("leetcode", "NOT_CONNECTED");
            // log.info("Dashboard Aggregation: User [{}] has no Leetcode account connected",email);
        }catch(ExternalApiException e){
            serviceStatus.put("leetcode","DOWN");
            errors.add("Leetcode is currently unavailable.");
            log.error("Dashboard: leetcode API failed for {}",email);
        }catch(Exception e){
            serviceStatus.put("Leetcode", "ERROR");
            errors.add("Unexpected error fetching leetcode data");
        }

        CodeforcesProfileResponse codeforcesProfile = null;
        try{
            log.info("Fetching Codeforces for Dashboard...");
            codeforcesProfile = codeforcesProfileService.fetchAndSaveMyStats(email);
            serviceStatus.put("codeforces","UP");
            if(!codeforcesProfile.isCached()) allCached = false;
        }catch(ResourceNotFoundException e){
            serviceStatus.put("codeforces", "NOT_CONNECTED");
            // log.info("Dashboard Aggregation: User [{}] has no Codeforces account connected", email);
        }catch(ExternalApiException e){
            serviceStatus.put("Codeforces", "DOWN");
            errors.add("codeforces is currently unavailable");
            log.error("Dashboard: codeforces API failed for {}", email);
            // log.error("Dashboard Aggregation: Faliled to fetch Codeforces stats for user [{}]", email, e);
        }catch(Exception e){
            serviceStatus.put("Codeforces", "ERROR");
            errors.add("Unexpected error fetching codeforces data.");
        }

        GithubProfileResponse githubStats = null;
        try{
            log.info("Fetching GitHub for dashboard...");
            githubStats = githubProfileService.fetchAndSaveMyProfile(email);
            serviceStatus.put("github", "UP");
            if (!githubStats.getCached()) allCached = false;
        }catch(ResourceNotFoundException e){
            serviceStatus.put("github", "NOT_CONNECTED");
        }catch(ExternalApiException e){
            serviceStatus.put("github", "DOWN");
            errors.add("GitHub is currently unavailable.");
            log.error("Dashboard: GitHub API failed for {}", email);
        }catch (Exception e) {
            serviceStatus.put("github", "ERROR");
            errors.add("Unexpected error fetching GitHub data.");
        }

        DashboardStats aggregateStats = DashboardStats.builder()
                            .totalProblemSolved(totalSolvedAggregate)
                            .build();
        
        log.info("Dashboard ready for user: {}", email);

        return DashboardResponse.builder()
                .Apiversion("v1.0")
                .generatedAt(LocalDateTime.now())
                .fullyCached(allCached && (leetcodeStats != null || codeforcesProfile != null ||githubStats != null))
                .user(userSummary)
                .leetcode(leetcodeStats)
                .codeforces(codeforcesProfile)
                .github(githubStats)
                .statistics(aggregateStats)
                .recentActivitiy(new ArrayList<>())
                .services(serviceStatus)
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }
}
