package com.abi.coding_tracker.contests.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.contests.dto.ContestResponse;
import com.abi.coding_tracker.contests.service.ContestService;

@RestController
@RequestMapping("/contests")
public class ContestController {
    private final ContestService contestService;

    public ContestController(ContestService contestService){
        this.contestService = contestService;
    }

    @GetMapping
    public ResponseEntity<List<ContestResponse>> getUpcomingConstests(){
        return ResponseEntity.ok(contestService.getUpcomingContests());
    }
}
