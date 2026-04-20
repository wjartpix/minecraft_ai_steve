package com.steve.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for module configurations.
 *
 * <p>Provides a unified interface for registering, retrieving, and reloading
 * module-specific configurations.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * ConfigManager manager = new ConfigManager();
 *
 * // Register module configs
 * manager.register(new LLMConfig());
 * manager.register(new BuildingConfig());
 *
 * // Retrieve config
 * LLMConfig llmConfig = manager.getConfig(LLMConfig.class);
 *
 * // Reload all configs
 * manager.reloadAll();
 * </pre>
 *
 * @since 2.0.0
 * @see ModuleConfig
 */
public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    private final Map<String, ModuleConfig> configs;
    private final Map<Class<?>, ModuleConfig> configsByClass;

    public ConfigManager() {
        this.configs = new ConcurrentHashMap<>();
        this.configsByClass = new ConcurrentHashMap<>();
    }

    /**
     * Registers a module configuration.
     *
     * @param config The configuration to register
     * @throws IllegalArgumentException if a config for the same module is already registered
     */
    public void register(ModuleConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        String moduleId = config.getModuleId();
        if (configs.containsKey(moduleId)) {
            throw new IllegalArgumentException("Config already registered for module: " + moduleId);
        }

        try {
            // Load and validate on registration
            config.load();
            config.validate();

            configs.put(moduleId, config);
            configsByClass.put(config.getClass(), config);

            LOGGER.info("Registered config for module: {} (v{})", moduleId, config.getVersion());
        } catch (ModuleConfig.ConfigValidationException e) {
            throw new IllegalArgumentException("Invalid configuration for module " + moduleId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Gets a configuration by module ID.
     *
     * @param moduleId The module ID
     * @return The configuration, or null if not found
     */
    public ModuleConfig getConfig(String moduleId) {
        return configs.get(moduleId);
    }

    /**
     * Gets a configuration by class type.
     *
     * @param <T>   The configuration type
     * @param clazz The configuration class
     * @return The configuration, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends ModuleConfig> T getConfig(Class<T> clazz) {
        return (T) configsByClass.get(clazz);
    }

    /**
     * Gets a configuration by module ID with type casting.
     *
     * @param <T>      The configuration type
     * @param moduleId The module ID
     * @param clazz    The expected configuration class
     * @return The configuration, or null if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T extends ModuleConfig> T getConfig(String moduleId, Class<T> clazz) {
        ModuleConfig config = configs.get(moduleId);
        if (config != null && clazz.isInstance(config)) {
            return (T) config;
        }
        return null;
    }

    /**
     * Checks if a configuration is registered.
     *
     * @param moduleId The module ID
     * @return true if registered
     */
    public boolean hasConfig(String moduleId) {
        return configs.containsKey(moduleId);
    }

    /**
     * Reloads a specific configuration.
     *
     * @param moduleId The module ID to reload
     * @return true if reload was successful
     */
    public boolean reload(String moduleId) {
        ModuleConfig config = configs.get(moduleId);
        if (config == null) {
            LOGGER.warn("Cannot reload unknown config: {}", moduleId);
            return false;
        }

        try {
            LOGGER.info("Reloading config for module: {}", moduleId);
            config.reload();
            LOGGER.info("Config reloaded successfully: {}", moduleId);
            return true;
        } catch (ModuleConfig.ConfigValidationException e) {
            LOGGER.error("Failed to reload config for module {}: {}", moduleId, e.getMessage());
            return false;
        }
    }

    /**
     * Reloads all configurations.
     *
     * @return Map of module ID to reload success status
     */
    public Map<String, Boolean> reloadAll() {
        Map<String, Boolean> results = new LinkedHashMap<>();

        LOGGER.info("Reloading all configurations ({} modules)...", configs.size());

        for (String moduleId : configs.keySet()) {
            results.put(moduleId, reload(moduleId));
        }

        return results;
    }

    /**
     * Unregisters a configuration.
     *
     * @param moduleId The module ID
     * @return true if a configuration was removed
     */
    public boolean unregister(String moduleId) {
        ModuleConfig removed = configs.remove(moduleId);
        if (removed != null) {
            configsByClass.remove(removed.getClass());
            LOGGER.info("Unregistered config for module: {}", moduleId);
            return true;
        }
        return false;
    }

    /**
     * Returns all registered module IDs.
     *
     * @return Set of module IDs
     */
    public Set<String> getRegisteredModules() {
        return Collections.unmodifiableSet(configs.keySet());
    }

    /**
     * Returns the number of registered configurations.
     *
     * @return Configuration count
     */
    public int getConfigCount() {
        return configs.size();
    }

    /**
     * Clears all configurations.
     */
    public void clear() {
        configs.clear();
        configsByClass.clear();
        LOGGER.info("Cleared all configurations");
    }

    /**
     * Returns a summary of all configurations.
     *
     * @return Map of module ID to version
     */
    public Map<String, String> getConfigSummary() {
        Map<String, String> summary = new LinkedHashMap<>();
        for (Map.Entry<String, ModuleConfig> entry : configs.entrySet()) {
            summary.put(entry.getKey(), entry.getValue().getVersion());
        }
        return summary;
    }
}
