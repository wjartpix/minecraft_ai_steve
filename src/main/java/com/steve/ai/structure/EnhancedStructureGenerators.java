package com.steve.ai.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Enhanced structure generation system with diverse building types,
 * humanistic elements, and comprehensive outdoor spaces.
 * Each building type has unique characteristics and interior layouts.
 */
public class EnhancedStructureGenerators {
    
    private static final Random random = new Random();
    
    // Building types for variety
    public enum BuildingType {
        COZY_COTTAGE,           // 温馨小屋 - 紧凑型住宅
        FAMILY_HOUSE,           // 家庭住宅 - 多层住宅
        GRAND_VILLA,            // 豪华别墅 - 大型住宅
        FARMHOUSE,              // 农舍 - 农业建筑
        WORKSHOP,               // 工坊 - 工作空间
        LIBRARY,                // 图书馆 - 知识空间
        TAVERN,                 // 酒馆 - 社交空间
        TOWER_HOUSE,            // 塔楼住宅 - 垂直住宅
        GARDEN_PAVILION,        // 花园凉亭 - 休闲空间
        MERCHANT_SHOP           // 商铺 - 商业空间
    }
    
    // Garden types
    public enum GardenType {
        COURTYARD,      // 庭院
        FLOWER_GARDEN,  // 花园
        VEGETABLE_PLOT, // 菜园
        ORCHARD,        // 果园
        FOUNTAIN_PLAZA, // 喷泉广场
        ZEN_GARDEN      // 禅意花园
    }

    /**
     * Generate a building with the specified type and style
     */
    public static List<BlockPlacement> generate(BuildingType type, BlockPos start, int width, int height, int depth, BuildingStyle style) {
        return switch (type) {
            case COZY_COTTAGE -> buildCozyCottage(start, width, height, depth, style);
            case FAMILY_HOUSE -> buildFamilyHouse(start, width, height, depth, style);
            case GRAND_VILLA -> buildGrandVilla(start, width, height, depth, style);
            case FARMHOUSE -> buildFarmhouse(start, width, height, depth, style);
            case WORKSHOP -> buildWorkshop(start, width, height, depth, style);
            case LIBRARY -> buildLibrary(start, width, height, depth, style);
            case TAVERN -> buildTavern(start, width, height, depth, style);
            case TOWER_HOUSE -> buildTowerHouse(start, width, height, depth, style);
            case GARDEN_PAVILION -> buildGardenPavilion(start, width, height, depth, style);
            case MERCHANT_SHOP -> buildMerchantShop(start, width, height, depth, style);
        };
    }
    
    /**
     * Get a random building type, ensuring variety
     */
    public static BuildingType getRandomBuildingType() {
        BuildingType[] types = BuildingType.values();
        return types[random.nextInt(types.length)];
    }

    // ==================== COZY COTTAGE ====================
    
    private static List<BlockPlacement> buildCozyCottage(BlockPos start, int width, int height, int depth, BuildingStyle style) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        // Materials
        Block wallMaterial = style.getWallBlock();
        Block floorMaterial = style.getFloorBlock();
        Block roofMaterial = style.getRoofBlock();
        Block windowMaterial = style.getWindowBlock();
        Block doorMaterial = style.getDoorBlock();
        
        // Ensure minimum size
        width = Math.max(7, width);
        depth = Math.max(7, depth);
        height = Math.max(5, height);
        
