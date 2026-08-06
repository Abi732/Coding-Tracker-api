package com.abi.coding_tracker.codeforces.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.abi.coding_tracker.codeforces.dto.CodeforcesApiResponse;
import com.abi.coding_tracker.codeforces.dto.CodeforcesProfileResponse;
import com.abi.coding_tracker.codeforces.dto.ContestHistoryResponse;
import com.abi.coding_tracker.exception.ExternalApiException;

import reactor.core.publisher.Mono;

@Component
public class CodeforcesClient {
    private final WebClient webClient;

    public CodeforcesClient(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("https://codeforces.com/api/").build();
    }

    public CodeforcesProfileResponse getUserInfo(String handle){
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("user.info").queryParam("handles", handle).build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response->Mono.error(new ExternalApiException("Codeforces handle not found"+handle, 404)))
                .onStatus(HttpStatusCode::is5xxServerError, response->Mono.error(new ExternalApiException("Codeforces Api is currently down.", 503)))
                .bodyToMono(new ParameterizedTypeReference<CodeforcesApiResponse<List<CodeforcesProfileResponse>>>() {})
                .map(apiResponse->{
                    if("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null && !apiResponse.getResult().isEmpty()){
                        return apiResponse.getResult().get(0);
                    }
                    throw new ExternalApiException("Invalid response structure from Codeforces", 502);
                })
                .block();
    }

    public List<ContestHistoryResponse> getUserRating(String handle){
        return webClient.get()
                .uri(uriBuilder->uriBuilder.path("user.rating").queryParam("handle", handle).build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response->Mono.error(new ExternalApiException("Codeforces handle not found: "+handle, response.statusCode().value())))
                .onStatus(HttpStatusCode::is5xxServerError, response->Mono.error(new ExternalApiException("Codeforces API is currently down.", response.statusCode().value())))
                .bodyToMono(new ParameterizedTypeReference<CodeforcesApiResponse<List<ContestHistoryResponse>>>(){})
                .map(apiResponse->{
                    if("OK".equals(apiResponse.getStatus()) && apiResponse.getResult() != null){
                        return apiResponse.getResult();
                    }
                    throw new ExternalApiException("Invalid response from Codeforces API", 502);
                })
                .block();

    }
}
