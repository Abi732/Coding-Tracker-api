package com.abi.coding_tracker.contests.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.abi.coding_tracker.contests.client.ContestClient;
import com.abi.coding_tracker.contests.dto.ContestResponse;
import com.abi.coding_tracker.contests.entity.Contest;
import com.abi.coding_tracker.contests.entity.ContestStatus;
import com.abi.coding_tracker.contests.repository.ContestRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ContestService {
    private final ContestRepository contestRepository;
    private final List<ContestClient> contestClients;

    public ContestService(ContestRepository contestRepository, List<ContestClient> contestClients){
        this.contestRepository = contestRepository;
        this.contestClients = contestClients;
    }

    @Cacheable(value = "contests")
    public List<ContestResponse> getUpcomingContests(){
        log.info("Fetching upcoming contests (Skeleton implementation)");

        List<Contest> upcoming = contestRepository.findAllByStatusOrderByStartTimeAsc(ContestStatus.UPCOMING);

        
        return upcoming.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ContestResponse> getLiveContests(){
        log.info("Returning live contest from database");

        List<Contest> liveContests = contestRepository.findAllByStatusOrderByStartTimeAsc(ContestStatus.LIVE);

        return liveContests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @CacheEvict(value = "contests", allEntries = true)
    @Transactional
    public void fetchAndSaveContests(){
        log.info("Fetching Contests from all connected platforms ....");
        List<ContestResponse> allFetchedContests = new ArrayList<>();

        for (ContestClient client : contestClients){
            try {
                allFetchedContests.addAll(client.fetchUpcomingContests());
            } catch (Exception e) {
                log.error("Failed to fetch contests from platform: {}", client.getPlatformName(), e);
            }
        }

        log.info("Saving {} contests to database", allFetchedContests.size());

        for(ContestResponse response : allFetchedContests){
            Optional<Contest> existingOpt = contestRepository.findByContestId(response.getContestId());

            if(existingOpt.isEmpty()){
                Contest newcontest = new Contest();
                newcontest.setPlatform(response.getPlatform());
                newcontest.setContestId(response.getContestId());
                newcontest.setName(response.getName());
                newcontest.setStartTime(response.getStartTime());
                newcontest.setDurationSeconds(response.getDurationSeconds());
                newcontest.setUrl(response.getUrl());
                newcontest.setStatus(ContestStatus.UPCOMING);
                contestRepository.save(newcontest);
            }else{
                Contest existing = existingOpt.get();
                existing.setStartTime(response.getStartTime());
                existing.setDurationSeconds(response.getDurationSeconds());
                existing.setName(response.getName());
                contestRepository.save(existing);
            }
        }
    }

    @Transactional
    public void updateContestStatus(){
        log.info("Updating contest status ...");
        LocalDateTime now = LocalDateTime.now();

        List<Contest> upcoming = contestRepository.findAllByStartTimeBeforeAndStatus(now, ContestStatus.UPCOMING);

        for(Contest c : upcoming){
            c.setStatus(ContestStatus.LIVE);
            contestRepository.save(c);
            log.info("Constest {} is now Live", c.getName());
        }

        List<Contest> live = contestRepository.findAllByStartTimeBeforeAndStatus(now, ContestStatus.LIVE);
        for(Contest c : live){
            LocalDateTime endTIme = c.getStartTime().plusSeconds(c.getDurationSeconds());
            if(now.isAfter(endTIme)){
                c.setStatus(ContestStatus.FINISHED);
                contestRepository.save(c);
                log.info("Contest {} has Finished", c.getName());
            }
        }
    }

    private ContestResponse mapToResponse(Contest contest) {
        ContestResponse response = ContestResponse.builder()
                .platform(contest.getPlatform())
                .contestId(contest.getContestId())
                .name(contest.getName())
                .startTime(contest.getStartTime())
                .durationSeconds(contest.getDurationSeconds())
                .url(contest.getUrl())
                .status(contest.getStatus().name())
                .build();
        
        if(contest.getStatus()==ContestStatus.UPCOMING){
            long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), contest.getStartTime());
            response.setStartsInMinutes(Math.max(0, minutes));
        }

        return response;
    }
}
