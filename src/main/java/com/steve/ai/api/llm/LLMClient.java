package com.steve.ai.api.llm;

import com.steve.ai.llm.async.LLMResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Unified interface for LLM (Large Language Model) clients.
 *
 * <p>This interface abstracts different LLM providers (OpenAI, Groq, Gemini, etc.)
 * behind a common API, enabling pluggable provider support.</p>
 *
 * <p><b>Design Pattern:</b> Strategy Pattern for pluggable LLM providers</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * LLMClient client = container.getService(LLMClient.class);
 * Map&lt;String, Object&gt; params = Map.of("model", "gpt-4", "maxTokens", 1000);
 *
 * client.sendAsync("Generate a task plan", params)
 *     .thenAccept(response -> {
 *         System.out.println("Received: " + response.getContent());
 *     });
 * </pre>
 *
 * @since 2.0.0
 * @see LLMResponse
 */
public interface LLMClient {

    /**
     * Sends an asynchronous request to the LLM provider.
     *
     * <p>This method returns immediately with a CompletableFuture, allowing the calling
     * thread to continue without blocking.</p>
     *
     * @param prompt The text prompt to send to the LLM
     * @param params Additional parameters (model, maxTokens, temperature, etc.)
     * @return A CompletableFuture that will complete with the LLM response
     */
    CompletableFuture<LLMResponse> sendAsync(String prompt, Map<String, Object> params);

    /**
     * Returns the unique identifier for this LLM provider.
     *
     * @return Provider ID (e.g., "openai", "groq", "gemini", "qwen")
     */
    String getProviderId();

    /**
     * Checks if the client is healthy and able to accept requests.
     *
     * @return true if client is healthy (circuit breaker CLOSED or HALF_OPEN)
     */
    boolean isHealthy();

    /**
     * Returns the default model for this provider.
     *
     * @return Default model name
     */
    String getDefaultModel();

    /**
     * Returns the maximum tokens supported by this provider.
     *
     * @return Maximum tokens
     */
    int getMaxTokens();
}
