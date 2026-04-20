package com.steve.ai.config;

/**
 * Interface for module-specific configuration.
 *
 * <p>Each module can implement this interface to define its own configuration
 * structure, validation, and reload behavior.</p>
 *
 * <p><b>Example Implementation:</b></p>
 * <pre>
 * public class LLMConfig implements ModuleConfig {
 *     private String provider = "groq";
 *     private int timeoutMs = 30000;
 *
 *     &#64;Override
 *     public String getModuleId() { return "llm"; }
 *
 *     &#64;Override
 *     public void validate() throws ConfigValidationException {
 *         if (timeoutMs < 1000) {
 *             throw new ConfigValidationException("timeoutMs must be at least 1000");
 *         }
 *     }
 *
 *     // Getters
 *     public String getProvider() { return provider; }
 *     public int getTimeoutMs() { return timeoutMs; }
 * }
 * </pre>
 *
 * @since 2.0.0
 * @see ConfigManager
 */
public interface ModuleConfig {

    /**
     * Returns the module ID this configuration belongs to.
     *
     * @return Unique module identifier
     */
    String getModuleId();

    /**
     * Validates the configuration.
     *
     * @throws ConfigValidationException if validation fails
     */
    default void validate() throws ConfigValidationException {
        // Default: no validation
    }

    /**
     * Loads configuration values from the global config.
     *
     * <p>This method is called during initialization and reload.</p>
     */
    default void load() {
        // Default: no loading needed
    }

    /**
     * Called when configuration should be reloaded.
     *
     * <p>Default implementation calls {@link #load()} and {@link #validate()}.</p>
     *
     * @throws ConfigValidationException if validation fails after reload
     */
    default void reload() throws ConfigValidationException {
        load();
        validate();
    }

    /**
     * Returns the configuration version.
     *
     * @return Version string for tracking config changes
     */
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * Exception thrown when configuration validation fails.
     */
    class ConfigValidationException extends Exception {
        public ConfigValidationException(String message) {
            super(message);
        }

        public ConfigValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
