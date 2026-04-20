package com.steve.ai.structure;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.List;

/**
 * Building categories for variety in generation
 */
enum BuildingCategory {
    RESIDENTIAL,    // Houses, cottages, manors
    COMMERCIAL,     // Shops, markets, inns
    AGRICULTURAL,   // Barns, farmhouses, stables
    FORTIFIED,      // Castles, towers, walls
    RELIGIOUS,      // Churches, temples, shrines
    UTILITARIAN,    // Workshops, storage, utilities
    LUXURY          // Mansions, villas, palaces
}

/**
 * Represents a building style with predefined materials for different parts of a structure.
 * Used by structure generators and building actions to create themed constructions.
 * Enhanced with humanistic elements and garden/outdoor support.
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
    
    // Humanistic style elements - NEW
    private final Block bedBlock;
    private final Block bookshelfBlock;
    private final Block craftingTableBlock;
    private final Block furnaceBlock;
    private final Block chestBlock;
    private final Block tableBlock;
    private final Block chairBlock;
    private final Block decorationBlock;
    private final Block gardenPathBlock;
    private final Block gardenPlantBlock;
    private final Block gardenTreeBlock;
    private final Block waterFeatureBlock;
    private final Block outdoorLightBlock;
    
    // Style category for variety
    private final BuildingCategory category;

    // Full constructor with all fields
    public BuildingStyle(String name, String displayName,
                         Block wallBlock, Block floorBlock, Block roofBlock,
                         Block pillarBlock, Block doorBlock, Block windowBlock,
                         Block stairBlock, Block slabBlock,
                         Block fenceBlock, Block trapdoorBlock, Block lanternBlock,
                         Block flowerPotBlock, Block carpetBlock, Block chimneyBlock,
                         Block pathBlock, Block leavesBlock,
                         Block bedBlock, Block bookshelfBlock, Block craftingTableBlock,
                         Block furnaceBlock, Block chestBlock, Block tableBlock,
                         Block chairBlock, Block decorationBlock, Block gardenPathBlock,
                         Block gardenPlantBlock, Block gardenTreeBlock, Block waterFeatureBlock,
                         Block outdoorLightBlock, BuildingCategory category) {
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
        this.bedBlock = bedBlock;
        this.bookshelfBlock = bookshelfBlock;
        this.craftingTableBlock = craftingTableBlock;
        this.furnaceBlock = furnaceBlock;
        this.chestBlock = chestBlock;
        this.tableBlock = tableBlock;
        this.chairBlock = chairBlock;
        this.decorationBlock = decorationBlock;
        this.gardenPathBlock = gardenPathBlock;
        this.gardenPlantBlock = gardenPlantBlock;
        this.gardenTreeBlock = gardenTreeBlock;
        this.waterFeatureBlock = waterFeatureBlock;
        this.outdoorLightBlock = outdoorLightBlock;
        this.category = category;
    }

    // Simplified constructor for backward compatibility
    public BuildingStyle(String name, String displayName,
                         Block wallBlock, Block floorBlock, Block roofBlock,
                         Block pillarBlock, Block doorBlock, Block windowBlock,
                         Block stairBlock, Block slabBlock) {
        this(name, displayName, wallBlock, floorBlock, roofBlock, pillarBlock, doorBlock,
             windowBlock, stairBlock, slabBlock, 
             Blocks.OAK_FENCE, Blocks.OAK_TRAPDOOR, Blocks.LANTERN,
             Blocks.FLOWER_POT, Blocks.WHITE_CARPET, Blocks.COBBLESTONE,
             Blocks.DIRT_PATH, Blocks.OAK_LEAVES,
             Blocks.RED_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
             Blocks.FURNACE, Blocks.CHEST, Blocks.OAK_PLANKS, Blocks.OAK_STAIRS,
             Blocks.FLOWER_POT, Blocks.DIRT_PATH, Blocks.POPPY, Blocks.OAK_SAPLING,
             Blocks.WATER, Blocks.LANTERN, BuildingCategory.RESIDENTIAL);
    }

    // Constructor with decorative elements
    public BuildingStyle(String name, String displayName,
                         Block wallBlock, Block floorBlock, Block roofBlock,
                         Block pillarBlock, Block doorBlock, Block windowBlock,
                         Block stairBlock, Block slabBlock,
                         Block fenceBlock, Block trapdoorBlock, Block lanternBlock,
                         Block flowerPotBlock, Block carpetBlock, Block chimneyBlock,
                         Block pathBlock, Block leavesBlock) {
        this(name, displayName, wallBlock, floorBlock, roofBlock, pillarBlock, doorBlock,
             windowBlock, stairBlock, slabBlock, fenceBlock, trapdoorBlock, lanternBlock,
             flowerPotBlock, carpetBlock, chimneyBlock, pathBlock, leavesBlock,
             Blocks.RED_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
             Blocks.FURNACE, Blocks.CHEST, Blocks.OAK_PLANKS, Blocks.OAK_STAIRS,
             Blocks.FLOWER_POT, Blocks.DIRT_PATH, Blocks.POPPY, Blocks.OAK_SAPLING,
             Blocks.WATER, Blocks.LANTERN, BuildingCategory.RESIDENTIAL);
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

    // NEW: Humanistic element getters
    public Block getBedBlock() {
        return bedBlock != null ? bedBlock : Blocks.RED_BED;
    }

    public Block getBookshelfBlock() {
        return bookshelfBlock != null ? bookshelfBlock : Blocks.BOOKSHELF;
    }

    public Block getCraftingTableBlock() {
        return craftingTableBlock != null ? craftingTableBlock : Blocks.CRAFTING_TABLE;
    }

    public Block getFurnaceBlock() {
        return furnaceBlock != null ? furnaceBlock : Blocks.FURNACE;
    }

    public Block getChestBlock() {
        return chestBlock != null ? chestBlock : Blocks.CHEST;
    }

    public Block getTableBlock() {
        return tableBlock != null ? tableBlock : Blocks.OAK_PLANKS;
    }

    public Block getChairBlock() {
        return chairBlock != null ? chairBlock : Blocks.OAK_STAIRS;
    }

    public Block getDecorationBlock() {
        return decorationBlock != null ? decorationBlock : Blocks.FLOWER_POT;
    }

    public Block getGardenPathBlock() {
        return gardenPathBlock != null ? gardenPathBlock : Blocks.DIRT_PATH;
    }

    public Block getGardenPlantBlock() {
        return gardenPlantBlock != null ? gardenPlantBlock : Blocks.POPPY;
    }

    public Block getGardenTreeBlock() {
        return gardenTreeBlock != null ? gardenTreeBlock : Blocks.OAK_SAPLING;
    }

    public Block getWaterFeatureBlock() {
        return waterFeatureBlock != null ? waterFeatureBlock : Blocks.WATER;
    }

    public Block getOutdoorLightBlock() {
        return outdoorLightBlock != null ? outdoorLightBlock : Blocks.LANTERN;
    }

    public BuildingCategory getCategory() {
        return category != null ? category : BuildingCategory.RESIDENTIAL;
    }

    /**
     * Returns a list of predefined building styles.
     * Each style uses a consistent set of materials for different building parts.
     * Enhanced with humanistic elements and garden features.
     */
    public static List<BuildingStyle> getDefaultStyles() {
        return Arrays.asList(
            // 1. oak_classic - 经典橡木屋 (温馨家庭风)
            new BuildingStyle("oak_classic", "Classic Oak House",
                Blocks.OAK_PLANKS, Blocks.OAK_PLANKS, Blocks.OAK_PLANKS,
                Blocks.OAK_LOG, Blocks.OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.OAK_STAIRS, Blocks.OAK_SLAB,
                Blocks.OAK_FENCE, Blocks.OAK_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.WHITE_CARPET, Blocks.COBBLESTONE,
                Blocks.DIRT_PATH, Blocks.OAK_LEAVES,
                Blocks.RED_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.CHEST, Blocks.OAK_PLANKS, Blocks.OAK_STAIRS,
                Blocks.FLOWER_POT, Blocks.DIRT_PATH, Blocks.DANDELION, Blocks.OAK_SAPLING,
                Blocks.WATER, Blocks.LANTERN, BuildingCategory.RESIDENTIAL),

            // 2. spruce_cabin - 云杉小木屋 (森林隐居风)
            new BuildingStyle("spruce_cabin", "Spruce Cabin",
                Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_PLANKS,
                Blocks.SPRUCE_LOG, Blocks.SPRUCE_DOOR, Blocks.GLASS_PANE,
                Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB,
                Blocks.SPRUCE_FENCE, Blocks.SPRUCE_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.RED_CARPET, Blocks.COBBLESTONE,
                Blocks.DIRT_PATH, Blocks.SPRUCE_LEAVES,
                Blocks.BLUE_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.BLAST_FURNACE, Blocks.CHEST, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_STAIRS,
                Blocks.POTTED_SPRUCE_SAPLING, Blocks.COARSE_DIRT, Blocks.FERN, Blocks.SPRUCE_SAPLING,
                Blocks.WATER, Blocks.SOUL_LANTERN, BuildingCategory.RESIDENTIAL),

            // 3. birch_cottage - 白桦小屋 (明亮田园风)
            new BuildingStyle("birch_cottage", "Birch Cottage",
                Blocks.BIRCH_PLANKS, Blocks.BIRCH_PLANKS, Blocks.BIRCH_PLANKS,
                Blocks.BIRCH_LOG, Blocks.BIRCH_DOOR, Blocks.GLASS_PANE,
                Blocks.BIRCH_STAIRS, Blocks.BIRCH_SLAB,
                Blocks.BIRCH_FENCE, Blocks.BIRCH_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.YELLOW_CARPET, Blocks.STONE_BRICKS,
                Blocks.DIRT_PATH, Blocks.BIRCH_LEAVES,
                Blocks.YELLOW_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.SMOKER, Blocks.CHEST, Blocks.BIRCH_PLANKS, Blocks.BIRCH_STAIRS,
                Blocks.POTTED_BIRCH_SAPLING, Blocks.DIRT_PATH, Blocks.OXEYE_DAISY, Blocks.BIRCH_SAPLING,
                Blocks.WATER, Blocks.LANTERN, BuildingCategory.RESIDENTIAL),

            // 4. stone_fortress - 石砖堡垒 (坚固防御风)
            new BuildingStyle("stone_fortress", "Stone Fortress",
                Blocks.STONE_BRICKS, Blocks.STONE, Blocks.STONE_BRICKS,
                Blocks.COBBLESTONE, Blocks.IRON_DOOR, Blocks.GLASS_PANE,
                Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_SLAB,
                Blocks.COBBLESTONE_WALL, Blocks.IRON_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.GRAY_CARPET, Blocks.STONE_BRICKS,
                Blocks.STONE_BRICKS, Blocks.OAK_LEAVES,
                Blocks.WHITE_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.CHEST, Blocks.STONE_BRICKS, Blocks.STONE_BRICK_STAIRS,
                Blocks.FLOWER_POT, Blocks.STONE_BRICKS, Blocks.DEAD_BUSH, Blocks.OAK_SAPLING,
                Blocks.WATER, Blocks.TORCH, BuildingCategory.FORTIFIED),

            // 5. sandstone_desert - 沙岩沙漠风 (沙漠绿洲风)
            new BuildingStyle("sandstone_desert", "Sandstone Desert Villa",
                Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE,
                Blocks.CUT_SANDSTONE, Blocks.OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.SANDSTONE_STAIRS, Blocks.SANDSTONE_SLAB,
                Blocks.SANDSTONE_WALL, Blocks.OAK_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.ORANGE_CARPET, Blocks.CUT_SANDSTONE,
                Blocks.SAND, Blocks.ACACIA_LEAVES,
                Blocks.GREEN_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.CHEST, Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE_STAIRS,
                Blocks.POTTED_CACTUS, Blocks.SAND, Blocks.DEAD_BUSH, Blocks.ACACIA_SAPLING,
                Blocks.WATER, Blocks.TORCH, BuildingCategory.RESIDENTIAL),

            // 6. dark_oak_manor - 深色橡木庄园 (哥特优雅风)
            new BuildingStyle("dark_oak_manor", "Dark Oak Manor",
                Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS,
                Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB,
                Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.PURPLE_CARPET, Blocks.DEEPSLATE_BRICKS,
                Blocks.DIRT_PATH, Blocks.DARK_OAK_LEAVES,
                Blocks.PURPLE_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.ENDER_CHEST, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_STAIRS,
                Blocks.POTTED_DARK_OAK_SAPLING, Blocks.DIRT_PATH, Blocks.ALLIUM, Blocks.DARK_OAK_SAPLING,
                Blocks.WATER, Blocks.SOUL_LANTERN, BuildingCategory.LUXURY),

            // 7. brick_house - 砖瓦房 (工业复古风)
            new BuildingStyle("brick_house", "Brick Townhouse",
                Blocks.BRICKS, Blocks.STONE, Blocks.BRICKS,
                Blocks.STONE_BRICKS, Blocks.IRON_DOOR, Blocks.GLASS_PANE,
                Blocks.BRICK_STAIRS, Blocks.BRICK_SLAB,
                Blocks.BRICK_WALL, Blocks.IRON_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.RED_CARPET, Blocks.BRICKS,
                Blocks.STONE_BRICKS, Blocks.OAK_LEAVES,
                Blocks.RED_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.BLAST_FURNACE, Blocks.CHEST, Blocks.BRICKS, Blocks.BRICK_STAIRS,
                Blocks.POTTED_RED_TULIP, Blocks.COBBLESTONE, Blocks.RED_TULIP, Blocks.OAK_SAPLING,
                Blocks.WATER, Blocks.LANTERN, BuildingCategory.RESIDENTIAL),

            // 8. jungle_hut - 丛林小屋 (热带度假风)
            new BuildingStyle("jungle_hut", "Jungle Retreat",
                Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_PLANKS,
                Blocks.JUNGLE_LOG, Blocks.JUNGLE_DOOR, Blocks.GLASS_PANE,
                Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_SLAB,
                Blocks.JUNGLE_FENCE, Blocks.JUNGLE_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.GREEN_CARPET, Blocks.MOSSY_COBBLESTONE,
                Blocks.DIRT_PATH, Blocks.JUNGLE_LEAVES,
                Blocks.LIME_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.CHEST, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_STAIRS,
                Blocks.POTTED_JUNGLE_SAPLING, Blocks.MOSS_BLOCK, Blocks.LARGE_FERN, Blocks.JUNGLE_SAPLING,
                Blocks.WATER, Blocks.LANTERN, BuildingCategory.RESIDENTIAL),

            // 9. acacia_outpost - 金合欢前哨站 (草原牧场风)
            new BuildingStyle("acacia_outpost", "Acacia Ranch",
                Blocks.ACACIA_PLANKS, Blocks.ACACIA_PLANKS, Blocks.ACACIA_PLANKS,
                Blocks.ACACIA_LOG, Blocks.ACACIA_DOOR, Blocks.GLASS_PANE,
                Blocks.ACACIA_STAIRS, Blocks.ACACIA_SLAB,
                Blocks.ACACIA_FENCE, Blocks.ACACIA_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.ORANGE_CARPET, Blocks.TERRACOTTA,
                Blocks.RED_SAND, Blocks.ACACIA_LEAVES,
                Blocks.ORANGE_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.SMOKER, Blocks.CHEST, Blocks.ACACIA_PLANKS, Blocks.ACACIA_STAIRS,
                Blocks.POTTED_ACACIA_SAPLING, Blocks.RED_SAND, Blocks.DEAD_BUSH, Blocks.ACACIA_SAPLING,
                Blocks.WATER, Blocks.LANTERN, BuildingCategory.AGRICULTURAL),

            // 10. cherry_blossom - 樱花小屋 (日式和风)
            new BuildingStyle("cherry_blossom", "Cherry Blossom Villa",
                Blocks.CHERRY_PLANKS, Blocks.CHERRY_PLANKS, Blocks.CHERRY_PLANKS,
                Blocks.CHERRY_LOG, Blocks.CHERRY_DOOR, Blocks.GLASS_PANE,
                Blocks.CHERRY_STAIRS, Blocks.CHERRY_SLAB,
                Blocks.CHERRY_FENCE, Blocks.CHERRY_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.PINK_CARPET, Blocks.PINK_TERRACOTTA,
                Blocks.PINK_CONCRETE_POWDER, Blocks.CHERRY_LEAVES,
                Blocks.PINK_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.CHEST, Blocks.CHERRY_PLANKS, Blocks.CHERRY_STAIRS,
                Blocks.POTTED_CHERRY_SAPLING, Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_TULIP, Blocks.CHERRY_SAPLING,
                Blocks.WATER, Blocks.LANTERN, BuildingCategory.LUXURY),

            // 11. bamboo_retreat - 竹林隐居 (东方禅意风)
            new BuildingStyle("bamboo_retreat", "Bamboo Zen Retreat",
                Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_PLANKS,
                Blocks.BAMBOO_BLOCK, Blocks.BAMBOO_DOOR, Blocks.GLASS_PANE,
                Blocks.BAMBOO_STAIRS, Blocks.BAMBOO_SLAB,
                Blocks.BAMBOO_FENCE, Blocks.BAMBOO_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.LIME_CARPET, Blocks.MOSSY_STONE_BRICKS,
                Blocks.DIRT_PATH, Blocks.AZALEA_LEAVES,
                Blocks.GREEN_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.CHEST, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_STAIRS,
                Blocks.POTTED_BAMBOO, Blocks.MOSS_BLOCK, Blocks.LILY_OF_THE_VALLEY, Blocks.BAMBOO,
                Blocks.WATER, Blocks.LANTERN, BuildingCategory.RESIDENTIAL),

            // 12. mangrove_swamp - 红树林沼泽屋 (湿地生态风)
            new BuildingStyle("mangrove_swamp", "Mangrove Swamp House",
                Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_PLANKS,
                Blocks.MANGROVE_LOG, Blocks.MANGROVE_DOOR, Blocks.GLASS_PANE,
                Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_SLAB,
                Blocks.MANGROVE_FENCE, Blocks.MANGROVE_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.CYAN_CARPET, Blocks.MUD_BRICKS,
                Blocks.MUDDY_MANGROVE_ROOTS, Blocks.MANGROVE_LEAVES,
                Blocks.CYAN_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.CHEST, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_STAIRS,
                Blocks.POTTED_MANGROVE_PROPAGULE, Blocks.MUDDY_MANGROVE_ROOTS, Blocks.BLUE_ORCHID, Blocks.MANGROVE_PROPAGULE,
                Blocks.WATER, Blocks.SOUL_LANTERN, BuildingCategory.RESIDENTIAL),

            // 13. deepslate_cavern - 深板岩洞穴屋 (地下庇护所风)
            new BuildingStyle("deepslate_cavern", "Deepslate Cavern",
                Blocks.DEEPSLATE_BRICKS, Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_TILES,
                Blocks.DEEPSLATE, Blocks.IRON_DOOR, Blocks.GLASS_PANE,
                Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.DEEPSLATE_BRICK_SLAB,
                Blocks.COBBLED_DEEPSLATE_WALL, Blocks.IRON_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.BLUE_CARPET, Blocks.CRACKED_DEEPSLATE_BRICKS,
                Blocks.COBBLED_DEEPSLATE, Blocks.OAK_LEAVES,
                Blocks.BLUE_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.BLAST_FURNACE, Blocks.ENDER_CHEST, Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_BRICK_STAIRS,
                Blocks.POTTED_AZALEA, Blocks.COBBLED_DEEPSLATE, Blocks.DEAD_BUSH, Blocks.AZALEA,
                Blocks.WATER, Blocks.SOUL_LANTERN, BuildingCategory.FORTIFIED),

            // 14. nether_brick_fortress - 下界砖要塞 (地狱堡垒风)
            new BuildingStyle("nether_brick_fortress", "Nether Brick Fortress",
                Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICKS,
                Blocks.NETHER_BRICKS, Blocks.CRIMSON_DOOR, Blocks.BLACK_STAINED_GLASS_PANE,
                Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICK_SLAB,
                Blocks.NETHER_BRICK_FENCE, Blocks.CRIMSON_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.BLACK_CARPET, Blocks.NETHER_BRICKS,
                Blocks.NETHERRACK, Blocks.WARPED_ROOTS,
                Blocks.RED_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.CHEST, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_STAIRS,
                Blocks.POTTED_WARPED_ROOTS, Blocks.NETHERRACK, Blocks.CRIMSON_ROOTS, Blocks.WARPED_FUNGUS,
                Blocks.LAVA, Blocks.SOUL_LANTERN, BuildingCategory.FORTIFIED),

            // 15. warped_forest - 诡异森林屋 (异界神秘风)
            new BuildingStyle("warped_forest", "Warped Forest House",
                Blocks.WARPED_PLANKS, Blocks.WARPED_PLANKS, Blocks.WARPED_PLANKS,
                Blocks.WARPED_STEM, Blocks.WARPED_DOOR, Blocks.CYAN_STAINED_GLASS_PANE,
                Blocks.WARPED_STAIRS, Blocks.WARPED_SLAB,
                Blocks.WARPED_FENCE, Blocks.WARPED_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.CYAN_CARPET, Blocks.WARPED_NYLIUM,
                Blocks.WARPED_NYLIUM, Blocks.WARPED_WART_BLOCK,
                Blocks.CYAN_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.ENDER_CHEST, Blocks.WARPED_PLANKS, Blocks.WARPED_STAIRS,
                Blocks.POTTED_WARPED_FUNGUS, Blocks.WARPED_NYLIUM, Blocks.WARPED_ROOTS, Blocks.WARPED_FUNGUS,
                Blocks.WATER, Blocks.SOUL_LANTERN, BuildingCategory.RESIDENTIAL),

            // 16. crimson_hunting_lodge - 绯红狩猎小屋 (狩猎狂野风)
            new BuildingStyle("crimson_hunting_lodge", "Crimson Hunting Lodge",
                Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_PLANKS,
                Blocks.CRIMSON_STEM, Blocks.CRIMSON_DOOR, Blocks.RED_STAINED_GLASS_PANE,
                Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_SLAB,
                Blocks.CRIMSON_FENCE, Blocks.CRIMSON_TRAPDOOR, Blocks.SOUL_LANTERN,
                Blocks.FLOWER_POT, Blocks.RED_CARPET, Blocks.CRIMSON_NYLIUM,
                Blocks.CRIMSON_NYLIUM, Blocks.NETHER_WART_BLOCK,
                Blocks.RED_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.SMOKER, Blocks.CHEST, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_STAIRS,
                Blocks.POTTED_CRIMSON_ROOTS, Blocks.CRIMSON_NYLIUM, Blocks.CRIMSON_ROOTS, Blocks.CRIMSON_FUNGUS,
                Blocks.LAVA, Blocks.SOUL_LANTERN, BuildingCategory.AGRICULTURAL),
                
            // 17. quartz_palace - 石英宫殿 (奢华宫廷风)
            new BuildingStyle("quartz_palace", "Quartz Palace",
                Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK,
                Blocks.QUARTZ_PILLAR, Blocks.DARK_OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.QUARTZ_STAIRS, Blocks.QUARTZ_SLAB,
                Blocks.QUARTZ_BRICKS, Blocks.DARK_OAK_TRAPDOOR, Blocks.SEA_LANTERN,
                Blocks.FLOWER_POT, Blocks.WHITE_CARPET, Blocks.QUARTZ_BRICKS,
                Blocks.SMOOTH_QUARTZ, Blocks.AZALEA_LEAVES,
                Blocks.WHITE_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.FURNACE, Blocks.ENDER_CHEST, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_STAIRS,
                Blocks.POTTED_AZALEA, Blocks.SMOOTH_QUARTZ, Blocks.AZURE_BLUET, Blocks.AZALEA,
                Blocks.WATER, Blocks.SEA_LANTERN, BuildingCategory.LUXURY),
                
            // 18. copper_workshop - 铜匠工坊 (工业工匠风)
            new BuildingStyle("copper_workshop", "Copper Workshop",
                Blocks.COPPER_BLOCK, Blocks.CUT_COPPER, Blocks.CUT_COPPER,
                Blocks.WAXED_COPPER_BLOCK, Blocks.IRON_DOOR, Blocks.GLASS_PANE,
                Blocks.CUT_COPPER_STAIRS, Blocks.CUT_COPPER_SLAB,
                Blocks.IRON_BARS, Blocks.IRON_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.GRAY_CARPET, Blocks.ANVIL,
                Blocks.STONE, Blocks.OAK_LEAVES,
                Blocks.GREEN_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.BLAST_FURNACE, Blocks.CHEST, Blocks.COPPER_BLOCK, Blocks.CUT_COPPER_STAIRS,
                Blocks.POTTED_OAK_SAPLING, Blocks.COBBLESTONE, Blocks.CORNFLOWER, Blocks.OAK_SAPLING,
                Blocks.WATER, Blocks.LANTERN, BuildingCategory.UTILITARIAN),
                
            // 19. amethyst_sanctuary - 紫水晶圣所 (神秘魔法风)
            new BuildingStyle("amethyst_sanctuary", "Amethyst Sanctuary",
                Blocks.AMETHYST_BLOCK, Blocks.CALCITE, Blocks.BUDDING_AMETHYST,
                Blocks.AMETHYST_BLOCK, Blocks.DARK_OAK_DOOR, Blocks.TINTED_GLASS,
                Blocks.AMETHYST_BLOCK, Blocks.AMETHYST_BLOCK,
                Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_TRAPDOOR, Blocks.GLOW_LICHEN,
                Blocks.FLOWER_POT, Blocks.PURPLE_CARPET, Blocks.AMETHYST_BLOCK,
                Blocks.CALCITE, Blocks.AZALEA_LEAVES,
                Blocks.PURPLE_BED, Blocks.BOOKSHELF, Blocks.ENCHANTING_TABLE,
                Blocks.FURNACE, Blocks.ENDER_CHEST, Blocks.AMETHYST_BLOCK, Blocks.AMETHYST_BLOCK,
                Blocks.POTTED_AZALEA, Blocks.CALCITE, Blocks.ALLIUM, Blocks.AZALEA,
                Blocks.WATER, Blocks.GLOW_LICHEN, BuildingCategory.RELIGIOUS),
                
            // 20. honey_farmhouse - 蜂蜜农舍 (田园甜美风)
            new BuildingStyle("honey_farmhouse", "Honey Farmhouse",
                Blocks.HONEYCOMB_BLOCK, Blocks.OAK_PLANKS, Blocks.YELLOW_WOOL,
                Blocks.OAK_LOG, Blocks.OAK_DOOR, Blocks.GLASS_PANE,
                Blocks.OAK_STAIRS, Blocks.OAK_SLAB,
                Blocks.OAK_FENCE, Blocks.OAK_TRAPDOOR, Blocks.LANTERN,
                Blocks.FLOWER_POT, Blocks.YELLOW_CARPET, Blocks.HONEY_BLOCK,
                Blocks.DIRT_PATH, Blocks.OAK_LEAVES,
                Blocks.YELLOW_BED, Blocks.BOOKSHELF, Blocks.CRAFTING_TABLE,
                Blocks.SMOKER, Blocks.CHEST, Blocks.OAK_PLANKS, Blocks.OAK_STAIRS,
                Blocks.FLOWER_POT, Blocks.DIRT_PATH, Blocks.DANDELION, Blocks.OAK_SAPLING,
                Blocks.WATER, Blocks.LANTERN, BuildingCategory.AGRICULTURAL)
        );
    }

    @Override
    public String toString() {
        return name + " (" + displayName + ")";
    }
}
