package com.steve.ai.perception;

import com.steve.ai.api.perception.WorldPerception;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Simulated implementation of WorldPerception for testing.
 *
 * <p>Allows tests to set up a virtual world without requiring a running
 * Minecraft server.</p>
 *
 * @since 2.0.0
 * @see WorldPerception
 */
public class SimulatedWorldPerception implements WorldPerception {

    private final Map<BlockPos, BlockState> blocks;
    private final List<EntityInfo> entities;
    private BiomeInfo defaultBiome;

    public SimulatedWorldPerception() {
        this.blocks = new HashMap<>();
        this.entities = new ArrayList<>();
        this.defaultBiome = new BiomeInfo("plains", "plains", 0.8f, 0.4f);
    }

    /**
     * Sets a block at a position.
     *
     * @param pos   The position
     * @param block The block to set
     */
    public void setBlock(BlockPos pos, Block block) {
        blocks.put(pos.immutable(), block.defaultBlockState());
    }

    /**
     * Sets a block state at a position.
     *
     * @param pos   The position
     * @param state The block state to set
     */
    public void setBlockState(BlockPos pos, BlockState state) {
        blocks.put(pos.immutable(), state);
    }

    /**
     * Removes a block at a position.
     *
     * @param pos The position
     */
    public void removeBlock(BlockPos pos) {
        blocks.remove(pos.immutable());
    }

    /**
     * Clears all blocks.
     */
    public void clearBlocks() {
        blocks.clear();
    }

    /**
     * Adds an entity to the simulation.
     *
     * @param entity The entity info
     */
    public void addEntity(EntityInfo entity) {
        entities.add(entity);
    }

    /**
     * Clears all entities.
     */
    public void clearEntities() {
        entities.clear();
    }

    /**
     * Sets the default biome.
     *
     * @param biome The biome info
     */
    public void setDefaultBiome(BiomeInfo biome) {
        this.defaultBiome = biome;
    }

    @Override
    public WorldScanResult scanAround(BlockPos center, int radius) {
        Map<BlockPos, BlockState> nearbyBlocks = new HashMap<>();

        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            if (pos.distSqr(center) <= radius * radius) {
                nearbyBlocks.put(pos, entry.getValue());
            }
        }

        List<EntityInfo> nearbyEntities = entities.stream()
            .filter(e -> e.getPosition().distSqr(center) <= radius * radius)
            .toList();

        return new WorldScanResult(
            center, radius, nearbyBlocks, nearbyEntities, defaultBiome, 0);
    }

    @Override
    public List<EntityInfo> scanEntities(BlockPos center, int radius) {
        return entities.stream()
            .filter(e -> e.getPosition().distSqr(center) <= radius * radius)
            .toList();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return blocks.getOrDefault(pos.immutable(), Blocks.AIR.defaultBlockState());
    }

    @Override
    public BiomeInfo getBiomeAt(BlockPos pos) {
        return defaultBiome;
    }

    @Override
    public boolean isWalkable(BlockPos pos) {
        BlockPos ground = pos.below();
        BlockState groundState = getBlockState(ground);
        BlockState footState = getBlockState(pos);
        BlockState headState = getBlockState(pos.above());

        return groundState.isSolid() && footState.isAir() && headState.isAir();
    }

    @Override
    public boolean isSafe(BlockPos pos) {
        BlockState state = getBlockState(pos);
        Block block = state.getBlock();

        return block != Blocks.LAVA &&
               block != Blocks.FIRE &&
               block != Blocks.SOUL_FIRE;
    }

    @Override
    public int findGroundLevel(int x, int z, int startY) {
        // Search downward from startY
        for (int y = startY; y >= -64; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (getBlockState(pos).isSolid()) {
                return y + 1;
            }
        }

        // Search upward
        for (int y = startY; y < 320; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (getBlockState(pos).isSolid()) {
                return y + 1;
            }
        }

        return -1;
    }
}
