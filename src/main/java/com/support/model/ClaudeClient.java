package com.support.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * HTTP wrapper for the Anthropic /v1/messages endpoint.
 * Includes exponential backoff retry on 429 and 5xx errors.
 */
public class ClaudeClient {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final MediaType JSON_TYPE = MediaType.get("application/json");

    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public ClaudeClient(String apiKey) {
        this.apiKey = apiKey;
    }

    /** Basic call: system prompt + messages, no tools. Returns the first text block. */
    public String chat(String systemPrompt,
                       List<Map<String, String>> messages,
                       int maxTokens) throws IOException {
        ObjectNode body = buildBaseBody(systemPrompt, messages, maxTokens);
        JsonNode response = postWithRetry(body);
        return extractText(response);
    }

    /** Tool-calling variant. Returns the full content array for the caller to inspect. */
    public JsonNode chatWithTools(String systemPrompt,
                                  List<Map<String, String>> messages,
                                  List<Map<String, Object>> tools,
                                  int maxTokens) throws IOException {
        ObjectNode body = buildBaseBody(systemPrompt, messages, maxTokens);

        ArrayNode toolsArray = mapper.createArrayNode();
        for (Map<String, Object> tool : tools) {
            toolsArray.add(mapper.valueToTree(tool));
        }
        body.set("tools", toolsArray);

        JsonNode response = postWithRetry(body);
        return response.get("content");
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private ObjectNode buildBaseBody(String systemPrompt,
                                     List<Map<String, String>> messages,
                                     int maxTokens) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", Config.MODEL);
        body.put("max_tokens", maxTokens);
        body.put("system", systemPrompt);

        ArrayNode msgs = mapper.createArrayNode();
        for (Map<String, String> m : messages) {
            ObjectNode node = mapper.createObjectNode();
            node.put("role", m.get("role"));
            node.put("content", m.get("content"));
            msgs.add(node);
        }
        body.set("messages", msgs);
        return body;
    }

    /** Posts with exponential backoff retry on 429 and 5xx responses. */
    private JsonNode postWithRetry(ObjectNode body) throws IOException {
        IOException lastException = null;

        for (int attempt = 0; attempt <= Config.HTTP_MAX_RETRIES; attempt++) {
            if (attempt > 0) {
                long delay = Config.HTTP_RETRY_BASE_DELAY_MS * (1L << (attempt - 1));
                System.out.printf("[ClaudeClient] Retrying in %dms (attempt %d/%d)%n",
                        delay, attempt, Config.HTTP_MAX_RETRIES);
                try { Thread.sleep(delay); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry wait", ie);
                }
            }

            RequestBody requestBody = RequestBody.create(
                    mapper.writeValueAsString(body), JSON_TYPE);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("content-type", "application/json")
                    .post(requestBody)
                    .build();

            try (Response response = http.newCall(request).execute()) {
                ResponseBody responseBodyObj = response.body();
                if (responseBodyObj == null) throw new IOException("Empty response body from API");
                String responseBody = responseBodyObj.string();

                if (response.isSuccessful()) {
                    return mapper.readTree(responseBody);
                }

                // Retry on rate limit or server error
                if (response.code() == 429 || response.code() >= 500) {
                    lastException = new IOException(
                            "API error " + response.code() + ": " + responseBody);
                    continue;
                }

                // Non-retryable error (4xx)
                throw new IOException("API error " + response.code() + ": " + responseBody);
            }
        }

        throw new IOException("API call failed after " + Config.HTTP_MAX_RETRIES
                + " retries", lastException);
    }

    private String extractText(JsonNode response) {
        JsonNode content = response.get("content");
        if (content != null && content.isArray()) {
            for (JsonNode block : content) {
                if ("text".equals(block.path("type").asText())) {
                    return block.path("text").asText();
                }
            }
        }
        return "";
    }
}
