package com.abi.coding_tracker.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.abi.coding_tracker.codeforces.dto.CodeforcesProfileResponse;
import com.abi.coding_tracker.contests.dto.ContestResponse;
import com.abi.coding_tracker.github.dto.GithubProfileResponse;
import com.abi.coding_tracker.leetcode.dto.LeetcodeStatsResponse;
import com.abi.coding_tracker.notification.dto.NotificationResponse;
import com.abi.coding_tracker.recommendation.dto.RecommendationResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private UserSummary user;
    private LeetcodeStatsResponse leetcode;
    private CodeforcesProfileResponse codeforces;
    private GithubProfileResponse github;

    private DashboardStats statistics;

    private RecommendationResponse recommendations;
    private ContestResponse upcomingContests;

    private List<NotificationResponse> notifications;

    private List<RecentActivity> recentActivitiy;

    private Map<String, String> services;

    private List<String> errors;

    private String Apiversion;
    private LocalDateTime generatedAt;
    private boolean fullyCached;
}
