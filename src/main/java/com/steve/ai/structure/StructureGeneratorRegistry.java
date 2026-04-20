package com.steve.ai.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for structure generators.
 *
 * <p>Manages available structure generators and provides lookup by
 * structure type and style.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * StructureGeneratorRegistry registry = new StructureGeneratorRegistry();
 *
 * // Register generators
 * registry.register(new HouseGenerator());
 * registry.register(new TowerGenerator());
 *
 * // Find generator
 * StructureGenerator generator = registry.findGenerator("house", BuildingStyle.OAK_CLASSIC);
 *
 * // Generate structure
 * List&lt;BlockPlacement&gt; blocks = generator.generate(context);
 * </pre>
 *
 * @since 2.0.0
 * @see StructureGenerator
 */
public class StructureGeneratorRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructureGeneratorRegistry.class);

    private final Map<String, StructureGenerator> generators;

    public StructureGeneratorRegistry() {
        this.generators = new ConcurrentHashMap<>();
    }

    /**
     * Registers a structure generator.
     *
     * @param generator The generator to register
     * @throws IllegalArgumentException if a generator with the same ID is already registered
     */
    public void register(StructureGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("Generator cannot be null");
        }

        String generatorId = generator.getGeneratorId().toLowerCase();

        if (generators.containsKey(generatorId)) {
            LOGGER.warn("Replacing existing generator: {}", generatorId);
        }

        generators.put(generatorId, generator);
        LOGGER.info("Registered structure generator: {} ({})",
            generatorId, generator.getDisplayName());
    }

    /**
     * Unregisters a structure generator.
     *
     * @param generatorId The generator ID
     * @return true if a generator was removed
     */
    public boolean unregister(String generatorId) {
        if (generatorId == null) {
            return false;
        }

        StructureGenerator removed = generators.remove(generatorId.toLowerCase());
        if (removed != null) {
            LOGGER.info("Unregistered structure generator: {}", generatorId);
            return true;
        }
        return false;
    }

    /**
     * Finds a generator that can handle the requested structure.
     *
     * @param structureType The type of structure
     * @param style         The building style
     * @return A suitable generator, or null if none found
     */
    public StructureGenerator findGenerator(String structureType, BuildingStyle style) {
        if (structureType == null) {
            return null;
        }

        // First try exact match
        StructureGenerator exactMatch = generators.get(structureType.toLowerCase());
        if (exactMatch != null && exactMatch.canGenerate(structureType, style)) {
            return exactMatch;
        }

        // Search for any compatible generator
        return generators.values().stream()
            .filter(g -> g.canGenerate(structureType, style))
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets a generator by ID.
     *
     * @param generatorId The generator ID
     * @return The generator, or null if not found
     */
    public StructureGenerator getGenerator(String generatorId) {
        if (generatorId == null) {
            return null;
        }
        return generators.get(generatorId.toLowerCase());
    }

    /**
     * Returns all registered generator IDs.
     *
     * @return Set of generator IDs
     */
    public Set<String> getRegisteredGenerators() {
        return Collections.unmodifiableSet(generators.keySet());
    }

    /**
     * Returns all registered generators.
     *
     * @return Collection of generators
     */
    public Collection<StructureGenerator> getAllGenerators() {
        return Collections.unmodifiableCollection(generators.values());
    }

    /**
     * Returns generators that can handle a specific structure type.
     *
     * @param structureType The structure type
     * @return List of compatible generators
     */
    public List<StructureGenerator> findCompatibleGenerators(String structureType) {
        return generators.values().stream()
            .filter(g -> g.canGenerate(structureType, null))
            .collect(Collectors.toList());
    }

    /**
     * Checks if a generator is registered.
     *
     * @param generatorId The generator ID
     * @return true if registered
     */
    public boolean hasGenerator(String generatorId) {
        if (generatorId == null) {
            return false;
        }
        return generators.containsKey(generatorId.toLowerCase());
    }

    /**
     * Returns the number of registered generators.
     *
     * @return Generator count
     */
    public int getGeneratorCount() {
        return generators.size();
    }

    /**
     * Clears all registered generators.
     */
    public void clear() {
        generators.clear();
        LOGGER.info("Cleared all structure generators");
    }

    /**
     * Returns a summary of all registered generators.
     *
     * @return Map of generator ID to display name
     */
    public Map<String, String> getGeneratorSummary() {
        Map<String, String> summary = new LinkedHashMap<>();
        for (Map.Entry<String, StructureGenerator> entry : generators.entrySet()) {
            summary.put(entry.getKey(), entry.getValue().getDisplayName());
        }
        return summary;
    }
}
