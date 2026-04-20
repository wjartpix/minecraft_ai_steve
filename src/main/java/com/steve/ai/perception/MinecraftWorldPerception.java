package com.steve.ai.perception;

import com.steve.ai.api.perception.WorldPerception;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Minecraft implementation of WorldPerception.
 *
 * <p>Provides world sensing capabilities by querying the Minecraft world directly.</p>
 *
 * @since 2.0.0
 * @see WorldPerception
 */
public class MinecraftWorldPerception implements WorldPerception {

    private final Level level;

    public MinecraftWorldPerception(Level level) {
        this.level = level;
    }

    @Override
    public WorldScanResult scanAround(BlockPos center, int radius) {
        long startTime = System.currentTimeMillis();

        Map<BlockPos, BlockState> blocks = new HashMap<>();
        List<EntityInfo> entities = scanEntities(center, radius);

        // Sparse sampling for performance
        int sampleInterval = Math.max(1, radius / 8);

        for (int x = -radius; x <= radius; x += sampleInterval) {
            for (int y = -radius; y <= radius; y += sampleInterval) {
                for (int z = -radius; z <= radius; z += sampleInterval) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir()) {
                        blocks.put(pos, state);
                    }
                }
            }
        }

        BiomeInfo biome = getBiomeAt(center);
        long scanTimeMs = System.currentTimeMillis() - startTime;

        return new WorldScanResult(center, radius, blocks, entities, biome, scanTimeMs);
    }

    @Override
    public List<EntityInfo> scanEntities(BlockPos center, int radius) {
        AABB searchBox = new AABB(center).inflate(radius);
        List<Entity> entities = level.getEntities(null, searchBox);

        return entities.stream()
            .map(e -> new EntityInfo(
                e.getType().getDescription().getString(),
                e.blockPosition(),
                Math.sqrt(e.distanceToSqr(center.getX(), center.getY(), center.getZ())),
                e instanceof Mob && ((Mob) e).getTarget() != null,
                e instanceof LivingEntity ? (int) ((LivingEntity) e).getHealth() : 0
            ))
            .collect(Collectors.toList());
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        return level.getBlockState(pos);
    }

    @Override
    public BiomeInfo getBiomeAt(BlockPos pos) {
        Biome biome = level.getBiome(pos).value();

        return new BiomeInfo(
            level.getBiome(pos).unwrapKey()
                .map(key -> key.location().getPath())
                .orElse("unknown"),
            "unknown",
            biome.getBaseTemperature(),
            0.5f
        );
    }

    @Override
    public boolean isWalkable(BlockPos pos) {
        BlockPos ground = pos.below();
        BlockState groundState = level.getBlockState(ground);
        BlockState footState = level.getBlockState(pos);
        BlockState headState = level.getBlockState(pos.above());

        // Ground must be solid
        if (!groundState.isSolid()) {
            return false;
        }

        // Foot and head positions must be passable
        return footState.isAir() && headState.isAir();
    }

    @Override
    public boolean isSafe(BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        // Check for hazards
        if (state.is(Blocks.LAVA) ||
            state.is(Blocks.FIRE) ||
            state.is(Blocks.SOUL_FIRE) ||
            state.is(Blocks.CAMPFIRE) ||
            state.is(Blocks.SOUL_CAMPFIRE)) {
            return false;
        }

        // Check for suffocation
        if (state.isSolid() && state.blocksMotion()) {
            return false;
        }

        return true;
    }

    @Override
    public int findGroundLevel(int x, int z, int startY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, startY, z);

        // Search downward
        for (int y = startY; y >= level.getMinBuildHeight(); y--) {
            pos.setY(y);
            BlockState state = level.getBlockState(pos);
            if (state.isSolid()) {
                return y + 1; // Return the air block above
            }
        }

        // Search upward if not found
        for (int y = startY; y < level.getMaxBuildHeight(); y++) {
            pos.setY(y);
            BlockState state = level.getBlockState(pos);
            if (state.isSolid()) {
                return y + 1;
            }
        }

        return -1;
    }
}
