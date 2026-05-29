package com.harbinger.api;

import com.harbinger.domain.AgentResponse;
import com.harbinger.orchestrator.Orchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chat", description = "Send queries to project-specific AI agents")
class ChatController {

    private final Orchestrator orchestrator;

    ChatController(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping
    @Operation(
            summary = "Send a message",
            description = "Routes the query to the most relevant project agent. "
                    + "Pass conversationId from a previous response to continue the same conversation.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Agent response",
                        content = @Content(schema = @Schema(implementation = ChatResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid request"),
                @ApiResponse(responseCode = "422", description = "Sensitive data detected in request")
            }
    )
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

    @Schema(description = "Chat request")
    record ChatRequest(
            @NotBlank @Schema(description = "User message", example = "How does api-meter track token usage?")
            String message,
            @Schema(description = "Conversation ID to continue an existing conversation", nullable = true)
            String conversationId
    ) {
    }

    @Schema(description = "Chat response")
    record ChatResponse(
            @Schema(description = "Agent answer") String answer,
            @Schema(description = "Name of the project agent that handled the request") String handledBy,
            @Schema(description = "Conversation ID — pass in next request to continue") String conversationId
    ) {
    }
}
