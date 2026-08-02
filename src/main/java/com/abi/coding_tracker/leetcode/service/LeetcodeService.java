package com.abi.coding_tracker.leetcode.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.abi.coding_tracker.exception.ExternalApiException;
import com.abi.coding_tracker.leetcode.dto.LeetcodeStatsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper; // ADD THIS IMPORT

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class LeetcodeService {
    
    private final WebClient webClient;

    public LeetcodeService(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("https://leetcode.com/").build();
    }

    public LeetcodeStatsResponse fetchUserProfile(String username){
        log.info("Fetching leetcode profile for username : {}", username);

        // 1. define the GraphQL query String
        String query = """
                query getUserProfile($username : String!){
                    matchedUser(username : $username){
                        profile {
                            ranking
                            reputation
                        }
                        submitStats {
                            acSubmissionNum {
                                difficulty
                                count
                            }
                        }
                    }
                }
                """;
        
        // 2. prepare the variables 
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);

        // 3. construct the GraphQL Request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);
        requestBody.put("operationName", "getUserProfile");

        // 4. Execute the request using webClient
        return this.webClient.post()
                .uri("/graphql")
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Referer", "https://leetcode.com/" + username + "/")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response->{
                    log.error("Client Error fetching Leetcode data for {} : {}", username, response.statusCode());
                    return Mono.error(new ExternalApiException("Failed to fetch user profile: Client Error. The user might not exist.", response.statusCode().value()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response->{
                    log.error("Server error from Leetcode API for {} : {}", username, response.statusCode());
                    return Mono.error(new ExternalApiException("Leetcode server is currently down or experiencing issue", response.statusCode().value()));
                })
                // 1. FETCH AS RAW STRING TO BYPASS WEBFLUX STRICT DECODERS
                .bodyToMono(String.class) 
                .flatMap(jsonString -> {
                    log.info("Successfully fetched data for {}", username);
                    
                    LeetcodeStatsResponse response = new LeetcodeStatsResponse();
                    response.setUsername(username);

                    try {
                        // 2. MANUALLY PARSE THE STRING INTO A JSON NODE
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(jsonString);

                        JsonNode matchedUser = node.path("data").path("matchedUser");

                        if(matchedUser.isMissingNode() || matchedUser.isNull()){
                            log.warn("Leetcode API returned null matchedUser for {}", username);
                            return Mono.error(new ExternalApiException("Leetcode user '"+username+"'not found.", 404));
                        }

                        JsonNode profile = matchedUser.path("profile");
                        response.setRanking(profile.path("ranking").asInt(0));
                        response.setReputation(profile.path("reputation").asInt(0));

                        // 3. Fallback check: Try submitStatsGlobal first, then submitStats if missing
                        JsonNode statsNode = matchedUser.path("submitStatsGlobal");
                        if (statsNode.isMissingNode()) {
                            statsNode = matchedUser.path("submitStats");
                        }
                        
                        JsonNode acSubmissionNum = statsNode.path("acSubmissionNum");

                        if(acSubmissionNum.isArray()){
                            for(JsonNode stat : acSubmissionNum){
                                String difficulty = stat.path("difficulty").asText();
                                int count = stat.path("count").asInt(0);

                                switch (difficulty) {
                                    case "All": response.setTotalSolved(count); break;
                                    case "Easy": response.setEasySolved(count); break;
                                    case "Medium": response.setMediumSolved(count); break;
                                    case "Hard": response.setHardSolved(count); break;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Failed to parse LeetCode JSON response: {}", ex.getMessage());
                        return Mono.error(new ExternalApiException("Failed to parse leetcode response", 500));
                    }
                    
                    return Mono.just(response);
                })
                .onErrorMap(e ->{
                    if(e instanceof ExternalApiException){
                        return e;
                    }
                    log.error("Exception occurred while fetching leetcode profile for {} : {}", username, e.getMessage());
                    return new ExternalApiException("An unexpected error occurred", 500);
                })
                .block();
    }
}