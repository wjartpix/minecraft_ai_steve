package com.steve.ai.action.actions;

import com.steve.ai.SteveMod;
import com.steve.ai.action.ActionResult;
import com.steve.ai.action.Task;
import com.steve.ai.entity.SteveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Action for creating vehicles and equipment: rockets, railways, aircraft, cannons, and event gear.
 * Supports multiple vehicle types with procedural generation.
 */
public class CreateVehicleAction extends BaseAction {
    private String vehicleType;
    private int ticksRunning;
    private int currentStep;
    private int totalSteps;
    private BlockPos buildPos;
    private Direction facing;
    private static final int MAX_TICKS = 12000; // 10 minutes timeout
    private static final int BUILD_DELAY = 5; // Fast building for vehicles
    private static final int GROUND_SEARCH_RANGE = 10;

    // Vehicle type aliases
    private static final Map<String, String> VEHICLE_ALIASES = new HashMap<>() {{
        // Rockets
        put("rocket", "rocket");
        put("spaceship", "rocket");
        put("missile", "rocket");
        put("space_rocket", "rocket");
        
        // Railways
        put("rail", "railway");
        put("rails", "railway");
        put("railway", "railway");
        put("train_track", "railway");
        put("minecart_track", "railway");
        put("track", "railway");
        
        // Aircraft
        put("plane", "aircraft");
        put("aircraft", "aircraft");
        put("airplane", "aircraft");
        put("jet", "aircraft");
        put("helicopter", "aircraft");
        put("flyer", "aircraft");
        
        // Cannons
        put("cannon", "cannon");
        put("tnt_cannon", "cannon");
        put("artillery", "cannon");
        put("gun", "cannon");
        
        // Event gear
        put("event_gear", "event_gear");
        put("party_equipment", "event_gear");
        put("celebration", "event_gear");
        put("fireworks", "event_gear");
        put("festival", "event_gear");
    }};

    public CreateVehicleAction(SteveEntity steve, Task task) {
        super(steve, task);
    }

    @Override
    protected void onStart() {
        String rawType = task.getStringParameter("vehicle");
        if (rawType == null || rawType.isEmpty()) {
            rawType = task.getStringParameter("type"); // Alternative parameter name
        }
        
        vehicleType = resolveVehicleType(rawType);
        ticksRunning = 0;
        currentStep = 0;
        
        // Find nearest player to build near
        Player nearestPlayer = findNearestPlayer();
        if (nearestPlayer != null) {
            buildPos = findBuildPosition(nearestPlayer);
            facing = nearestPlayer.getDirection();
        } else {
            buildPos = steve.blockPosition().offset(2, 0, 2);
            facing = Direction.NORTH;
        }
        
        // Ensure we're on solid ground
        ensureOnGround();
        
        // Initialize build steps based on vehicle type
        totalSteps = getTotalSteps(vehicleType);
        
        steve.setFlying(true); // Enable flying for building
        
        SteveMod.LOGGER.info("Steve '{}' starting to create {} at {} facing {}",
            steve.getSteveName(), vehicleType, buildPos, facing);
    }

    @Override
    protected void onTick() {
        ticksRunning++;
        
        if (ticksRunning > MAX_TICKS) {
            steve.setFlying(false);
            result = ActionResult.failure("Vehicle creation timeout");
            return;
        }
        
        if (currentStep >= totalSteps) {
            steve.setFlying(false);
            result = ActionResult.success("Created " + vehicleType + " at " + buildPos);
            playCompletionEffects();
            return;
        }
        
        // Build one step per tick with delay
        if (ticksRunning % BUILD_DELAY == 0) {
            buildStep(currentStep);
            currentStep++;
            
            // Visual feedback
            steve.swing(InteractionHand.MAIN_HAND, true);
            spawnBuildParticles();
        }
    }

    @Override
    protected void onCancel() {
        steve.setFlying(false);
        steve.getNavigation().stop();
    }

    @Override
    public String getDescription() {
        return "Create " + vehicleType + " (" + currentStep + "/" + totalSteps + ")";
    }

    private String resolveVehicleType(String rawType) {
        if (rawType == null) return "rocket";
        String normalized = rawType.toLowerCase().replace(" ", "_");
        return VEHICLE_ALIASES.getOrDefault(normalized, normalized);
    }

    private Player findNearestPlayer() {
        var players = steve.level().players();
        if (players.isEmpty()) return null;
        
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;
        
        for (Player player : players) {
            if (!player.isAlive() || player.isRemoved()) continue;
            double dist = steve.distanceTo(player);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }
        return nearest;
    }

    private BlockPos findBuildPosition(Player player) {
        // Build 15 blocks in front of player to give more space
        BlockPos playerPos = player.blockPosition();
        Direction lookDir = player.getDirection();
        return playerPos.relative(lookDir, 15);
    }

    private void ensureOnGround() {
        BlockPos below = buildPos.below();
        if (!steve.level().getBlockState(below).isSolid()) {
            // Search for ground
            for (int dy = 0; dy >= -GROUND_SEARCH_RANGE; dy--) {
                BlockPos check = buildPos.offset(0, dy, 0);
                if (steve.level().getBlockState(check.below()).isSolid()) {
                    buildPos = check;
                    return;
                }
            }
        }
    }

