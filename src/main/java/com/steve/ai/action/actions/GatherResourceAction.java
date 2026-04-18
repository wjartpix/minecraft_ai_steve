package com.steve.ai.action.actions;

import com.steve.ai.SteveMod;
import com.steve.ai.action.ActionResult;
import com.steve.ai.action.Task;
import com.steve.ai.entity.SteveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

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
    private static final int MAX_TICKS = 6000; // 5 minutes timeout
    private static final int GATHER_DELAY = 10; // Delay between gathering blocks
    private static final int SEARCH_RADIUS = 32; // Search radius for resources

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
    }};

    public GatherResourceAction(SteveEntity steve, Task task) {
        super(steve, task);
    }

    @Override
    protected void onStart() {
        resourceType = task.getStringParameter("resource");
        targetQuantity = task.getIntParameter("quantity", 16);
        gatheredCount = 0;
        ticksRunning = 0;
        ticksSinceLastGather = 0;

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

        SteveMod.LOGGER.info("Steve '{}' starting to gather {} {} (target blocks: {})",
            steve.getSteveName(), targetQuantity, resourceType, targetBlocks.size());

        // Find first target
        findNextBlock();

        if (currentTarget == null) {
            SteveMod.LOGGER.warn("Steve '{}' could not find any {} nearby within {} blocks",
                steve.getSteveName(), resourceType, SEARCH_RADIUS);
        }
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

        // Find target if we don't have one
        if (currentTarget == null) {
            findNextBlock();

            if (currentTarget == null) {
                // No more targets found
                if (gatheredCount > 0) {
                    result = ActionResult.success("Gathered " + gatheredCount + " " + resourceType + " (no more nearby)");
                } else {
                    result = ActionResult.failure("No " + resourceType + " found nearby");
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
     * Find the nearest target block within search radius
     */
    private void findNextBlock() {
        BlockPos stevePos = steve.blockPosition();
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        // Search in a cube around Steve
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -SEARCH_RADIUS / 2; dy <= SEARCH_RADIUS / 2; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    BlockPos checkPos = stevePos.offset(dx, dy, dz);
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
            SteveMod.LOGGER.debug("Steve '{}' found {} at {} (distance: {})",
                steve.getSteveName(), resourceType,
                currentTarget, Math.sqrt(nearestDistance));
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

