package com.steve.ai.module;

import com.steve.ai.di.ServiceContainer;
import com.steve.ai.entity.SteveManager;
import com.steve.ai.event.EventBus;
import com.steve.ai.event.SimpleEventBus;
import com.steve.ai.plugin.ActionRegistry;
import com.steve.ai.plugin.CoreActionsPlugin;
import com.steve.ai.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core module for Steve AI.
 *
 * <p>This module provides the foundational infrastructure for Steve AI,
 * including the event bus, plugin system, and entity management.</p>
 *
 * @since 2.0.0
 * @see SteveModule
 */
public class CoreModule implements SteveModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreModule.class);

    private EventBus eventBus;
    private ActionRegistry actionRegistry;
    private SteveManager steveManager;
    private ServiceContainer container;

    @Override
    public String getModuleId() {
        return "core";
    }

    @Override
    public int getPriority() {
        return 1000; // Highest priority - must load first
    }

    @Override
    public void onInit(ServiceContainer container) {
        LOGGER.info("Initializing Core Module");
        this.container = container;

        // Initialize event bus
        this.eventBus = new SimpleEventBus();
        container.register(EventBus.class, eventBus);
        LOGGER.debug("EventBus registered");

        // Initialize action registry (singleton)
        this.actionRegistry = ActionRegistry.getInstance();
        container.register(ActionRegistry.class, actionRegistry);
        LOGGER.debug("ActionRegistry registered");

        // Initialize Steve manager
        this.steveManager = new SteveManager();
        container.register(SteveManager.class, steveManager);
        LOGGER.debug("SteveManager registered");

        LOGGER.info("Core Module initialized");
    }

    @Override
    public void onStart() {
        LOGGER.info("Starting Core Module");

        // Load plugins via SPI
        PluginManager.getInstance().loadPlugins(actionRegistry, container);

        // Register core actions plugin manually as fallback
        if (actionRegistry.getActionCount() == 0) {
            LOGGER.warn("No plugins loaded via SPI, registering CoreActionsPlugin manually");
            CoreActionsPlugin corePlugin = new CoreActionsPlugin();
            corePlugin.onLoad(actionRegistry, container);
        }

        LOGGER.info("Core Module started with {} actions", actionRegistry.getActionCount());
    }

    @Override
    public void onStop() {
        LOGGER.info("Stopping Core Module");

        // Unload plugins
        PluginManager.getInstance().unloadPlugins();

        // Shutdown event bus
        if (eventBus instanceof SimpleEventBus simpleEventBus) {
            simpleEventBus.shutdown();
        }

        // Clear all Steves
        if (steveManager != null) {
            steveManager.clearAllSteves();
        }

        LOGGER.info("Core Module stopped");
    }

    @Override
    public void onReload() {
        LOGGER.info("Reloading Core Module");

        // Reload plugins
        PluginManager.getInstance().unloadPlugins();
        PluginManager.getInstance().loadPlugins(actionRegistry, container);

        LOGGER.info("Core Module reloaded");
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public String getDescription() {
        return "Core infrastructure module providing EventBus, Plugin system, and Entity management";
    }

    /**
     * Gets the event bus.
     *
     * @return The event bus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Gets the action registry.
     *
     * @return The action registry
     */
    public ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    /**
     * Gets the Steve manager.
     *
     * @return The Steve manager
     */
    public SteveManager getSteveManager() {
        return steveManager;
    }
}
