package com.abi.coding_tracker.contests.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.contests.dto.ContestResponse;
import com.abi.coding_tracker.contests.service.ContestService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/contests")
public class ContestController {
    private final ContestService contestService;

    public ContestController(ContestService contestService){
        this.contestService = contestService;
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ContestResponse>> getUpcomingConstests(){
        log.info("Received request for upcoming contests");
        return ResponseEntity.ok(contestService.getUpcomingContests());
    }

    @GetMapping("/live")
    public ResponseEntity<List<ContestResponse>> getLiveContests(){
        log.info("Receivec request for live contests");
        return ResponseEntity.ok(contestService.getLiveContests());
    }

    @GetMapping("/{platform}")
    public ResponseEntity<List<ContestResponse>> getContestsByPlatform(@PathVariable String platform){
        log.info("Received request for upcoming contests on platform: {}", platform);
        List<ContestResponse> allUpcoming = contestService.getUpcomingContests();
        List<ContestResponse> filtered =  allUpcoming.stream()
                                .filter(c->c.getPlatform().equalsIgnoreCase(platform))
                                .collect(Collectors.toList());
        
        return ResponseEntity.ok(filtered);
    }


    @GetMapping("/fetch")
    public ResponseEntity<String> forceFetchContests(){
        log.info("Received manual request to fetch contests");
        contestService.fetchAndSaveContests();
        return ResponseEntity.ok("Contests fetched and saved successfully");
    }
}
