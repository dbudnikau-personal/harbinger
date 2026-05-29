package com.harbinger.conversation;

import com.harbinger.domain.Message;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationStoreTest {

    private final ConversationStore store = new ConversationStore();

    @Test
    void shouldReturnEmptyHistoryForNewConversation() {
        assertTrue(store.get("new-id").isEmpty());
    }

    @Test
    void shouldStoreAndRetrieveMessages() {
        store.append("conv-1",
                new Message(Message.Role.USER, "Hello"),
                new Message(Message.Role.ASSISTANT, "Hi there")
        );

        List<Message> history = store.get("conv-1");
        assertEquals(2, history.size());
        assertEquals("Hello", history.get(0).content());
        assertEquals("Hi there", history.get(1).content());
    }

    @Test
    void shouldKeepHistoryAcrossMultipleTurns() {
        store.append("conv-2",
                new Message(Message.Role.USER, "First question"),
                new Message(Message.Role.ASSISTANT, "First answer")
        );
        store.append("conv-2",
                new Message(Message.Role.USER, "Second question"),
                new Message(Message.Role.ASSISTANT, "Second answer")
        );

        assertEquals(4, store.get("conv-2").size());
    }

    @Test
    void shouldCapHistoryAtMaxSize() {
        for (int i = 0; i < 12; i++) {
            store.append("conv-3",
                    new Message(Message.Role.USER, "question " + i),
                    new Message(Message.Role.ASSISTANT, "answer " + i)
            );
        }

        assertEquals(20, store.get("conv-3").size());
    }

    @Test
    void shouldClearConversation() {
        store.append("conv-4",
                new Message(Message.Role.USER, "Hello"),
                new Message(Message.Role.ASSISTANT, "Hi")
        );
        store.clear("conv-4");

        assertTrue(store.get("conv-4").isEmpty());
    }

    @Test
    void shouldIsolateConversations() {
        store.append("conv-a",
                new Message(Message.Role.USER, "A question"),
                new Message(Message.Role.ASSISTANT, "A answer")
        );

        assertTrue(store.get("conv-b").isEmpty());
    }
}
