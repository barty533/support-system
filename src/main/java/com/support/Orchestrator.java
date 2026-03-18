package com.support;

import com.support.agents.AgentA;
import com.support.agents.AgentB;
import com.support.model.ClaudeClient;
import com.support.model.Config;
import com.support.model.ConversationHistory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Orchestrator — classifies each user message and routes to the correct agent.
 *
 * TECHNICAL → Agent A (RAG over technical documentation)
 * BILLING   → Agent B (tool-calling for finances, billing, warranty)
 * OTHER     → graceful fallback
 */
public class Orchestrator {

    private static final String ROUTER_SYSTEM = """
        You are a classifier for a car dealership support system.
        Output ONLY one word — no punctuation, no explanation.

        PRIORITY RULE: If the message contains ANY financing or billing keyword
        (financing, finance, loan, APR, monthly, afford, payment, how much per month,
        deposit, refund, warranty, claim, balance, invoice, charge) classify as BILLING
        even if it also mentions a specific vehicle model or make.
        Examples:
          "financing options on the Tacoma"       → BILLING
          "how much would a RAV4 cost monthly"    → BILLING
          "can I get a loan on the F-150"         → BILLING
          "what APR do you offer on a Honda"      → BILLING

        BILLING — money, accounts, or financing, including:
          - financing, loan, APR, monthly payments, how much per month, afford
          - my account, my balance, my loan, payment history
          - billing statement, invoices, charges
          - warranty coverage, warranty claim, service claim
          - refunds, returns, deposits
          - a name or customer ID given in response to a billing question

        TECHNICAL — cars and services (only when no billing keyword is present):
          - inventory, vehicles, what cars do you have, on sale, for sale
          - deals, discounts, cheapest, best price, what models, show me
          - specific makes or models: Honda, Toyota, Ford, Hyundai, Kia, Mazda, Chevrolet, Tesla
          - vehicle specs, colours, mileage, features, availability, towing capacity
          - diagnostics, warning lights, OBD codes, check engine light
          - engine, transmission, battery, electrical, brakes, tyres
          - maintenance, oil change, service appointments
          - pre-owned, used cars, certified pre-owned

        OTHER — topics completely unrelated to a car dealership.

        IMPORTANT: If the previous assistant message asked for a name or customer ID,
        classify the user's reply as BILLING.

        Output ONLY the single word: TECHNICAL, BILLING, or OTHER.
        """;

    private static final String OUT_OF_SCOPE_MESSAGE =
            "I'm sorry, but I cannot assist with that request. "
            + "Please contact our support team at support@autoprime.com for further help.";

    private final ClaudeClient claude;
    private final AgentA agentA;
    private final AgentB agentB;

    public Orchestrator(ClaudeClient claude, AgentA agentA, AgentB agentB) {
        this.claude = claude;
        this.agentA = agentA;
        this.agentB = agentB;
    }

    public String route(String userMessage, ConversationHistory history) throws IOException {
        String intent = classify(userMessage, history);
        System.out.println("[Orchestrator] Routing intent: " + intent);

        return switch (intent) {
            case "TECHNICAL" -> {
                System.out.println("[Orchestrator] -> Agent A (Technical Specialist)");
                yield agentA.handle(userMessage, history);
            }
            case "BILLING" -> {
                System.out.println("[Orchestrator] -> Agent B (Finance & Billing Specialist)");
                yield agentB.handle(history);
            }
            default -> {
                System.out.println("[Orchestrator] -> Out-of-scope fallback");
                history.addAssistantMessage(OUT_OF_SCOPE_MESSAGE);
                yield OUT_OF_SCOPE_MESSAGE;
            }
        };
    }

    private String classify(String userMessage, ConversationHistory history) throws IOException {
        // Include last 2 turns as context so follow-up replies are classified correctly
        StringBuilder ctx = new StringBuilder();
        List<Map<String, String>> recent = history.toApiFormat(4);
        for (int i = 0; i < recent.size() - 1; i++) {
            var msg = recent.get(i);
            String role = "assistant".equals(msg.get("role")) ? "Assistant" : "User";
            ctx.append(role).append(": ").append(msg.get("content")).append("\n");
        }

        String prompt = !ctx.isEmpty()
                ? "[Previous conversation]\n" + ctx + "\n[Latest message to classify]\n" + userMessage
                : userMessage;

        String raw = claude.chat(
                ROUTER_SYSTEM,
                List.of(Map.of("role", "user", "content", prompt)),
                Config.ROUTER_MAX_TOKENS
        );
        String cleaned = raw.trim().toUpperCase().replaceAll("[^A-Z]", "");
        System.out.println("[Orchestrator] Router raw: '" + raw.trim() + "' cleaned: '" + cleaned + "'");

        if (cleaned.startsWith("BILLING"))   return "BILLING";
        if (cleaned.startsWith("TECHNICAL")) return "TECHNICAL";
        return "OTHER";
    }
}
