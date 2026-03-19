package com.support;

import com.support.agents.AgentA;
import com.support.agents.AgentB;
import com.support.model.ClaudeClient;
import com.support.model.Config;
import com.support.model.ConversationHistory;
import com.support.rag.DocumentStore;
import com.support.rag.EmbeddingClient;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Entry point for the AutoPrime conversational AI support system.
 *
 * Requires two environment variables:
 *   ANTHROPIC_API_KEY — for Claude (routing + agents)
 *   VOYAGE_API_KEY    — for Voyage AI (semantic document embeddings)
 *
 * Documentation files must be in the docs/ directory relative to
 * the working directory.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        // Read API keys
        String anthropicKey = System.getenv(Config.ANTHROPIC_API_KEY_ENV);
        if (anthropicKey == null || anthropicKey.isBlank()) {
            System.err.println("Error: ANTHROPIC_API_KEY environment variable is not set.");
            System.exit(1);
        }

        String voyageKey = System.getenv(Config.VOYAGE_API_KEY_ENV);
        if (voyageKey == null || voyageKey.isBlank()) {
            System.err.println("Error: VOYAGE_API_KEY environment variable is not set.");
            System.err.println("Get a free key at https://dash.voyageai.com");
            System.exit(1);
        }

        // Load and embed documentation
        String docsPathStr = System.getenv().getOrDefault(
                Config.DOCS_PATH_ENV, Config.DOCS_PATH_DEFAULT);

        EmbeddingClient embeddingClient = new EmbeddingClient(voyageKey);
        DocumentStore docs = new DocumentStore(embeddingClient);
        docs.loadDirectory(Paths.get(docsPathStr));

        // Wire up agents
        ClaudeClient claude       = new ClaudeClient(anthropicKey);
        AgentA agentA             = new AgentA(claude, docs);
        AgentB agentB             = new AgentB(claude);
        Orchestrator orchestrator = new Orchestrator(claude, agentA, agentB);
        ConversationHistory history = new ConversationHistory();

        Scanner scanner = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  AutoPrime Conversational Support System");
        System.out.println("  Type 'exit' to end the session.");
        System.out.println("==============================================\n");

        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.isBlank()) continue;
            if (userInput.equalsIgnoreCase("exit") ||
                userInput.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!");
                break;
            }

            history.addUserMessage(userInput);

            try {
                String reply = orchestrator.route(userInput, history);
                System.out.println("\nSupport: " + reply + "\n");
            } catch (IOException e) {
                System.err.println("Error communicating with the API: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
