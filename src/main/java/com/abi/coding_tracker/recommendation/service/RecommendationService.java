package com.abi.coding_tracker.recommendation.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.abi.coding_tracker.analytics.dto.AnalyticsSummaryResponse;
import com.abi.coding_tracker.analytics.dto.DifficultyDistributionResponse;
import com.abi.coding_tracker.analytics.service.AnalyticsService;
import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.exception.ResourceNotFoundException;
import com.abi.coding_tracker.github.entity.GithubProfile;
import com.abi.coding_tracker.github.entity.GithubSnapshot;
import com.abi.coding_tracker.github.repository.GithubProfileRepository;
import com.abi.coding_tracker.github.repository.GithubSnapshotRepository;
import com.abi.coding_tracker.recommendation.dto.RecommendationResponse;
import com.abi.coding_tracker.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RecommendationService {
    
    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;
    private final GithubProfileRepository githubProfileRepository;
    private final GithubSnapshotRepository githubSnapshotRepository;

    public RecommendationService(AnalyticsService analyticsService,
                                UserRepository userRepository,
                                GithubProfileRepository githubProfileRepository,
                                GithubSnapshotRepository githubSnapshotRepository
    ){
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
        this.githubProfileRepository = githubProfileRepository;
        this.githubSnapshotRepository = githubSnapshotRepository;
    }

    public RecommendationResponse generateRecommendation(String email){
        log.info("Generating personalized recommendation for user: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        AnalyticsSummaryResponse summaryResponse = analyticsService.getAnalyticsSummary(email);
        DifficultyDistributionResponse difficulty = analyticsService.getDifficultyDistribution(email);

        int totalSolved = summaryResponse.getLeetcodeSolved();
        Integer cfRating = summaryResponse.getCodeforcesRating();
        int streak = summaryResponse.getCurrentStreak();

        GithubProfile githubProfile = githubProfileRepository.findByUser(user).orElse(null);
        GithubSnapshot githubSnapshot = null;
        if(githubProfile != null){
            githubSnapshot = githubSnapshotRepository.findTopByProfileOrderByFetchedAtDesc(githubProfile).orElse(null);
        } 

        //classify user level 
        String level = classifyUserLevel(totalSolved);

        List<String> recommendation = new ArrayList<>();

        if(totalSolved==0 && cfRating==null){
            recommendation.add("Welcome! Start by connecting your coding accounts and solving your first problem.");
            return new RecommendationResponse(level,recommendation);
        }

        // Rule: LeetCode Difficulty Ratios
        if (difficulty.getHard() < 30 && difficulty.getMedium() > 50) {
            recommendation.add("You have a solid grasp of Medium problems. Time to push your limits and focus on Hard problems.");
        } else if (difficulty.getEasy() > (difficulty.getMedium() * 3)) {
            recommendation.add("You are solving too many Easy problems. Step out of your comfort zone and tackle more Mediums!");
        }

                // Rule: Codeforces Engagement
        if (cfRating == null) {
            recommendation.add("Consider connecting a Codeforces account to practice competitive programming under time pressure.");
        } else if (cfRating == 0) {
            recommendation.add("Take part in your first Codeforces contest to establish your initial rating!");
        } else if (cfRating < 1200) {
            recommendation.add("Your Codeforces rating is in the Newbie range. Participate in Div-3 and Div-4 contests to build speed.");
        } else if (cfRating >= 1200 && cfRating < 1400) {
            recommendation.add("Great job reaching Pupil! Focus on solving B and C level problems in Div-2 contests.");
        }

        //Github Recommendation
        if(githubProfile == null){
            recommendation.add("Connect your Github account to track your open-source contribution and repository growth.");
        }else if(githubSnapshot != null){
            if(githubSnapshot.getPublic_repos() == 0){
                recommendation.add("You have no public repositories on GitHub. Try building and publishing a new project!");
            }else if (githubSnapshot.getPublic_repos() < 3) {
                recommendation.add("You have a few repositories. Keep building projects to showcase your skills to employers.");
            }
            if (githubSnapshot.getTotalStars() == 0 && githubSnapshot.getPublic_repos() > 0) {
                 recommendation.add("Share your repositories with others or write great documentation to start earning GitHub stars.");
            }
        }

        // Rule: Consistency and Streaks
        if (streak == 0 && totalSolved > 0) {
            recommendation.add("Your coding streak has paused. Solve a quick problem today to get your momentum back!");
        } else if (streak >= 7) {
            recommendation.add("Incredible " + streak + "-day streak! Keep up the daily consistency, you are building great habits.");
        }

        // Rule: General Growth
        if (summaryResponse.getLeetcodeGrowth() < 3 && totalSolved > 0) {
            recommendation.add("Your problem-solving rate has slowed down this week. Try to dedicate 30 minutes a day to practice.");
        }

        log.info("Successfully generated {} recommendations for {}", recommendation.size(), email);
        return RecommendationResponse.builder()
            .overallLevel(level)
            .recommendations(recommendation)
            .build();

    }

    private String classifyUserLevel(int totalSolved){
        if(totalSolved < 150) return "Beginner";
        if(totalSolved < 500) return "Intermediate";
        if(totalSolved < 1000) return "Advanced";
        return "Expert";
    }
}
