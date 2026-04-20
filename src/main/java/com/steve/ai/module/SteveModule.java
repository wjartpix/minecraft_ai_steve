package com.steve.ai.module;

import com.steve.ai.di.ServiceContainer;

/**
 * Interface for Steve AI modules.
 *
 * <p>Modules are the primary building blocks of the Steve AI system.
 * Each module encapsulates a specific domain (e.g., LLM, Building, Combat)
 * and manages its own lifecycle.</p>
 *
 * <p><b>Lifecycle:</b></p>
 * <ol>
 *   <li>{@link #onInit(ServiceContainer)} - Initialize the module and register services</li>
 *   <li>{@link #onStart()} - Start the module's operations</li>
 *   <li>{@link #onStop()} - Stop and cleanup the module</li>
 *   <li>{@link #onReload()} - Reload configuration (optional)</li>
 * </ol>
 *
 * <p><b>Example Implementation:</b></p>
 * <pre>
 * public class LLMModule implements SteveModule {
 *     private LLMClientRegistry clientRegistry;
 *
 *     &#64;Override
 *     public String getModuleId() { return "llm"; }
 *
 *     &#64;Override
 *     public void onInit(ServiceContainer container) {
 *         clientRegistry = new LLMClientRegistry();
 *         container.register(LLMClientRegistry.class, clientRegistry);
 *     }
 *
 *     &#64;Override
 *     public void onStart() {
 *         // Warm up connections
 *     }
 *
 *     &#64;Override
 *     public void onStop() {
 *         // Close connections
 *     }
 * }
 * </pre>
 *
 * @since 2.0.0
 * @see ModuleManager
 * @see ServiceContainer
 */
public interface SteveModule {

    /**
     * Returns the unique identifier for this module.
     *
     * <p>Must be unique across all loaded modules. Use lowercase with hyphens
     * (e.g., "llm", "building", "combat").</p>
     *
     * @return Unique module identifier
     */
    String getModuleId();

    /**
     * Called during the initialization phase.
     *
     * <p>Use this method to:
     * <ul>
     *   <li>Register services with the container</li>
     *   <li>Load configuration</li>
     *   <li>Set up internal data structures</li>
     * </ul>
     * </p>
     *
     * @param container The service container for dependency registration
     */
    void onInit(ServiceContainer container);

    /**
     * Called during the startup phase.
     *
     * <p>Use this method to:
     * <ul>
     *   <li>Start background threads</li>
     *   <li>Connect to external services</li>
     *   <li>Initialize caches</li>
     * </ul>
     * </p>
     */
    void onStart();

    /**
     * Called during the shutdown phase.
     *
     * <p>Use this method to:
     * <ul>
     *   <li>Stop background threads</li>
     *   <li>Close connections</li>
     *   <li>Release resources</li>
     *   <li>Save state</li>
     * </ul>
     * </p>
     */
    void onStop();

    /**
     * Called when the module should reload its configuration.
     *
     * <p>Default implementation does nothing. Override to support
     * configuration hot-reloading.</p>
     */
    default void onReload() {
        // Default: no reload support
    }

    /**
     * Returns the priority for module loading order.
     *
     * <p>Higher priority modules are loaded first. Core infrastructure
     * modules should have higher priority than feature modules.</p>
     *
     * <p><b>Priority Guidelines:</b></p>
     * <ul>
     *   <li>1000+: Core infrastructure (EventBus, Config)</li>
     *   <li>500-999: Core services (LLM, WorldPerception)</li>
     *   <li>100-499: Feature modules (Building, Combat)</li>
     *   <li>0-99: Optional/Extension modules</li>
     * </ul>
     *
     * @return Module priority (higher = loaded earlier)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Returns module dependencies that must be loaded before this module.
     *
     * @return Array of module IDs this module depends on
     */
    default String[] getDependencies() {
        return new String[0];
    }

    /**
     * Returns the module version.
     *
     * @return Module version string (semantic versioning recommended)
     */
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * Returns a human-readable description of this module.
     *
     * @return Module description
     */
    default String getDescription() {
        return "No description provided";
    }

    /**
     * Checks if this module is enabled.
     *
     * <p>Disabled modules are not loaded. Override to implement
     * conditional module loading based on configuration.</p>
     *
     * @return true if the module should be loaded
     */
    default boolean isEnabled() {
        return true;
    }
}