    private int getTotalSteps(String type) {
        return switch (type) {
            case "rocket" -> 20;
            case "railway" -> 30;
            case "aircraft" -> 25;
            case "cannon" -> 15;
            case "event_gear" -> 12;
            default -> 15;
        };
    }

    private void buildStep(int step) {
        switch (vehicleType) {
            case "rocket" -> buildRocketStep(step);
            case "railway" -> buildRailwayStep(step);
            case "aircraft" -> buildAircraftStep(step);
            case "cannon" -> buildCannonStep(step);
            case "event_gear" -> buildEventGearStep(step);
            default -> buildRocketStep(step);
        }
    }

    private void buildRocketStep(int step) {
        // Rocket structure: tall vertical tower with fins
        BlockPos basePos = buildPos;
        
        if (step < 8) {
            // Main body - vertical column
            BlockPos bodyPos = basePos.above(step);
            setBlock(bodyPos, Blocks.WHITE_CONCRETE);
            // Add some detail blocks
            if (step % 2 == 0) {
                setBlock(bodyPos.north(), Blocks.LIGHT_GRAY_CONCRETE);
                setBlock(bodyPos.south(), Blocks.LIGHT_GRAY_CONCRETE);
                setBlock(bodyPos.east(), Blocks.LIGHT_GRAY_CONCRETE);
                setBlock(bodyPos.west(), Blocks.LIGHT_GRAY_CONCRETE);
            }
        } else if (step < 12) {
            // Fins at bottom - spread outward from base
            int finStep = step - 8;
            int offset = 2 - finStep;
            BlockPos finBase = basePos.below(1); // Start below the rocket base
            setBlock(finBase.north(offset), Blocks.ORANGE_CONCRETE);
            setBlock(finBase.south(offset), Blocks.ORANGE_CONCRETE);
            setBlock(finBase.east(offset), Blocks.ORANGE_CONCRETE);
            setBlock(finBase.west(offset), Blocks.ORANGE_CONCRETE);
        } else if (step < 16) {
            // Top cone - tapering to top
            int coneLevel = step - 12;
            BlockPos conePos = basePos.above(7 + coneLevel);
            setBlock(conePos, Blocks.RED_CONCRETE);
        } else {
            // Windows/details on rocket body
            BlockPos windowPos = basePos.above(4);
            setBlock(windowPos.north(), Blocks.GLASS);
            setBlock(windowPos.south(), Blocks.GLASS);
        }
    }

    private void buildRailwayStep(int step) {
        // Build a straight railway track
        int trackLength = 20;
        if (step >= trackLength) return;
        
        BlockPos trackPos = buildPos.relative(facing, step);
        
        // Place rail
        BlockState railState = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, getRailShape(facing));
        steve.level().setBlock(trackPos.above(), railState, 3);
        
        // Support blocks below rail
        setBlock(trackPos, Blocks.OAK_PLANKS);
        
        // Add powered rails every 8 blocks
        if (step % 8 == 0 && step > 0) {
            steve.level().setBlock(trackPos.above(), Blocks.POWERED_RAIL.defaultBlockState()
                .setValue(BlockStateProperties.RAIL_SHAPE, getRailShape(facing)), 3);
        }
        
