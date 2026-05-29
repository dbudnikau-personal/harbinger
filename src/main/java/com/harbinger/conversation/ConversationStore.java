package com.harbinger.conversation;

import com.harbinger.domain.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ConversationStore {

    private static final int MAX_HISTORY = 20;

    private final ConcurrentHashMap<String, List<Message>> store = new ConcurrentHashMap<>();

    public List<Message> get(String conversationId) {
        return List.copyOf(store.getOrDefault(conversationId, List.of()));
    }

    public void append(String conversationId, Message userMessage, Message assistantMessage) {
        store.compute(conversationId, (id, history) -> {
            List<Message> updated = new ArrayList<>(history != null ? history : List.of());
            updated.add(userMessage);
            updated.add(assistantMessage);
            if (updated.size() > MAX_HISTORY) {
                return updated.subList(updated.size() - MAX_HISTORY, updated.size());
            }
            return updated;
        });
    }

    public void clear(String conversationId) {
        store.remove(conversationId);
    }
}
