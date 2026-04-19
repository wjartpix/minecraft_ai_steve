package com.steve.ai.structure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Registry for managing available building styles.
 * Provides methods for style selection and distribution.
 */
public class BuildingStyleRegistry {

    private static final Logger LOGGER = LogManager.getLogger(BuildingStyleRegistry.class);

    private static final List<BuildingStyle> availableStyles = new ArrayList<>();
    private static final Map<String, BuildingStyle> styleMap = new HashMap<>();
    private static final Set<String> historicallyUsedStyles = new HashSet<>();
    private static final Random random = new Random();
    private static boolean initialized = false;

    /**
     * Initializes the registry with enabled styles.
     *
     * @param enabledStyleNames Comma-separated list of style names to enable,
     *                          or null/empty to enable all default styles
     */
    public static void initialize(String enabledStyleNames) {
        if (initialized) {
            LOGGER.warn("BuildingStyleRegistry already initialized, skipping re-initialization");
            return;
        }

        List<BuildingStyle> defaultStyles = BuildingStyle.getDefaultStyles();

        if (enabledStyleNames == null || enabledStyleNames.trim().isEmpty()) {
            // Enable all default styles
            availableStyles.addAll(defaultStyles);
            LOGGER.info("Initialized with all {} default building styles", defaultStyles.size());
        } else {
            // Parse enabled style names
            String[] names = enabledStyleNames.split(",");
            for (String name : names) {
                String trimmedName = name.trim();
                if (trimmedName.isEmpty()) {
                    continue;
                }

                // Find matching style from defaults
                for (BuildingStyle style : defaultStyles) {
                    if (style.name().equalsIgnoreCase(trimmedName)) {
                        availableStyles.add(style);
                        break;
                    }
                }
            }
            LOGGER.info("Initialized with {} enabled building styles from config", availableStyles.size());
        }

        // Populate style map
        for (BuildingStyle style : availableStyles) {
            styleMap.put(style.name().toLowerCase(), style);
        }

        initialized = true;
    }

    /**
     * Returns a random available building style.
     *
     * @return A randomly selected building style, or null if no styles available
     */
    public static BuildingStyle getRandomStyle() {
        if (availableStyles.isEmpty()) {
            LOGGER.warn("No building styles available");
            return null;
        }

        BuildingStyle style = availableStyles.get(random.nextInt(availableStyles.size()));
        LOGGER.debug("Selected random building style: {}", style.name());
        return style;
    }

    /**
     * Returns a building style that hasn't been used yet.
     * If all styles have been used, returns a random one (cycling behavior).
     *
     * @param usedStyles Set of style names already used in active builds
     * @return A building style not in usedStyles, or any style if all used
     */
    public static synchronized BuildingStyle getUniqueStyle(Set<String> usedStyles) {
        if (availableStyles.isEmpty()) {
            LOGGER.warn("No building styles available");
            return null;
        }

        // Merge with historically used styles for better variety across sessions
        Set<String> allUsedStyles = new HashSet<>();
        allUsedStyles.addAll(usedStyles);
        allUsedStyles.addAll(historicallyUsedStyles);

        // Find unused styles
        List<BuildingStyle> unusedStyles = new ArrayList<>();
        for (BuildingStyle style : availableStyles) {
            if (!allUsedStyles.contains(style.name())) {
                unusedStyles.add(style);
            }
        }

        BuildingStyle selectedStyle;
        if (!unusedStyles.isEmpty()) {
            // Return a random unused style
            selectedStyle = unusedStyles.get(random.nextInt(unusedStyles.size()));
            LOGGER.info("Selected unique building style: {} ({} of {} styles available)", 
                selectedStyle.name(), unusedStyles.size(), availableStyles.size());
        } else {
            // All styles used, cycle and return random one
            selectedStyle = availableStyles.get(random.nextInt(availableStyles.size()));
            LOGGER.info("All styles used, cycling with random style: {}", selectedStyle.name());
            // Clear history when all styles have been used to start fresh
            historicallyUsedStyles.clear();
        }

        // Track this style as historically used
        historicallyUsedStyles.add(selectedStyle.name());

        return selectedStyle;
    }

    /**
     * Clears the historical style usage tracking.
     * Call this when you want to reset style rotation.
     */
    public static synchronized void clearHistoricalUsage() {
        historicallyUsedStyles.clear();
        LOGGER.info("Cleared historical style usage tracking");
    }

    /**
     * Gets the set of historically used styles.
     * @return Set of style names that have been used
     */
    public static Set<String> getHistoricallyUsedStyles() {
        return new HashSet<>(historicallyUsedStyles);
    }

    /**
     * Gets a building style by its name.
     *
     * @param name The style name to look up
     * @return The building style, or null if not found
     */
    public static BuildingStyle getStyleByName(String name) {
        if (name == null) {
            return null;
        }
        BuildingStyle style = styleMap.get(name.toLowerCase());
        if (style != null) {
            LOGGER.debug("Found building style by name '{}': {}", name, style.name());
        } else {
            LOGGER.debug("Building style not found for name: {}", name);
        }
        return style;
    }

    /**
     * Returns an unmodifiable list of all available building styles.
     *
     * @return Unmodifiable list of available styles
     */
    public static List<BuildingStyle> getAvailableStyles() {
        return Collections.unmodifiableList(availableStyles);
    }

    /**
     * Returns the count of available building styles.
     *
     * @return Number of available styles
     */
    public static int getStyleCount() {
        return availableStyles.size();
    }
}
