package com.support.rag;

import com.support.model.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Loads markdown documentation files and retrieves the most relevant
 * sections using semantic / similarity search.
 *
 * Each chunk is embedded into a vector at load time using the Voyage AI
 * embeddings API. At query time the query is also embedded and cosine
 * similarity is computed against every chunk vector to find the best matches.
 *
 * This replaces the previous TF-IDF approach and understands meaning rather
 * than just matching exact words — so "car won't start" correctly matches
 * documentation about "battery failure" even though the words differ.
 */
public class DocumentStore {

    public record Chunk(String filename, String content, double[] embedding) {}

    private final List<Chunk> chunks = new ArrayList<>();
    private final EmbeddingClient embeddingClient;

    public DocumentStore(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }

    // -----------------------------------------------------------------------
    // Loading
    // -----------------------------------------------------------------------

    public void loadDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            System.err.println("[DocumentStore] WARNING: docs directory not found at: "
                    + dir.toAbsolutePath());
            return;
        }

        // Collect all chunk texts first so we can embed them in one batch call
        List<String> filenames = new ArrayList<>();
        List<String> contents  = new ArrayList<>();

        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".md"))
                  .sorted()
                  .forEach(p -> {
                      try {
                          collectChunks(p, filenames, contents);
                      } catch (IOException e) {
                          System.err.println("Warning: could not load " + p + ": " + e.getMessage());
                      }
                  });
        }

        if (contents.isEmpty()) {
            System.err.println("[DocumentStore] WARNING: No chunks loaded! "
                    + "Ensure the docs/ folder is in the directory you run the jar from.");
            return;
        }

        // Embed all chunks in one batch API call
        System.out.println("[DocumentStore] Embedding " + contents.size()
                + " chunks via Voyage AI...");
        List<double[]> embeddings = embeddingClient.embedBatch(contents);

        for (int i = 0; i < contents.size(); i++) {
            chunks.add(new Chunk(filenames.get(i), contents.get(i), embeddings.get(i)));
        }

        System.out.println("[DocumentStore] Loaded and embedded " + chunks.size()
                + " chunks from " + dir.toAbsolutePath());
    }

    private void collectChunks(Path file,
                                List<String> filenames,
                                List<String> contents) throws IOException {
        String[] lines = Files.readString(file).split("\n");
        String filename = file.getFileName().toString();
        System.out.println("[DocumentStore] Loading: " + filename + " (" + lines.length + " lines)");

        for (int i = 0; i < lines.length; i += Config.RAG_CHUNK_SIZE_LINES) {
            int end = Math.min(i + Config.RAG_CHUNK_SIZE_LINES, lines.length);
            String content = String.join("\n", Arrays.copyOfRange(lines, i, end)).trim();
            if (!content.isBlank()) {
                filenames.add(filename);
                contents.add(content);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Retrieval — semantic similarity search
    // -----------------------------------------------------------------------

    /**
     * Embeds the query and returns the top-K chunks by cosine similarity.
     */
    public List<Chunk> retrieve(String query) throws IOException {
        if (chunks.isEmpty()) return List.of();

        System.out.println("[DocumentStore] Embedding query for semantic search...");
        double[] queryEmbedding = embeddingClient.embed(query);

        // Score each chunk by cosine similarity with the query vector
        List<Map.Entry<Chunk, Double>> scored = new ArrayList<>();
        for (Chunk chunk : chunks) {
            double similarity = cosineSimilarity(queryEmbedding, chunk.embedding());
            scored.add(Map.entry(chunk, similarity));
        }

        // Log top scores for debugging
        scored.stream()
                .sorted(Map.Entry.<Chunk, Double>comparingByValue().reversed())
                .limit(6)
                .forEach(e -> System.out.printf(
                        "[DocumentStore] Score %.4f — %s%n", e.getValue(), e.getKey().filename()));

        // Pick top chunks but limit to 2 per file so results are always
        // diversified across documents rather than returning 4 chunks from
        // the same file when scores are close
        Map<String, Integer> perFileCount = new java.util.HashMap<>();
        List<Chunk> results = new ArrayList<>();

        for (Map.Entry<Chunk, Double> entry : scored.stream()
                .sorted(Map.Entry.<Chunk, Double>comparingByValue().reversed())
                .collect(Collectors.toList())) {
            if (results.size() >= Config.RAG_TOP_K) break;
            String filename = entry.getKey().filename();
            int count = perFileCount.getOrDefault(filename, 0);
            if (count < 1) {
                results.add(entry.getKey());
                perFileCount.put(filename, count + 1);
            }
        }

        return results;
    }

    // -----------------------------------------------------------------------
    // Cosine similarity
    // -----------------------------------------------------------------------

    /**
     * Computes cosine similarity between two vectors.
     * Returns a value between -1 (opposite) and 1 (identical meaning).
     * Values above ~0.7 are generally considered semantically similar.
     */
    private double cosineSimilarity(double[] a, double[] b) {
        double dotProduct  = 0;
        double normA       = 0;
        double normB       = 0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA      += a[i] * a[i];
            normB      += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) return 0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Returns all chunks belonging to a specific filename. */
    public List<Chunk> getChunksFromFile(String filename) {
        return chunks.stream()
                .filter(c -> c.filename().equals(filename))
                .collect(Collectors.toList());
    }
}
