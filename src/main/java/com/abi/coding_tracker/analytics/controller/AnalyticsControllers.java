package com.abi.coding_tracker.analytics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.analytics.dto.AnalyticsSummaryResponse;
import com.abi.coding_tracker.analytics.dto.BestDayResponse;
import com.abi.coding_tracker.analytics.dto.DailyProgressResponse;
import com.abi.coding_tracker.analytics.dto.DifficultyDistributionResponse;
import com.abi.coding_tracker.analytics.dto.PeriodProgressResponse;
import com.abi.coding_tracker.analytics.dto.ProgressResponse;
import com.abi.coding_tracker.analytics.dto.RatingProgressResponse;
import com.abi.coding_tracker.analytics.dto.StreakResponse;
import com.abi.coding_tracker.analytics.dto.WeeklyGoalResponse;
import com.abi.coding_tracker.analytics.service.AnalyticsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/analytics")
public class AnalyticsControllers {
    
    private final AnalyticsService analyticsService;

    public AnalyticsControllers(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    @GetMapping("/progress")
    public ResponseEntity<List<ProgressResponse>> getMyProgress(Authentication authentication){
        String email = authentication.getName();
        log.info("User [{}] requested progress graph data");

        List<ProgressResponse> response = analyticsService.getLeetcodeProgress(email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/difficulty")
    public ResponseEntity<DifficultyDistributionResponse> getDifficultyDistribution(Authentication authentication){
        String email = authentication.getName();
        log.info("User [{}] requested difficulty distribution widget", email);

        DifficultyDistributionResponse response = analyticsService.getDifficultyDistribution(email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/daily")
    public ResponseEntity<DailyProgressResponse> getDailyProgress(Authentication authentication){
        String email = authentication.getName();
        log.info("User [{}] requested daily progress delta", email);

        DailyProgressResponse response = analyticsService.getDailyProgress(email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly")
    public ResponseEntity<PeriodProgressResponse> getWeeklyProgress(Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getWeeklyProgress(authentication.getName()));
    }

    @GetMapping("/monthly")
    public ResponseEntity<PeriodProgressResponse> getMonthlyProgress(Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getMonthlyProgress(authentication.getName()));
    }

    @GetMapping("/streak")
    public ResponseEntity<StreakResponse> getStreak(Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getStreak(authentication.getName()));
    }

    @GetMapping("/best-day")
    public ResponseEntity<BestDayResponse> getBestDay(Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getBestDay(authentication.getName()));
    }

    @GetMapping("/rating")
    public ResponseEntity<List<RatingProgressResponse>> getCodeforcesRating(Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getCodeforcesRatingGraph(authentication.getName()));
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getAnalyticsSummary(Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getAnalyticsSummary(authentication.getName()));
    }

    @GetMapping("/goal")
    public ResponseEntity<WeeklyGoalResponse> getWeeklyGoal(Authentication authentication){
        return ResponseEntity.ok(analyticsService.getWeeklyGoal(authentication.getName()));
    }
}
