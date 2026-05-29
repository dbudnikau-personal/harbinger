# Harbinger

![CI](https://github.com/dbudnikau-personal/harbinger/actions/workflows/ci.yml/badge.svg)

AI agent orchestrator that routes queries to project-specific agents, each loaded with context from its own `CLAUDE.md`. Built as a central knowledge hub for managing multiple pet projects without re-explaining context every time.

## Architecture

```
POST /api/v1/chat
        │
        ▼
  Orchestrator  ──── LlmRouter (identifies project via LLM) ────►  ProjectAgent
        │                                                                │
        │  (no match / "general")                                        │  loads + scans CLAUDE.md
        ▼                                                                ▼
  FallbackAgent                                                    LlmGateway
                                                                         │
                                                            Anthropic / OpenAI / DeepSeek / Ollama
```

### Modules (Spring Modulith)

| Module | Responsibility |
|--------|---------------|
| `domain` | Pure domain types — `Project`, `AgentPort`, `LlmPort`, `SecretPatternMatcher`. No Spring. |
| `llm` | Spring AI adapter. Vendor-agnostic. Guards every call with `SecretsGuard` and `InputSanitizer`. |
| `context` | Loads and security-scans `CLAUDE.md` from each project path before injecting as agent context. |
| `orchestrator` | LLM-based routing to the matching `AgentPort`. Manages conversation dispatch. |
| `agents` | One agent per project: `ApiMeterAgent`, `HermesAgent`, `JuntoAgent`, `HrVacancyAgent`, `ResumeAgent`. |
| `conversation` | In-memory conversation store — maintains history per `conversationId`, capped at 20 messages. |
| `audit` | Logs agent name, project, and latency after every dispatch. Query content never logged. |
| `api` | REST endpoint + global exception handler. OpenAPI at `/swagger-ui.html`. |

Module boundaries verified at test time via `ApplicationModules.verify()`.

## Security

Every LLM call passes through multiple guards:

| What | Where | How |
|------|-------|-----|
| API keys, connection strings, private keys | User input, system prompt, LLM response | `SecretsGuard` → throws `SecretLeakException` |
| Prompt injection | User input + conversation history | `InputSanitizer` → replaces with `[filtered]` |
| Secrets in project context | `CLAUDE.md` before injecting into prompt | `SecretPatternMatcher` → excludes context, logs warning |

Secrets never appear in agent context, prompts, or logs. Query content is never written to logs.

## Agents

| Agent | Project | Routes when query mentions |
|-------|---------|---------------------------|
| `ApiMeterAgent` | api-meter | usage tracking, token cost, REST API metrics |
| `HermesAgent` | hermes | Telegram bot, GCP deployment, Python agent |
| `JuntoAgent` | junto-app | Keycloak, Kafka, Spring Cloud, GKE |
| `HrVacancyAgent` | hr-vacancy-bot-aws | AWS Lambda, vacancy scraping, Playwright |
| `ResumeAgent` | resume | CV content, US/EU variants, Pandoc build |
| `FallbackAgent` | — | everything else |

Routing is LLM-based — the orchestrator asks the model which project the query belongs to.

## Stack

- Java 21, Spring Boot 3.5.14
- [Spring AI 1.0.0](https://spring.io/projects/spring-ai) — vendor-agnostic LLM client
- [Spring Modulith 1.4.1](https://spring.io/projects/spring-modulith) — enforced module boundaries
- Maven, Checkstyle, Mockito, SpringDoc OpenAPI

## Getting started

**Requirements:** Java 21, Maven 3.9+

```bash
git clone https://github.com/dbudnikau-personal/harbinger.git
cd harbinger

export ANTHROPIC_API_KEY=your_key_here

JAVA_HOME=/path/to/java21 mvn spring-boot:run
```

Send a query:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "How does api-meter track token usage?"}'
```

Response:

```json
{
  "answer": "api-meter exposes a REST endpoint that records...",
  "handledBy": "api-meter",
  "conversationId": "abc-123"
}
```

Continue the conversation:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What endpoints does it expose?", "conversationId": "abc-123"}'
```

## LLM providers

Switch vendor via `application.yml` — no code changes needed:

| Profile | Provider | Model |
|---------|----------|-------|
| default | Anthropic | claude-sonnet-4-6 |
| `deepseek` | DeepSeek (OpenAI-compatible) | deepseek-chat |
| — | OpenAI | gpt-4o |
| — | Ollama (local) | llama3.2 |

```bash
# Run with DeepSeek
export DEEPSEEK_API_KEY=sk-your-token
SPRING_PROFILES_ACTIVE=deepseek JAVA_HOME=/path/to/java21 mvn spring-boot:run
```

## Docker

```bash
export ANTHROPIC_API_KEY=your_key
docker compose up
```

Includes Ollama as local LLM option. Project repos mounted read-only for context loading.

## API docs

Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI spec: `http://localhost:8080/api-docs`

## Running tests

```bash
JAVA_HOME=/path/to/java21 mvn test
```

56 unit tests covering security guards, routing logic, conversation store, and orchestrator.

## Adding a new project agent

1. Create `<Name>Agent extends ProjectAgent` in `agents/`
2. Inject path via `@Value("${harbinger.projects.<name>.path}")`
3. Add path config to `application.yml`
4. LLM router picks it up automatically — no routing code needed
