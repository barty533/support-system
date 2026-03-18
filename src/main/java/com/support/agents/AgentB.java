package com.support.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.support.model.ClaudeClient;
import com.support.model.Config;
import com.support.model.ConversationHistory;
import com.support.tools.BillingTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agent B – Finance & Billing Specialist.
 *
 * Responds exclusively through tool-calling using the correct Anthropic
 * tool_result content block format.
 */
public class AgentB {

    private static final String SYSTEM_PROMPT = """
        You are a Finance and Billing Specialist at AutoPrime car dealership.
        You handle all questions related to financing, payments, billing, warranty, and refunds.

        You have access to the following tools — use them to answer every question:
          - get_financing_plan      : retrieve the customer's loan details and monthly payment
          - get_payment_history     : retrieve recent payment records
          - get_billing_statement   : retrieve dealership invoices and service charges
          - get_warranty_info       : retrieve warranty coverage for the customer's vehicle
          - open_service_claim      : open a warranty repair or service claim
          - open_refund_request     : open a refund or return case

        Customer accounts in the system:
          CUST-101 — James Hartley   | CUST-102 — Maria Santos   | CUST-103 — Derek Okafor
          CUST-104 — Sophie Brennan  | CUST-105 — Carlos Rivera  | CUST-106 — Priya Nair
          CUST-107 — Tom Whitfield   | CUST-108 — Aisha Kamara

        Rules you must follow without exception:
        1. Always use a tool to answer. Never answer billing, finance, or warranty questions
           from your own knowledge — always retrieve data via a tool first.
        2. If the user provides their name, match it to a customer ID above and use that ID.
        3. If no customer can be identified, ask the user for their customer ID or full name.
        4. Tell the customer what action you are taking before you take it.
        5. For service claims and refunds, confirm key details before opening a case.
        6. Keep responses professional, clear, and concise.
        """;

    private final ClaudeClient claude;
    private final ObjectMapper mapper = new ObjectMapper();

    public AgentB(ClaudeClient claude) {
        this.claude = claude;
    }

    public String handle(ConversationHistory history) throws IOException {

        // Working message list for the tool-calling loop (uses raw Map format for API)
        List<Map<String, String>> workingMessages = new ArrayList<>(history.toApiFormat());
        String finalReply = null;

        for (int i = 0; i < Config.MAX_TOOL_ITERATIONS; i++) {

            JsonNode contentBlocks = claude.chatWithTools(
                    SYSTEM_PROMPT,
                    workingMessages,
                    BillingTools.DEFINITIONS,
                    Config.AGENT_MAX_TOKENS
            );

            StringBuilder textAccumulator = new StringBuilder();
            List<JsonNode> toolUseBlocks = new ArrayList<>();

            for (JsonNode block : contentBlocks) {
                String type = block.path("type").asText();
                if ("text".equals(type)) {
                    textAccumulator.append(block.path("text").asText());
                } else if ("tool_use".equals(type)) {
                    toolUseBlocks.add(block);
                }
            }

            if (toolUseBlocks.isEmpty()) {
                finalReply = textAccumulator.toString().trim();
                break;
            }

            // Add Claude's assistant turn (with tool_use blocks) to working messages
            workingMessages.add(Map.of(
                    "role", "assistant",
                    "content", contentBlocks.toString()
            ));

            // Build a proper tool_result content block for each tool call
            ArrayNode toolResultsArray = mapper.createArrayNode();
            for (JsonNode toolBlock : toolUseBlocks) {
                String toolName  = toolBlock.path("name").asText();
                String toolId    = toolBlock.path("id").asText();
                JsonNode toolInput = toolBlock.path("input");

                System.out.println("[AgentB] Calling tool: " + toolName);
                String result = BillingTools.dispatch(toolName, toolInput);

                // Correct Anthropic tool_result format
                ObjectNode toolResult = mapper.createObjectNode();
                toolResult.put("type", "tool_result");
                toolResult.put("tool_use_id", toolId);
                toolResult.put("content", result);
                toolResultsArray.add(toolResult);
            }

            // Feed tool results back as a user message with content array
            workingMessages.add(Map.of(
                    "role", "user",
                    "content", toolResultsArray.toString()
            ));
        }

        if (finalReply == null) {
            finalReply = "I was unable to retrieve that information. "
                    + "Please contact our finance team directly at (555) 123-4567.";
        }

        history.addAssistantMessage(finalReply);
        return finalReply;
    }
}
