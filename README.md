# 🐙 GitHub User Activity CLI

> Um cliente de API RESTful em formato CLI que consulta e exibe a atividade pública de qualquer utilizador do GitHub, diretamente no terminal.

---

## 📑 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Como Executar](#como-executar)
- [Referência de Comandos](#referência-de-comandos)
- [Arquitetura](#arquitetura)
- [Fluxo de Execução](#fluxo-de-execução)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Configuração](#configuração)
- [Próximos Passos](#próximos-passos)

---

## 🚀 Sobre o Projeto

O **GitHub User Activity CLI** permite consultar as últimas ações públicas de qualquer utilizador do GitHub (commits, criações de repositórios, estrelas, etc.) com um único comando no terminal.

```bash
shell:> github-activity NefeshDev
```

```
📦 Pushed 3 commit(s) to NefeshDev/task-tracker-cli
⭐ Starred torvalds/linux
🆕 Created repository NefeshDev/github-activity-cli
...
```

---

## 🛠 Tecnologias

| Tecnologia | Versão | Papel no projeto |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.3.5 | Framework base |
| Spring Shell | 3.3.3 | Interface interativa no terminal |
| Jackson | — | Desserialização de JSON |
| Lombok | — | Redução de código boilerplate |
| Maven Wrapper | — | Build e execução |

---

## 🚀 Como Executar

**1. Clona o repositório**

```bash
git clone https://github.com/teu-utilizador/github-activity-cli.git
cd github-activity-cli
```

**2. Inicia a aplicação**

```bash
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

**3. Experimenta um comando**

```
shell:> github-activity torvalds
```

---

## 📖 Referência de Comandos

| Comando | Descrição | Exemplo |
|---|---|---|
| `github-activity <username>` | Lista a atividade recente de um utilizador | `github-activity NefeshDev` |
| `help` | Mostra todos os comandos disponíveis | `help` |

### Tipos de eventos suportados

| Evento GitHub | Saída no terminal |
|---|---|
| `PushEvent` | `📦 Pushed N commit(s) to owner/repo` |
| `CreateEvent` | `🆕 Created repository owner/repo` |
| `WatchEvent` | `⭐ Starred owner/repo` |
| `ForkEvent` | `🍴 Forked owner/repo` |
| *(outros)* | `🔔 <tipo> in owner/repo` |

---

## 🏛 Arquitetura

O projeto aplica o princípio de **Separação de Responsabilidades**, dividindo o código em três camadas independentes:

```
┌─────────────────────────────────────┐
│         Camada de Interface         │
│  GitHubCommands (@ShellComponent)   │  ← O utilizador interage aqui
└────────────────┬────────────────────┘
                 │ chama
┌────────────────▼────────────────────┐
│        Camada de Integração         │
│      GitHubClient (@Component)      │  ← Fala com a API do GitHub
└────────────────┬────────────────────┘
                 │ retorna
┌────────────────▼────────────────────┐
│          Camada de Modelo           │
│   GitHubEventDTO / RepoDTO /        │
│         PayloadDTO (@Data)          │  ← Os dados ganham forma aqui
└─────────────────────────────────────┘
```

### Por que esta separação importa?

Se amanhã quiseres criar uma interface web para este projeto, **não tocas no DTO nem no Client**. Basta criar um `GitHubController` em vez do `GitHubCommands`. A arquitetura absorve a mudança sem dor.

---

### Anatomia das classes

#### `dto/` — Os moldes dos dados

**Classes:** `GitHubEventDTO`, `RepoDTO`, `PayloadDTO`

O GitHub devolve um JSON com dezenas de campos. Os DTOs funcionam como filtros: apenas os campos que te interessam são mapeados. O resto é descartado.

```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)  // ← ignora campos não mapeados
public class GitHubEventDTO {
    private String type;
    private RepoDTO repo;
    private PayloadDTO payload;
}
```

> **`@JsonIgnoreProperties(ignoreUnknown = true)`** é a anotação mais importante aqui. Sem ela, o Jackson lançaria um erro para cada campo do JSON que não tivesse variável correspondente na classe.

---

#### `GitHubClient` — A ponte para a internet

A **única** classe do projeto que sabe que o GitHub existe. Se a URL da API mudar, só esta classe precisa de ser alterada.

```java
@Component
public class GitHubClient {

    public List<GitHubEventDTO> getUserActivity(String username) {
        // faz GET https://api.github.com/users/{username}/events
        // devolve a resposta desserializada como List<GitHubEventDTO>
    }
}
```

---

#### `GitHubCommands` — O cérebro do terminal

Recebe o input do utilizador, orquestra as chamadas e formata a resposta.

```java
@ShellComponent  // equivalente a @RestController, mas para o terminal
public class GitHubCommands {

    @ShellMethod(key = "github-activity")
    public String getActivity(String username) {
        // 1. Busca os dados
        // 2. Valida se existem resultados
        // 3. Formata via switch (Java 21)
        // 4. Devolve o texto formatado
    }
}
```

A injeção do `GitHubClient` é feita pelo Spring automaticamente via construtor — não é necessário `new GitHubClient()` em lado nenhum.

---

## ⚡ Fluxo de Execução

O que acontece nos milissegundos após premires Enter:

```
Utilizador digita: github-activity NefeshDev
        │
        ▼
Spring Shell encontra @ShellMethod(key = "github-activity")
        │
        ▼
getActivity("NefeshDev") é invocado
        │
        ▼
GitHubClient faz GET https://api.github.com/users/NefeshDev/events
        │
        ▼
GitHub devolve JSON (centenas de linhas)
        │
        ▼
Jackson desserializa → List<GitHubEventDTO>
        │
        ▼
switch(event.getType()) formata cada evento
        │
        ▼
StringBuilder monta a resposta final
        │
        ▼
Resultado impresso no terminal ✅
```

---

## 📁 Estrutura do Projeto

```
github-activity-cli/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/exemplo/githubactivity/
│       │       ├── GitHubActivityApplication.java   # Ponto de entrada
│       │       ├── commands/
│       │       │   └── GitHubCommands.java           # Comandos Shell
│       │       ├── client/
│       │       │   └── GitHubClient.java             # Integração com a API
│       │       └── dto/
│       │           ├── GitHubEventDTO.java           # Evento principal
│       │           ├── RepoDTO.java                  # Dados do repositório
│       │           └── PayloadDTO.java               # Detalhes do evento
│       └── resources/
│           └── application.properties
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## 💡 Configuração

```properties
# Ativa o modo interativo do Spring Shell
spring.shell.interactive.enabled=true
```

---

## 🗺 Próximos Passos

- [ ] **Cache local** — evitar chamadas repetidas para o mesmo utilizador num curto espaço de tempo
- [ ] **Paginação** — suporte ao parâmetro `--limit N` para controlar o número de eventos exibidos
- [ ] **Autenticação** — usar um token GitHub para aumentar o rate limit de 60 para 5000 req/hora
- [ ] **Mais eventos** — mapear tipos como `IssuesEvent`, `PullRequestEvent`, `ReleaseEvent`
- [ ] **Output em JSON** — flag `--json` para exportar os resultados em formato estruturado
- [ ] **Testes** — cobertura com JUnit 5 e mocks da API com WireMock

---

## 📄 Licença

Este projeto está sob a licença MIT. Consulta o ficheiro [LICENSE](LICENSE) para mais detalhes.

---

<div align="center">
  Feito com ☕ e Spring Boot
  https://roadmap.sh/projects/github-user-activity
</div>
