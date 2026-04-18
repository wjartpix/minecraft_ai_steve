package com.steve.ai.structure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
     * @param usedStyles Set of style names already used
     * @return A building style not in usedStyles, or any style if all used
     */
    public static synchronized BuildingStyle getUniqueStyle(Set<String> usedStyles) {
        if (availableStyles.isEmpty()) {
            LOGGER.warn("No building styles available");
            return null;
        }

        // Find unused styles
        List<BuildingStyle> unusedStyles = new ArrayList<>();
        for (BuildingStyle style : availableStyles) {
            if (!usedStyles.contains(style.name())) {
                unusedStyles.add(style);
            }
        }

        BuildingStyle selectedStyle;
        if (!unusedStyles.isEmpty()) {
            // Return a random unused style
            selectedStyle = unusedStyles.get(random.nextInt(unusedStyles.size()));
            LOGGER.debug("Selected unique building style: {}", selectedStyle.name());
        } else {
            // All styles used, cycle and return random one
            selectedStyle = availableStyles.get(random.nextInt(availableStyles.size()));
            LOGGER.debug("All styles used, cycling with random style: {}", selectedStyle.name());
        }

        return selectedStyle;
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
