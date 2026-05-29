package com.harbinger.api;

import com.harbinger.domain.AgentResponse;
import com.harbinger.orchestrator.Orchestrator;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
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
        String conversationId = request.conversationId() != null
                ? request.conversationId()
                : UUID.randomUUID().toString();

        AgentResponse response = orchestrator.dispatch(request.message(), conversationId);
        return ResponseEntity.ok(new ChatResponse(
                response.content(),
                response.project().name(),
                conversationId
        ));
    }

    record ChatRequest(@NotBlank String message, String conversationId) {
    }

    record ChatResponse(String answer, String handledBy, String conversationId) {
    }
}
