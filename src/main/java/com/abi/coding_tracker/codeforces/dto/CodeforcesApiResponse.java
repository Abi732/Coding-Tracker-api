package com.abi.coding_tracker.codeforces.dto;

import lombok.Data;

@Data
public class CodeforcesApiResponse<T> {
    
    private String status;
    private String comment;

    private T result;
}