        // Add redstone torch for powered rails - place next to the rail at same level
        if (step % 8 == 0 && step > 0) {
            BlockPos torchPos = trackPos.relative(facing.getClockWise());
            setBlock(torchPos, Blocks.REDSTONE_TORCH);
        }
    }

    private RailShape getRailShape(Direction dir) {
        return switch (dir) {
            case NORTH, SOUTH -> RailShape.NORTH_SOUTH;
            case EAST, WEST -> RailShape.EAST_WEST;
            default -> RailShape.NORTH_SOUTH;
        };
    }

    private void buildAircraftStep(int step) {
        BlockPos pos = buildPos.above(2);
        
        if (step < 8) {
            // Fuselage
            setBlock(pos.relative(facing, step), Blocks.WHITE_WOOL);
            // Wings
            if (step >= 3 && step <= 6) {
                setBlock(pos.relative(facing, step).relative(facing.getClockWise(), 2), Blocks.WHITE_WOOL);
                setBlock(pos.relative(facing, step).relative(facing.getCounterClockWise(), 2), Blocks.WHITE_WOOL);
                setBlock(pos.relative(facing, step).relative(facing.getClockWise(), 3), Blocks.WHITE_WOOL);
                setBlock(pos.relative(facing, step).relative(facing.getCounterClockWise(), 3), Blocks.WHITE_WOOL);
            }
        } else if (step < 12) {
            // Tail
            int tailPos = step - 8;
            setBlock(pos.relative(facing, 7).above(tailPos), Blocks.WHITE_WOOL);
            // Vertical stabilizer
            if (tailPos == 1) {
                setBlock(pos.relative(facing, 7).above(1).relative(facing.getClockWise()), Blocks.RED_WOOL);
                setBlock(pos.relative(facing, 7).above(1).relative(facing.getCounterClockWise()), Blocks.RED_WOOL);
            }
        } else if (step < 16) {
            // Cockpit windows
            setBlock(pos.relative(facing, 1).above(), Blocks.GLASS);
            setBlock(pos.relative(facing, 2).above(), Blocks.GLASS);
        } else {
            // Engines
            setBlock(pos.relative(facing, 4).relative(facing.getClockWise(), 2).below(), Blocks.GRAY_WOOL);
            setBlock(pos.relative(facing, 4).relative(facing.getCounterClockWise(), 2).below(), Blocks.GRAY_WOOL);
        }
    }

    private void buildCannonStep(int step) {
        BlockPos pos = buildPos;
        
        if (step < 6) {
            // Barrel - horizontal tube
            setBlock(pos.relative(facing, step).above(1), Blocks.BLACK_CONCRETE);
            // Barrel hollow
            if (step < 5) {
                // Leave center hollow for visual effect
            }
        } else if (step < 10) {
            // Base/support
            int baseStep = step - 6;
            setBlock(pos.relative(facing, 2).below(baseStep), Blocks.OBSIDIAN);
            setBlock(pos.relative(facing, 3).below(baseStep), Blocks.OBSIDIAN);
            // Side supports
            setBlock(pos.relative(facing, 2).below(baseStep).relative(facing.getClockWise()), Blocks.OBSIDIAN);
            setBlock(pos.relative(facing, 2).below(baseStep).relative(facing.getCounterClockWise()), Blocks.OBSIDIAN);
        } else if (step < 13) {
            // TNT chamber
            setBlock(pos.relative(facing, 4).above(1), Blocks.TNT);
            setBlock(pos.relative(facing, 5).above(1), Blocks.TNT);
        } else {
            // Redstone mechanism
            setBlock(pos.below(), Blocks.REDSTONE_BLOCK);
            setBlock(pos.relative(facing.getOpposite()), Blocks.LEVER);
        }
    }

    private void buildEventGearStep(int step) {
        BlockPos pos = buildPos;
        
        switch (step) {
            case 0 -> {
                // Stage platform - build in one step
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        setBlock(pos.offset(x, 0, z), Blocks.OAK_PLANKS);
                    }
                }
            }
            case 1, 2, 3, 4 -> {
                // Stage pillars - one layer per step
                int height = step;
                setBlock(pos.north(2).east(2).above(height), Blocks.OAK_FENCE);
                setBlock(pos.north(2).west(2).above(height), Blocks.OAK_FENCE);
                setBlock(pos.south(2).east(2).above(height), Blocks.OAK_FENCE);
                setBlock(pos.south(2).west(2).above(height), Blocks.OAK_FENCE);
            }
            case 5 -> {
                // Roof
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        setBlock(pos.offset(x, 5, z), Blocks.RED_WOOL);
                    }
                }
            }
            case 6, 7, 8, 9 -> {
                // Fireworks launchers around the stage
                int launcherIndex = step - 6;
                BlockPos[] launcherPos = {
                    pos.north(3).above(),
                    pos.south(3).above(),
                    pos.east(3).above(),
                    pos.west(3).above()
                };
                if (launcherIndex < launcherPos.length) {
                    setBlock(launcherPos[launcherIndex], Blocks.DISPENSER);
                }
            }
            case 10, 11 -> {
                // Redstone for fireworks - one per step
                int redstoneIndex = step - 10;
                BlockPos[] redstonePos = {
                    pos.north(3).below(),
                    pos.south(3).below(),
                    pos.east(3).below(),
                    pos.west(3).below()
                };
                if (redstoneIndex * 2 < redstonePos.length) {
                    setBlock(redstonePos[redstoneIndex * 2], Blocks.REDSTONE_BLOCK);
                    if (redstoneIndex * 2 + 1 < redstonePos.length) {
                        setBlock(redstonePos[redstoneIndex * 2 + 1], Blocks.REDSTONE_BLOCK);
                    }
                }
            }
        }
    }

    private void setBlock(BlockPos pos, Block block) {
        if (steve.level().getBlockState(pos).isAir() || steve.level().getBlockState(pos).canBeReplaced()) {
            steve.level().setBlock(pos, block.defaultBlockState(), 3);
        }
    }

    private void spawnBuildParticles() {
        if (steve.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                steve.getX(), steve.getY() + 1, steve.getZ(),
                3, 0.5, 0.5, 0.5, 0.1
            );
        }
    }

    private void playCompletionEffects() {
        if (steve.level() instanceof ServerLevel serverLevel) {
            // Fireworks effect
            serverLevel.sendParticles(
                ParticleTypes.FIREWORK,
                buildPos.getX() + 0.5, buildPos.getY() + 2, buildPos.getZ() + 0.5,
                50, 1, 1, 1, 0.3
            );
            
            // Sound effect
            steve.level().playSound(null, buildPos, 
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }
}