        // Floor with pattern
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                Block floor = ((x + z) % 2 == 0) ? floorMaterial : slabMaterial(floorMaterial);
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floor));
            }
        }
        
        // Walls with door and windows
        int doorX = width / 2;
        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    if (!isEdge) continue;
                    
                    Block material = wallMaterial;
                    
                    // Door
                    if (z == 0 && x == doorX && y <= 2) {
                        material = doorMaterial;
                    }
                    // Windows - front and back
                    else if ((z == 0 || z == depth - 1) && y == 3 && (x == 2 || x == width - 3)) {
                        material = windowMaterial;
                    }
                    // Windows - sides
                    else if ((x == 0 || x == width - 1) && y == 3 && z == depth / 2) {
                        material = windowMaterial;
                    }
                    
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
        
        // Gable roof
        buildGableRoof(blocks, start, width, height, depth, roofMaterial);
        
        // Interior - cozy elements
        buildCozyInterior(blocks, start, width, height, depth, style);
        
        // Small front garden
        buildFrontGarden(blocks, start, width, doorX, style, GardenType.FLOWER_GARDEN);
        
        return blocks;
    }

    // ==================== FAMILY HOUSE ====================
    
    private static List<BlockPlacement> buildFamilyHouse(BlockPos start, int width, int height, int depth, BuildingStyle style) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = style.getWallBlock();
        Block floorMaterial = style.getFloorBlock();
        Block roofMaterial = style.getRoofBlock();
        Block windowMaterial = style.getWindowBlock();
        Block doorMaterial = style.getDoorBlock();
        
        width = Math.max(9, width);
        depth = Math.max(9, depth);
        height = Math.max(6, height);
        
        int secondFloorHeight = height / 2 + 1;
        
        // First floor
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
            }
        }
        
        // Second floor
        for (int x = 1; x < width - 1; x++) {
            for (int z = 1; z < depth - 1; z++) {
                blocks.add(new BlockPlacement(start.offset(x, secondFloorHeight, z), floorMaterial));
            }
        }
        
        // Staircase
        buildStaircase(blocks, start, width, secondFloorHeight, style.getStairBlock());
        
        // Walls
        int doorX = width / 2;
        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    if (!isEdge) continue;
                    
                    Block material = wallMaterial;
                    
                    // Door on first floor
                    if (z == 0 && x == doorX && y <= 2) {
                        material = doorMaterial;
                    }
                    // Windows - multiple on each floor
                    else if (y == 3 || y == secondFloorHeight + 2) {
                        if ((z == 0 || z == depth - 1) && x % 3 == 1 && x > 1 && x < width - 2) {
                            material = windowMaterial;
                        }
                        if ((x == 0 || x == width - 1) && z % 3 == 1 && z > 1 && z < depth - 2) {
                            material = windowMaterial;
                        }
                    }
                    
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
        
        // Complex roof
        buildComplexRoof(blocks, start, width, height, depth, roofMaterial);
        
        // Interior - family spaces
        buildFamilyInterior(blocks, start, width, height, depth, secondFloorHeight, style);
        
        // Front yard with fence
        buildFrontYard(blocks, start, width, depth, doorX, style);
        
        return blocks;
    }

    // ==================== GRAND VILLA ====================
    
    private static List<BlockPlacement> buildGrandVilla(BlockPos start, int width, int height, int depth, BuildingStyle style) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = style.getWallBlock();
        Block floorMaterial = style.getFloorBlock();
        Block accentMaterial = style.getPillarBlock();
        Block roofMaterial = style.getRoofBlock();
        Block windowMaterial = style.getWindowBlock();
        Block doorMaterial = style.getDoorBlock();
        
        width = Math.max(13, width);
        depth = Math.max(13, depth);
        height = Math.max(8, height);
        
        int secondFloorHeight = height / 2 + 2;
        
        // Grand entrance hall
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                // Checkerboard marble pattern for grand floor
                Block floor = ((x + z) % 2 == 0) ? floorMaterial : accentMaterial;
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floor));
            }
        }
        
        // Second floor
        for (int x = 2; x < width - 2; x++) {
            for (int z = 2; z < depth - 2; z++) {
                blocks.add(new BlockPlacement(start.offset(x, secondFloorHeight, z), floorMaterial));
            }
        }
        
        // Grand staircase
        buildGrandStaircase(blocks, start, width, secondFloorHeight, style.getStairBlock());
        
        // Walls with columns
        int doorX = width / 2;
        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    boolean isCorner = (x <= 1 || x >= width - 2) && (z <= 1 || z >= depth - 2);
                    if (!isEdge) continue;
                    
                    Block material = wallMaterial;
                    
                    // Columns at corners and entrance
                    if (isCorner || (z == 0 && (x == doorX - 2 || x == doorX + 2))) {
                        material = accentMaterial;
                    }
                    // Grand double doors
                    else if (z == 0 && (x == doorX - 1 || x == doorX || x == doorX + 1) && y <= 3) {
                        material = doorMaterial;
                    }
                    // Large windows
                    else if (y >= 2 && y <= height - 1) {
                        if ((x % 2 == 1 && (z == 0 || z == depth - 1)) || 
                            (z % 2 == 1 && (x == 0 || x == width - 1))) {
                            material = windowMaterial;
                        }
                    }
                    
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
        
        // Balconies on second floor
        buildBalconies(blocks, start, width, secondFloorHeight, depth, style);
        
        // Flat roof with parapet
        buildFlatRoofWithParapet(blocks, start, width, height, depth, roofMaterial);
        
        // Luxurious interior
        buildVillaInterior(blocks, start, width, height, depth, secondFloorHeight, style);
        
        // Grand garden
        buildGrandGarden(blocks, start, width, depth, doorX, style);
        
        return blocks;
    }

    // ==================== FARMHOUSE ====================
    
    private static List<BlockPlacement> buildFarmhouse(BlockPos start, int width, int height, int depth, BuildingStyle style) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = style.getWallBlock();
        Block floorMaterial = style.getFloorBlock();
        Block roofMaterial = style.getRoofBlock();
        Block windowMaterial = style.getWindowBlock();
        Block doorMaterial = style.getDoorBlock();
        
        width = Math.max(11, width);
        depth = Math.max(11, depth);
        height = Math.max(6, height);
        
        // Main house
        for (int x = 0; x < width - 4; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
            }
        }
        
        // Porch area
        for (int x = width - 4; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), slabMaterial(floorMaterial)));
            }
        }
        
        // Walls
        int doorX = (width - 4) / 2;
        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width - 4; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 5 || z == 0 || z == depth - 1);
                    if (!isEdge) continue;
                    
                    Block material = wallMaterial;
                    
                    // Door
                    if (z == 0 && x == doorX && y <= 2) {
                        material = doorMaterial;
                    }
                    // Windows
                    else if (y == 3 && (x % 3 == 1 || z == depth / 2)) {
                        material = windowMaterial;
                    }
                    
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
        
        // Porch columns and roof
        buildPorch(blocks, start, width - 4, depth, height, style);
        
        // Gambrel roof (classic farmhouse style)
        buildGambrelRoof(blocks, start, width - 4, height, depth, roofMaterial);
        
        // Farm interior
        buildFarmInterior(blocks, start, width - 4, height, depth, style);
        
        // Farm yard with vegetable plots
        buildFarmYard(blocks, start, width, depth, style);
        
        return blocks;
    }

    // ==================== WORKSHOP ====================
    
    private static List<BlockPlacement> buildWorkshop(BlockPos start, int width, int height, int depth, BuildingStyle style) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = style.getWallBlock();
        Block floorMaterial = style.getFloorBlock();
        Block roofMaterial = style.getRoofBlock();
        Block windowMaterial = style.getWindowBlock();
        Block doorMaterial = style.getDoorBlock();
        
        width = Math.max(9, width);
        depth = Math.max(9, depth);
        height = Math.max(5, height);
        
        // Stone floor for workshop
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), Blocks.STONE));
            }
        }
        
        // High walls with large windows
        int doorX = width / 2;
        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    if (!isEdge) continue;
                    
                    Block material = wallMaterial;
                    
                    // Large workshop door
                    if (z == 0 && x >= doorX - 1 && x <= doorX + 1 && y <= 3) {
                        if (y == 3) material = wallMaterial; // Door frame
                        else material = Blocks.AIR; // Open door space
                    }
                    // Large windows for natural light
                    else if (y >= 2 && y <= height - 1) {
                        if (x % 2 == 0 || z % 2 == 0) {
                            material = windowMaterial;
                        }
                    }
                    
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
        
        // Flat roof
        buildFlatRoof(blocks, start, width, height, depth, roofMaterial);
        
        // Workshop interior with crafting stations
        buildWorkshopInterior(blocks, start, width, height, depth, style);
        
        // Loading area
        buildLoadingArea(blocks, start, width, depth, doorX, style);
        
        return blocks;
    }

    // ==================== LIBRARY ====================
    
    private static List<BlockPlacement> buildLibrary(BlockPos start, int width, int height, int depth, BuildingStyle style) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = style.getWallBlock();
        Block floorMaterial = style.getFloorBlock();
        Block roofMaterial = style.getRoofBlock();
        Block windowMaterial = style.getWindowBlock();
        Block doorMaterial = style.getDoorBlock();
        
        width = Math.max(11, width);
        depth = Math.max(9, depth);
        height = Math.max(7, height);
        
        // Reading room floor
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                Block floor = (x < 3 || x > width - 4) ? Blocks.OAK_PLANKS : floorMaterial;
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floor));
            }
        }
        
        // Tall walls
        int doorX = width / 2;
        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    if (!isEdge) continue;
                    
                    Block material = wallMaterial;
                    
                    // Door
                    if (z == 0 && x == doorX && y <= 2) {
                        material = doorMaterial;
                    }
                    // Tall windows
                    else if (y >= 2 && y <= height - 1 && y != 4) {
                        if ((x % 4 == 2 && (z == 0 || z == depth - 1)) ||
                            (z == depth / 2 && (x == 0 || x == width - 1))) {
                            material = windowMaterial;
                        }
                    }
                    
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
        
        // Vaulted ceiling effect
        buildVaultedRoof(blocks, start, width, height, depth, roofMaterial);
        
        // Library interior with bookshelves
        buildLibraryInterior(blocks, start, width, height, depth, style);
        
        // Reading garden
        buildReadingGarden(blocks, start, width, depth, style);
        
        return blocks;
    }

    // ==================== TAVERN ====================
    
    private static List<BlockPlacement> buildTavern(BlockPos start, int width, int height, int depth, BuildingStyle style) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = style.getWallBlock();
        Block floorMaterial = style.getFloorBlock();
        Block roofMaterial = style.getRoofBlock();
        Block windowMaterial = style.getWindowBlock();
        Block doorMaterial = style.getDoorBlock();
        
        width = Math.max(11, width);
        depth = Math.max(11, depth);
        height = Math.max(6, height);
        
        // Common room floor
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                Block floor = (z < 4) ? Blocks.OAK_PLANKS : floorMaterial;
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floor));
            }
        }
        
        // Walls
        int doorX = width / 2;
        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    if (!isEdge) continue;
                    
                    Block material = wallMaterial;
                    
                    // Main entrance
                    if (z == 0 && x == doorX && y <= 2) {
                        material = doorMaterial;
                    }
                    // Large tavern windows
                    else if (y >= 2 && y <= 4) {
                        if ((z == 0 && x % 3 == 1) || (x == 0 || x == width - 1)) {
                            material = windowMaterial;
                        }
                    }
                    
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
        
        // Steep roof
        buildSteepRoof(blocks, start, width, height, depth, roofMaterial);
        
        // Tavern interior with bar and seating
        buildTavernInterior(blocks, start, width, height, depth, style);
        
        // Outdoor seating area
        buildOutdoorSeating(blocks, start, width, depth, doorX, style);
        
        return blocks;
    }

    // ==================== TOWER HOUSE ====================
    
    private static List<BlockPlacement> buildTowerHouse(BlockPos start, int width, int height, int depth, BuildingStyle style) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = style.getWallBlock();
        Block floorMaterial = style.getFloorBlock();
        Block roofMaterial = style.getRoofBlock();
        Block windowMaterial = style.getWindowBlock();
        Block doorMaterial = style.getDoorBlock();
        
        width = Math.max(7, width);
        depth = Math.max(7, depth);
        height = Math.max(10, height);
        
        int floors = 3;
        int floorHeight = height / floors;
        
        // Multiple floors
        for (int floor = 0; floor < floors; floor++) {
            int yBase = floor * floorHeight;
            
            // Floor
            for (int x = 1; x < width - 1; x++) {
                for (int z = 1; z < depth - 1; z++) {
                    blocks.add(new BlockPlacement(start.offset(x, yBase, z), floorMaterial));
                }
            }
            
            // Walls for this floor
            for (int y = yBase + 1; y <= yBase + floorHeight && y <= height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                        if (!isEdge) continue;
                        
                        Block material = wallMaterial;
                        
                        // Door on first floor
                        if (floor == 0 && z == 0 && x == width / 2 && y <= 2) {
                            material = doorMaterial;
                        }
                        // Windows on each floor
                        else if (y == yBase + floorHeight - 1) {
                            if ((x == 0 || x == width - 1) && z == depth / 2) {
                                material = windowMaterial;
                            }
                            if ((z == 0 || z == depth - 1) && x == width / 2) {
                                material = windowMaterial;
                            }
                        }
                        
                        blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                    }
                }
            }
        }
        
        // Spiral staircase
        buildSpiralStaircase(blocks, start, width, height, style.getStairBlock());
        
        // Pointed roof
        buildPointedRoof(blocks, start, width, height, depth, roofMaterial);
        
        // Tower interior
        buildTowerInterior(blocks, start, width, height, depth, floorHeight, style);
        
        return blocks;
    }

    // ==================== GARDEN PAVILION ====================
    
    private static List<BlockPlacement> buildGardenPavilion(BlockPos start, int width, int height, int depth, BuildingStyle style) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block floorMaterial = style.getFloorBlock();
        Block pillarMaterial = style.getPillarBlock();
        Block roofMaterial = style.getRoofBlock();
        Block fenceMaterial = style.getFenceBlock();
        
        width = Math.max(7, width);
        depth = Math.max(7, depth);
        height = Math.max(5, height);
        
        // Elevated platform
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
                // Pillars at corners
                if ((x == 1 || x == width - 2) && (z == 1 || z == depth - 2)) {
                    for (int y = -2; y < 0; y++) {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), pillarMaterial));
                    }
                }
            }
        }
        
        // Open walls with railings
        for (int y = 1; y <= 2; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    if (!isEdge) continue;
                    
                    // Railing
                    if (y == 2) {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), fenceMaterial));
                    }
                }
            }
        }
        
        // Roof pillars
        for (int y = 1; y <= height; y++) {
            blocks.add(new BlockPlacement(start.offset(1, y, 1), pillarMaterial));
            blocks.add(new BlockPlacement(start.offset(width - 2, y, 1), pillarMaterial));
            blocks.add(new BlockPlacement(start.offset(1, y, depth - 2), pillarMaterial));
            blocks.add(new BlockPlacement(start.offset(width - 2, y, depth - 2), pillarMaterial));
        }
        
        // Pagoda-style roof
        buildPagodaRoof(blocks, start, width, height, depth, roofMaterial);
        
        // Pavilion interior
        buildPavilionInterior(blocks, start, width, height, depth, style);
        
        // Surrounding garden
        buildSurroundingGarden(blocks, start, width, depth, style);
        
        return blocks;
    }

    // ==================== MERCHANT SHOP ====================
    
    private static List<BlockPlacement> buildMerchantShop(BlockPos start, int width, int height, int depth, BuildingStyle style) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = style.getWallBlock();
        Block floorMaterial = style.getFloorBlock();
        Block roofMaterial = style.getRoofBlock();
        Block windowMaterial = style.getWindowBlock();
        Block doorMaterial = style.getDoorBlock();
        Block trapdoorMaterial = style.getTrapdoorBlock();
        
        width = Math.max(9, width);
        depth = Math.max(7, depth);
        height = Math.max(5, height);
        
        // Shop floor
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
            }
        }
        
        // Shop front with display windows
        int doorX = width / 2;
        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    if (!isEdge) continue;
                    
                    Block material = wallMaterial;
                    
                    // Shop entrance
                    if (z == 0 && x == doorX && y <= 2) {
                        material = doorMaterial;
                    }
                    // Large display windows
                    else if (z == 0 && y >= 2 && y <= 3 && x != doorX) {
                        material = windowMaterial;
                        // Window sill
                        if (y == 2) {
                            blocks.add(new BlockPlacement(start.offset(x, y - 1, z - 1), trapdoorMaterial));
                        }
                    }
                    // Side windows
                    else if ((x == 0 || x == width - 1) && y == 3 && z == depth / 2) {
                        material = windowMaterial;
                    }
                    
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
        
        // Awning over shop front
        buildShopAwning(blocks, start, width, doorX, style);
        
        // Sloped roof
        buildGableRoof(blocks, start, width, height, depth, roofMaterial);
        
        // Shop interior
        buildShopInterior(blocks, start, width, height, depth, style);
        
        // Market area in front
        buildMarketArea(blocks, start, width, doorX, style);
        
        return blocks;
    }

    // ==================== HELPER METHODS ====================

    private static void buildGableRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int roofStart = height + 1;
        int peakHeight = Math.min(width, depth) / 2;
        
        for (int h = 0; h <= peakHeight; h++) {
            int y = roofStart + h;
            int inset = h;
            
            for (int x = inset; x < width - inset; x++) {
                for (int z = 0; z < depth; z++) {
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
    }

    private static void buildComplexRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int roofStart = height + 1;
        int layers = Math.min(width, depth) / 2;
        
        for (int layer = 0; layer < layers; layer++) {
            int y = roofStart + layer;
            int inset = layer;
            
            for (int x = inset; x < width - inset; x++) {
                for (int z = inset; z < depth - inset; z++) {
                    if (x == inset || x == width - 1 - inset || z == inset || z == depth - 1 - inset) {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                    }
                }
            }
        }
    }

    private static void buildFlatRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int y = height + 1;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, y, z), material));
            }
        }
    }

    private static void buildFlatRoofWithParapet(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int y = height + 1;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                // Parapet
                if (x == 0 || x == width - 1 || z == 0 || z == depth - 1) {
                    blocks.add(new BlockPlacement(start.offset(x, y + 1, z), slabMaterial(material)));
                }
            }
        }
    }

    private static void buildGambrelRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int roofPeak = height + width / 3;
        for (int x = 0; x < width; x++) {
            int distFromCenter = Math.abs(x - width / 2);
            int roofY;
            
            if (distFromCenter < width / 4) {
                roofY = roofPeak - (distFromCenter * 3 / 2);
            } else {
                roofY = roofPeak - (width / 4 * 3 / 2) - (distFromCenter - width / 4);
            }
            
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, roofY, z), material));
                for (int y = height; y < roofY; y++) {
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
    }

    private static void buildSteepRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int roofStart = height + 1;
        int peakHeight = Math.min(width, depth) / 2 + 2;
        
        for (int h = 0; h < peakHeight; h++) {
            int y = roofStart + h;
            int inset = h;
            
            for (int x = inset; x < width - inset; x++) {
                for (int z = inset; z < depth - inset; z++) {
                    if (x == inset || x == width - 1 - inset || z == inset || z == depth - 1 - inset) {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                    }
                }
            }
        }
    }

    private static void buildVaultedRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int roofStart = height + 1;
        int centerX = width / 2;
        int centerZ = depth / 2;
        
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                int distFromCenter = Math.max(Math.abs(x - centerX), Math.abs(z - centerZ));
                int roofY = roofStart + (Math.min(width, depth) / 2 - distFromCenter);
                if (roofY >= roofStart) {
                    blocks.add(new BlockPlacement(start.offset(x, roofY, z), material));
                }
            }
        }
    }

    private static void buildPointedRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int centerX = width / 2;
        int centerZ = depth / 2;
        int spireHeight = height + Math.min(width, depth);
        
        for (int y = height; y < spireHeight; y++) {
            int shrink = (y - height) / 2;
            if (centerX - shrink >= 0 && centerZ - shrink >= 0) {
                blocks.add(new BlockPlacement(start.offset(centerX - shrink, y, centerZ - shrink), material));
                blocks.add(new BlockPlacement(start.offset(centerX + shrink, y, centerZ + shrink), material));
                blocks.add(new BlockPlacement(start.offset(centerX - shrink, y, centerZ + shrink), material));
                blocks.add(new BlockPlacement(start.offset(centerX + shrink, y, centerZ - shrink), material));
            }
        }
        blocks.add(new BlockPlacement(start.offset(centerX, spireHeight, centerZ), material));
    }

    private static void buildPagodaRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int layers = 3;
        int layerHeight = height / layers;
        
        for (int layer = 0; layer < layers; layer++) {
            int y = height - (layer * layerHeight);
            int inset = layer;
            
            for (int x = inset; x < width - inset; x++) {
                for (int z = inset; z < depth - inset; z++) {
                    if (x == inset || x == width - 1 - inset || z == inset || z == depth - 1 - inset) {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                    }
                }
            }
        }
    }

    private static void buildStaircase(List<BlockPlacement> blocks, BlockPos start, int width, int height, Block stairMaterial) {
        int stairX = width - 2;
        for (int y = 1; y < height; y++) {
            int z = y;
            blocks.add(new BlockPlacement(start.offset(stairX, y, z), stairMaterial));
        }
    }

    private static void buildGrandStaircase(List<BlockPlacement> blocks, BlockPos start, int width, int height, Block stairMaterial) {
        int centerX = width / 2;
        for (int y = 1; y < height; y++) {
            int z = y;
            blocks.add(new BlockPlacement(start.offset(centerX - 1, y, z), stairMaterial));
            blocks.add(new BlockPlacement(start.offset(centerX, y, z), stairMaterial));
            blocks.add(new BlockPlacement(start.offset(centerX + 1, y, z), stairMaterial));
        }
    }

    private static void buildSpiralStaircase(List<BlockPlacement> blocks, BlockPos start, int width, int height, Block material) {
        int centerX = width / 2;
        int centerZ = width / 2;
        
        for (int y = 0; y < height; y++) {
            int rotation = y % 4;
            int stairX = centerX + (rotation == 0 ? 1 : rotation == 2 ? -1 : 0);
            int stairZ = centerZ + (rotation == 1 ? 1 : rotation == 3 ? -1 : 0);
            blocks.add(new BlockPlacement(start.offset(stairX, y, stairZ), material));
        }
    }

    private static void buildBalconies(List<BlockPlacement> blocks, BlockPos start, int width, int y, int depth, BuildingStyle style) {
        Block fenceMaterial = style.getFenceBlock();
        Block slabMaterial = slabMaterial(style.getFloorBlock());
        
        // Front balcony
        for (int x = width / 2 - 2; x <= width / 2 + 2; x++) {
            blocks.add(new BlockPlacement(start.offset(x, y, -1), slabMaterial));
            blocks.add(new BlockPlacement(start.offset(x, y + 1, -1), fenceMaterial));
        }
    }

    private static void buildPorch(List<BlockPlacement> blocks, BlockPos start, int width, int depth, int height, BuildingStyle style) {
        Block pillarMaterial = style.getPillarBlock();
        Block slabMaterial = slabMaterial(style.getFloorBlock());
        Block fenceMaterial = style.getFenceBlock();
        
        int porchX = width;
        // Porch floor
        for (int z = 0; z < depth; z++) {
            blocks.add(new BlockPlacement(start.offset(porchX, 0, z), slabMaterial));
            blocks.add(new BlockPlacement(start.offset(porchX + 1, 0, z), slabMaterial));
            blocks.add(new BlockPlacement(start.offset(porchX + 2, 0, z), slabMaterial));
        }
        
        // Porch columns
        for (int y = 1; y <= height; y++) {
            blocks.add(new BlockPlacement(start.offset(porchX, y, 0), pillarMaterial));
            blocks.add(new BlockPlacement(start.offset(porchX, y, depth - 1), pillarMaterial));
            blocks.add(new BlockPlacement(start.offset(porchX + 2, y, 0), pillarMaterial));
            blocks.add(new BlockPlacement(start.offset(porchX + 2, y, depth - 1), pillarMaterial));
        }
        
        // Porch roof
        for (int x = porchX - 1; x <= porchX + 3; x++) {
            for (int z = -1; z <= depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, height + 1, z), slabMaterial));
            }
        }
    }

    private static void buildShopAwning(List<BlockPlacement> blocks, BlockPos start, int width, int doorX, BuildingStyle style) {
        Block awningMaterial = style.getCarpetBlock();
        for (int x = 1; x < width - 1; x++) {
            blocks.add(new BlockPlacement(start.offset(x, 4, -1), awningMaterial));
            blocks.add(new BlockPlacement(start.offset(x, 4, -2), awningMaterial));
        }
    }

    // ==================== INTERIOR METHODS ====================

    private static void buildCozyInterior(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, BuildingStyle style) {
        // Bed
        blocks.add(new BlockPlacement(start.offset(2, 1, depth - 2), style.getBedBlock()));
        
        // Crafting table
        blocks.add(new BlockPlacement(start.offset(width - 2, 1, 2), style.getCraftingTableBlock()));
        
        // Furnace
        blocks.add(new BlockPlacement(start.offset(width - 2, 1, 3), style.getFurnaceBlock()));
        
        // Chest
        blocks.add(new BlockPlacement(start.offset(width - 2, 1, 4), style.getChestBlock()));
        
        // Table with flower
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, depth / 2), style.getTableBlock()));
        blocks.add(new BlockPlacement(start.offset(width / 2, 2, depth / 2), style.getDecorationBlock()));
        
        // Lighting
        blocks.add(new BlockPlacement(start.offset(width / 2, height - 1, depth / 2), style.getLanternBlock()));
        blocks.add(new BlockPlacement(start.offset(2, 2, 2), Blocks.WALL_TORCH));
    }

    private static void buildFamilyInterior(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, int secondFloorHeight, BuildingStyle style) {
        // First floor - living area
        blocks.add(new BlockPlacement(start.offset(2, 1, 2), style.getCraftingTableBlock()));
        blocks.add(new BlockPlacement(start.offset(3, 1, 2), style.getFurnaceBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 2, 1, 2), style.getChestBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 3, 1, 2), style.getChestBlock()));
        
        // Dining table
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, depth / 2), style.getTableBlock()));
        blocks.add(new BlockPlacement(start.offset(width / 2 - 1, 1, depth / 2), style.getChairBlock()));
        blocks.add(new BlockPlacement(start.offset(width / 2 + 1, 1, depth / 2), style.getChairBlock()));
        
        // Bookshelves
        for (int z = 2; z < depth - 2; z++) {
            blocks.add(new BlockPlacement(start.offset(2, 1, z), style.getBookshelfBlock()));
            blocks.add(new BlockPlacement(start.offset(2, 2, z), style.getBookshelfBlock()));
        }
        
        // Second floor - bedrooms
        blocks.add(new BlockPlacement(start.offset(2, secondFloorHeight + 1, 2), style.getBedBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 3, secondFloorHeight + 1, 2), style.getBedBlock()));
        blocks.add(new BlockPlacement(start.offset(width / 2, secondFloorHeight + 1, depth - 2), style.getBedBlock()));
        
        // Lighting
        blocks.add(new BlockPlacement(start.offset(width / 2, height - 1, depth / 2), style.getLanternBlock()));
        blocks.add(new BlockPlacement(start.offset(width / 2, secondFloorHeight + 2, depth / 2), style.getLanternBlock()));
    }

    private static void buildVillaInterior(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, int secondFloorHeight, BuildingStyle style) {
        // Grand hall chandelier
        blocks.add(new BlockPlacement(start.offset(width / 2, height - 1, depth / 2), Blocks.SEA_LANTERN));
        
        // Reception area
        for (int x = 3; x < width - 3; x++) {
            for (int z = 3; z < 6; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 1, z), style.getCarpetBlock()));
            }
        }
        
        // Grand staircase already built
        
        // Library corner
        for (int z = 2; z < depth - 2; z++) {
            blocks.add(new BlockPlacement(start.offset(2, 1, z), style.getBookshelfBlock()));
            blocks.add(new BlockPlacement(start.offset(2, 2, z), style.getBookshelfBlock()));
            blocks.add(new BlockPlacement(start.offset(2, 3, z), style.getBookshelfBlock()));
        }
        
        // Dining hall
        blocks.add(new BlockPlacement(start.offset(width - 4, 1, depth / 2), style.getTableBlock()));
        for (int i = 0; i < 4; i++) {
            blocks.add(new BlockPlacement(start.offset(width - 4, 1, depth / 2 - 2 + i), style.getChairBlock()));
        }
        
        // Second floor bedrooms
        blocks.add(new BlockPlacement(start.offset(4, secondFloorHeight + 1, 3), style.getBedBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 5, secondFloorHeight + 1, 3), style.getBedBlock()));
        blocks.add(new BlockPlacement(start.offset(4, secondFloorHeight + 1, depth - 3), style.getBedBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 5, secondFloorHeight + 1, depth - 3), style.getBedBlock()));
    }

    private static void buildFarmInterior(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, BuildingStyle style) {
        // Kitchen area
        blocks.add(new BlockPlacement(start.offset(2, 1, 2), style.getFurnaceBlock()));
        blocks.add(new BlockPlacement(start.offset(3, 1, 2), Blocks.SMOKER));
        blocks.add(new BlockPlacement(start.offset(2, 1, 3), style.getCraftingTableBlock()));
        
        // Storage chests
        for (int x = width - 3; x < width - 1; x++) {
            for (int z = 2; z < 5; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 1, z), style.getChestBlock()));
            }
        }
        
        // Dining table
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, depth / 2), style.getTableBlock()));
        
        // Beds
        blocks.add(new BlockPlacement(start.offset(2, 1, depth - 2), style.getBedBlock()));
        blocks.add(new BlockPlacement(start.offset(5, 1, depth - 2), style.getBedBlock()));
    }

    private static void buildWorkshopInterior(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, BuildingStyle style) {
        // Crafting stations
        blocks.add(new BlockPlacement(start.offset(2, 1, 2), style.getCraftingTableBlock()));
        blocks.add(new BlockPlacement(start.offset(3, 1, 2), Blocks.ANVIL));
        blocks.add(new BlockPlacement(start.offset(4, 1, 2), Blocks.SMITHING_TABLE));
        blocks.add(new BlockPlacement(start.offset(5, 1, 2), Blocks.GRINDSTONE));
        
        // Furnaces
        blocks.add(new BlockPlacement(start.offset(2, 1, depth - 2), style.getFurnaceBlock()));
        blocks.add(new BlockPlacement(start.offset(3, 1, depth - 2), Blocks.BLAST_FURNACE));
        
        // Storage
        for (int x = width - 3; x < width - 1; x++) {
            for (int z = 2; z < depth - 2; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 1, z), style.getChestBlock()));
            }
        }
        
        // Work tables
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, depth / 2), style.getTableBlock()));
    }

    private static void buildLibraryInterior(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, BuildingStyle style) {
        // Bookshelves along walls
        for (int y = 1; y <= height - 2; y++) {
            for (int z = 1; z < depth - 1; z++) {
                blocks.add(new BlockPlacement(start.offset(1, y, z), style.getBookshelfBlock()));
                blocks.add(new BlockPlacement(start.offset(width - 2, y, z), style.getBookshelfBlock()));
            }
        }
        
        // Reading tables
        for (int x = 3; x < width - 3; x += 3) {
            for (int z = 3; z < depth - 3; z += 3) {
                blocks.add(new BlockPlacement(start.offset(x, 1, z), style.getTableBlock()));
                blocks.add(new BlockPlacement(start.offset(x, 2, z), style.getDecorationBlock()));
            }
        }
        
        // Enchanting table in center
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, depth / 2), Blocks.ENCHANTING_TABLE));
        
        // Lighting
        blocks.add(new BlockPlacement(start.offset(width / 2, height - 1, depth / 2), Blocks.SEA_LANTERN));
    }

    private static void buildTavernInterior(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, BuildingStyle style) {
        // Bar counter
        for (int x = 2; x < width - 2; x++) {
            blocks.add(new BlockPlacement(start.offset(x, 1, depth - 3), style.getTableBlock()));
        }
        
        // Bar stools
        for (int x = 2; x < width - 2; x += 2) {
            blocks.add(new BlockPlacement(start.offset(x, 1, depth - 4), style.getChairBlock()));
        }
        
        // Dining tables
        for (int x = 2; x < width - 2; x += 3) {
            for (int z = 2; z < depth - 5; z += 3) {
                blocks.add(new BlockPlacement(start.offset(x, 1, z), style.getTableBlock()));
                blocks.add(new BlockPlacement(start.offset(x - 1, 1, z), style.getChairBlock()));
                blocks.add(new BlockPlacement(start.offset(x + 1, 1, z), style.getChairBlock()));
            }
        }
        
        // Fireplace
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, 1), Blocks.CAMPFIRE));
        
        // Storage
        blocks.add(new BlockPlacement(start.offset(width - 2, 1, depth - 2), style.getChestBlock()));
    }

    private static void buildTowerInterior(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, int floorHeight, BuildingStyle style) {
        // Ground floor - storage
        blocks.add(new BlockPlacement(start.offset(2, 1, 2), style.getChestBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 3, 1, 2), style.getChestBlock()));
        blocks.add(new BlockPlacement(start.offset(2, 1, depth - 3), style.getFurnaceBlock()));
        
        // Second floor - living
        blocks.add(new BlockPlacement(start.offset(2, floorHeight + 1, 2), style.getCraftingTableBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 3, floorHeight + 1, 2), style.getTableBlock()));
        blocks.add(new BlockPlacement(start.offset(width / 2, floorHeight + 1, depth / 2), style.getChairBlock()));
        
        // Third floor - bedroom
        blocks.add(new BlockPlacement(start.offset(2, floorHeight * 2 + 1, 2), style.getBedBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 3, floorHeight * 2 + 1, 2), style.getBookshelfBlock()));
    }

    private static void buildPavilionInterior(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, BuildingStyle style) {
        // Central table
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, depth / 2), style.getTableBlock()));
        
        // Seating around
        blocks.add(new BlockPlacement(start.offset(width / 2 - 2, 1, depth / 2), style.getChairBlock()));
        blocks.add(new BlockPlacement(start.offset(width / 2 + 2, 1, depth / 2), style.getChairBlock()));
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, depth / 2 - 2), style.getChairBlock()));
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, depth / 2 + 2), style.getChairBlock()));
        
        // Decorations
        blocks.add(new BlockPlacement(start.offset(width / 2, 2, depth / 2), style.getDecorationBlock()));
        
        // Lanterns
        blocks.add(new BlockPlacement(start.offset(2, height - 1, 2), style.getLanternBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 3, height - 1, 2), style.getLanternBlock()));
        blocks.add(new BlockPlacement(start.offset(2, height - 1, depth - 3), style.getLanternBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 3, height - 1, depth - 3), style.getLanternBlock()));
    }

    private static void buildShopInterior(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, BuildingStyle style) {
        // Display counter
        for (int x = 2; x < width - 2; x++) {
            blocks.add(new BlockPlacement(start.offset(x, 1, depth - 3), style.getTableBlock()));
        }
        
        // Display items (flower pots)
        for (int x = 2; x < width - 2; x += 2) {
            blocks.add(new BlockPlacement(start.offset(x, 2, depth - 3), style.getDecorationBlock()));
        }
        
        // Storage in back
        for (int x = 1; x < width - 1; x++) {
            blocks.add(new BlockPlacement(start.offset(x, 1, 1), style.getChestBlock()));
        }
        
        // Shopkeeper area
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, depth - 2), style.getCraftingTableBlock()));
    }

    // ==================== OUTDOOR SPACE METHODS ====================

    private static void buildFrontGarden(List<BlockPlacement> blocks, BlockPos start, int width, int doorX, BuildingStyle style, GardenType gardenType) {
        Block pathMaterial = style.getGardenPathBlock();
        Block plantMaterial = style.getGardenPlantBlock();
        
        // Path to door
        for (int z = -3; z < 0; z++) {
            blocks.add(new BlockPlacement(start.offset(doorX, 0, z), pathMaterial));
            blocks.add(new BlockPlacement(start.offset(doorX - 1, 0, z), pathMaterial));
            blocks.add(new BlockPlacement(start.offset(doorX + 1, 0, z), pathMaterial));
        }
        
        // Garden beds
        for (int x = 1; x < width - 1; x++) {
            if (Math.abs(x - doorX) > 2) {
                blocks.add(new BlockPlacement(start.offset(x, 0, -2), Blocks.GRASS_BLOCK));
                blocks.add(new BlockPlacement(start.offset(x, 1, -2), plantMaterial));
            }
        }
    }

    private static void buildFrontYard(List<BlockPlacement> blocks, BlockPos start, int width, int depth, int doorX, BuildingStyle style) {
        Block fenceMaterial = style.getFenceBlock();
        Block pathMaterial = style.getGardenPathBlock();
        Block treeMaterial = style.getGardenTreeBlock();
        
        // Fence
        for (int x = -2; x < width + 2; x++) {
            blocks.add(new BlockPlacement(start.offset(x, 1, -4), fenceMaterial));
        }
        for (int z = -4; z < 0; z++) {
            blocks.add(new BlockPlacement(start.offset(-2, 1, z), fenceMaterial));
            blocks.add(new BlockPlacement(start.offset(width + 1, 1, z), fenceMaterial));
        }
        
        // Path
        for (int z = -4; z < 0; z++) {
            blocks.add(new BlockPlacement(start.offset(doorX, 0, z), pathMaterial));
        }
        
        // Trees
        blocks.add(new BlockPlacement(start.offset(0, 1, -3), treeMaterial));
        blocks.add(new BlockPlacement(start.offset(width - 1, 1, -3), treeMaterial));
    }

    private static void buildGrandGarden(List<BlockPlacement> blocks, BlockPos start, int width, int depth, int doorX, BuildingStyle style) {
        Block pathMaterial = style.getGardenPathBlock();
        Block waterMaterial = style.getWaterFeatureBlock();
        Block plantMaterial = style.getGardenPlantBlock();
        Block lightMaterial = style.getOutdoorLightBlock();
        
        // Grand path
        for (int z = -6; z < 0; z++) {
            for (int x = doorX - 2; x <= doorX + 2; x++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), pathMaterial));
            }
        }
        
        // Fountain
        for (int x = doorX - 1; x <= doorX + 1; x++) {
            for (int z = -4; z <= -2; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), Blocks.STONE_BRICKS));
            }
        }
        blocks.add(new BlockPlacement(start.offset(doorX, 1, -3), waterMaterial));
        
        // Garden beds with flowers
        for (int x = 2; x < width - 2; x += 2) {
            for (int z = -6; z < -1; z += 2) {
                if (Math.abs(x - doorX) > 3 || z < -4) {
                    blocks.add(new BlockPlacement(start.offset(x, 0, z), Blocks.GRASS_BLOCK));
                    blocks.add(new BlockPlacement(start.offset(x, 1, z), plantMaterial));
                }
            }
        }
        
        // Garden lights
        blocks.add(new BlockPlacement(start.offset(2, 2, -2), lightMaterial));
        blocks.add(new BlockPlacement(start.offset(width - 3, 2, -2), lightMaterial));
        blocks.add(new BlockPlacement(start.offset(2, 2, -5), lightMaterial));
        blocks.add(new BlockPlacement(start.offset(width - 3, 2, -5), lightMaterial));
    }

    private static void buildFarmYard(List<BlockPlacement> blocks, BlockPos start, int width, int depth, BuildingStyle style) {
        // Vegetable plots
        for (int x = -4; x < 0; x++) {
            for (int z = 2; z < depth - 2; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), Blocks.FARMLAND));
                if (random.nextBoolean()) {
                    blocks.add(new BlockPlacement(start.offset(x, 1, z), Blocks.WHEAT));
                }
            }
        }
        
        // Animal pen
        for (int x = width; x < width + 4; x++) {
            for (int z = 2; z < 6; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), Blocks.GRASS_BLOCK));
            }
        }
        // Fence for animal pen
        for (int x = width; x < width + 4; x++) {
            blocks.add(new BlockPlacement(start.offset(x, 1, 2), style.getFenceBlock()));
            blocks.add(new BlockPlacement(start.offset(x, 1, 5), style.getFenceBlock()));
        }
        for (int z = 2; z <= 5; z++) {
            blocks.add(new BlockPlacement(start.offset(width, 1, z), style.getFenceBlock()));
            blocks.add(new BlockPlacement(start.offset(width + 3, 1, z), style.getFenceBlock()));
        }
        
        // Water trough
        for (int x = width + 1; x < width + 3; x++) {
            blocks.add(new BlockPlacement(start.offset(x, 0, 3), Blocks.WATER));
        }
    }

    private static void buildLoadingArea(List<BlockPlacement> blocks, BlockPos start, int width, int depth, int doorX, BuildingStyle style) {
        // Loading platform
        for (int x = doorX - 2; x <= doorX + 2; x++) {
            for (int z = depth; z < depth + 3; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), Blocks.STONE));
            }
        }
        
        // Crates/boxes
        blocks.add(new BlockPlacement(start.offset(doorX - 2, 1, depth + 1), Blocks.BARREL));
        blocks.add(new BlockPlacement(start.offset(doorX + 2, 1, depth + 1), Blocks.BARREL));
    }

    private static void buildReadingGarden(List<BlockPlacement> blocks, BlockPos start, int width, int depth, BuildingStyle style) {
        // Benches
        blocks.add(new BlockPlacement(start.offset(2, 1, depth + 1), style.getChairBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 3, 1, depth + 1), style.getChairBlock()));
        
        // Reading tables
        blocks.add(new BlockPlacement(start.offset(width / 2, 1, depth + 2), style.getTableBlock()));
        
        // Trees for shade
        blocks.add(new BlockPlacement(start.offset(1, 1, depth + 2), style.getGardenTreeBlock()));
        blocks.add(new BlockPlacement(start.offset(width - 2, 1, depth + 2), style.getGardenTreeBlock()));
    }

    private static void buildOutdoorSeating(List<BlockPlacement> blocks, BlockPos start, int width, int depth, int doorX, BuildingStyle style) {
        // Patio area
        for (int x = doorX - 3; x <= doorX + 3; x++) {
            for (int z = depth; z < depth + 4; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), style.getGardenPathBlock()));
            }
        }
        
        // Outdoor tables and chairs
        blocks.add(new BlockPlacement(start.offset(doorX - 2, 1, depth + 1), style.getTableBlock()));
        blocks.add(new BlockPlacement(start.offset(doorX - 2, 1, depth + 2), style.getChairBlock()));
        blocks.add(new BlockPlacement(start.offset(doorX + 2, 1, depth + 1), style.getTableBlock()));
        blocks.add(new BlockPlacement(start.offset(doorX + 2, 1, depth + 2), style.getChairBlock()));
        
        // Outdoor lighting
        blocks.add(new BlockPlacement(start.offset(doorX - 3, 2, depth), style.getOutdoorLightBlock()));
        blocks.add(new BlockPlacement(start.offset(doorX + 3, 2, depth), style.getOutdoorLightBlock()));
    }

    private static void buildSurroundingGarden(List<BlockPlacement> blocks, BlockPos start, int width, int depth, BuildingStyle style) {
        Block pathMaterial = style.getGardenPathBlock();
        Block plantMaterial = style.getGardenPlantBlock();
        Block treeMaterial = style.getGardenTreeBlock();
        
        // Circular path around pavilion
        int centerX = width / 2;
        int centerZ = depth / 2;
        int radius = Math.max(width, depth) / 2 + 2;
        
        for (int angle = 0; angle < 360; angle += 30) {
            double radians = Math.toRadians(angle);
            int x = (int) (centerX + Math.cos(radians) * radius);
            int z = (int) (centerZ + Math.sin(radians) * radius);
            blocks.add(new BlockPlacement(start.offset(x, 0, z), pathMaterial));
            
            // Plants along path
            int px = (int) (centerX + Math.cos(radians) * (radius + 1));
            int pz = (int) (centerZ + Math.sin(radians) * (radius + 1));
            blocks.add(new BlockPlacement(start.offset(px, 0, pz), Blocks.GRASS_BLOCK));
            blocks.add(new BlockPlacement(start.offset(px, 1, pz), plantMaterial));
        }
        
        // Trees at corners
        blocks.add(new BlockPlacement(start.offset(-2, 1, -2), treeMaterial));
        blocks.add(new BlockPlacement(start.offset(width + 1, 1, -2), treeMaterial));
        blocks.add(new BlockPlacement(start.offset(-2, 1, depth + 1), treeMaterial));
        blocks.add(new BlockPlacement(start.offset(width + 1, 1, depth + 1), treeMaterial));
    }

    private static void buildMarketArea(List<BlockPlacement> blocks, BlockPos start, int width, int doorX, BuildingStyle style) {
        // Market stalls
        for (int x = 2; x < width - 2; x += 3) {
            blocks.add(new BlockPlacement(start.offset(x, 1, -2), style.getTableBlock()));
            blocks.add(new BlockPlacement(start.offset(x, 2, -2), style.getTrapdoorBlock()));
            blocks.add(new BlockPlacement(start.offset(x, 1, -3), style.getChestBlock()));
        }
        
        // Path
        for (int z = -4; z < 0; z++) {
            blocks.add(new BlockPlacement(start.offset(doorX, 0, z), style.getGardenPathBlock()));
        }
    }

    // ==================== UTILITY METHODS ====================

    private static Block slabMaterial(Block baseBlock) {
        if (baseBlock == Blocks.OAK_PLANKS) return Blocks.OAK_SLAB;
        if (baseBlock == Blocks.SPRUCE_PLANKS) return Blocks.SPRUCE_SLAB;
        if (baseBlock == Blocks.BIRCH_PLANKS) return Blocks.BIRCH_SLAB;
        if (baseBlock == Blocks.JUNGLE_PLANKS) return Blocks.JUNGLE_SLAB;
        if (baseBlock == Blocks.ACACIA_PLANKS) return Blocks.ACACIA_SLAB;
        if (baseBlock == Blocks.DARK_OAK_PLANKS) return Blocks.DARK_OAK_SLAB;
        if (baseBlock == Blocks.CHERRY_PLANKS) return Blocks.CHERRY_SLAB;
        if (baseBlock == Blocks.BAMBOO_PLANKS) return Blocks.BAMBOO_SLAB;
        if (baseBlock == Blocks.MANGROVE_PLANKS) return Blocks.MANGROVE_SLAB;
        if (baseBlock == Blocks.CRIMSON_PLANKS) return Blocks.CRIMSON_SLAB;
        if (baseBlock == Blocks.WARPED_PLANKS) return Blocks.WARPED_SLAB;
        if (baseBlock == Blocks.STONE_BRICKS) return Blocks.STONE_BRICK_SLAB;
        if (baseBlock == Blocks.COBBLESTONE) return Blocks.COBBLESTONE_SLAB;
        if (baseBlock == Blocks.BRICKS) return Blocks.BRICK_SLAB;
        if (baseBlock == Blocks.SANDSTONE) return Blocks.SANDSTONE_SLAB;
        if (baseBlock == Blocks.NETHER_BRICKS) return Blocks.NETHER_BRICK_SLAB;
        if (baseBlock == Blocks.DEEPSLATE_BRICKS) return Blocks.DEEPSLATE_BRICK_SLAB;
        if (baseBlock == Blocks.QUARTZ_BLOCK) return Blocks.QUARTZ_SLAB;
        return Blocks.OAK_SLAB;
    }
}
