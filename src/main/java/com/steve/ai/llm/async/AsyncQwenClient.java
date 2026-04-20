package com.steve.ai.llm.async;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.steve.ai.config.SteveConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous Qwen (DashScope) API client using Java HttpClient's sendAsync().
 *
 * <p>Qwen is Alibaba Cloud's large language model series, providing high-quality
 * Chinese and English language understanding and generation capabilities.
 * Uses OpenAI-compatible API format for easy integration.</p>
 *
 * <p><b>API Endpoint:</b> https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions</p>
 *
 * <p><b>Performance:</b> 1-3 seconds typical latency for standard models</p>
 *
 * <p><b>Supported Models:</b></p>
 * <ul>
 *   <li>qwen-turbo (fastest, cost-effective)</li>
 *   <li>qwen-plus (balanced performance)</li>
 *   <li>qwen-max (best quality)</li>
 *   <li>qwen-coder-plus (optimized for code generation)</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Thread-safe. HttpClient is thread-safe and immutable.</p>
 *
 * @since 1.1.0
 */
public class AsyncQwenClient implements AsyncLLMClient {

    private static final Logger LOGGER = LogManager.getLogger(AsyncQwenClient.class);
    private static final String QWEN_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String PROVIDER_ID = "qwen";

    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    /**
     * Constructs an AsyncQwenClient.
     *
     * @param apiKey      Qwen API key (required)
     * @param model       Model to use (e.g., "qwen-turbo")
     * @param maxTokens   Maximum tokens in response (e.g., 500)
     * @param temperature Response randomness (0.0 - 2.0)
     * @throws IllegalArgumentException if apiKey is null or empty
     */
    public AsyncQwenClient(String apiKey, String model, int maxTokens, double temperature) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("Qwen API key cannot be null or empty");
        }

        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;

        // Build HttpClient - Qwen (DashScope) is a domestic Chinese API,
        // proxy is NOT needed by default. Only use proxy when explicitly enabled.
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10));

        boolean useProxy = SteveConfig.QWEN_USE_PROXY.get();

        if (useProxy) {
            // Proxy explicitly enabled for Qwen - use configured proxy
            String proxyHost = SteveConfig.PROXY_HOST.get();
            int proxyPort = SteveConfig.PROXY_PORT.get();

            if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
                clientBuilder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
                LOGGER.info("Using configured HTTP proxy for Qwen: {}:{}", proxyHost, proxyPort);
            } else {
                clientBuilder.proxy(ProxySelector.getDefault());
                LOGGER.debug("Qwen proxy enabled but no proxy configured, using system default");
            }
        } else {
            // No proxy - direct connection (default for domestic Chinese API)
            LOGGER.info("Qwen direct connection (proxy disabled, DashScope is a domestic API)");
        }

        this.httpClient = clientBuilder.build();

        LOGGER.info("AsyncQwenClient initialized (model: {}, maxTokens: {}, temperature: {}, proxy: {})",
            model, maxTokens, temperature, useProxy);
    }

    @Override
    public CompletableFuture<LLMResponse> sendAsync(String prompt, Map<String, Object> params) {
        long startTime = System.currentTimeMillis();

        String requestBody = buildRequestBody(prompt, params);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(QWEN_API_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(45))
            .build();

        LOGGER.debug("[qwen] Sending async request (prompt length: {} chars)", prompt.length());

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                long latencyMs = System.currentTimeMillis() - startTime;

                if (response.statusCode() != 200) {
                    LLMException.ErrorType errorType = determineErrorType(response.statusCode());
                    boolean retryable = response.statusCode() == 429 || response.statusCode() >= 500;

                    LOGGER.error("[qwen] API error: status={}, body={}", response.statusCode(),
                        truncate(response.body(), 200));

                    throw new LLMException(
                        "Qwen API error: HTTP " + response.statusCode(),
                        errorType,
                        PROVIDER_ID,
                        retryable
                    );
                }

                return parseResponse(response.body(), latencyMs);
            });
    }

    /**
     * Builds the JSON request body (OpenAI-compatible format).
     *
     * @param prompt User prompt
     * @param params Additional parameters
     * @return JSON string
     */
    private String buildRequestBody(String prompt, Map<String, Object> params) {
        JsonObject body = new JsonObject();

        String modelToUse = (String) params.getOrDefault("model", this.model);
        int maxTokensToUse = (int) params.getOrDefault("maxTokens", this.maxTokens);
        double tempToUse = (double) params.getOrDefault("temperature", this.temperature);

        body.addProperty("model", modelToUse);
        body.addProperty("max_tokens", maxTokensToUse);
        body.addProperty("temperature", tempToUse);

        JsonArray messages = new JsonArray();

        // System message
        String systemPrompt = (String) params.get("systemPrompt");
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", systemPrompt);
            messages.add(systemMessage);
        }

        // User message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        body.add("messages", messages);

        return body.toString();
    }

    /**
     * Parses Qwen API response (OpenAI-compatible format).
     *
     * @param responseBody Raw JSON response
     * @param latencyMs    Request latency
     * @return Parsed LLMResponse
     */
    private LLMResponse parseResponse(String responseBody, long latencyMs) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            if (!json.has("choices") || json.getAsJsonArray("choices").isEmpty()) {
                throw new LLMException(
                    "Qwen response missing 'choices' array",
                    LLMException.ErrorType.INVALID_RESPONSE,
                    PROVIDER_ID,
                    false
                );
            }

            JsonObject firstChoice = json.getAsJsonArray("choices").get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            String content = message.get("content").getAsString();

            int tokensUsed = 0;
            if (json.has("usage")) {
                JsonObject usage = json.getAsJsonObject("usage");
                tokensUsed = usage.get("total_tokens").getAsInt();
            }

            LOGGER.debug("[qwen] Response received (latency: {}ms, tokens: {})", latencyMs, tokensUsed);

            return LLMResponse.builder()
                .content(content)
                .model(model)
                .providerId(PROVIDER_ID)
                .latencyMs(latencyMs)
                .tokensUsed(tokensUsed)
                .fromCache(false)
                .build();

        } catch (LLMException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("[qwen] Failed to parse response: {}", truncate(responseBody, 200), e);
            throw new LLMException(
                "Failed to parse Qwen response: " + e.getMessage(),
                LLMException.ErrorType.INVALID_RESPONSE,
                PROVIDER_ID,
                false,
                e
            );
        }
    }

    private LLMException.ErrorType determineErrorType(int statusCode) {
        return switch (statusCode) {
            case 429 -> LLMException.ErrorType.RATE_LIMIT;
            case 401, 403 -> LLMException.ErrorType.AUTH_ERROR;
            case 400 -> LLMException.ErrorType.CLIENT_ERROR;
            case 408 -> LLMException.ErrorType.TIMEOUT;
            default -> {
                if (statusCode >= 500) {
                    yield LLMException.ErrorType.SERVER_ERROR;
                }
                yield LLMException.ErrorType.CLIENT_ERROR;
            }
        };
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "[null]";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

    @Override
    public String getDefaultModel() {
        return model;
    }

    @Override
    public int getMaxTokens() {
        return maxTokens;
    }
}
