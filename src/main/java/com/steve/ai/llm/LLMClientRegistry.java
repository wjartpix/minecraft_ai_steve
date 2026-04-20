package com.steve.ai.llm;

import com.steve.ai.api.llm.LLMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Registry for managing LLM client implementations.
 *
 * <p>This registry decouples the TaskPlanner from specific LLM provider
 * implementations, enabling pluggable provider support.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * LLMClientRegistry registry = new LLMClientRegistry();
 *
 * // Register clients
 * registry.registerClient(new AsyncOpenAIClient(...));
 * registry.registerClient(new AsyncGroqClient(...));
 *
 * // Get client by provider ID
 * LLMClient openai = registry.getClient("openai");
 *
 * // Get any healthy client (fallback)
 * LLMClient client = registry.getHealthyClient();
 * </pre>
 *
 * @since 2.0.0
 * @see LLMClient
 * @see TaskPlanner
 */
public class LLMClientRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(LLMClientRegistry.class);

    private final Map<String, LLMClient> clients;
    private final List<String> priorityOrder;
    private String defaultProvider;

    public LLMClientRegistry() {
        this.clients = new ConcurrentHashMap<>();
        this.priorityOrder = new CopyOnWriteArrayList<>();
        this.defaultProvider = "groq"; // Default fallback
    }

    /**
     * Registers an LLM client.
     *
     * @param client The client to register
     * @throws IllegalArgumentException if a client for the same provider is already registered
     */
    public void registerClient(LLMClient client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }

        String providerId = client.getProviderId().toLowerCase();

        if (clients.containsKey(providerId)) {
            LOGGER.warn("Replacing existing client for provider: {}", providerId);
        }

        clients.put(providerId, client);
        if (!priorityOrder.contains(providerId)) {
            priorityOrder.add(providerId);
        }

        LOGGER.info("Registered LLM client: {} (model: {}, maxTokens: {})",
            providerId, client.getDefaultModel(), client.getMaxTokens());
    }

    /**
     * Unregisters an LLM client.
     *
     * @param providerId The provider ID
     * @return true if a client was removed
     */
    public boolean unregisterClient(String providerId) {
        if (providerId == null) {
            return false;
        }

        String normalizedId = providerId.toLowerCase();
        LLMClient removed = clients.remove(normalizedId);
        priorityOrder.remove(normalizedId);

        if (removed != null) {
            LOGGER.info("Unregistered LLM client: {}", normalizedId);
            return true;
        }
        return false;
    }

    /**
     * Gets a client by provider ID.
     *
     * @param providerId The provider ID (e.g., "openai", "groq", "gemini")
     * @return The client, or null if not found
     */
    public LLMClient getClient(String providerId) {
        if (providerId == null) {
            return null;
        }
        return clients.get(providerId.toLowerCase());
    }

    /**
     * Gets a client by provider ID, falling back to the default if not found.
     *
     * @param providerId The preferred provider ID
     * @return The client, or the default client if not found
     * @throws IllegalStateException if no clients are available
     */
    public LLMClient getClientOrDefault(String providerId) {
        LLMClient client = getClient(providerId);
        if (client != null) {
            return client;
        }

        LOGGER.warn("Provider '{}' not found, falling back to default: {}",
            providerId, defaultProvider);
        return getDefaultClient();
    }

    /**
     * Gets the default client.
     *
     * @return The default client
     * @throws IllegalStateException if no clients are available
     */
    public LLMClient getDefaultClient() {
        LLMClient client = clients.get(defaultProvider);
        if (client != null) {
            return client;
        }

        // Try any available client
        return getHealthyClient()
            .orElseThrow(() -> new IllegalStateException(
                "No LLM clients available. Please configure at least one provider."));
    }

    /**
     * Finds a healthy client, trying providers in priority order.
     *
     * @return Optional containing a healthy client
     */
    public Optional<LLMClient> getHealthyClient() {
        // Try in priority order
        for (String providerId : priorityOrder) {
            LLMClient client = clients.get(providerId);
            if (client != null && client.isHealthy()) {
                return Optional.of(client);
            }
        }

        // Try any healthy client
        return clients.values().stream()
            .filter(LLMClient::isHealthy)
            .findFirst();
    }

    /**
     * Gets a healthy client for a specific provider, with fallback.
     *
     * @param preferredProvider The preferred provider ID
     * @return A healthy client
     * @throws IllegalStateException if no healthy clients are available
     */
    public LLMClient getHealthyClient(String preferredProvider) {
        // Try preferred provider first
        LLMClient preferred = getClient(preferredProvider);
        if (preferred != null && preferred.isHealthy()) {
            return preferred;
        }

        LOGGER.warn("Preferred provider '{}' is not healthy, trying fallback", preferredProvider);

        // Try fallback
        return getHealthyClient()
            .orElseThrow(() -> new IllegalStateException(
                "No healthy LLM clients available. All providers are down."));
    }

    /**
     * Sets the default provider.
     *
     * @param providerId The provider ID to use as default
     * @throws IllegalArgumentException if the provider is not registered
     */
    public void setDefaultProvider(String providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("Provider ID cannot be null");
        }

        String normalizedId = providerId.toLowerCase();
        if (!clients.containsKey(normalizedId)) {
            throw new IllegalArgumentException("Provider not registered: " + providerId);
        }

        this.defaultProvider = normalizedId;
        LOGGER.info("Set default LLM provider to: {}", normalizedId);
    }

    /**
     * Sets the priority order for fallback selection.
     *
     * @param providerIds Ordered list of provider IDs
     */
    public void setPriorityOrder(List<String> providerIds) {
        priorityOrder.clear();
        for (String id : providerIds) {
            if (id != null) {
                priorityOrder.add(id.toLowerCase());
            }
        }
        LOGGER.info("Set LLM client priority order: {}", priorityOrder);
    }

    /**
     * Returns all registered provider IDs.
     *
     * @return Set of provider IDs
     */
    public Set<String> getRegisteredProviders() {
        return Collections.unmodifiableSet(clients.keySet());
    }

    /**
     * Returns all registered clients.
     *
     * @return Collection of clients
     */
    public Collection<LLMClient> getAllClients() {
        return Collections.unmodifiableCollection(clients.values());
    }

    /**
     * Returns all healthy clients.
     *
     * @return List of healthy clients
     */
    public List<LLMClient> getHealthyClients() {
        return clients.values().stream()
            .filter(LLMClient::isHealthy)
            .collect(Collectors.toList());
    }

    /**
     * Checks if a provider is registered.
     *
     * @param providerId The provider ID
     * @return true if registered
     */
    public boolean hasClient(String providerId) {
        if (providerId == null) {
            return false;
        }
        return clients.containsKey(providerId.toLowerCase());
    }

    /**
     * Checks if a provider is healthy.
     *
     * @param providerId The provider ID
     * @return true if healthy
     */
    public boolean isHealthy(String providerId) {
        LLMClient client = getClient(providerId);
        return client != null && client.isHealthy();
    }

    /**
     * Returns the number of registered clients.
     *
     * @return Client count
     */
    public int getClientCount() {
        return clients.size();
    }

    /**
     * Clears all registered clients.
     */
    public void clear() {
        clients.clear();
        priorityOrder.clear();
        LOGGER.info("Cleared all LLM clients");
    }

    /**
     * Returns a health status report for all providers.
     *
     * @return Map of provider ID to health status
     */
    public Map<String, Boolean> getHealthReport() {
        Map<String, Boolean> report = new LinkedHashMap<>();
        for (String providerId : priorityOrder) {
            LLMClient client = clients.get(providerId);
            report.put(providerId, client != null && client.isHealthy());
        }
        return report;
    }
}
