package com.example.github_user_activity.client;

import com.example.github_user_activity.dto.GitHubEventDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class GitHubClient {
    private final RestClient restClient;

    public GitHubClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .build();
    }

    public List<GitHubEventDTO> getUserActivity(String username) {
        return restClient.get()
                .uri("/users/{username}/events", username)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}