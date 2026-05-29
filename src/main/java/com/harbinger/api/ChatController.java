package com.harbinger.api;

import com.harbinger.domain.AgentResponse;
import com.harbinger.orchestrator.Orchestrator;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@Validated
class ChatController {

    private final Orchestrator orchestrator;

    ChatController(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping
    ResponseEntity<ChatResponse> chat(@RequestBody @Validated ChatRequest request) {
        AgentResponse response = orchestrator.dispatch(request.message());
        return ResponseEntity.ok(new ChatResponse(
            response.content(),
            response.project().name()
        ));
    }

    record ChatRequest(@NotBlank String message) {
    }

    record ChatResponse(String answer, String handledBy) {
    }
}
