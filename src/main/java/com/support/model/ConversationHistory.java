package com.support.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Stores the full multi-turn conversation history.
 * Uses the Message record internally; exposes Map format for the API client.
 */
public class ConversationHistory {

    private final List<Message> messages = new ArrayList<>();

    public void addUserMessage(String content) {
        messages.add(Message.user(content));
    }

    public void addAssistantMessage(String content) {
        messages.add(Message.assistant(content));
    }

    /** Returns messages in the Map format required by ClaudeClient. */
    public List<Map<String, String>> toApiFormat() {
        return messages.stream()
                .map(Message::toMap)
                .collect(Collectors.toList());
    }

    /** Returns the last N messages in API format. */
    public List<Map<String, String>> toApiFormat(int lastN) {
        int start = Math.max(0, messages.size() - lastN);
        return messages.subList(start, messages.size()).stream()
                .map(Message::toMap)
                .collect(Collectors.toList());
    }

    public int size() {
        return messages.size();
    }
}
