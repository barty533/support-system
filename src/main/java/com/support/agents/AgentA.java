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
 *
 * Answers ONLY from retrieved documentation chunks.
 * Uses semantic similarity search (embeddings) to find relevant content.
 *
 * A keyword fallback ensures the vehicle inventory doc is always included
 * when the user is browsing stock, since everyday words like "cars" or "sale"
 * may score lower than technical terms even with semantic search.
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

    // Inventory browse keywords — ensures inventory doc is included even when
    // semantic similarity scores it lower than technical docs
    // Only specific browsing-intent words and make/model names — deliberately
    // excludes generic words like "car", "have", "need" which appear in all
    // kinds of questions and would cause false inventory matches
    private static final Set<String> INVENTORY_KEYWORDS = Set.of(
        "inventory", "stock", "sale", "buy", "purchase",
        "show", "list", "browse", "available", "instock",
        "sedan", "suv", "truck", "van", "electric", "hybrid", "preowned",
        "honda", "toyota", "ford", "hyundai", "kia", "mazda",
        "chevrolet", "chevy", "tesla", "camry", "rav4", "tacoma",
        "f150", "explorer", "tucson", "telluride", "ioniq", "mache"
    );

    private static final String INVENTORY_DOC = "vehicle-inventory.md";

    private final ClaudeClient claude;
    private final DocumentStore docs;

    public AgentA(ClaudeClient claude, DocumentStore docs) {
        this.claude = claude;
        this.docs = docs;
    }

    public String handle(String userMessage, ConversationHistory history) throws IOException {

        // 1. Semantic similarity retrieval
        List<Chunk> relevant = new ArrayList<>(docs.retrieve(userMessage));

        // 2. If query is clearly technical (repair/fault/maintenance), remove
        //    inventory chunks since they are irrelevant and score too broadly
        if (isTechnicalQuery(userMessage)) {
            relevant.removeIf(c -> c.filename().equals(INVENTORY_DOC));
            System.out.println("[AgentA] Technical query detected — excluded inventory doc");
        }

        // 3. Inventory browsing: replace results entirely with all inventory chunks
        //    so no section (trucks, EVs etc.) gets cut off by the TOP_K limit
        if (isInventoryQuery(userMessage) && !isTechnicalQuery(userMessage)) {
            List<Chunk> inventoryChunks = docs.getChunksFromFile(INVENTORY_DOC);
            System.out.println("[AgentA] Inventory query — using all "
                    + inventoryChunks.size() + " inventory chunk(s)");
            relevant.clear();
            relevant.addAll(inventoryChunks);
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

    private boolean isInventoryQuery(String message) {
        String lower = message.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        for (String token : lower.split("\\s+")) {
            if (INVENTORY_KEYWORDS.contains(token)) return true;
        }
        return false;
    }

    // Words that indicate a technical/service question — not inventory browsing
    private static final Set<String> TECHNICAL_KEYWORDS = Set.of(
        "start", "starting", "wont", "broken", "fix", "repair", "problem",
        "issue", "fault", "error", "light", "warning", "noise", "leak",
        "smoke", "overheat", "battery", "engine", "transmission", "brake",
        "tyre", "tire", "oil", "fluid", "maintenance", "service", "check",
        "diagnostic", "code", "rpm", "stall", "idle", "vibration", "shake"
    );

    private boolean isTechnicalQuery(String message) {
        String lower = message.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        for (String token : lower.split("\\s+")) {
            if (TECHNICAL_KEYWORDS.contains(token)) return true;
        }
        return false;
    }
}
