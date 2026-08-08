package com.abi.coding_tracker.contests.client;

import java.util.List;

import com.abi.coding_tracker.contests.dto.ContestResponse;

public interface ContestClient {
    /**
     * @return The identifier of the platform (e.g., "LeetCode", "Codeforces")
     */
    String getPlatformName();

    /**
     * Fetches the raw upcoming contests from the external API and maps them to our uniform DTO.
     * @return List of standardized ContestResponse objects
     */
    List<ContestResponse> fetchUpcomingContests();
}
