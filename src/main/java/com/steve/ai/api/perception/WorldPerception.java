package com.steve.ai.api.perception;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

/**
 * Interface for world perception and environmental scanning.
 *
 * <p>This interface abstracts world sensing capabilities, allowing for:
 * <ul>
 *   <li>Different implementation strategies (sparse vs dense scanning)</li>
 *   <li>Mock implementations for testing</li>
 *   <li>Caching and optimization layers</li>
 * </ul>
 * </p>
 *
 * <p><b>Design Pattern:</b> Strategy Pattern for world sensing</p>
 *
 * @since 2.0.0
 * @see WorldScanResult
 */
public interface WorldPerception {

    /**
     * Scans the world around a center position.
     *
     * @param center The center position to scan from
     * @param radius The radius to scan (in blocks)
     * @return Scan result containing blocks, entities, and biome info
     */
    WorldScanResult scanAround(BlockPos center, int radius);

    /**
     * Scans for entities within a radius.
     *
     * @param center The center position
     * @param radius The radius to scan
     * @return List of entities found
     */
    List<EntityInfo> scanEntities(BlockPos center, int radius);

    /**
     * Gets the block state at a specific position.
     *
     * @param pos The position to check
     * @return The block state, or null if unloaded
     */
    BlockState getBlockState(BlockPos pos);

    /**
     * Gets the biome at a specific position.
     *
     * @param pos The position to check
     * @return Biome information
     */
    BiomeInfo getBiomeAt(BlockPos pos);

    /**
     * Checks if a position is walkable (solid ground, not obstructed).
     *
     * @param pos The position to check
     * @return true if walkable
     */
    boolean isWalkable(BlockPos pos);

    /**
     * Checks if a position is safe (no hazards like lava, fire).
     *
     * @param pos The position to check
     * @return true if safe
     */
    boolean isSafe(BlockPos pos);

    /**
     * Finds the ground level at a given x, z coordinate.
     *
     * @param x X coordinate
     * @param z Z coordinate
     * @param startY Starting Y to search from
     * @return The Y coordinate of the ground, or -1 if not found
     */
    int findGroundLevel(int x, int z, int startY);

    /**
     * Result of a world scan operation.
     */
    class WorldScanResult {
        private final BlockPos center;
        private final int radius;
        private final Map<BlockPos, BlockState> blocks;
        private final List<EntityInfo> entities;
        private final BiomeInfo biome;
        private final long scanTimeMs;

        public WorldScanResult(BlockPos center, int radius, Map<BlockPos, BlockState> blocks,
                               List<EntityInfo> entities, BiomeInfo biome, long scanTimeMs) {
            this.center = center;
            this.radius = radius;
            this.blocks = blocks;
            this.entities = entities;
            this.biome = biome;
            this.scanTimeMs = scanTimeMs;
        }

        public BlockPos getCenter() { return center; }
        public int getRadius() { return radius; }
        public Map<BlockPos, BlockState> getBlocks() { return blocks; }
        public List<EntityInfo> getEntities() { return entities; }
        public BiomeInfo getBiome() { return biome; }
        public long getScanTimeMs() { return scanTimeMs; }
    }

    /**
     * Information about an entity in the world.
     */
    class EntityInfo {
        private final String type;
        private final BlockPos position;
        private final double distance;
        private final boolean hostile;
        private final int health;

        public EntityInfo(String type, BlockPos position, double distance, boolean hostile, int health) {
            this.type = type;
            this.position = position;
            this.distance = distance;
            this.hostile = hostile;
            this.health = health;
        }

        public String getType() { return type; }
        public BlockPos getPosition() { return position; }
        public double getDistance() { return distance; }
        public boolean isHostile() { return hostile; }
        public int getHealth() { return health; }
    }

    /**
     * Information about a biome.
     */
    class BiomeInfo {
        private final String name;
        private final String category;
        private final float temperature;
        private final float rainfall;

        public BiomeInfo(String name, String category, float temperature, float rainfall) {
            this.name = name;
            this.category = category;
            this.temperature = temperature;
            this.rainfall = rainfall;
        }

        public String getName() { return name; }
        public String getCategory() { return category; }
        public float getTemperature() { return temperature; }
        public float getRainfall() { return rainfall; }
    }
}
