package com.steve.ai.llm;

import com.steve.ai.SteveMod;
import com.steve.ai.action.Task;
import com.steve.ai.config.SteveConfig;
import com.steve.ai.entity.SteveEntity;
import com.steve.ai.llm.async.*;
import com.steve.ai.llm.resilience.LLMFallbackHandler;
import com.steve.ai.llm.resilience.ResilientLLMClient;
import com.steve.ai.memory.WorldKnowledge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Plans tasks for Steve using async LLM clients with resilience patterns.
 *
 * <p>Supports multiple LLM providers (OpenAI, Groq, Gemini, Qwen) with
 * automatic fallback, circuit breaking, retry, and caching.</p>
 */
public class TaskPlanner {

    private final AsyncLLMClient asyncOpenAIClient;
    private final AsyncLLMClient asyncGroqClient;
    private final AsyncLLMClient asyncGeminiClient;
    private final AsyncLLMClient asyncQwenClient;
    private final LLMCache llmCache;
    private final LLMFallbackHandler fallbackHandler;

    public TaskPlanner() {
        // Initialize async infrastructure
        this.llmCache = new LLMCache();
        this.fallbackHandler = new LLMFallbackHandler();

        // Initialize async clients with resilience wrappers - only if API key is configured
        AsyncLLMClient baseOpenAI = createAsyncClientSafely("OpenAI", () -> new AsyncOpenAIClient(
            SteveConfig.OPENAI_API_KEY.get(),
            SteveConfig.OPENAI_MODEL.get(),
            SteveConfig.OPENAI_MAX_TOKENS.get(),
            SteveConfig.OPENAI_TEMPERATURE.get()));

        AsyncLLMClient baseGroq = createAsyncClientSafely("Groq", () -> new AsyncGroqClient(
            SteveConfig.GROQ_API_KEY.get(),
            SteveConfig.GROQ_MODEL.get(),
            SteveConfig.GROQ_MAX_TOKENS.get(),
            SteveConfig.GROQ_TEMPERATURE.get()));

        AsyncLLMClient baseGemini = createAsyncClientSafely("Gemini", () -> new AsyncGeminiClient(
            SteveConfig.GEMINI_API_KEY.get(),
            SteveConfig.GEMINI_MODEL.get(),
            SteveConfig.GEMINI_MAX_TOKENS.get(),
            SteveConfig.GEMINI_TEMPERATURE.get()));

        AsyncLLMClient baseQwen = createAsyncClientSafely("Qwen", () -> new AsyncQwenClient(
            SteveConfig.QWEN_API_KEY.get(),
            SteveConfig.QWEN_MODEL.get(),
            SteveConfig.QWEN_MAX_TOKENS.get(),
            SteveConfig.QWEN_TEMPERATURE.get()));

        // Wrap with resilience patterns (only if base client exists)
        this.asyncOpenAIClient = baseOpenAI != null ? new ResilientLLMClient(baseOpenAI, llmCache, fallbackHandler) : null;
        this.asyncGroqClient = baseGroq != null ? new ResilientLLMClient(baseGroq, llmCache, fallbackHandler) : null;
        this.asyncGeminiClient = baseGemini != null ? new ResilientLLMClient(baseGemini, llmCache, fallbackHandler) : null;
        this.asyncQwenClient = baseQwen != null ? new ResilientLLMClient(baseQwen, llmCache, fallbackHandler) : null;

        SteveMod.LOGGER.info("TaskPlanner initialized with async resilient clients (OpenAI: {}, Groq: {}, Gemini: {}, Qwen: {})",
            this.asyncOpenAIClient != null ? "enabled" : "disabled",
            this.asyncGroqClient != null ? "enabled" : "disabled",
            this.asyncGeminiClient != null ? "enabled" : "disabled",
            this.asyncQwenClient != null ? "enabled" : "disabled");
    }

    @FunctionalInterface
    private interface ClientFactory<T> {
        T create() throws Exception;
    }

    private AsyncLLMClient createAsyncClientSafely(String name, ClientFactory<AsyncLLMClient> factory) {
        try {
            return factory.create();
        } catch (Exception e) {
            SteveMod.LOGGER.warn("Failed to initialize {} async client: {}", name, e.getMessage());
            return null;
        }
    }

