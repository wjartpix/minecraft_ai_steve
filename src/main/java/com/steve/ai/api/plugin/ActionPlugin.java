package com.steve.ai.api.plugin;

import com.steve.ai.di.ServiceContainer;
import com.steve.ai.plugin.ActionRegistry;

/**
 * Service Provider Interface (SPI) for action plugins.
 *
 * <p>This is the core extension point for Steve AI. Implement this interface
 * to add custom actions, behaviors, or integrations to the system.</p>
 *
 * <p><b>Registration:</b> Create a file at:
 * {@code src/main/resources/META-INF/services/com.steve.ai.api.plugin.ActionPlugin}
 * containing the fully qualified class name of your plugin implementation.</p>
 *
 * <p><b>Example Implementation:</b></p>
 * <pre>
 * public class CustomActionsPlugin implements ActionPlugin {
 *     &#64;Override
 *     public String getPluginId() { return "custom-actions"; }
 *
 *     &#64;Override
 *     public void onLoad(ActionRegistry registry, ServiceContainer container) {
 *         registry.register("dance", (steve, task, ctx) -> new DanceAction(steve, task));
 *     }
 * }
 * </pre>
 *
 * @since 2.0.0
 * @see ActionRegistry
 * @see ServiceContainer
 */
public interface ActionPlugin {

    /**
     * Returns the unique identifier for this plugin.
     *
     * <p>Must be unique across all loaded plugins. Use lowercase with hyphens
     * (e.g., "core-actions", "combat-ai", "building-tools").</p>
     *
     * @return Unique plugin identifier
     */
    String getPluginId();

    /**
     * Called when the plugin is loaded during server startup.
     *
     * <p>Register your actions here using the provided registry.
     * The ServiceContainer provides access to shared services.</p>
     *
     * @param registry  Action registry to register factories
     * @param container Service container for dependency injection
     */
    void onLoad(ActionRegistry registry, ServiceContainer container);

    /**
     * Called when the plugin is unloaded during server shutdown.
     *
     * <p>Perform cleanup operations here. Default implementation does nothing.</p>
     */
    default void onUnload() {
        // Default: no cleanup needed
    }

    /**
     * Returns the priority for plugin loading order.
     *
     * <p>Higher priority plugins are loaded first.</p>
     *
     * @return Plugin priority (higher = loaded earlier)
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Returns plugin dependencies that must be loaded before this plugin.
     *
     * @return Array of plugin IDs this plugin depends on
     */
    default String[] getDependencies() {
        return new String[0];
    }

    /**
     * Returns the plugin version.
     *
     * @return Plugin version string (semantic versioning recommended)
     */
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * Returns a human-readable description of this plugin.
     *
     * @return Plugin description
     */
    default String getDescription() {
        return "No description provided";
    }
}
