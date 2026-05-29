# Harbinger

AI agent orchestrator — routes user queries to project-specific agents, each loaded with context from its own `CLAUDE.md`.

## Stack

- Java 21, Spring Boot 4.0.6
- Spring AI (vendor-agnostic LLM: Anthropic, OpenAI, Ollama)
- Spring Modulith (enforced module boundaries)
- Maven, Checkstyle, Lombok

## Architecture (Spring Modulith)

```
com.harbinger/
├── domain/        # Pure domain: Project, Message, AgentResponse, LlmPort, AgentPort — no Spring
├── context/       # Loads CLAUDE.md from project paths — supplies context to agents
├── llm/           # Spring AI adapter implementing LlmPort — swap vendor via application.yml
├── orchestrator/  # Routes query to matching AgentPort; FallbackAgent if no match
├── agents/        # One agent per project: ApiMeter, Hermes, Junto, HrVacancy
└── api/           # REST: POST /api/v1/chat → ChatController → Orchestrator
```

Module rules (verified by `modules.verify()` in tests):
- `api` depends on `orchestrator` only
- `orchestrator` depends on `domain`, `agents`
- `agents` depends on `domain`, `context`, `llm`
- `llm` depends on `domain` only
- `domain` has zero dependencies

## Configuration

All LLM credentials and project paths via environment variables — never hardcoded.

| Variable | Description |
|---|---|
| `ANTHROPIC_API_KEY` | Anthropic API key |
| `OPENAI_API_KEY` | OpenAI API key (optional) |
| `OLLAMA_BASE_URL` | Ollama base URL (default: http://localhost:11434) |
| `HARBINGER_PROJECT_API_METER_PATH` | Path to api-meter repo |
| `HARBINGER_PROJECT_HERMES_PATH` | Path to hermes repo |
| `HARBINGER_PROJECT_JUNTO_PATH` | Path to junto-app repo |
| `HARBINGER_PROJECT_HR_PATH` | Path to hr-vacancy-bot-aws repo |

## Adding a new project agent

1. Create `<Name>Agent extends ProjectAgent` in `agents/`
2. Implement `supports(String query)` — keyword matching
3. Inject path via `@Value("${harbinger.projects.<name>.path}")`
4. Add path config to `application.yml`

## Core rules for agent systems

These rules apply to Harbinger and any future agent-based project.

### Rule 1 — Secrets never reach agents or LLM

Secrets (API keys, tokens, passwords, connection strings) must be **architecturally isolated** from the agent layer:

- Secrets live only in environment variables or a secrets manager (Vault, AWS Secrets Manager)
- No secret is ever injected into a system prompt, user message, or agent context
- `SecretsGuard` (in `llm/`) intercepts every outbound LLM call and scans for secret-like patterns — throws if found
- Agents receive only sanitized, secret-free context from `ProjectContextLoader`
- LLM adapter (`SpringAiLlmAdapter`) is the only component that handles credentials, and only to authenticate the HTTP call — it never passes them downstream to prompts
- Logs must never contain secrets — `SensitiveDataMasker` masks known patterns before any log output

### Rule 2 — Agent boundary isolation

Each agent sees only its own project context. An agent for `api-meter` must never receive context from `hermes` or any other project. The orchestrator routes, it does not merge contexts.

### Rule 3 — Prompt injection defense

All user input passes through `InputSanitizer` before reaching any agent. It strips instruction-override patterns (`ignore previous instructions`, `you are now`, role-switching attempts). Sanitization is logged but the original input is never logged.

### Rule 4 — Output validation before returning to caller

LLM responses pass through `OutputValidator` before leaving the `llm` module. It checks for secret-pattern leakage in the response itself (a model can sometimes echo back what was in context).

### Rule 5 — Principle of least privilege per agent

An agent may only read the file system path configured for its own project. No agent has write access. No agent can invoke another agent directly — all routing goes through `Orchestrator`.

### Rule 6 — Audit log, never data log

Every agent invocation is logged: which agent handled it, latency, token count, project name. The user query and LLM response are **never** written to logs in plain text.

## Code style

- Checkstyle enforced at `validate` phase
- No wildcard imports
- Braces on all control flow
- Package-private classes inside modules (only expose what's needed)

## Infrastructure Changelog

| Date | Change |
|---|---|
| 2026-05-29 | Initial project scaffold — domain, llm, context, orchestrator, agents, api |
