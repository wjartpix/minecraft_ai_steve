package com.steve.ai.plugin;

import com.steve.ai.di.ServiceContainer;

/**
 * Service Provider Interface (SPI) for action plugins.
 * 
 * <p><b>DEPRECATED:</b> This interface has been moved to {@link com.steve.ai.api.plugin.ActionPlugin}.
 * Please use the new API package for new implementations. This interface is kept for backward
 * compatibility and will be removed in a future version.</p>
 *
 * <p>Implement this interface to add custom actions to Steve AI.
 * The plugin system uses Java's ServiceLoader mechanism for discovery.</p>
 *
 * <p><b>Registration:</b> Create a file at:
 * {@code src/main/resources/META-INF/services/com.steve.ai.plugin.ActionPlugin}
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
 *         registry.register("dance", (steve, task, ctx) -&gt; new DanceAction(steve, task));
 *         registry.register("greet", (steve, task, ctx) -&gt; new GreetAction(steve, task));
 *     }
 * }
 * </pre>
 *
 * <p><b>Lifecycle:</b></p>
 * <ol>
 *   <li>Server starting: {@link #onLoad(ActionRegistry, ServiceContainer)} called</li>
 *   <li>Server running: Plugin's registered actions available</li>
 *   <li>Server stopping: {@link #onUnload()} called for cleanup</li>
 * </ol>
 *
 * <p><b>Design Patterns Used:</b></p>
 * <ul>
 *   <li><b>SPI (Service Provider Interface)</b>: Java ServiceLoader discovery</li>
 *   <li><b>Dependency Injection</b>: ServiceContainer provides dependencies</li>
 *   <li><b>Factory Pattern</b>: Plugins register ActionFactory instances</li>
 * </ul>
 *
 * @since 1.1.0
 * @see ActionRegistry
 * @see ActionFactory
 * @see ServiceContainer
 */
@Deprecated
public interface ActionPlugin extends com.steve.ai.api.plugin.ActionPlugin {

    /**
     * Returns the unique identifier for this plugin.
     *
     * <p>Must be unique across all loaded plugins. Use lowercase with hyphens
     * (e.g., "core-actions", "combat-ai", "building-tools").</p>
     *
     * @return Unique plugin identifier
     */
    @Override
    String getPluginId();

    /**
     * Called when the plugin is loaded during server startup.
     *
     * <p>Register your actions here using the provided registry.
     * The ServiceContainer provides access to shared services like
     * LLM clients, memory, and world knowledge.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * &#64;Override
     * public void onLoad(ActionRegistry registry, ServiceContainer container) {
     *     // Get shared services
     *     LLMCache cache = container.getService(LLMCache.class);
     *
     *     // Register actions with factory lambdas
     *     registry.register("mine", (steve, task, ctx) -&gt;
     *         new MineBlockAction(steve, task));
     *
     *     registry.register("smart_mine", (steve, task, ctx) -&gt;
     *         new SmartMineAction(steve, task, cache));
     * }
     * </pre>
     *
     * @param registry  Action registry to register factories
     * @param container Service container for dependency injection
     */
    @Override
    void onLoad(ActionRegistry registry, ServiceContainer container);

    /**
     * Called when the plugin is unloaded during server shutdown.
     *
     * <p>Perform cleanup operations here (close connections, flush caches, etc.).
     * Default implementation does nothing.</p>
     */
    @Override
    default void onUnload() {
        // Default: no cleanup needed
    }

    /**
     * Returns the priority for plugin loading order.
     *
     * <p>Higher priority plugins are loaded first. Used to resolve conflicts
     * when multiple plugins register the same action name.</p>
     *
     * @return Plugin priority (higher = loaded earlier)
     */
    @Override
    default int getPriority() {
        return 0;
    }

    /**
     * Returns plugin dependencies that must be loaded before this plugin.
     *
     * @return Array of plugin IDs this plugin depends on
     */
    @Override
    default String[] getDependencies() {
        return new String[0];
    }

    /**
     * Returns the plugin version for conflict resolution and logging.
     *
     * @return Plugin version string
     */
    @Override
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * Returns a human-readable description of this plugin.
     *
     * @return Plugin description
     */
    @Override
    default String getDescription() {
        return "No description provided";
    }
}
