package com.abi.coding_tracker.notification.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.abi.coding_tracker.notification.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationScheduler {
    
    private final NotificationService notificationService;

    public NotificationScheduler(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scheduleContestReminders(){
        notificationService.sendContestReminders();
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void scheduleStreakWarnings(){
        notificationService.sendStreakWarning();
    }

    @Scheduled(cron = "0 0 10 * * SUN")
    public void scheduleWeeklyReport(){
        notificationService.sendWeeklyReport();
    }

    @Scheduled(fixedRate = 300000)
    public void retryFailedEmails(){
        notificationService.retryFailedNotifications();
    }
}
