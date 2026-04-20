package com.steve.ai.di;

/**
 * Interface for components that require lifecycle management.
 *
 * <p>Implement this interface to receive lifecycle callbacks during
 * application startup and shutdown.</p>
 *
 * <p><b>Lifecycle Order:</b></p>
 * <ol>
 *   <li>{@link #onInit(ServiceContainer)} - Initialize the component</li>
 *   <li>{@link #onStart()} - Start the component</li>
 *   <li>{@link #onStop()} - Stop the component</li>
 * </ol>
 *
 * @since 2.0.0
 * @see ServiceContainer
 */
public interface Lifecycle {

    /**
     * Called during initialization phase.
     *
     * <p>Use this method to register dependencies and perform setup that
     * doesn't require other services to be fully started.</p>
     *
     * @param container The service container for dependency lookup
     */
    default void onInit(ServiceContainer container) {
        // Default: no initialization needed
    }

    /**
     * Called during startup phase.
     *
     * <p>Use this method to start background tasks, connect to services,
     * or perform any operations that require all dependencies to be initialized.</p>
     */
    default void onStart() {
        // Default: no startup needed
    }

    /**
     * Called during shutdown phase.
     *
     * <p>Use this method to release resources, close connections,
     * and perform cleanup operations.</p>
     */
    default void onStop() {
        // Default: no cleanup needed
    }

    /**
     * Called when the component should reload its configuration.
     *
     * <p>Use this method to respond to configuration changes without
     * requiring a full restart.</p>
     */
    default void onReload() {
        // Default: no reload support
    }
}
