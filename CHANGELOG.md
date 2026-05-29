# Changelog

## [Unreleased]

### Planned
- Tool Use / Function Calling — agents can read files, run commands, write code
- RAG over CLAUDE.md — vector search instead of full-file injection
- API authentication (API key / JWT)
- Streaming responses (SSE)
- Persistent conversation history (Redis)
- Circuit breaker (Resilience4j)

---

## [0.2.0] — 2026-05-29

### Added
- **DeepSeek profile** — `application-deepseek.yml` + `ChatClientConfiguration` for explicit provider selection per profile. Run with `SPRING_PROFILES_ACTIVE=deepseek`.
- **Security audit** — `SecretPatternMatcher` in `domain` module covers API keys, AWS credentials, connection strings (`jdbc:`, `mongodb://`, `redis://`), bearer tokens, SSH/OpenSSH private keys. `ProjectContextLoader` scans `CLAUDE.md` before injecting into prompts. Conversation history sanitized through `InputSanitizer` on every turn.
- **Conversation history** — in-memory `ConversationStore` per `conversationId`, capped at 20 messages. `ChatResponse` returns `conversationId` for multi-turn dialogs.
- **Audit logging** — `AuditLogger` logs agent name, project, latency after every dispatch. Query and response content never logged (Rule 6).
- **OpenAPI / Swagger** — `springdoc-openapi-starter-webmvc-ui:2.8.6`. UI at `/swagger-ui.html`, spec at `/api-docs`.
- **Docker** — multi-stage `Dockerfile` (`eclipse-temurin:21.0.5-jdk-alpine` → `jre-alpine`). `docker-compose.yml` with Ollama and project repo mounts.
- **Resume agent** — `ResumeAgent` for the resume project (US/EU variants, Pandoc build).

### Fixed
- Downgraded Spring Boot `4.0.6 → 3.5.14` — Spring AI 1.0.0 is incompatible with Spring Boot 4.x (`RestClientAutoConfiguration` removed).

---

## [0.1.0] — 2026-05-29

### Added
- **Spring Modulith architecture** — 7 modules: `domain`, `llm`, `context`, `orchestrator`, `agents`, `conversation`, `api`. Boundaries verified via `ApplicationModules.verify()`.
- **LLM-based routing** — `LlmRouter` asks the model which project a query belongs to. Replaced keyword matching in `supports()`.
- **5 project agents** — `ApiMeterAgent`, `HermesAgent`, `JuntoAgent`, `HrVacancyAgent`, `ResumeAgent`. Each loaded with project `CLAUDE.md` as context.
- **Vendor-agnostic LLM** — `SpringAiLlmAdapter` supports Anthropic, OpenAI, Ollama. Swap via `application.yml`.
- **Security guards** — `SecretsGuard` blocks secrets in prompts and responses. `InputSanitizer` strips prompt injection patterns.
- **CI pipeline** — GitHub Actions on `ubuntu-22.04`, Corretto 21, pinned versions.
- **56 unit tests** — `SecretPatternMatcherTest`, `SecretsGuardTest`, `InputSanitizerTest`, `OrchestratorTest`, `LlmRouterTest`, `ConversationStoreTest`, `AuditLoggerTest`.
- **Global exception handler** — `ApiExceptionHandler` with 400/422/500 responses.
- **Checkstyle** — enforced at `validate` phase, indentation rules aligned with IDE.
- **maven-enforcer** — fails build if not Java 21.
