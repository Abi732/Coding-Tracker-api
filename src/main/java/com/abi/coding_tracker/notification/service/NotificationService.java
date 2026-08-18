package com.abi.coding_tracker.notification.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.abi.coding_tracker.analytics.dto.AnalyticsSummaryResponse;
import com.abi.coding_tracker.analytics.service.AnalyticsService;
import com.abi.coding_tracker.contests.dto.ContestResponse;
import com.abi.coding_tracker.contests.service.ContestService;
import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.notification.entity.Notification;
import com.abi.coding_tracker.notification.entity.NotificationStatus;
import com.abi.coding_tracker.notification.entity.NotificationType;
import com.abi.coding_tracker.notification.repository.NotificationRepository;
import com.abi.coding_tracker.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final ContestService contestService;
    private final AnalyticsService analyticsService;

    public NotificationService(UserRepository userRepository,
                            EmailService emailService,
                            NotificationRepository notificationRepository,
                            ContestService contestService,
                            AnalyticsService analyticsService
    ){
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
        this.contestService = contestService;
        this.analyticsService = analyticsService;
    }

    public void sendContestReminders(){
        log.info("Running contest reminder Job....");

        List<ContestResponse> upcomingContests =  contestService.getUpcomingContests();

        List<ContestResponse> imminentContests = upcomingContests.stream()
                    .filter(c ->{
                        long hoursUntil = ChronoUnit.HOURS.between(LocalDateTime.now(), c.getStartTime());
                        return hoursUntil >=0 && hoursUntil <=24;
                    })
                    .collect(Collectors.toList());

        if(imminentContests.isEmpty()){
            log.info("No contest in next 24 hours to remind users about");
            return;
        }

        List<User> users = userRepository.findAll();
        for(User user : users){
            for(ContestResponse contest : imminentContests){
                Context context = new Context();
                context.setVariable("name", user.getName());
                context.setVariable("contestName", contest.getName());
                context.setVariable("platform", contest.getPlatform());
                context.setVariable("time", contest.getStartTime().toString());
                context.setVariable("url", contest.getUrl());

                String subject = "Reminder: "+contest.getName()+" starts soom!";
                String dashboardMsg = contest.getName() + "starts in 2 Hours!";

                createAndSendNotification(user, NotificationType.CONTEST, subject, dashboardMsg, "contest-reminder", context);
            }
        }
    }

    public void sendStreakWarning(){
        log.info("Running Streak warning job...");
        List<User> users =userRepository.findAll();

        for(User user : users){
            try{
                int currentStreak = analyticsService.getStreak(user.getEmail()).getCurrentStreak();
                int solvedToday = analyticsService.getDailyProgress(user.getEmail()).getSolvedToday();

                if(currentStreak > 0 && solvedToday==0){
                    Context context = new Context();
                    context.setVariable("name", user.getName());
                    context.setVariable("streak", currentStreak);

                    String subject = "Don't Lose your Coding Streak";
                    String dashboardMsg = "You haven't solved any problems today. Solve one to keep your " + currentStreak + "-day streak alive!";
            
                    createAndSendNotification(user, NotificationType.STREAK, subject, dashboardMsg,"Streak-warning", context);

                }
            }catch(Exception e){
                log.error("Failed to process streak warning for user {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    public void sendWeeklyReport(){
        log.info("Running weekly report job...");
        List<User> users = userRepository.findAll();

        ContestResponse nextContest = contestService.getUpcomingContests().stream().findFirst().orElse(null);

        for(User user : users){
            try{
                AnalyticsSummaryResponse summary = analyticsService.getAnalyticsSummary(user.getEmail());

                Context context = new Context();
                context.setVariable("name", user.getName());
                context.setVariable("problemsSolved", summary.getLeetcodeGrowth());
                context.setVariable("streak", summary.getCurrentStreak());
                context.setVariable("rating", summary.getCodeforcesRating() != null ? summary.getCodeforcesRating() : "Unrated");
                context.setVariable("upcomingContest", nextContest != null ? nextContest.getName() + " on " + nextContest.getPlatform() : "None soon");

                String subject = "📊 Your Weekly Coding Report";
                String dashboardMsg = "Your weekly summary is ready. You solved " + summary.getLeetcodeGrowth() + " problems this week!";

                
                createAndSendNotification(user, NotificationType.WEEKLY_REPORT, subject, dashboardMsg, "weekly-report", context);
            }catch(Exception e){
                log.error("Failed to generate weekly report for user {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    public void retryFailedNotifications(){
        log.info("Checking for failed notification to retry ...");
        List<Notification> failed = notificationRepository.findAllByStatusAndRetryCountLessThan(NotificationStatus.FAILED, 3);

        for(Notification notification: failed){
            log.info("Retrying Notification ID: {}", notification.getId());
            notification.setRetryCount(notification.getRetryCount()+1);

            boolean success = emailService.sendHtmlEmail(notification.getUser().getEmail(), notification.getSubject(), "plain-fallback", new Context());

            if(success){
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                log.info("Retry successful for notification ID: {}", notification.getId());
            }else if(notification.getRetryCount()>= 3){
                log.warn("Notification ID {} failed after 3 retries. Giving up.", notification.getId());
            }
            notificationRepository.save(notification);
        }
    }


    private void createAndSendNotification(User user, NotificationType type, String subject, String message, String templateName, Context context){
        log.info("Creating {} notification for {}", type, user.getEmail());

        Notification notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .subject(subject)
                    .message(message)
                    .status(NotificationStatus.PENDING)
                    .isRead(false)
                    .retryCount(0)
                    .build();

        notification = notificationRepository.save(notification);

        boolean success = emailService.sendHtmlEmail(user.getEmail(), subject, templateName, context);

        if(success){
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            log.info("Email sent Successfully. ");
        }else{
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("Mail failed. Marked as FAILED for future retry.");
        }

        notificationRepository.save(notification);
    }
}
