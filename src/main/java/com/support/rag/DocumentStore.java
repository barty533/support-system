package com.support.rag;

import com.support.model.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Loads markdown documentation files and retrieves the most relevant
 * sections for a query using TF-IDF scoring.
 *
 * IDF scores are computed once when documents are loaded and cached,
 * avoiding repeated recalculation on every query.
 */
public class DocumentStore {

    public record Chunk(String filename, String content, List<String> tokens) {}

    private final List<Chunk> chunks = new ArrayList<>();

    // Cached IDF: term -> log((N / df) + 1)
    private final Map<String, Double> idfCache = new HashMap<>();

    // -----------------------------------------------------------------------
    // Loading
    // -----------------------------------------------------------------------

    public void loadDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            System.err.println("[DocumentStore] WARNING: docs directory not found at: "
                    + dir.toAbsolutePath());
            return;
        }

        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".md"))
                  .sorted()
                  .forEach(p -> {
                      try { loadFile(p); }
                      catch (IOException e) {
                          System.err.println("Warning: could not load " + p + ": " + e.getMessage());
                      }
                  });
        }

        buildIdfCache();

        System.out.println("[DocumentStore] Loaded " + chunks.size()
                + " chunks from " + dir.toAbsolutePath());

        if (chunks.isEmpty()) {
            System.err.println("[DocumentStore] WARNING: No chunks loaded! "
                    + "Ensure the docs/ folder is in the directory you run the jar from.");
        }
    }

    private void loadFile(Path file) throws IOException {
        String[] lines = Files.readString(file).split("\n");
        String filename = file.getFileName().toString();
        System.out.println("[DocumentStore] Loading: " + filename + " (" + lines.length + " lines)");

        for (int i = 0; i < lines.length; i += Config.RAG_CHUNK_SIZE_LINES) {
            int end = Math.min(i + Config.RAG_CHUNK_SIZE_LINES, lines.length);
            String content = String.join("\n", Arrays.copyOfRange(lines, i, end)).trim();
            if (!content.isBlank()) {
                chunks.add(new Chunk(filename, content, tokenise(content)));
            }
        }
    }

    /** Pre-computes IDF for every term that appears in the corpus. */
    private void buildIdfCache() {
        int N = chunks.size();
        if (N == 0) return;

        // Collect all unique terms across all chunks
        Set<String> allTerms = chunks.stream()
                .flatMap(c -> c.tokens().stream())
                .collect(Collectors.toSet());

        for (String term : allTerms) {
            long df = chunks.stream()
                    .filter(c -> c.tokens().contains(term))
                    .count();
            idfCache.put(term, Math.log((double) N / df + 1));
        }

        System.out.println("[DocumentStore] IDF cache built: " + idfCache.size() + " unique terms");
    }

    // -----------------------------------------------------------------------
    // Retrieval
    // -----------------------------------------------------------------------

    public List<Chunk> retrieve(String query) {
        List<String> queryTerms = tokenise(query);
        System.out.println("[DocumentStore] Query terms: " + queryTerms);

        if (queryTerms.isEmpty() || chunks.isEmpty()) return List.of();

        List<Map.Entry<Chunk, Double>> scored = new ArrayList<>();

        for (Chunk chunk : chunks) {
            double score = 0;
            for (String qt : queryTerms) {
                long tf = chunk.tokens().stream().filter(qt::equals).count();
                if (tf > 0) {
                    double idf = idfCache.getOrDefault(qt, 0.0);
                    score += tf * idf;
                }
            }
            if (score > 0) scored.add(Map.entry(chunk, score));
        }

        return scored.stream()
                .sorted(Map.Entry.<Chunk, Double>comparingByValue().reversed())
                .limit(Config.RAG_TOP_K)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Tokenisation
    // -----------------------------------------------------------------------

    private List<String> tokenise(String text) {
        return Arrays.stream(text.toLowerCase()
                        .replaceAll("[^a-z0-9\\s]", " ")
                        .split("\\s+"))
                .filter(t -> t.length() >= 2)
                .collect(Collectors.toList());
    }

    /** Returns all chunks belonging to a specific filename. */
    public List<Chunk> getChunksFromFile(String filename) {
        return chunks.stream()
                .filter(c -> c.filename().equals(filename))
                .collect(Collectors.toList());
    }

}
