# Harbinger

![CI](https://github.com/dbudnikau-personal/harbinger/actions/workflows/ci.yml/badge.svg)

AI agent orchestrator that routes queries to project-specific agents, each loaded with its own context. Built to manage multiple pet projects from a single entry point.

## Architecture

```
POST /api/v1/chat
        │
        ▼
  Orchestrator  ──── routes by keyword / LLM intent ────►  ProjectAgent
        │                                                        │
        │  (no match)                                            │  loads CLAUDE.md
        ▼                                                        ▼
  FallbackAgent                                            LlmGateway
                                                                 │
                                                    Anthropic / OpenAI / Ollama
```

### Modules (Spring Modulith)

| Module | Responsibility |
|--------|---------------|
| `domain` | Pure domain types — `Project`, `AgentPort`, `LlmPort`, `SecretLeakException`. No Spring. |
| `llm` | Spring AI adapter. Vendor-agnostic — swap provider via `application.yml`. Guards every call with `SecretsGuard` and `InputSanitizer`. |
| `context` | Loads `CLAUDE.md` from each project path and injects it as agent context. |
| `orchestrator` | Routes queries to the first matching `AgentPort`. Falls back to general assistant. |
| `agents` | One agent per project: `ApiMeterAgent`, `HermesAgent`, `JuntoAgent`, `HrVacancyAgent`. |
| `api` | REST endpoint. Global exception handler. |

Module boundaries are verified at test time via `ApplicationModules.verify()`.

## Security

Every LLM call passes through two guards:

- **`SecretsGuard`** — scans system prompt, user message, and LLM response for secret-like patterns (API keys, AWS credentials, private keys). Throws `SecretLeakException` if any are found.
- **`InputSanitizer`** — strips prompt injection patterns (`ignore previous instructions`, role-switching, jailbreak attempts) before the message reaches the model.

Secrets never appear in agent context, prompts, or logs.

## Stack

- Java 21, Spring Boot 4.0.6
- [Spring AI](https://spring.io/projects/spring-ai) — vendor-agnostic LLM client
- [Spring Modulith](https://spring.io/projects/spring-modulith) — enforced module boundaries
- Maven, Checkstyle, Mockito

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
  "handledBy": "api-meter"
}
```

## Switching LLM provider

Change `spring.ai.*` in `application.yml` — no code changes needed:

```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}   # default
    openai:
      api-key: ${OPENAI_API_KEY}
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
```

## Adding a new project agent

1. Create `<Name>Agent extends ProjectAgent` in `agents/`
2. Implement `supports(String query)` — keyword or LLM-based routing
3. Add `@Value("${harbinger.projects.<name>.path}")` for the project path
4. Register the path in `application.yml`

## Running tests

```bash
JAVA_HOME=/path/to/java21 mvn test
```

32 unit tests covering `SecretsGuard`, `InputSanitizer`, and `Orchestrator` routing logic.
