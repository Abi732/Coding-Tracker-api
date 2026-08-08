package com.abi.coding_tracker.contests.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.abi.coding_tracker.contests.service.ContestService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ContestSchedular {
    
    private final ContestService contestService;

    public ContestSchedular(ContestService contestService){
        this.contestService = contestService;
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void fetchUpcomingContestsTask(){
        log.info("Scheduler Triggered: Fetching upcoming contest from external APIs");
        contestService.fetchAndSaveContests();
    }

    @Scheduled(fixedRate = 300000)
    public void UpdateContestStatusTask(){
        log.info("Scheculer Triggered: Updating contest status");
        contestService.updateContestStatus();
    }
}
