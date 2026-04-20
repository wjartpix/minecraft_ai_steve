package com.steve.ai.module;

import com.steve.ai.di.ServiceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle of Steve AI modules.
 *
 * <p>Handles module discovery, dependency resolution, and lifecycle
 * management (init, start, stop, reload).</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * ModuleManager manager = new ModuleManager(container);
 *
 * // Register modules
 * manager.register(new CoreModule());
 * manager.register(new LLMModule());
 * manager.register(new BuildingModule());
 *
 * // Start all modules in dependency order
 * manager.startAll();
 *
 * // Later, reload configuration
 * manager.reload("llm");
 *
 * // Shutdown
 * manager.stopAll();
 * </pre>
 *
 * @since 2.0.0
 * @see SteveModule
 * @see ServiceContainer
 */
public class ModuleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManager.class);

    private final ServiceContainer container;
    private final Map<String, SteveModule> modules;
    private final List<SteveModule> initOrder;
    private volatile boolean initialized;
    private volatile boolean started;

    public ModuleManager(ServiceContainer container) {
        this.container = container;
        this.modules = new ConcurrentHashMap<>();
        this.initOrder = new ArrayList<>();
        this.initialized = false;
        this.started = false;
    }

    /**
     * Registers a module.
     *
     * <p>Modules are not initialized until {@link #initAll()} or
     * {@link #startAll()} is called.</p>
     *
     * @param module The module to register
     * @throws IllegalArgumentException if a module with the same ID is already registered
     */
    public void register(SteveModule module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null");
        }

        String moduleId = module.getModuleId();
        if (modules.containsKey(moduleId)) {
            throw new IllegalArgumentException("Module already registered: " + moduleId);
        }

        if (!module.isEnabled()) {
            LOGGER.info("Skipping disabled module: {}", moduleId);
            return;
        }

        modules.put(moduleId, module);
        LOGGER.debug("Registered module: {} (priority: {})", moduleId, module.getPriority());
    }

    /**
     * Initializes all registered modules.
     *
     * <p>Modules are initialized in dependency order (topological sort).
     * Dependencies are initialized before dependent modules.</p>
     */
    public synchronized void initAll() {
        if (initialized) {
            LOGGER.warn("Modules already initialized, skipping");
            return;
        }

        LOGGER.info("Initializing {} modules...", modules.size());

        // Sort modules by dependencies and priority
        List<SteveModule> sorted = sortModules(new ArrayList<>(modules.values()));

        // Initialize in order
        for (SteveModule module : sorted) {
            try {
                LOGGER.info("Initializing module: {} (v{})",
                    module.getModuleId(), module.getVersion());
                module.onInit(container);
                initOrder.add(module);
            } catch (Exception e) {
                LOGGER.error("Failed to initialize module: {}", module.getModuleId(), e);
                // Continue with other modules
            }
        }

        initialized = true;
        LOGGER.info("All modules initialized ({} succeeded)", initOrder.size());
    }

    /**
     * Starts all registered modules.
     *
     * <p>Modules are started in initialization order. If not already
     * initialized, this method will call {@link #initAll()} first.</p>
     */
    public synchronized void startAll() {
        if (started) {
            LOGGER.warn("Modules already started, skipping");
            return;
        }

        if (!initialized) {
            LOGGER.info("Auto-initializing modules before start");
            initAll();
        }

        LOGGER.info("Starting {} modules...", initOrder.size());

        for (SteveModule module : initOrder) {
            try {
                LOGGER.info("Starting module: {}", module.getModuleId());
                module.onStart();
            } catch (Exception e) {
                LOGGER.error("Failed to start module: {}", module.getModuleId(), e);
                // Continue with other modules
            }
        }

        started = true;
        LOGGER.info("All modules started");
    }

    /**
     * Stops all registered modules.
     *
     * <p>Modules are stopped in reverse initialization order to ensure
     * proper cleanup of dependencies.</p>
     */
    public synchronized void stopAll() {
        if (!started && !initialized) {
            LOGGER.warn("Modules not started, nothing to stop");
            return;
        }

        LOGGER.info("Stopping {} modules...", initOrder.size());

        // Stop in reverse order
        List<SteveModule> reverseOrder = new ArrayList<>(initOrder);
        Collections.reverse(reverseOrder);

        for (SteveModule module : reverseOrder) {
            try {
                LOGGER.info("Stopping module: {}", module.getModuleId());
                module.onStop();
            } catch (Exception e) {
                LOGGER.error("Failed to stop module: {}", module.getModuleId(), e);
                // Continue with other modules
            }
        }

        started = false;
        initialized = false;
        initOrder.clear();
        modules.clear();

        LOGGER.info("All modules stopped");
    }

    /**
     * Reloads a specific module.
     *
     * @param moduleId The ID of the module to reload
     */
    public void reload(String moduleId) {
        SteveModule module = modules.get(moduleId);
        if (module == null) {
            LOGGER.warn("Cannot reload unknown module: {}", moduleId);
            return;
        }

        try {
            LOGGER.info("Reloading module: {}", moduleId);
            module.onReload();
        } catch (Exception e) {
            LOGGER.error("Failed to reload module: {}", moduleId, e);
        }
    }

    /**
     * Reloads all modules.
     */
    public void reloadAll() {
        LOGGER.info("Reloading all modules...");
        for (SteveModule module : initOrder) {
            reload(module.getModuleId());
        }
    }

    /**
     * Returns a registered module by ID.
     *
     * @param moduleId The module ID
     * @return The module, or null if not found
     */
    public SteveModule getModule(String moduleId) {
        return modules.get(moduleId);
    }

    /**
     * Checks if a module is registered.
     *
     * @param moduleId The module ID
     * @return true if registered
     */
    public boolean hasModule(String moduleId) {
        return modules.containsKey(moduleId);
    }

    /**
     * Returns all registered module IDs.
     *
     * @return Set of module IDs
     */
    public Set<String> getModuleIds() {
        return Collections.unmodifiableSet(modules.keySet());
    }

    /**
     * Returns the number of registered modules.
     *
     * @return Module count
     */
    public int getModuleCount() {
        return modules.size();
    }

    /**
     * Checks if modules have been initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Checks if modules have been started.
     *
     * @return true if started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Sorts modules by dependencies (topological sort) and priority.
     *
     * @param modules Unsorted modules
     * @return Sorted modules respecting dependencies and priority
     */
    private List<SteveModule> sortModules(List<SteveModule> modules) {
        // Build dependency graph
        Map<String, SteveModule> moduleMap = new HashMap<>();
        Map<String, Set<String>> dependencies = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        for (SteveModule module : modules) {
            String id = module.getModuleId();
            moduleMap.put(id, module);
            dependencies.put(id, new HashSet<>(Arrays.asList(module.getDependencies())));
            inDegree.put(id, 0);
        }

        // Calculate in-degrees
        for (SteveModule module : modules) {
            for (String dep : module.getDependencies()) {
                if (moduleMap.containsKey(dep)) {
                    inDegree.merge(module.getModuleId(), 1, Integer::sum);
                }
            }
        }

        // Topological sort with priority-based tie-breaking
        List<SteveModule> sorted = new ArrayList<>();
        PriorityQueue<SteveModule> queue = new PriorityQueue<>(
            Comparator.comparingInt(SteveModule::getPriority).reversed());

        // Start with modules that have no dependencies
        for (SteveModule module : modules) {
            if (inDegree.get(module.getModuleId()) == 0) {
                queue.offer(module);
            }
        }

        Set<String> processed = new HashSet<>();
        while (!queue.isEmpty()) {
            SteveModule module = queue.poll();
            sorted.add(module);
            processed.add(module.getModuleId());

            // Update in-degrees for modules that depend on this one
            for (SteveModule other : modules) {
                if (processed.contains(other.getModuleId())) continue;

                Set<String> deps = dependencies.get(other.getModuleId());
                if (deps.contains(module.getModuleId())) {
                    int newDegree = inDegree.get(other.getModuleId()) - 1;
                    inDegree.put(other.getModuleId(), newDegree);

                    // Check if all dependencies are now satisfied
                    boolean allSatisfied = deps.stream().allMatch(processed::contains);
                    if (allSatisfied && !queue.contains(other)) {
                        queue.offer(other);
                    }
                }
            }
        }

        // Check for circular dependencies
        if (sorted.size() != modules.size()) {
            LOGGER.error("Circular dependency detected! Some modules could not be sorted.");
            // Add remaining modules anyway
            for (SteveModule module : modules) {
                if (!processed.contains(module.getModuleId())) {
                    LOGGER.warn("Module {} has unresolved dependencies, loading anyway",
                        module.getModuleId());
                    sorted.add(module);
                }
            }
        }

        return sorted;
    }
}
