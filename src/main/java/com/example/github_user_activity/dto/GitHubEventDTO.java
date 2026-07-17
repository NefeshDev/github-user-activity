package com.example.github_user_activity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubEventDTO {
    private String type;
    private RepoDTO repo;
    private String created_at;
}
