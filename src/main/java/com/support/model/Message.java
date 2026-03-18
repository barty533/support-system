package com.support.model;

import java.util.Map;

/**
 * Represents a single conversation message with a role and content.
 * Replaces the raw Map<String, String> used previously.
 */
public record Message(String role, String content) {

    public static Message user(String content) {
        return new Message("user", content);
    }

    public static Message assistant(String content) {
        return new Message("assistant", content);
    }

    /** Converts to the Map format expected by ClaudeClient for API calls. */
    public Map<String, String> toMap() {
        return Map.of("role", role, "content", content);
    }
}
