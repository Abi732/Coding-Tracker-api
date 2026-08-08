package com.abi.coding_tracker.github.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubProfileResponse{
    @JsonProperty("login")
    private String username;

    private String name;

    private Integer followers;

    private Integer following;

    @JsonProperty("public_repos")
    private Integer publicRepos;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String bio;

    private Boolean cached;
    private LocalDateTime lastUpdated;
}
