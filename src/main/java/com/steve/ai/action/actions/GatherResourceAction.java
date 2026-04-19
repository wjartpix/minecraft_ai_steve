package com.steve.ai.action.actions;

import com.steve.ai.SteveMod;
import com.steve.ai.action.ActionResult;
import com.steve.ai.action.Task;
import com.steve.ai.config.SteveConfig;
import com.steve.ai.entity.SteveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Action for gathering natural resources like wood logs, flowers, mushrooms, etc.
 * Searches for target blocks in the world, navigates to them, and harvests them.
 */
public class GatherResourceAction extends BaseAction {
    private String resourceType;
    private int targetQuantity;
    private int gatheredCount;
    private Set<Block> targetBlocks;
    private BlockPos currentTarget;
    private int ticksRunning;
    private int ticksSinceLastGather;
    private Player targetPlayer; // The player we're gathering for (center of search)
    private static final int MAX_TICKS = 7200; // 6 minutes timeout
    private static final int GATHER_DELAY = 10; // Delay between gathering blocks
    private int playerSearchRadius; // Configurable search radius (default 50 = 100x100 area)

    // Resource type mappings to handle variations
    private static final Map<String, String> RESOURCE_ALIASES = new HashMap<>() {{
        // Generic names mapped to groups (with "group:" prefix)
        put("wood", "group:logs");
        put("log", "group:logs");
        put("tree", "group:logs");
        put("timber", "group:logs");
        put("logs", "group:logs");
        put("plank", "oak_planks");
        put("planks", "oak_planks");
        put("flower", "group:flowers");
        put("flowers", "group:flowers");
        put("mushroom", "group:mushrooms");
        put("mushrooms", "group:mushrooms");
        put("ore", "group:ores");
        put("ores", "group:ores");
        put("stone", "group:stones");
        put("stones", "group:stones");
        // Oak wood aliases
        put("oak", "oak_log");
        put("oak_wood", "oak_log");
        put("oak_log", "oak_log");
        put("oak_tree", "oak_log");
        // Spruce wood aliases
        put("spruce", "spruce_log");
        put("spruce_wood", "spruce_log");
        put("spruce_log", "spruce_log");
        put("spruce_tree", "spruce_log");
        put("pine", "spruce_log");
        put("pine_wood", "spruce_log");
        // Birch wood aliases
        put("birch", "birch_log");
        put("birch_wood", "birch_log");
        put("birch_log", "birch_log");
        put("birch_tree", "birch_log");
        put("white_wood", "birch_log");
        // Jungle wood aliases
        put("jungle", "jungle_log");
        put("jungle_wood", "jungle_log");
        put("jungle_log", "jungle_log");
        put("jungle_tree", "jungle_log");
        // Acacia wood aliases
        put("acacia", "acacia_log");
        put("acacia_wood", "acacia_log");
        put("acacia_log", "acacia_log");
        put("acacia_tree", "acacia_log");
        // Dark oak wood aliases
        put("dark_oak", "dark_oak_log");
        put("dark_oak_wood", "dark_oak_log");
        put("dark_oak_log", "dark_oak_log");
        put("dark_oak_tree", "dark_oak_log");
        // Mangrove wood aliases
        put("mangrove", "mangrove_log");
        put("mangrove_wood", "mangrove_log");
        put("mangrove_log", "mangrove_log");
        put("mangrove_tree", "mangrove_log");
        // Cherry wood aliases
        put("cherry", "cherry_log");
        put("cherry_wood", "cherry_log");
        put("cherry_log", "cherry_log");
        put("cherry_tree", "cherry_log");
        put("cherry_blossom", "cherry_log");
        put("sakura", "cherry_log");
        // Flower aliases
        put("rose", "poppy");
        put("poppy", "poppy");
        put("dandelion", "dandelion");
        // Mushroom aliases
        put("red_mushroom", "red_mushroom");
        put("brown_mushroom", "brown_mushroom");
        put("brown_mushrooms", "brown_mushroom");
        // Stone aliases
        put("cobblestone", "cobblestone");
        put("deepslate", "deepslate");
        // Berry aliases
        put("berry", "sweet_berry_bush");
        put("berries", "sweet_berry_bush");
        // New simplified commands
        put("everything", "group:all");
        put("all", "group:all");
        put("anything", "group:all");
        put("stuff", "group:all");
        put("collect", "group:all");
        put("harvest", "group:all");
        put("resources", "group:all");
    }};

