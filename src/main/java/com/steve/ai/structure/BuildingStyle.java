package com.steve.ai.structure;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
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
    
    // Decorative elements for more creative buildings
    private final Block fenceBlock;
    private final Block trapdoorBlock;
    private final Block lanternBlock;
    private final Block flowerPotBlock;
    private final Block carpetBlock;
    private final Block chimneyBlock;
    private final Block pathBlock;
    private final Block leavesBlock;

    public BuildingStyle(String name, String displayName,
                         Block wallBlock, Block floorBlock, Block roofBlock,
                         Block pillarBlock, Block doorBlock, Block windowBlock,
                         Block stairBlock, Block slabBlock) {
        this(name, displayName, wallBlock, floorBlock, roofBlock, pillarBlock, doorBlock, 
             windowBlock, stairBlock, slabBlock, null, null, null, null, null, null, null, null);
    }

    public BuildingStyle(String name, String displayName,
                         Block wallBlock, Block floorBlock, Block roofBlock,
                         Block pillarBlock, Block doorBlock, Block windowBlock,
                         Block stairBlock, Block slabBlock,
                         Block fenceBlock, Block trapdoorBlock, Block lanternBlock,
                         Block flowerPotBlock, Block carpetBlock, Block chimneyBlock,
                         Block pathBlock, Block leavesBlock) {
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
        this.fenceBlock = fenceBlock;
        this.trapdoorBlock = trapdoorBlock;
        this.lanternBlock = lanternBlock;
        this.flowerPotBlock = flowerPotBlock;
        this.carpetBlock = carpetBlock;
        this.chimneyBlock = chimneyBlock;
        this.pathBlock = pathBlock;
        this.leavesBlock = leavesBlock;
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

    public Block getFenceBlock() {
        return fenceBlock != null ? fenceBlock : Blocks.OAK_FENCE;
    }

    public Block getTrapdoorBlock() {
        return trapdoorBlock != null ? trapdoorBlock : Blocks.OAK_TRAPDOOR;
    }

    public Block getLanternBlock() {
        return lanternBlock != null ? lanternBlock : Blocks.LANTERN;
    }

    public Block getFlowerPotBlock() {
        return flowerPotBlock != null ? flowerPotBlock : Blocks.FLOWER_POT;
    }

    public Block getCarpetBlock() {
        return carpetBlock != null ? carpetBlock : Blocks.WHITE_CARPET;
    }

    public Block getChimneyBlock() {
        return chimneyBlock != null ? chimneyBlock : Blocks.COBBLESTONE;
    }

    public Block getPathBlock() {
        return pathBlock != null ? pathBlock : Blocks.DIRT_PATH;
    }

    public Block getLeavesBlock() {
        return leavesBlock != null ? leavesBlock : Blocks.OAK_LEAVES;
    }

    /**
     * Returns a list of predefined building styles.
     * Each style uses a consistent set of materials for different building parts.
     */
    public static List<BuildingStyle> getDefaultStyles() {
        return Arrays.asList(
            // 1. oak_classic - 经典橡木屋 (带完整装饰)
            new BuildingStyle("oak_classic", "Classic Oak House",
                Blocks.OAK_PLANKS, Blocks.OAK_PLANKS, Blocks.OAK_PLANKS,
                Blocks.OAK_LOG, Blocks.OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.OAK_STAIRS, Blocks.OAK_SLAB,
                Blocks.OAK_FENCE, Blocks.OAK_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.WHITE_CARPET, Blocks.COBBLESTONE,
                Blocks.DIRT_PATH, Blocks.OAK_LEAVES),

            // 2. spruce_cabin - 云杉小木屋 (带完整装饰)
            new BuildingStyle("spruce_cabin", "Spruce Cabin",
                Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_PLANKS,
                Blocks.SPRUCE_LOG, Blocks.SPRUCE_DOOR, Blocks.GLASS_PANE,
                Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB,
                Blocks.SPRUCE_FENCE, Blocks.SPRUCE_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.RED_CARPET, Blocks.COBBLESTONE,
                Blocks.DIRT_PATH, Blocks.SPRUCE_LEAVES),

            // 3. birch_cottage - 白桦小屋 (带完整装饰)
            new BuildingStyle("birch_cottage", "Birch Cottage",
                Blocks.BIRCH_PLANKS, Blocks.BIRCH_PLANKS, Blocks.BIRCH_PLANKS,
                Blocks.BIRCH_LOG, Blocks.BIRCH_DOOR, Blocks.GLASS_PANE,
                Blocks.BIRCH_STAIRS, Blocks.BIRCH_SLAB,
                Blocks.BIRCH_FENCE, Blocks.BIRCH_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.YELLOW_CARPET, Blocks.STONE_BRICKS,
                Blocks.DIRT_PATH, Blocks.BIRCH_LEAVES),

            // 4. stone_fortress - 石砖堡垒 (带完整装饰)
            new BuildingStyle("stone_fortress", "Stone Fortress",
                Blocks.STONE_BRICKS, Blocks.STONE, Blocks.STONE_BRICKS,
                Blocks.COBBLESTONE, Blocks.IRON_DOOR, Blocks.GLASS_PANE,
                Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_SLAB,
                Blocks.COBBLESTONE_WALL, Blocks.IRON_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.GRAY_CARPET, Blocks.STONE_BRICKS,
                Blocks.STONE_BRICKS, Blocks.OAK_LEAVES),

            // 5. sandstone_desert - 沙岩沙漠风 (带完整装饰)
            new BuildingStyle("sandstone_desert", "Sandstone Desert",
                Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE,
                Blocks.CUT_SANDSTONE, Blocks.OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.SANDSTONE_STAIRS, Blocks.SANDSTONE_SLAB,
                Blocks.SANDSTONE_WALL, Blocks.OAK_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.ORANGE_CARPET, Blocks.CUT_SANDSTONE,
                Blocks.SAND, Blocks.ACACIA_LEAVES),

            // 6. dark_oak_manor - 深色橡木庄园 (带完整装饰)
            new BuildingStyle("dark_oak_manor", "Dark Oak Manor",
                Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS,
                Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB,
                Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.PURPLE_CARPET, Blocks.DEEPSLATE_BRICKS,
                Blocks.DIRT_PATH, Blocks.DARK_OAK_LEAVES),

            // 7. brick_house - 砖瓦房 (带完整装饰)
            new BuildingStyle("brick_house", "Brick House",
                Blocks.BRICKS, Blocks.STONE, Blocks.BRICKS,
                Blocks.STONE_BRICKS, Blocks.IRON_DOOR, Blocks.GLASS_PANE,
                Blocks.BRICK_STAIRS, Blocks.BRICK_SLAB,
                Blocks.BRICK_WALL, Blocks.IRON_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.RED_CARPET, Blocks.BRICKS,
                Blocks.STONE_BRICKS, Blocks.OAK_LEAVES),

            // 8. jungle_hut - 丛林小屋 (带完整装饰)
            new BuildingStyle("jungle_hut", "Jungle Hut",
                Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_PLANKS,
                Blocks.JUNGLE_LOG, Blocks.JUNGLE_DOOR, Blocks.GLASS_PANE,
                Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_SLAB,
                Blocks.JUNGLE_FENCE, Blocks.JUNGLE_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.GREEN_CARPET, Blocks.MOSSY_COBBLESTONE,
                Blocks.DIRT_PATH, Blocks.JUNGLE_LEAVES),

            // 9. acacia_outpost - 金合欢前哨站 (新风格)
            new BuildingStyle("acacia_outpost", "Acacia Outpost",
                Blocks.ACACIA_PLANKS, Blocks.ACACIA_PLANKS, Blocks.ACACIA_PLANKS,
                Blocks.ACACIA_LOG, Blocks.ACACIA_DOOR, Blocks.GLASS_PANE,
                Blocks.ACACIA_STAIRS, Blocks.ACACIA_SLAB,
                Blocks.ACACIA_FENCE, Blocks.ACACIA_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.ORANGE_CARPET, Blocks.TERRACOTTA,
                Blocks.RED_SAND, Blocks.ACACIA_LEAVES),

            // 10. cherry_blossom - 樱花小屋 (新风格)
            new BuildingStyle("cherry_blossom", "Cherry Blossom House",
                Blocks.CHERRY_PLANKS, Blocks.CHERRY_PLANKS, Blocks.CHERRY_PLANKS,
                Blocks.CHERRY_LOG, Blocks.CHERRY_DOOR, Blocks.GLASS_PANE,
                Blocks.CHERRY_STAIRS, Blocks.CHERRY_SLAB,
                Blocks.CHERRY_FENCE, Blocks.CHERRY_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.PINK_CARPET, Blocks.PINK_TERRACOTTA,
                Blocks.PINK_CONCRETE_POWDER, Blocks.CHERRY_LEAVES),

            // 11. bamboo_retreat - 竹林隐居 (新风格)
            new BuildingStyle("bamboo_retreat", "Bamboo Retreat",
                Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_PLANKS,
                Blocks.BAMBOO_BLOCK, Blocks.BAMBOO_DOOR, Blocks.GLASS_PANE,
                Blocks.BAMBOO_STAIRS, Blocks.BAMBOO_SLAB,
                Blocks.BAMBOO_FENCE, Blocks.BAMBOO_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.LIME_CARPET, Blocks.MOSSY_STONE_BRICKS,
                Blocks.DIRT_PATH, Blocks.AZALEA_LEAVES),

            // 12. mangrove_swamp - 红树林沼泽屋 (新风格)
            new BuildingStyle("mangrove_swamp", "Mangrove Swamp House",
                Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_PLANKS,
                Blocks.MANGROVE_LOG, Blocks.MANGROVE_DOOR, Blocks.GLASS_PANE,
                Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_SLAB,
                Blocks.MANGROVE_FENCE, Blocks.MANGROVE_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.CYAN_CARPET, Blocks.MUD_BRICKS,
                Blocks.MUDDY_MANGROVE_ROOTS, Blocks.MANGROVE_LEAVES),

            // 13. deepslate_cavern - 深板岩洞穴屋 (新风格)
            new BuildingStyle("deepslate_cavern", "Deepslate Cavern",
                Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_TILES,
                Blocks.DEEPSLATE, Blocks.IRON_DOOR, Blocks.GLASS_PANE,
                Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.DEEPSLATE_BRICK_SLAB,
                Blocks.COBBLED_DEEPSLATE_WALL, Blocks.IRON_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.BLUE_CARPET, Blocks.CRACKED_DEEPSLATE_BRICKS,
                Blocks.COBBLED_DEEPSLATE, Blocks.OAK_LEAVES),

            // 14. nether_brick_fortress - 下界砖要塞 (新风格)
            new BuildingStyle("nether_brick_fortress", "Nether Brick Fortress",
                Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS,
                Blocks.NETHER_BRICKS, Blocks.CRIMSON_DOOR, Blocks.BLACK_STAINED_GLASS_PANE,
                Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICK_SLAB,
                Blocks.NETHER_BRICK_FENCE, Blocks.CRIMSON_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.BLACK_CARPET, Blocks.NETHER_BRICKS,
                Blocks.NETHERRACK, Blocks.WARPED_ROOTS),

            // 15. warped_forest - 诡异森林屋 (新风格)
            new BuildingStyle("warped_forest", "Warped Forest House",
                Blocks.WARPED_PLANKS, Blocks.WARPED_PLANKS, Blocks.WARPED_PLANKS,
                Blocks.WARPED_STEM, Blocks.WARPED_DOOR, Blocks.CYAN_STAINED_GLASS_PANE,
                Blocks.WARPED_STAIRS, Blocks.WARPED_SLAB,
                Blocks.WARPED_FENCE, Blocks.WARPED_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.CYAN_CARPET, Blocks.WARPED_NYLIUM,
                Blocks.WARPED_NYLIUM, Blocks.WARPED_WART_BLOCK),

            // 16. crimson_hunting_lodge - 绯红狩猎小屋 (新风格)
            new BuildingStyle("crimson_hunting_lodge", "Crimson Hunting Lodge",
                Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_PLANKS,
                Blocks.CRIMSON_STEM, Blocks.CRIMSON_DOOR, Blocks.RED_STAINED_GLASS_PANE,
                Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_SLAB,
                Blocks.CRIMSON_FENCE, Blocks.CRIMSON_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.RED_CARPET, Blocks.CRIMSON_NYLIUM,
                Blocks.CRIMSON_NYLIUM, Blocks.NETHER_WART_BLOCK)
        );
    }

    @Override
    public String toString() {
        return name + " (" + displayName + ")";
    }
}
