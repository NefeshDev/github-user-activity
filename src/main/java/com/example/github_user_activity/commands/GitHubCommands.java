package com.example.github_user_activity.commands;

import com.example.github_user_activity.client.GitHubClient;
import com.example.github_user_activity.dto.GitHubEventDTO;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

@ShellComponent
public class GitHubCommands {

    private final GitHubClient gitHubClient;

    public GitHubCommands(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @ShellMethod(key = "github-activity", value = "Busca as atividades recentes de um usuário no GitHub")
    public String getActivity(String username) {
        try {
            List<GitHubEventDTO> events = gitHubClient.getUserActivity(username);

            if (events.isEmpty()) {
                return "Nenhuma atividade encontrada para o usuário: " + username;
            }

            StringBuilder output = new StringBuilder("Atividades recentes de " + username + ":\n");
            for (GitHubEventDTO event : events) {
                output.append("- ").append(event.getType())
                        .append(" em ").append(event.getRepo().getName())
                        .append("\n");
            }
            return output.toString();
        } catch (Exception e) {
            return "Erro ao buscar atividades: " + e.getMessage();
        }
    }
}