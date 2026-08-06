package com.abi.coding_tracker.analytics.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.abi.coding_tracker.analytics.dto.AnalyticsSummaryResponse;
import com.abi.coding_tracker.analytics.dto.BestDayResponse;
import com.abi.coding_tracker.analytics.dto.DailyProgressResponse;
import com.abi.coding_tracker.analytics.dto.DifficultyDistributionResponse;
import com.abi.coding_tracker.analytics.dto.PeriodProgressResponse;
import com.abi.coding_tracker.analytics.dto.ProgressResponse;
import com.abi.coding_tracker.analytics.dto.RatingProgressResponse;
import com.abi.coding_tracker.analytics.dto.StreakResponse;
import com.abi.coding_tracker.analytics.dto.WeeklyGoalResponse;
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
import com.abi.coding_tracker.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AnalyticsService {

    private final UserRepository userRepository;
    private final LeetcodeProfileRespository leetcodeProfileRepository;
    private final LeetcodeStatsSnapshotsRepository leetcodeSnapshotRepository;
    private final CodeForcesProfileRepository codeforcesProfileRepository;
    private final CodeforcesSnapshotRepository codeforcesSnapshotRepository;

    public AnalyticsService(UserRepository userRepository, 
                            LeetcodeProfileRespository leetcodeProfileRepository, 
                            LeetcodeStatsSnapshotsRepository leetcodeSnapshotRepository,
                            CodeForcesProfileRepository codeforcesProfileRepository,
                            CodeforcesSnapshotRepository codeforcesSnapshotRepository) {
        this.userRepository = userRepository;
        this.leetcodeProfileRepository = leetcodeProfileRepository;
        this.leetcodeSnapshotRepository = leetcodeSnapshotRepository;
        this.codeforcesProfileRepository = codeforcesProfileRepository;
        this.codeforcesSnapshotRepository = codeforcesSnapshotRepository;
    }

    // ==========================================
    // LEETCODE TIMELINE & PROGRESS
    // ==========================================

    private TreeMap<LocalDate, Integer> getDailyProgressMap(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<LeetcodeProfile> profileOpt = leetcodeProfileRepository.findByUser(user);
        if (profileOpt.isEmpty()) {
            return new TreeMap<>(); 
        }

        List<LeetcodeStatsSnapshot> snapshots = leetcodeSnapshotRepository.findAllByProfileOrderByFetchedAtAsc(profileOpt.get());
        TreeMap<LocalDate, Integer> dailyProgress = new TreeMap<>();
        
        for (LeetcodeStatsSnapshot snap : snapshots) {
            LocalDate date = snap.getFetchedAt().toLocalDate();
            dailyProgress.put(date, Math.max(
                dailyProgress.getOrDefault(date, 0), 
                snap.getTotalSolved()
            ));
        }
        return dailyProgress;
    }

    public List<ProgressResponse> getLeetcodeProgress(String email) {
        log.info("Generating analytics: LeetCode progress for {}", email);
        TreeMap<LocalDate, Integer> dailyProgress = getDailyProgressMap(email);

        return dailyProgress.entrySet().stream()
                .map(entry -> ProgressResponse.builder()
                        .date(entry.getKey().toString())
                        .totalSolved(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public DifficultyDistributionResponse getDifficultyDistribution(String email) {
        log.info("Generating analytics: Difficulty distribution for {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return leetcodeProfileRepository.findByUser(user)
            .flatMap(leetcodeSnapshotRepository::findTopByProfileOrderByFetchedAtDesc)
            .map(snap -> new DifficultyDistributionResponse(
                    snap.getEasySolved(), snap.getMediumSolved(), snap.getHardSolved()
            ))
            .orElse(new DifficultyDistributionResponse(0, 0, 0));
    }

    public DailyProgressResponse getDailyProgress(String email) {
        log.info("Generating analytics: Daily progress for {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Optional<LeetcodeProfile> profileOpt = leetcodeProfileRepository.findByUser(user);
        if (profileOpt.isEmpty()) return new DailyProgressResponse(0);

        List<LeetcodeStatsSnapshot> lastTwo = leetcodeSnapshotRepository.findTop2ByProfileOrderByFetchedAtDesc(profileOpt.get());
        
        if (lastTwo.size() < 2) return new DailyProgressResponse(0);
        return new DailyProgressResponse(lastTwo.get(0).getTotalSolved() - lastTwo.get(1).getTotalSolved());
    }

    // ==========================================
    // PERIOD PROGRESS (WEEKLY/MONTHLY)
    // ==========================================

    public PeriodProgressResponse getWeeklyProgress(String email) {
        return calculatePeriodProgress(email, 7);
    }

    public PeriodProgressResponse getMonthlyProgress(String email) {
        return calculatePeriodProgress(email, 30);
    }

    private PeriodProgressResponse calculatePeriodProgress(String email, int daysAgo) {
        TreeMap<LocalDate, Integer> timeline = getDailyProgressMap(email);
        if (timeline.isEmpty()) return new PeriodProgressResponse(0, 0, 0);

        LocalDate today = LocalDate.now();
        LocalDate targetPastDate = today.minusDays(daysAgo);

        Map.Entry<LocalDate, Integer> endEntry = timeline.floorEntry(today);
        int endSolved = endEntry != null ? endEntry.getValue() : 0;

        Map.Entry<LocalDate, Integer> startEntry = timeline.floorEntry(targetPastDate);
        int startSolved = startEntry != null ? startEntry.getValue() : timeline.firstEntry().getValue();

        return new PeriodProgressResponse(startSolved, endSolved, endSolved - startSolved);
    }

    // ==========================================
    // STREAK & BEST DAY
    // ==========================================

    public StreakResponse getStreak(String email) {
        log.info("Calculating streak for {}", email);
        TreeMap<LocalDate, Integer> timeline = getDailyProgressMap(email);
        if (timeline.isEmpty()) return new StreakResponse(0);

        Set<LocalDate> activeDates = new HashSet<>();
        int prevSolved = -1;

        for (Map.Entry<LocalDate, Integer> entry : timeline.entrySet()) {
            if (prevSolved != -1 && entry.getValue() > prevSolved) {
                activeDates.add(entry.getKey());
            }
            prevSolved = entry.getValue();
        }

        int streak = 0;
        LocalDate curr = LocalDate.now();

        if (activeDates.contains(curr)) {
            streak++; curr = curr.minusDays(1);
        } else if (activeDates.contains(curr.minusDays(1))) {
            curr = curr.minusDays(1); streak++; curr = curr.minusDays(1);
        } else {
            return new StreakResponse(0);
        }

        while (activeDates.contains(curr)) {
            streak++; curr = curr.minusDays(1);
        }

        return new StreakResponse(streak);
    }

    public BestDayResponse getBestDay(String email) {
        log.info("Generating analytics: Best day for {}", email);
        TreeMap<LocalDate, Integer> timeline = getDailyProgressMap(email);
        
        if (timeline.size() < 2) return new BestDayResponse(LocalDate.now().toString(), 0);

        String bestDate = null;
        int maxIncrease = 0;
        int previousSolved = -1;

        for (Map.Entry<LocalDate, Integer> entry : timeline.entrySet()) {
            if (previousSolved != -1) {
                int increase = entry.getValue() - previousSolved;
                if (increase > maxIncrease) {
                    maxIncrease = increase;
                    bestDate = entry.getKey().toString();
                }
            }
            previousSolved = entry.getValue();
        }

        return new BestDayResponse(bestDate != null ? bestDate : LocalDate.now().toString(), maxIncrease);
    }

    // ==========================================
    // CODEFORCES RATING
    // ==========================================

    public List<RatingProgressResponse> getCodeforcesRatingGraph(String email) {
        log.info("Generating analytics: Codeforces rating graph for {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<CodeforcesProfile> profileOpt = codeforcesProfileRepository.findByUser(user);
        if (profileOpt.isEmpty()) return List.of();

        List<CodeforcesSnapshot> snapshots = codeforcesSnapshotRepository.findAllByProfileOrderByFetchedAtAsc(profileOpt.get());
        
        // Group by Date, taking the highest rating achieved on that day
        TreeMap<LocalDate, Integer> dailyRatingMap = new TreeMap<>();
        for (CodeforcesSnapshot snap : snapshots) {
            if (snap.getRating() != null) {
                LocalDate date = snap.getFetchedAt().toLocalDate();
                dailyRatingMap.put(date, Math.max(
                    dailyRatingMap.getOrDefault(date, 0), 
                    snap.getRating()
                ));
            }
        }

        return dailyRatingMap.entrySet().stream()
                .map(entry -> new RatingProgressResponse(entry.getKey().toString(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // ==========================================
    // COMBINED SUMMARY
    // ==========================================

    public AnalyticsSummaryResponse getAnalyticsSummary(String email) {
        log.info("Returning analytics summary for {}", email);
        
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        int totalSolved = leetcodeProfileRepository.findByUser(user)
                .flatMap(leetcodeSnapshotRepository::findTopByProfileOrderByFetchedAtDesc)
                .map(LeetcodeStatsSnapshot::getTotalSolved)
                .orElse(0);

        Integer cfRating = codeforcesProfileRepository.findByUser(user)
                .flatMap(codeforcesSnapshotRepository::findTopByProfileOrderByFetchedAtDesc)
                .map(CodeforcesSnapshot::getRating)
                .orElse(null);

        int weeklyGrowth = getWeeklyProgress(email).getGrowth();
        int currentStreak = getStreak(email).getCurrentStreak();
        int bestDayDelta = getBestDay(email).getIncrease();

        return AnalyticsSummaryResponse.builder()
                .leetcodeSolved(totalSolved)
                .leetcodeGrowth(weeklyGrowth)
                .codeforcesRating(cfRating)
                .currentStreak(currentStreak)
                .bestDay(bestDayDelta)
                .build();
    }

    public WeeklyGoalResponse getWeeklyGoal(String email){
        log.info("Generating analytics: Weekly goal goal for: {} ", email);
        User user = userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("Users not found"));

        int totalSolved = leetcodeProfileRepository.findByUser(user)
                    .flatMap(leetcodeSnapshotRepository::findTopByProfileOrderByFetchedAtDesc)
                    .map(LeetcodeStatsSnapshot:: getTotalSolved)
                    .orElse(0);

        int goal = ((totalSolved/50 )+1)*50;
        int remaining = goal - totalSolved;

        return new WeeklyGoalResponse(goal,remaining);
    }
}