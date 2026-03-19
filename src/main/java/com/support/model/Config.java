package com.support.model;

/**
 * Central configuration constants for the support agent system.
 * Change values here to affect the whole application.
 */
public final class Config {

    private Config() {}

    // Anthropic model to use for all agents and the router
    public static final String MODEL = "claude-haiku-4-5-20251001";

    // Max tokens for router responses (single word only)
    public static final int ROUTER_MAX_TOKENS = 20;

    // Max tokens for agent responses
    public static final int AGENT_MAX_TOKENS = 2048;

    // Number of top document chunks to retrieve per query
    public static final int RAG_TOP_K = 4;

    // Number of lines per document chunk
    public static final int RAG_CHUNK_SIZE_LINES = 80;

    // Maximum tool-calling iterations for Agent B
    public static final int MAX_TOOL_ITERATIONS = 6;

    // HTTP retry settings
    public static final int HTTP_MAX_RETRIES = 3;
    public static final long HTTP_RETRY_BASE_DELAY_MS = 1000;

    // Environment variable names
    public static final String ANTHROPIC_API_KEY_ENV = "ANTHROPIC_API_KEY";
    public static final String VOYAGE_API_KEY_ENV    = "VOYAGE_API_KEY";
    public static final String DOCS_PATH_ENV         = "DOCS_PATH";
    public static final String DOCS_PATH_DEFAULT     = "docs";
}
