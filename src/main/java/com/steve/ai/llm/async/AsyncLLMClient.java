package com.steve.ai.llm.async;

import com.steve.ai.api.llm.LLMClient;

/**
 * Asynchronous interface for LLM (Large Language Model) clients.
 * Provides non-blocking API calls using CompletableFuture to prevent game thread blocking.
 *
 * <p>This interface extends the base LLMClient interface and is implemented by
 * provider-specific clients (OpenAI, Groq, Gemini) and wrapped by ResilientLLMClient
 * for fault tolerance patterns.</p>
 *
 * <p><b>Design Pattern:</b> Strategy pattern for pluggable LLM providers</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * AsyncLLMClient client = new AsyncOpenAIClient(apiKey, model, maxTokens, temperature);
 * Map&lt;String, Object&gt; params = Map.of("model", "gpt-3.5-turbo", "maxTokens", 1000);
 *
 * client.sendAsync("Generate a task plan", params)
 *     .thenAccept(response -> {
 *         System.out.println("Received: " + response.getContent());
 *     })
 *     .exceptionally(throwable -> {
 *         System.err.println("LLM call failed: " + throwable.getMessage());
 *         return null;
 *     });
 * </pre>
 *
 * @see LLMResponse
 * @see LLMException
 * @see LLMClient
 * @since 1.1.0
 */
public interface AsyncLLMClient extends LLMClient {
    // All methods inherited from LLMClient
}