    // Resource groups: generic name -> list of block IDs
    private static final Map<String, List<String>> RESOURCE_GROUPS = new HashMap<>() {{
        put("logs", java.util.Arrays.asList(
            "oak_log", "spruce_log", "birch_log", "jungle_log",
            "acacia_log", "dark_oak_log", "mangrove_log", "cherry_log"
        ));
        put("flowers", java.util.Arrays.asList(
            "poppy", "dandelion", "sunflower", "blue_orchid",
            "allium", "azure_bluet", "oxeye_daisy", "cornflower", "lily_of_the_valley"
        ));
        put("mushrooms", java.util.Arrays.asList("red_mushroom", "brown_mushroom"));
        put("ores", java.util.Arrays.asList(
            "coal_ore", "iron_ore", "gold_ore", "diamond_ore",
            "copper_ore", "lapis_ore", "redstone_ore", "emerald_ore"
        ));
        put("stones", java.util.Arrays.asList(
            "stone", "cobblestone", "deepslate", "granite", "diorite", "andesite"
        ));
        // New: "all" group for gathering everything useful
        put("all", java.util.Arrays.asList(
            "oak_log", "spruce_log", "birch_log", "jungle_log",
            "acacia_log", "dark_oak_log", "mangrove_log", "cherry_log",
            "poppy", "dandelion", "sunflower", "blue_orchid",
            "allium", "azure_bluet", "oxeye_daisy", "cornflower", "lily_of_the_valley",
            "red_mushroom", "brown_mushroom",
            "sweet_berry_bush",
            "cactus", "sugar_cane", "bamboo",
            "wheat", "carrots", "potatoes", "beetroots"
        ));
    }};

    public GatherResourceAction(SteveEntity steve, Task task) {
        super(steve, task);
    }

    @Override
    protected void onStart() {
        resourceType = task.getStringParameter("resource");
        targetQuantity = task.getIntParameter("quantity", 32); // Default to 32 for balanced gathering
        gatheredCount = 0;
        ticksRunning = 0;
        ticksSinceLastGather = 0;
        targetPlayer = null;

        // Load configurable search radius
        playerSearchRadius = SteveConfig.GATHER_SEARCH_RADIUS.get();

        // Find the nearest player to gather for (center of search area)
        findTargetPlayer();

        if (resourceType == null || resourceType.isEmpty()) {
            result = ActionResult.failure("No resource type specified for gathering");
            return;
        }

        // Apply aliases if present
        String normalizedResource = resourceType.toLowerCase().replace(" ", "_");
        String resolved = normalizedResource;
        if (RESOURCE_ALIASES.containsKey(normalizedResource)) {
            resolved = RESOURCE_ALIASES.get(normalizedResource);
        }

        // Parse resource - either a group or single block
        if (resolved.startsWith("group:")) {
            // Group search
            String groupName = resolved.substring(6);
            List<String> blockIds = RESOURCE_GROUPS.get(groupName);
            if (blockIds != null) {
                targetBlocks = new HashSet<>();
                for (String id : blockIds) {
                    Block block = parseBlock(id);
                    if (block != null && block != Blocks.AIR) {
                        targetBlocks.add(block);
                    }
                }
            }
        } else {
            // Single block search (backward compatible)
            Block block = parseBlock(resolved);
            if (block != null && block != Blocks.AIR) {
                targetBlocks = new HashSet<>();
                targetBlocks.add(block);
            }
        }

        if (targetBlocks == null || targetBlocks.isEmpty()) {
            result = ActionResult.failure("Invalid resource type: " + resourceType);
            return;
        }

        String playerName = targetPlayer != null ? targetPlayer.getName().getString() : "unknown";
        int area = playerSearchRadius * 2;
        SteveMod.LOGGER.info("Steve '{}' gathering for player '{}', target: {} {} (target blocks: {}) in {}x{} area",
            steve.getSteveName(), playerName, targetQuantity, resourceType, targetBlocks.size(), area, area);

        // Find first target
        findNextBlock();

        if (currentTarget == null) {
            SteveMod.LOGGER.warn("Steve '{}' could not find any {} nearby player '{}' within {} blocks",
                steve.getSteveName(), resourceType, playerName, playerSearchRadius);
        }
    }

    /**
     * Find the nearest player to gather for (center of search area)
     */
    private void findTargetPlayer() {
        List<? extends Player> players = steve.level().players();
        if (players.isEmpty()) {
            targetPlayer = null;
            return;
        }

        // Find nearest player to Steve
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : players) {
            if (!player.isAlive() || player.isRemoved()) {
                continue;
            }
            double distance = steve.distanceTo(player);
            if (distance < nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }

        targetPlayer = nearest;
    }

