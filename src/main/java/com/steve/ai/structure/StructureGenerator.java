package com.steve.ai.structure;

import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * Interface for structure generators.
 *
 * <p>Implementations generate block placements for specific structure types
 * (houses, towers, castles, etc.) and styles (modern, medieval, etc.).</p>
 *
 * <p><b>Design Pattern:</b> Strategy Pattern for structure generation</p>
 *
 * <p><b>Example Implementation:</b></p>
 * <pre>
 * public class HouseGenerator implements StructureGenerator {
 *     &#64;Override
 *     public String getGeneratorId() { return "house"; }
 *
 *     &#64;Override
 *     public boolean canGenerate(String structureType, BuildingStyle style) {
 *         return "house".equals(structureType);
 *     }
 *
 *     &#64;Override
 *     public List&lt;BlockPlacement&gt; generate(GenerationContext context) {
 *         // Generate house blocks
 *         List&lt;BlockPlacement&gt; blocks = new ArrayList&lt;&gt;();
 *         // ... generation logic
 *         return blocks;
 *     }
 * }
 * </pre>
 *
 * @since 2.0.0
 * @see StructureGeneratorRegistry
 * @see BuildingStyle
 */
public interface StructureGenerator {

    /**
     * Returns the unique identifier for this generator.
     *
     * @return Generator ID (e.g., "house", "tower", "castle")
     */
    String getGeneratorId();

    /**
     * Checks if this generator can generate the requested structure.
     *
     * @param structureType The type of structure (e.g., "house", "tower")
     * @param style         The building style
     * @return true if this generator can handle the request
     */
    boolean canGenerate(String structureType, BuildingStyle style);

    /**
     * Generates the structure.
     *
     * @param context Generation context with parameters
     * @return List of block placements
     */
    List<BlockPlacement> generate(GenerationContext context);

    /**
     * Returns the estimated number of blocks for a structure.
     *
     * @param context Generation context
     * @return Estimated block count
     */
    default int estimateBlockCount(GenerationContext context) {
        // Default: return 0 (unknown)
        return 0;
    }

    /**
     * Returns the display name for this generator.
     *
     * @return Human-readable name
     */
    default String getDisplayName() {
        return getGeneratorId();
    }

    /**
     * Returns the description for this generator.
     *
     * @return Human-readable description
     */
    default String getDescription() {
        return "No description provided";
    }

    /**
     * Context for structure generation.
     */
    class GenerationContext {
        private final String structureType;
        private final BuildingStyle style;
        private final BlockPos startPos;
        private final int[] dimensions;
        private final String material;

        public GenerationContext(String structureType, BuildingStyle style, BlockPos startPos,
                                 int[] dimensions, String material) {
            this.structureType = structureType;
            this.style = style;
            this.startPos = startPos;
            this.dimensions = dimensions != null ? dimensions.clone() : new int[]{9, 6, 9};
            this.material = material;
        }

        public String getStructureType() {
            return structureType;
        }

        public BuildingStyle getStyle() {
            return style;
        }

        public BlockPos getStartPos() {
            return startPos;
        }

        public int[] getDimensions() {
            return dimensions.clone();
        }

        public String getMaterial() {
            return material;
        }

        public int getWidth() {
            return dimensions.length > 0 ? dimensions[0] : 9;
        }

        public int getHeight() {
            return dimensions.length > 1 ? dimensions[1] : 6;
        }

        public int getDepth() {
            return dimensions.length > 2 ? dimensions[2] : 9;
        }
    }
}
