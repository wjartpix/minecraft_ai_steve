package com.steve.ai.module;

import com.steve.ai.di.ServiceContainer;
import com.steve.ai.execution.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action module for Steve AI.
 *
 * <p>This module manages action execution infrastructure, including
 * interceptor chains, state machines, and execution context.</p>
 *
 * @since 2.0.0
 * @see SteveModule
 */
public class ActionModule implements SteveModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionModule.class);

    @Override
    public String getModuleId() {
        return "action";
    }

    @Override
    public int getPriority() {
        return 400; // After core, before building
    }

    @Override
    public String[] getDependencies() {
        return new String[]{"core"};
    }

    @Override
    public void onInit(ServiceContainer container) {
        LOGGER.info("Initializing Action Module");

        // Register global interceptors
        // Note: Individual ActionExecutors create their own interceptor chains
        // This module provides shared interceptor instances if needed

        LOGGER.info("Action Module initialized");
    }

    @Override
    public void onStart() {
        LOGGER.info("Starting Action Module");

        // Module is ready - actual action execution happens per-Steve via ActionExecutor

        LOGGER.info("Action Module started");
    }

    @Override
    public void onStop() {
        LOGGER.info("Stopping Action Module");

        // Nothing to clean up at module level

        LOGGER.info("Action Module stopped");
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public String getDescription() {
        return "Action execution module providing interceptor chains and state management";
    }
}
