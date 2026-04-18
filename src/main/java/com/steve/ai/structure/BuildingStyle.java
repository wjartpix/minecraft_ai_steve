package com.steve.ai.structure;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Represents a building style with predefined materials for different parts of a structure.
 * Used by structure generators and building actions to create themed constructions.
 */
public class BuildingStyle {
    private final String name;
    private final String displayName;
    private final Block wallBlock;
    private final Block floorBlock;
    private final Block roofBlock;
    private final Block pillarBlock;
    private final Block doorBlock;
    private final Block windowBlock;
    private final Block stairBlock;
    private final Block slabBlock;

    public BuildingStyle(String name, String displayName,
                         Block wallBlock, Block floorBlock, Block roofBlock,
                         Block pillarBlock, Block doorBlock, Block windowBlock,
                         Block stairBlock, Block slabBlock) {
        this.name = name;
        this.displayName = displayName;
        this.wallBlock = wallBlock;
        this.floorBlock = floorBlock;
        this.roofBlock = roofBlock;
        this.pillarBlock = pillarBlock;
        this.doorBlock = doorBlock;
        this.windowBlock = windowBlock;
        this.stairBlock = stairBlock;
        this.slabBlock = slabBlock;
    }

    public String name() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Block getWallBlock() {
        return wallBlock;
    }

    public Block getFloorBlock() {
        return floorBlock;
    }

    public Block getRoofBlock() {
        return roofBlock;
    }

    public Block getPillarBlock() {
        return pillarBlock;
    }

    public Block getDoorBlock() {
        return doorBlock;
    }

    public Block getWindowBlock() {
        return windowBlock;
    }

    public Block getStairBlock() {
        return stairBlock;
    }

    public Block getSlabBlock() {
        return slabBlock;
    }

    /**
     * Returns a list of predefined building styles.
     * Each style uses a consistent set of materials for different building parts.
     */
    public static List<BuildingStyle> getDefaultStyles() {
        return List.of(
            // 1. oak_classic - 经典橡木屋
            new BuildingStyle("oak_classic", "Classic Oak House",
                Blocks.OAK_PLANKS, Blocks.OAK_PLANKS, Blocks.OAK_PLANKS,
                Blocks.OAK_LOG, Blocks.OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.OAK_STAIRS, Blocks.OAK_SLAB),

            // 2. spruce_cabin - 云杉小木屋
            new BuildingStyle("spruce_cabin", "Spruce Cabin",
                Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_PLANKS,
                Blocks.SPRUCE_LOG, Blocks.SPRUCE_DOOR, Blocks.GLASS_PANE,
                Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB),

            // 3. birch_cottage - 白桦小屋
            new BuildingStyle("birch_cottage", "Birch Cottage",
                Blocks.BIRCH_PLANKS, Blocks.BIRCH_PLANKS, Blocks.BIRCH_PLANKS,
                Blocks.BIRCH_LOG, Blocks.BIRCH_DOOR, Blocks.GLASS_PANE,
                Blocks.BIRCH_STAIRS, Blocks.BIRCH_SLAB),

            // 4. stone_fortress - 石砖堡垒
            new BuildingStyle("stone_fortress", "Stone Fortress",
                Blocks.STONE_BRICKS, Blocks.STONE, Blocks.STONE_BRICKS,
                Blocks.COBBLESTONE, Blocks.IRON_DOOR, Blocks.GLASS_PANE,
                Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_SLAB),

            // 5. sandstone_desert - 沙岩沙漠风
            new BuildingStyle("sandstone_desert", "Sandstone Desert",
                Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE,
                Blocks.CUT_SANDSTONE, Blocks.OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.SANDSTONE_STAIRS, Blocks.SANDSTONE_SLAB),

            // 6. dark_oak_manor - 深色橡木庄园
            new BuildingStyle("dark_oak_manor", "Dark Oak Manor",
                Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS,
                Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB),

            // 7. brick_house - 砖瓦房
            new BuildingStyle("brick_house", "Brick House",
                Blocks.BRICKS, Blocks.STONE, Blocks.BRICKS,
                Blocks.STONE_BRICKS, Blocks.IRON_DOOR, Blocks.GLASS_PANE,
                Blocks.BRICK_STAIRS, Blocks.BRICK_SLAB),

            // 8. jungle_hut - 丛林小屋
            new BuildingStyle("jungle_hut", "Jungle Hut",
                Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_PLANKS,
                Blocks.JUNGLE_LOG, Blocks.JUNGLE_DOOR, Blocks.GLASS_PANE,
                Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_SLAB)
        );
    }

    @Override
    public String toString() {
        return name + " (" + displayName + ")";
    }
}
