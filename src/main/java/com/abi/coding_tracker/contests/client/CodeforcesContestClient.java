package com.abi.coding_tracker.contests.client;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.abi.coding_tracker.codeforces.dto.CodeforcesApiResponse;
import com.abi.coding_tracker.contests.dto.ContestResponse;
import com.abi.coding_tracker.exception.ExternalApiException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CodeforcesContestClient implements ContestClient {
    
    private final WebClient webClient;

    public CodeforcesContestClient(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("https://codeforces.com/api/").build();
    }

    @Override
    public String getPlatformName() {
        return "Codeforces";
    }

    @Override
    public List<ContestResponse> fetchUpcomingContests(){
        log.info("Fetching upcoming contests from Codeforces...");

        try{
            CodeforcesApiResponse<List<CFContestDto>> response = this.webClient.get()
                    .uri("contest.list")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res-> Mono.error(new ExternalApiException("Failed to fetch codeforces contests", res.statusCode().value())))
                    .bodyToMono(new ParameterizedTypeReference<CodeforcesApiResponse<List<CFContestDto>>>() {})
                    .block();

            List<ContestResponse> upcomingContests = new ArrayList<>();

            if(response != null && "OK".equals(response.getStatus()) && response.getResult() != null){
                for(CFContestDto cfContest : response.getResult()){
                    if("BEFORE".equals(cfContest.getPhase())){
                        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(cfContest.getStartTimeSeconds()), ZoneId.systemDefault());
                        
                        ContestResponse contest = ContestResponse.builder()
                                    .platform(getPlatformName())
                                    .contestId(cfContest.getId().toString())
                                    .name(cfContest.getName())
                                    .startTime(startTime)
                                    .durationSeconds(cfContest.getDurationSeconds())
                                    .url("https://codeforces.com/contest/" + cfContest.getId())
                                    .status("UPCOMING")
                                    .build();
                        
                        upcomingContests.add(contest);
                    }
                }
            }
            return upcomingContests;
        }catch(Exception e){
            log.error("Error fetching Codeforces contests: {}", e.getMessage());
            return new ArrayList<>(); 
        }
    }

    @Data
    private static class CFContestDto{
        private Long id;
        private String name;
        private String phase;
        private int durationSeconds;
        private long startTimeSeconds;
    }
}
