package com.support.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Calls the Voyage AI embeddings API to convert text into numerical vectors.
 * Voyage AI is Anthropic's official embedding partner.
 *
 * Free tier available at https://dash.voyageai.com — no credit card required.
 * API docs: https://docs.voyageai.com/reference/embeddings-api
 */
public class EmbeddingClient {

    private static final String API_URL = "https://api.voyageai.com/v1/embeddings";
    private static final String MODEL   = "voyage-3";
    private static final MediaType JSON_TYPE = MediaType.get("application/json");

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public EmbeddingClient(String apiKey) {
        this.apiKey = apiKey;
    }

    /** Converts a single text string into an embedding vector. */
    public double[] embed(String text) throws IOException {
        return embedBatch(List.of(text)).get(0);
    }

    /**
     * Converts a batch of texts into embedding vectors in one API call.
     * Processes in batches of 10 to stay within request size limits.
     */
    public List<double[]> embedBatch(List<String> texts) throws IOException {
        List<double[]> allEmbeddings = new ArrayList<>();
        int batchSize = 10;

        for (int i = 0; i < texts.size(); i += batchSize) {
            List<String> batch = texts.subList(i, Math.min(i + batchSize, texts.size()));
            allEmbeddings.addAll(embedBatchInternal(batch));
        }

        return allEmbeddings;
    }

    private List<double[]> embedBatchInternal(List<String> texts) throws IOException {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", MODEL);

        ArrayNode inputArray = mapper.createArrayNode();
        texts.forEach(inputArray::add);
        body.set("input", inputArray);

        RequestBody requestBody = RequestBody.create(
                mapper.writeValueAsString(body), JSON_TYPE);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("content-type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = http.newCall(request).execute()) {
            ResponseBody responseBodyObj = response.body();
            if (responseBodyObj == null) throw new IOException("Empty response from Voyage AI");
            String responseBody = responseBodyObj.string();

            if (!response.isSuccessful()) {
                throw new IOException("Voyage AI error " + response.code()
                        + ": " + responseBody);
            }

            JsonNode json       = mapper.readTree(responseBody);
            JsonNode data       = json.get("data");
            List<double[]> embeddings = new ArrayList<>();

            for (JsonNode item : data) {
                JsonNode embeddingArray = item.get("embedding");
                double[] vector = new double[embeddingArray.size()];
                for (int i = 0; i < embeddingArray.size(); i++) {
                    vector[i] = embeddingArray.get(i).asDouble();
                }
                embeddings.add(vector);
            }
            return embeddings;
        }
    }
}
