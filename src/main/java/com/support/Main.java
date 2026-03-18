package com.support;

import com.support.agents.AgentA;
import com.support.agents.AgentB;
import com.support.model.ClaudeClient;
import com.support.model.Config;
import com.support.model.ConversationHistory;
import com.support.rag.DocumentStore;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Entry point for the AutoPrime conversational AI support system.
 *
 * Requires the ANTHROPIC_API_KEY environment variable to be set.
 * Documentation files must be in the docs/ directory relative to the working directory.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Error: ANTHROPIC_API_KEY environment variable is not set.");
            System.exit(1);
        }

        String docsPathStr = System.getenv().getOrDefault(
                Config.DOCS_PATH_ENV, Config.DOCS_PATH_DEFAULT);

        DocumentStore docs = new DocumentStore();
        docs.loadDirectory(Paths.get(docsPathStr));

        ClaudeClient claude       = new ClaudeClient(apiKey);
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
