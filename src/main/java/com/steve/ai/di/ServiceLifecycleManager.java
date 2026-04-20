package com.steve.ai.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle of services in the container.
 *
 * <p>Handles initialization, startup, and shutdown of services that implement
 * the {@link Lifecycle} interface.</p>
 *
 * @since 2.0.0
 * @see Lifecycle
 * @see ServiceContainer
 */
public class ServiceLifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLifecycleManager.class);

    private final ServiceContainer container;
    private final Set<Lifecycle> managedServices;
    private final List<Lifecycle> initOrder;
    private volatile boolean initialized;
    private volatile boolean started;

    public ServiceLifecycleManager(ServiceContainer container) {
        this.container = container;
        this.managedServices = ConcurrentHashMap.newKeySet();
        this.initOrder = new ArrayList<>();
        this.initialized = false;
        this.started = false;
    }

    /**
     * Registers a service for lifecycle management.
     *
     * @param service The service to manage
     */
    public void register(Lifecycle service) {
        if (service == null) {
            return;
        }

        if (managedServices.add(service)) {
            LOGGER.debug("Registered service for lifecycle management: {}", service.getClass().getSimpleName());

            // If already initialized, initialize immediately
            if (initialized) {
                initializeService(service);
            }

            // If already started, start immediately
            if (started) {
                startService(service);
            }
        }
    }

    /**
     * Initializes all registered services.
     *
     * <p>Services are initialized in registration order.</p>
     */
    public synchronized void initAll() {
        if (initialized) {
            LOGGER.warn("Services already initialized, skipping");
            return;
        }

        LOGGER.info("Initializing {} services...", managedServices.size());

        for (Lifecycle service : managedServices) {
            initializeService(service);
        }

        initialized = true;
        LOGGER.info("All services initialized");
    }

    /**
     * Starts all registered services.
     *
     * <p>Services are started in the order they were initialized.</p>
     */
    public synchronized void startAll() {
        if (started) {
            LOGGER.warn("Services already started, skipping");
            return;
        }

        if (!initialized) {
            LOGGER.info("Auto-initializing services before start");
            initAll();
        }

        LOGGER.info("Starting {} services...", initOrder.size());

        for (Lifecycle service : initOrder) {
            startService(service);
        }

        started = true;
        LOGGER.info("All services started");
    }

    /**
     * Stops all registered services.
     *
     * <p>Services are stopped in reverse initialization order.</p>
     */
    public synchronized void stopAll() {
        if (!started && !initialized) {
            LOGGER.warn("Services not started, nothing to stop");
            return;
        }

        LOGGER.info("Stopping {} services...", initOrder.size());

        // Stop in reverse order
        List<Lifecycle> reverseOrder = new ArrayList<>(initOrder);
        Collections.reverse(reverseOrder);

        for (Lifecycle service : reverseOrder) {
            stopService(service);
        }

        started = false;
        initialized = false;
        initOrder.clear();
        managedServices.clear();

        LOGGER.info("All services stopped");
    }

    /**
     * Reloads all registered services.
     */
    public synchronized void reloadAll() {
        LOGGER.info("Reloading {} services...", initOrder.size());

        for (Lifecycle service : initOrder) {
            try {
                LOGGER.debug("Reloading service: {}", service.getClass().getSimpleName());
                service.onReload();
            } catch (Exception e) {
                LOGGER.error("Failed to reload service: {}", service.getClass().getSimpleName(), e);
            }
        }

        LOGGER.info("All services reloaded");
    }

    private void initializeService(Lifecycle service) {
        try {
            LOGGER.debug("Initializing service: {}", service.getClass().getSimpleName());
            service.onInit(container);
            initOrder.add(service);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize service: {}", service.getClass().getSimpleName(), e);
        }
    }

    private void startService(Lifecycle service) {
        try {
            LOGGER.debug("Starting service: {}", service.getClass().getSimpleName());
            service.onStart();
        } catch (Exception e) {
            LOGGER.error("Failed to start service: {}", service.getClass().getSimpleName(), e);
        }
    }

    private void stopService(Lifecycle service) {
        try {
            LOGGER.debug("Stopping service: {}", service.getClass().getSimpleName());
            service.onStop();
        } catch (Exception e) {
            LOGGER.error("Failed to stop service: {}", service.getClass().getSimpleName(), e);
        }
    }

    /**
     * Returns the number of managed services.
     *
     * @return Service count
     */
    public int getServiceCount() {
        return managedServices.size();
    }

    /**
     * Checks if services have been initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Checks if services have been started.
     *
     * @return true if started
     */
    public boolean isStarted() {
        return started;
    }
}
