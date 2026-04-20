package com.steve.ai.module;

import com.steve.ai.config.SteveConfig;
import com.steve.ai.di.ServiceContainer;
import com.steve.ai.structure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Building module for Steve AI.
 *
 * <p>This module manages building functionality, including structure generators,
 * building styles, and style registry.</p>
 *
 * @since 2.0.0
 * @see SteveModule
 */
public class BuildingModule implements SteveModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildingModule.class);

    private StructureGeneratorRegistry generatorRegistry;

    @Override
    public String getModuleId() {
        return "building";
    }

    @Override
    public int getPriority() {
        return 300; // Feature module - after core and LLM
    }

    @Override
    public String[] getDependencies() {
        return new String[]{"core"}; // Depends on core module
    }

    @Override
    public void onInit(ServiceContainer container) {
        LOGGER.info("Initializing Building Module");

        // Initialize structure generator registry
        this.generatorRegistry = new StructureGeneratorRegistry();

        // Register built-in generators
        registerBuiltinGenerators();

        // Register with container
        container.register(StructureGeneratorRegistry.class, generatorRegistry);

        LOGGER.info("Building Module initialized with {} generators", 
            generatorRegistry.getGeneratorCount());
    }

    @Override
    public void onStart() {
        LOGGER.info("Starting Building Module");

        // Initialize building style registry from config
        String enabledStyles = SteveConfig.BUILDING_STYLES_ENABLED.get() 
            ? SteveConfig.BUILDING_STYLES.get() 
            : null;
        BuildingStyleRegistry.initialize(enabledStyles);

        LOGGER.info("Building Module started with {} styles available",
            BuildingStyleRegistry.getStyleCount());
    }

    @Override
    public void onStop() {
        LOGGER.info("Stopping Building Module");

        // Clear generators
        if (generatorRegistry != null) {
            generatorRegistry.clear();
        }

        LOGGER.info("Building Module stopped");
    }

    @Override
    public void onReload() {
        LOGGER.info("Reloading Building Module");

        // Re-initialize building styles from config
        String enabledStyles = SteveConfig.BUILDING_STYLES_ENABLED.get() 
            ? SteveConfig.BUILDING_STYLES.get() 
            : null;
        BuildingStyleRegistry.initialize(enabledStyles);

        LOGGER.info("Building Module reloaded");
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public String getDescription() {
        return "Building module providing structure generators and building styles";
    }

    /**
     * Gets the structure generator registry.
     *
     * @return The generator registry
     */
    public StructureGeneratorRegistry getGeneratorRegistry() {
        return generatorRegistry;
    }

    /**
     * Registers built-in structure generators.
     */
    private void registerBuiltinGenerators() {
        // Note: These are placeholder generators
        // In a real implementation, you would register actual generators here
        // For now, we just log that we're ready to accept generators

        LOGGER.debug("Built-in generators registration ready");
        LOGGER.info("StructureGeneratorRegistry ready for plugin registration");
    }
}
