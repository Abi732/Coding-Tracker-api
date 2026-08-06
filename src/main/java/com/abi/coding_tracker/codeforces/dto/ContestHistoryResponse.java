package com.abi.coding_tracker.codeforces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ContestHistoryResponse {
    
    private long contestId;
    private String contestName;
    private Integer rank;

    private Integer oldRating;
    private Integer newRating;

    @JsonProperty("ratingUpdateTimeSeconds")
    private long contestTime;

    public Integer getRatingChange(){
        if(newRating != null && oldRating != null){
            return newRating - oldRating;
        }
        return 0;
    }
}
