package com.steve.ai.module;

import com.steve.ai.config.SteveConfig;
import com.steve.ai.di.ServiceContainer;
import com.steve.ai.llm.LLMClientRegistry;
import com.steve.ai.llm.async.*;
import com.steve.ai.llm.resilience.LLMFallbackHandler;
import com.steve.ai.llm.resilience.ResilientLLMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM (Large Language Model) module for Steve AI.
 *
 * <p>This module manages LLM client integrations, providing a unified
 * interface for multiple providers (OpenAI, Groq, Gemini, Qwen).</p>
 *
 * @since 2.0.0
 * @see SteveModule
 * @see LLMClientRegistry
 */
public class LLMModule implements SteveModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(LLMModule.class);

    private LLMClientRegistry clientRegistry;
    private LLMCache llmCache;
    private LLMFallbackHandler fallbackHandler;

    @Override
    public String getModuleId() {
        return "llm";
    }

    @Override
    public int getPriority() {
        return 800; // High priority - core service
    }

    @Override
    public void onInit(ServiceContainer container) {
        LOGGER.info("Initializing LLM Module");

        // Initialize supporting services
        this.llmCache = new LLMCache();
        this.fallbackHandler = new LLMFallbackHandler();
        this.clientRegistry = new LLMClientRegistry();

        // Register clients based on configuration
        initializeClients();

        // Register services with container
        container.register(LLMClientRegistry.class, clientRegistry);
        container.register(LLMCache.class, llmCache);

        LOGGER.info("LLM Module initialized with {} clients", clientRegistry.getClientCount());
    }

    @Override
    public void onStart() {
        LOGGER.info("Starting LLM Module");

        // Set default provider from config
        String defaultProvider = SteveConfig.AI_PROVIDER.get().toLowerCase();
        if (clientRegistry.hasClient(defaultProvider)) {
            clientRegistry.setDefaultProvider(defaultProvider);
            LOGGER.info("Set default LLM provider to: {}", defaultProvider);
        } else {
            LOGGER.warn("Configured provider '{}' not available, using fallback", defaultProvider);
        }

        // Log health status
        LOGGER.info("LLM client health report: {}", clientRegistry.getHealthReport());
    }

    @Override
    public void onStop() {
        LOGGER.info("Stopping LLM Module");

        // Clear cache
        if (llmCache != null) {
            llmCache.clear();
        }

        // Clear registry
        if (clientRegistry != null) {
            clientRegistry.clear();
        }

        LOGGER.info("LLM Module stopped");
    }

    @Override
    public void onReload() {
        LOGGER.info("Reloading LLM Module configuration");

        // Update default provider
        String newProvider = SteveConfig.AI_PROVIDER.get().toLowerCase();
        if (clientRegistry.hasClient(newProvider)) {
            clientRegistry.setDefaultProvider(newProvider);
        }

        LOGGER.info("LLM Module reloaded");
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public String getDescription() {
        return "LLM integration module supporting OpenAI, Groq, Gemini, and Qwen providers";
    }

    /**
     * Initializes LLM clients based on configuration.
     */
    private void initializeClients() {
        List<String> initializedProviders = new ArrayList<>();

        // Try to initialize each provider
        initializeOpenAI(initializedProviders);
        initializeGroq(initializedProviders);
        initializeGemini(initializedProviders);
        initializeQwen(initializedProviders);

        LOGGER.info("Initialized LLM providers: {}", initializedProviders);

        // Set priority order (Groq first for speed, then others)
        List<String> priorityOrder = new ArrayList<>();
        if (initializedProviders.contains("groq")) {
            priorityOrder.add("groq");
        }
        if (initializedProviders.contains("gemini")) {
            priorityOrder.add("gemini");
        }
        if (initializedProviders.contains("openai")) {
            priorityOrder.add("openai");
        }
        if (initializedProviders.contains("qwen")) {
            priorityOrder.add("qwen");
        }
        clientRegistry.setPriorityOrder(priorityOrder);
    }

    private void initializeOpenAI(List<String> initialized) {
        try {
            String apiKey = SteveConfig.OPENAI_API_KEY.get();
            if (apiKey == null || apiKey.isBlank()) {
                LOGGER.debug("OpenAI API key not configured, skipping");
                return;
            }

            AsyncLLMClient baseClient = new AsyncOpenAIClient(
                apiKey,
                SteveConfig.OPENAI_MODEL.get(),
                SteveConfig.OPENAI_MAX_TOKENS.get(),
                SteveConfig.OPENAI_TEMPERATURE.get()
            );

            ResilientLLMClient resilientClient = new ResilientLLMClient(
                baseClient, llmCache, fallbackHandler);

            clientRegistry.registerClient(resilientClient);
            initialized.add("openai");

        } catch (Exception e) {
            LOGGER.warn("Failed to initialize OpenAI client: {}", e.getMessage());
        }
    }

    private void initializeGroq(List<String> initialized) {
        try {
            String apiKey = SteveConfig.GROQ_API_KEY.get();
            if (apiKey == null || apiKey.isBlank()) {
                LOGGER.debug("Groq API key not configured, skipping");
                return;
            }

            AsyncLLMClient baseClient = new AsyncGroqClient(
                apiKey,
                SteveConfig.GROQ_MODEL.get(),
                SteveConfig.GROQ_MAX_TOKENS.get(),
                SteveConfig.GROQ_TEMPERATURE.get()
            );

            ResilientLLMClient resilientClient = new ResilientLLMClient(
                baseClient, llmCache, fallbackHandler);

            clientRegistry.registerClient(resilientClient);
            initialized.add("groq");

        } catch (Exception e) {
            LOGGER.warn("Failed to initialize Groq client: {}", e.getMessage());
        }
    }

    private void initializeGemini(List<String> initialized) {
        try {
            String apiKey = SteveConfig.GEMINI_API_KEY.get();
            if (apiKey == null || apiKey.isBlank()) {
                LOGGER.debug("Gemini API key not configured, skipping");
                return;
            }

            AsyncLLMClient baseClient = new AsyncGeminiClient(
                apiKey,
                SteveConfig.GEMINI_MODEL.get(),
                SteveConfig.GEMINI_MAX_TOKENS.get(),
                SteveConfig.GEMINI_TEMPERATURE.get()
            );

            ResilientLLMClient resilientClient = new ResilientLLMClient(
                baseClient, llmCache, fallbackHandler);

            clientRegistry.registerClient(resilientClient);
            initialized.add("gemini");

        } catch (Exception e) {
            LOGGER.warn("Failed to initialize Gemini client: {}", e.getMessage());
        }
    }

    private void initializeQwen(List<String> initialized) {
        try {
            String apiKey = SteveConfig.QWEN_API_KEY.get();
            if (apiKey == null || apiKey.isBlank()) {
                LOGGER.debug("Qwen API key not configured, skipping");
                return;
            }

            AsyncLLMClient baseClient = new AsyncQwenClient(
                apiKey,
                SteveConfig.QWEN_MODEL.get(),
                SteveConfig.QWEN_MAX_TOKENS.get(),
                SteveConfig.QWEN_TEMPERATURE.get()
            );

            ResilientLLMClient resilientClient = new ResilientLLMClient(
                baseClient, llmCache, fallbackHandler);

            clientRegistry.registerClient(resilientClient);
            initialized.add("qwen");

        } catch (Exception e) {
            LOGGER.warn("Failed to initialize Qwen client: {}", e.getMessage());
        }
    }
}