    @Override
    protected void onTick() {
        ticksRunning++;
        ticksSinceLastGather++;

        // Check timeout
        if (ticksRunning > MAX_TICKS) {
            if (gatheredCount > 0) {
                result = ActionResult.success("Gathered " + gatheredCount + " " + resourceType + " (timeout)");
            } else {
                result = ActionResult.failure("Gathering timeout - no " + resourceType + " found");
            }
            return;
        }

        // Check if we've gathered enough
        if (gatheredCount >= targetQuantity) {
            result = ActionResult.success("Gathered " + gatheredCount + " " + resourceType);
            return;
        }

        // Wait for gather delay
        if (ticksSinceLastGather < GATHER_DELAY) {
            return;
        }

        // Periodically refresh player position and re-scan for new targets
        if (ticksRunning % 40 == 0) {
            findTargetPlayer();
        }

        // Find target if we don't have one
        if (currentTarget == null) {
            findNextBlock();

            if (currentTarget == null) {
                // No more targets found in search area around player
                String playerName = targetPlayer != null ? targetPlayer.getName().getString() : "unknown";
                int area = playerSearchRadius * 2;
                if (gatheredCount > 0) {
                    result = ActionResult.success("Gathered " + gatheredCount + " " + resourceType + 
                        " (no more in " + area + "x" + area + " area around " + playerName + ")");
                } else {
                    result = ActionResult.failure("No " + resourceType + " found in " + area + "x" + area + " area around " + playerName);
                }
                return;
            }
        }

        // Check if target still exists
        BlockState currentState = steve.level().getBlockState(currentTarget);
        if (!targetBlocks.contains(currentState.getBlock())) {
            // Target was already harvested
            currentTarget = null;
            return;
        }

        // Navigate to target if too far
        double distance = steve.distanceToSqr(
            currentTarget.getX() + 0.5,
            currentTarget.getY() + 0.5,
            currentTarget.getZ() + 0.5
        );

        if (distance > 16.0) { // 4 blocks away
            // Need to pathfind closer
            steve.getNavigation().moveTo(
                currentTarget.getX() + 0.5,
                currentTarget.getY(),
                currentTarget.getZ() + 0.5,
                1.0
            );
            return;
        }

        // Close enough to gather - look at target and harvest
        steve.getLookControl().setLookAt(
            currentTarget.getX() + 0.5,
            currentTarget.getY() + 0.5,
            currentTarget.getZ() + 0.5
        );

        // Swing hand (animation)
        steve.swing(InteractionHand.MAIN_HAND, true);

        // Destroy the block and drop items
        steve.level().destroyBlock(currentTarget, true);
        gatheredCount++;
        ticksSinceLastGather = 0;

        SteveMod.LOGGER.info("Steve '{}' gathered {} at {} - Total: {}/{}",
            steve.getSteveName(), resourceType, currentTarget,
            gatheredCount, targetQuantity);

        // Clear target to find next
        currentTarget = null;

        // Check if we're done
        if (gatheredCount >= targetQuantity) {
            result = ActionResult.success("Gathered " + gatheredCount + " " + resourceType);
        }
    }

    @Override
    protected void onCancel() {
        steve.getNavigation().stop();
        SteveMod.LOGGER.info("Steve '{}' cancelled gathering {} (gathered {}/{})",
            steve.getSteveName(), resourceType, gatheredCount, targetQuantity);
    }

    @Override
    public String getDescription() {
        return "Gather " + targetQuantity + " " + resourceType + " (" + gatheredCount + " gathered)";
    }

    /**
     * Find the nearest target block within search radius around the target player
     * Searches in a configurable area centered on the player
     */
    private void findNextBlock() {
        // Use target player as center of search, or fall back to Steve's position
        double centerX, centerY, centerZ;
        if (targetPlayer != null) {
            centerX = targetPlayer.getX();
            centerY = targetPlayer.getY();
            centerZ = targetPlayer.getZ();
        } else {
            centerX = steve.getX();
            centerY = steve.getY();
            centerZ = steve.getZ();
        }

        // Create search box centered on player (configurable radius)
        AABB searchBox = new AABB(
            centerX - playerSearchRadius, centerY - playerSearchRadius / 2, centerZ - playerSearchRadius,
            centerX + playerSearchRadius, centerY + playerSearchRadius / 2, centerZ + playerSearchRadius
        );

        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        BlockPos stevePos = steve.blockPosition();

        // Search all blocks in the area
        for (int dx = -playerSearchRadius; dx <= playerSearchRadius; dx++) {
            for (int dy = -playerSearchRadius / 2; dy <= playerSearchRadius / 2; dy++) {
                for (int dz = -playerSearchRadius; dz <= playerSearchRadius; dz++) {
                    BlockPos checkPos = new BlockPos((int)centerX + dx, (int)centerY + dy, (int)centerZ + dz);
                    
                    // Skip if outside search box
                    if (!searchBox.contains(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5)) {
                        continue;
                    }
                    
                    BlockState state = steve.level().getBlockState(checkPos);

                    if (targetBlocks.contains(state.getBlock())) {
                        double distance = stevePos.distSqr(checkPos);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearest = checkPos;
                        }
                    }
                }
            }
        }

        currentTarget = nearest;

        if (currentTarget != null) {
            String centerName = targetPlayer != null ? targetPlayer.getName().getString() : "Steve";
            int area = playerSearchRadius * 2;
            SteveMod.LOGGER.debug("Steve '{}' found {} at {} (distance: {} from Steve, near {}, {}x{} area)",
                steve.getSteveName(), resourceType,
                currentTarget, Math.sqrt(nearestDistance), centerName, area, area);
        }
    }

    /**
     * Parse block name to Block object
     */
    private Block parseBlock(String blockName) {
        blockName = blockName.toLowerCase().replace(" ", "_");

        // Add minecraft: prefix if no namespace
        if (!blockName.contains(":")) {
            blockName = "minecraft:" + blockName;
        }

        ResourceLocation resourceLocation = new ResourceLocation(blockName);
        return BuiltInRegistries.BLOCK.get(resourceLocation);
    }
}

