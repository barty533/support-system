package com.support.agents;

import com.support.model.ClaudeClient;
import com.support.model.Config;
import com.support.model.ConversationHistory;
import com.support.rag.DocumentStore;
import com.support.rag.DocumentStore.Chunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Agent A – Technical Specialist.
 * Answers ONLY from retrieved documentation chunks.
 *
 * Uses TF-IDF retrieval for most queries, with a keyword-based fallback
 * that forces vehicle-inventory.md into context when the user is browsing
 * stock but uses everyday words ("cars", "sale") that don't appear in the doc.
 */
public class AgentA {

    private static final String SYSTEM_PROMPT = """
        You are a Technical Specialist at AutoPrime car dealership.
        You answer customer questions STRICTLY based on the documentation
        excerpts provided to you. These cover vehicle inventory, diagnostics,
        engine and electrical systems, and scheduled maintenance.

        Rules you must follow without exception:
        1. Only use information present in the provided documentation excerpts.
        2. If the documentation does not contain a sufficient answer, respond with:
           "I don't have that information in our technical documentation.
            Please call our service team at (555) 123-4567."
        3. Never guess, infer beyond the text, or draw on outside knowledge.
        4. Reference the relevant section of the documentation when helpful.
        5. Be clear, accurate, and concise.
        """;

    // Words that signal the user wants to browse inventory but won't match
    // the doc's vocabulary ("vehicles", "stock") via TF-IDF
    private static final Set<String> INVENTORY_KEYWORDS = Set.of(
        "car", "cars", "sale", "buy", "purchase", "available", "have",
        "show", "list", "looking", "need", "want", "get", "find",
        "sedan", "suv", "truck", "van", "electric", "hybrid",
        "honda", "toyota", "ford", "hyundai", "kia", "mazda",
        "chevrolet", "chevy", "tesla", "new", "used", "preowned"
    );

    private static final String INVENTORY_DOC = "vehicle-inventory.md";

    private final ClaudeClient claude;
    private final DocumentStore docs;

    public AgentA(ClaudeClient claude, DocumentStore docs) {
        this.claude = claude;
        this.docs = docs;
    }

    public String handle(String userMessage, ConversationHistory history) throws IOException {

        // 1. TF-IDF retrieval
        List<Chunk> relevant = new ArrayList<>(docs.retrieve(userMessage));

        // 2. Keyword fallback: if inventory doc is not already in results
        //    but the query looks like an inventory browse, force-include it
        boolean inventoryAlreadyIncluded = relevant.stream()
                .anyMatch(c -> c.filename().equals(INVENTORY_DOC));

        if (!inventoryAlreadyIncluded && isInventoryQuery(userMessage)) {
            List<Chunk> inventoryChunks = docs.getChunksFromFile(INVENTORY_DOC);
            System.out.println("[AgentA] Keyword fallback: adding "
                    + inventoryChunks.size() + " chunk(s) from " + INVENTORY_DOC);
            relevant.addAll(0, inventoryChunks); // prepend so they appear first
        }

        System.out.println("[AgentA] Final chunks (" + relevant.size() + "):");
        relevant.forEach(c -> System.out.println("  - " + c.filename()));

        String contextBlock = relevant.isEmpty()
                ? "[No relevant documentation found for this query.]"
                : relevant.stream()
                        .map(c -> "--- From: " + c.filename() + " ---\n" + c.content())
                        .collect(Collectors.joining("\n\n"));

        String augmentedMessage = """
                [Documentation excerpts]
                %s

                [Customer question]
                %s
                """.formatted(contextBlock, userMessage);

        List<Map<String, String>> messagesForApi = new ArrayList<>(
                history.toApiFormat(history.size() - 1)
        );
        messagesForApi.add(Map.of("role", "user", "content", augmentedMessage));

        String reply = claude.chat(SYSTEM_PROMPT, messagesForApi, Config.AGENT_MAX_TOKENS);
        history.addAssistantMessage(reply);
        return reply;
    }

    /** Returns true if the query contains any inventory-browse keyword. */
    private boolean isInventoryQuery(String message) {
        String lower = message.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        for (String token : lower.split("\\s+")) {
            if (INVENTORY_KEYWORDS.contains(token)) return true;
        }
        return false;
    }
}
