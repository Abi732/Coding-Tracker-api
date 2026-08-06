package com.abi.coding_tracker.platform;

import java.time.LocalDateTime;

import com.abi.coding_tracker.entity.User;

public interface CodingPlatformService {
    /**
     * @return The unique identifier of the platform (e.g., "leetcode", "codeforces")
     */
    String getPlatformName();

    /**
     * @param user The user to check
     * @return The exact time the user's stats were last synced, or null if not connected
     */
    LocalDateTime getLastSync(User user);

    /**
     * Forces a background refresh of the user's data for this platform
     * @param user The user to refresh
     */

    void refresh(User user);
}