    /**
     * Asynchronously plans tasks for Steve using the configured LLM provider.
     *
     * <p>This method returns immediately with a CompletableFuture, allowing the game thread
     * to continue without blocking. The actual LLM call is executed on a separate thread pool
     * with full resilience patterns (circuit breaker, retry, rate limiting, caching).</p>
     *
     * <p><b>Non-blocking:</b> Game thread is never blocked</p>
     * <p><b>Resilient:</b> Automatic retry, circuit breaker, fallback on failure</p>
     * <p><b>Cached:</b> Repeated prompts may hit cache (40-60% hit rate)</p>
     *
     * @param steve   The Steve entity making the request
     * @param command The user command to plan
     * @return CompletableFuture that completes with the parsed response, or null on failure
     */
    public CompletableFuture<ResponseParser.ParsedResponse> planTasksAsync(SteveEntity steve, String command) {
        try {
            String systemPrompt = PromptBuilder.buildSystemPrompt();
            WorldKnowledge worldKnowledge = new WorldKnowledge(steve);
            String userPrompt = PromptBuilder.buildUserPrompt(steve, command, worldKnowledge);

            String provider = SteveConfig.AI_PROVIDER.get().toLowerCase();
            SteveMod.LOGGER.info("[Async] Requesting AI plan for Steve '{}' using {}: {}",
                steve.getSteveName(), provider, command);

            // 根据 provider 选择对应配置
            Map<String, Object> params = switch (provider) {
                case "gemini" -> Map.of(
                    "systemPrompt", systemPrompt,
                    "model", SteveConfig.GEMINI_MODEL.get(),
                    "maxTokens", SteveConfig.GEMINI_MAX_TOKENS.get(),
                    "temperature", SteveConfig.GEMINI_TEMPERATURE.get()
                );
                case "groq" -> Map.of(
                    "systemPrompt", systemPrompt,
                    "model", SteveConfig.GROQ_MODEL.get(),
                    "maxTokens", SteveConfig.GROQ_MAX_TOKENS.get(),
                    "temperature", SteveConfig.GROQ_TEMPERATURE.get()
                );
                case "qwen" -> Map.of(
                    "systemPrompt", systemPrompt,
                    "model", SteveConfig.QWEN_MODEL.get(),
                    "maxTokens", SteveConfig.QWEN_MAX_TOKENS.get(),
                    "temperature", SteveConfig.QWEN_TEMPERATURE.get()
                );
                default -> Map.of(
                    "systemPrompt", systemPrompt,
                    "model", SteveConfig.OPENAI_MODEL.get(),
                    "maxTokens", SteveConfig.OPENAI_MAX_TOKENS.get(),
                    "temperature", SteveConfig.OPENAI_TEMPERATURE.get()
                );
            };

            // Select async client based on provider
            AsyncLLMClient client = getAsyncClient(provider);

            // Execute async request
            return client.sendAsync(userPrompt, params)
                .thenApply(response -> {
                    String content = response.getContent();
                    if (content == null || content.isEmpty()) {
                        SteveMod.LOGGER.error("[Async] Empty response from LLM");
                        return null;
                    }

                    ResponseParser.ParsedResponse parsed = ResponseParser.parseAIResponse(content);
                    if (parsed == null) {
                        SteveMod.LOGGER.error("[Async] Failed to parse AI response");
                        return null;
                    }

                    SteveMod.LOGGER.info("[Async] Plan received: {} ({} tasks, {}ms, {} tokens, cache: {})",
                        parsed.getPlan(),
                        parsed.getTasks().size(),
                        response.getLatencyMs(),
                        response.getTokensUsed(),
                        response.isFromCache());

                    return parsed;
                })
                .exceptionally(throwable -> {
                    SteveMod.LOGGER.error("[Async] Error planning tasks: {}", throwable.getMessage());
                    return null;
                });

        } catch (Exception e) {
            SteveMod.LOGGER.error("[Async] Error setting up task planning", e);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Returns the appropriate async client based on provider config.
     * Falls back to available clients if the requested one is not initialized.
     *
     * @param provider Provider name ("openai", "groq", "gemini", "qwen")
     * @return Resilient async client
     * @throws IllegalStateException if no LLM client is available
     */
    private AsyncLLMClient getAsyncClient(String provider) {
        AsyncLLMClient client = switch (provider) {
            case "openai" -> asyncOpenAIClient;
            case "gemini" -> asyncGeminiClient;
            case "groq" -> asyncGroqClient;
            case "qwen" -> asyncQwenClient;
            default -> {
                SteveMod.LOGGER.warn("[Async] Unknown provider '{}', trying fallback", provider);
                yield null;
            }
        };

        // If requested client is not available, try fallback to any available client
        if (client == null) {
            SteveMod.LOGGER.warn("[Async] Provider '{}' is not available, trying fallback", provider);

            // Try fallback in order: Groq -> Gemini -> OpenAI -> Qwen
            if (asyncGroqClient != null) {
                SteveMod.LOGGER.info("[Async] Falling back to Groq client");
                return asyncGroqClient;
            }
            if (asyncGeminiClient != null) {
                SteveMod.LOGGER.info("[Async] Falling back to Gemini client");
                return asyncGeminiClient;
            }
            if (asyncOpenAIClient != null) {
                SteveMod.LOGGER.info("[Async] Falling back to OpenAI client");
                return asyncOpenAIClient;
            }
            if (asyncQwenClient != null) {
                SteveMod.LOGGER.info("[Async] Falling back to Qwen client");
                return asyncQwenClient;
            }

            // No client available
            throw new IllegalStateException("No LLM client is available. Please configure at least one API key (OpenAI, Groq, Gemini, or Qwen) in steve-common.toml");
        }

        return client;
    }

    /**
     * Returns the LLM cache for monitoring.
     *
     * @return LLM cache instance
     */
    public LLMCache getLLMCache() {
        return llmCache;
    }

    /**
     * Checks if the specified provider's async client is healthy.
     *
     * @param provider Provider name
     * @return true if healthy (circuit breaker not OPEN)
     */
    public boolean isProviderHealthy(String provider) {
        return getAsyncClient(provider).isHealthy();
    }

    public boolean validateTask(Task task) {
        String action = task.getAction();
        
        return switch (action) {
            case "pathfind" -> task.hasParameters("x", "y", "z");
            case "mine" -> task.hasParameters("block", "quantity");
            case "place" -> task.hasParameters("block", "x", "y", "z");
            case "craft" -> task.hasParameters("item", "quantity");
            case "attack" -> task.hasParameters("target");
            case "follow" -> task.hasParameters("player");
            case "gather" -> task.hasParameters("resource", "quantity");
            case "build" -> task.hasParameters("structure", "blocks", "dimensions");
            default -> {
                SteveMod.LOGGER.warn("Unknown action type: {}", action);
                yield false;
            }
        };
    }

    public List<Task> validateAndFilterTasks(List<Task> tasks) {
        return tasks.stream()
            .filter(this::validateTask)
            .toList();
    }
}

