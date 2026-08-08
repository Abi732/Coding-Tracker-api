package com.abi.coding_tracker.contests.client;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.abi.coding_tracker.contests.dto.ContestResponse;
import com.abi.coding_tracker.exception.ExternalApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LeetcodeContestClient implements ContestClient {
    
    private final WebClient webClient;

    public LeetcodeContestClient(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("https://leetcode.com/").build();
    }

    @Override
    public String getPlatformName(){
        return "Leetcode";
    }

    @Override
    public List<ContestResponse> fetchUpcomingContests(){
        log.info("Fetching upcoming contest from Leetcode....");

        String query = """
                query{
                    topTwoContests{
                        title,
                        titleSlug,
                        startTime,
                        duration
                    }
                }
                """;
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);    

        try{
            String jsonResponse = this.webClient.post()
                    .uri("graphql")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response->
                        Mono.error(new ExternalApiException("Failed to fetch LeetCode contests", response.statusCode().value())))
                    .bodyToMono(String.class)
                    .block();

            return parseGraphQLRepsonse(jsonResponse);
        }catch(Exception e){
            log.error("Error fetching leetcode Contests: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<ContestResponse> parseGraphQLRepsonse(String jsonString){
        List<ContestResponse> contests = new ArrayList<>();

        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonString);
            JsonNode topTwo = root.path("data").path("topTwoContests");

            if(topTwo.isArray()){
                for(JsonNode node : topTwo){
                    Long startTimeUnix = node.path("startTime").asLong();
                    LocalDateTime starTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTimeUnix), ZoneId.systemDefault());

                    String titleSlug = node.path("titleSlug").asText();

                    ContestResponse contest = ContestResponse.builder()
                                        .platform(getPlatformName())
                                        .contestId(titleSlug)
                                        .name(node.path("title").asText())
                                        .startTime(starTime)
                                        .durationSeconds(node.path("duration").asInt())
                                        .url("https://leetcode.com/contest/" + titleSlug)
                                        .status("UPCOMING")
                                        .build();
                    contests.add(contest);
                }
            }
        }catch(Exception e){
            log.error("Failed to parse LeetCode contest JSON", e);
        }

        return contests;
    }
}
