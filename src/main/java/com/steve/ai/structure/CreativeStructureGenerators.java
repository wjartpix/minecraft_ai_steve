package com.steve.ai.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creative and diverse structure generation system.
 * Each building type has multiple variations in shape, material, and decoration.
 */
public class CreativeStructureGenerators {
    
    private static final Random random = new Random();
    
    // Roof types for variety
    private enum RoofType {
        PYRAMID, GABLE, FLAT, STEEP, ROUNDED, TOWERED
    }
    
    // House layout types
    private enum HouseLayout {
        RECTANGLE, L_SHAPE, T_SHAPE, U_SHAPE, SQUARE_WITH_COURTYARD
    }

    public static List<BlockPlacement> generate(String structureType, BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        return switch (structureType.toLowerCase()) {
            case "house", "home" -> buildCreativeHouse(start, width, height, depth, style, materials);
            case "castle", "catle", "fort" -> buildCreativeCastle(start, width, height, depth, style, materials);
            case "tower" -> buildCreativeTower(start, width, height, style, materials);
            case "wall" -> buildCreativeWall(start, width, height, style, materials);
            case "platform" -> buildCreativePlatform(start, width, depth, style, materials);
            case "barn", "shed" -> buildCreativeBarn(start, width, height, depth, style, materials);
            case "modern", "modern_house" -> buildCreativeModernHouse(start, width, height, depth, style, materials);
            case "box", "cube" -> buildCreativeBox(start, width, height, depth, style, materials);
            default -> buildCreativeHouse(start, Math.max(5, width), Math.max(4, height), Math.max(5, depth), style, materials);
        };
    }

    // ==================== CREATIVE HOUSE ====================
    
    private static List<BlockPlacement> buildCreativeHouse(BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        // Materials with variation
        Block[] wallMaterials = selectWallMaterials(style);
        Block floorMaterial = style != null ? style.getFloorBlock() : Blocks.OAK_PLANKS;
        Block roofMaterial = style != null ? style.getRoofBlock() : Blocks.OAK_PLANKS;
        Block windowMaterial = style != null ? style.getWindowBlock() : Blocks.GLASS_PANE;
        Block doorMaterial = style != null ? style.getDoorBlock() : Blocks.OAK_DOOR;
        Block fenceMaterial = style != null ? style.getFenceBlock() : Blocks.OAK_FENCE;
        Block lanternMaterial = style != null ? style.getLanternBlock() : Blocks.LANTERN;
        
        // Random design choices
        HouseLayout layout = HouseLayout.values()[random.nextInt(HouseLayout.values().length)];
        RoofType roofType = RoofType.values()[random.nextInt(RoofType.values().length)];
        boolean hasSecondFloor = width >= 7 && depth >= 7 && random.nextBoolean();
        boolean hasPorch = random.nextBoolean();
        boolean hasChimney = random.nextBoolean();
        boolean hasBayWindow = random.nextBoolean();
        boolean hasDeck = width >= 8 && random.nextBoolean();
        int doorX = width / 2 + random.nextInt(3) - 1;
        
        // Build based on layout
        switch (layout) {
            case RECTANGLE -> buildRectangularHouse(blocks, start, width, height, depth, wallMaterials, floorMaterial, roofMaterial, windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType, hasSecondFloor, hasPorch, hasChimney, hasBayWindow, hasDeck, doorX);
            case L_SHAPE -> buildLShapedHouse(blocks, start, width, height, depth, wallMaterials, floorMaterial, roofMaterial, windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType, hasSecondFloor, hasPorch, hasChimney, doorX);
            case T_SHAPE -> buildTShapedHouse(blocks, start, width, height, depth, wallMaterials, floorMaterial, roofMaterial, windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType, hasSecondFloor, hasPorch, hasChimney, doorX);
            case U_SHAPE -> buildUShapedHouse(blocks, start, width, height, depth, wallMaterials, floorMaterial, roofMaterial, windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType, hasSecondFloor, hasPorch, hasChimney, doorX);
            case SQUARE_WITH_COURTYARD -> buildCourtyardHouse(blocks, start, width, height, depth, wallMaterials, floorMaterial, roofMaterial, windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType, hasSecondFloor, hasPorch, hasChimney, doorX);
        }
        
        return blocks;
    }
    
    private static Block[] selectWallMaterials(BuildingStyle style) {
        Block base = style != null ? style.getWallBlock() : Blocks.OAK_PLANKS;
        Block accent = style != null ? style.getPillarBlock() : Blocks.OAK_LOG;
        return new Block[]{base, accent, mixMaterials(base, accent)};
    }
    
    private static Block mixMaterials(Block a, Block b) {
        // Randomly choose between two materials for variety
        return random.nextBoolean() ? a : b;
    }
    
    private static void buildRectangularHouse(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth,
            Block[] wallMaterials, Block floorMaterial, Block roofMaterial, Block windowMaterial, Block doorMaterial,
            Block fenceMaterial, Block lanternMaterial, RoofType roofType, boolean hasSecondFloor, boolean hasPorch,
            boolean hasChimney, boolean hasBayWindow, boolean hasDeck, int doorX) {
        
        // Ensure minimum interior space - if too small, expand
        int minInteriorWidth = 6;
        int minInteriorDepth = 6;
        if (width < minInteriorWidth + 2) width = minInteriorWidth + 2;
        if (depth < minInteriorDepth + 2) depth = minInteriorDepth + 2;
        
        int secondFloorHeight = height;
        int totalHeight = hasSecondFloor ? height * 2 : height;
        
        // Floor with pattern
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                // Create floor pattern - checkerboard or border
                Block floorBlock = floorMaterial;
                if (x == 0 || x == width - 1 || z == 0 || z == depth - 1) {
                    floorBlock = wallMaterials[1]; // Border with accent material
                } else if ((x + z) % 2 == 0) {
                    floorBlock = slabMaterial(floorMaterial); // Checkerboard pattern
                }
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floorBlock));
            }
        }
        
        // Second floor if present
        if (hasSecondFloor) {
            for (int x = 1; x < width - 1; x++) {
                for (int z = 1; z < depth - 1; z++) {
                    blocks.add(new BlockPlacement(start.offset(x, secondFloorHeight, z), floorMaterial));
                }
            }
            
            // Proper staircase with landing - ensures access to second floor
            int stairX = width / 2;
            int stairZ = 2;
            
            // Build staircase going up along Z axis
            for (int step = 0; step < secondFloorHeight; step++) {
                int y = 1 + step;
                int z = stairZ + step;
                
                // Place stair block
                blocks.add(new BlockPlacement(start.offset(stairX, y, z), Blocks.OAK_STAIRS));
                
                // Clear space above stair for headroom
                blocks.add(new BlockPlacement(start.offset(stairX, y + 1, z), Blocks.AIR));
                
                // Also clear side spaces for wider stairs feel
                if (stairX > 1) {
                    blocks.add(new BlockPlacement(start.offset(stairX - 1, y, z), Blocks.AIR));
                }
                if (stairX < width - 2) {
                    blocks.add(new BlockPlacement(start.offset(stairX + 1, y, z), Blocks.AIR));
                }
            }
            
            // Create opening in second floor for stairwell
            for (int step = 0; step < secondFloorHeight; step++) {
                int z = stairZ + step;
                // Clear the floor above the stairs
                blocks.add(new BlockPlacement(start.offset(stairX, secondFloorHeight, z), Blocks.AIR));
                if (stairX > 1) {
                    blocks.add(new BlockPlacement(start.offset(stairX - 1, secondFloorHeight, z), Blocks.AIR));
                }
                if (stairX < width - 2) {
                    blocks.add(new BlockPlacement(start.offset(stairX + 1, secondFloorHeight, z), Blocks.AIR));
                }
            }
            
            // Landing platform at top of stairs on second floor
            int landingZ = stairZ + secondFloorHeight;
            if (landingZ < depth - 1) {
                blocks.add(new BlockPlacement(start.offset(stairX, secondFloorHeight, landingZ), floorMaterial));
            }
        }
        
        // Interior decorations - before walls so we don't overwrite
        buildInteriorDecorations(blocks, start, width, height, depth, totalHeight, hasSecondFloor, secondFloorHeight);
        
        // Walls with pattern - only outer shell, ensuring interior space
        for (int y = 1; y <= totalHeight; y++) {
            boolean isSecondFloor = hasSecondFloor && y > secondFloorHeight;
            
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    boolean isCorner = (x == 0 || x == width - 1) && (z == 0 || z == depth - 1);
                    
                    // Skip interior - only build walls on edges
                    if (!isEdge) continue;
                    
                    Block material = selectWallPattern(wallMaterials, x, y, z, isCorner);
                    
                    // Door on first floor - ensure proper placement
                    if (z == 0 && x == doorX && y <= 2) {
                        if (y == 1) {
                            // Door bottom
                            blocks.add(new BlockPlacement(start.offset(x, y, z), doorMaterial));
                        } else if (y == 2) {
                            // Door top
                            blocks.add(new BlockPlacement(start.offset(x, y, z), doorMaterial));
                        }
                    }
                    // Windows - more windows for better lighting
                    else if (shouldPlaceWindow(x, y, z, width, totalHeight, depth, isSecondFloor, hasSecondFloor)) {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), windowMaterial));
                    }
                    else {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                    }
                }
            }
        }
        
        // Bay window
        if (hasBayWindow) {
            int bayX = width / 2;
            int bayZ = depth - 1;
            for (int y = 1; y <= 3; y++) {
                for (int x = bayX - 1; x <= bayX + 1; x++) {
                    blocks.add(new BlockPlacement(start.offset(x, y, bayZ + 1), wallMaterials[0]));
                    if (x == bayX) {
                        blocks.add(new BlockPlacement(start.offset(x, y, bayZ + 1), windowMaterial));
                    }
                }
            }
            // Bay window roof
            for (int x = bayX - 1; x <= bayX + 1; x++) {
                blocks.add(new BlockPlacement(start.offset(x, 4, bayZ + 1), roofMaterial));
            }
        }
        
        // Porch
        if (hasPorch) {
            buildPorch(blocks, start, doorX, width, wallMaterials[0], fenceMaterial, lanternMaterial);
        }
        
        // Deck
        if (hasDeck) {
            buildDeck(blocks, start, width, depth, wallMaterials[0], fenceMaterial);
        }
        
        // Chimney
        if (hasChimney) {
            buildChimney(blocks, start, width, depth, totalHeight, wallMaterials[1]);
        }
        
        // Roof based on type
        buildRoof(blocks, start, width, totalHeight, depth, roofMaterial, roofType);
        
        // Exterior decorations
        buildExteriorDecorations(blocks, start, width, totalHeight, depth, doorX, lanternMaterial, fenceMaterial);
    }
    
    private static void buildInteriorDecorations(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, 
            int totalHeight, boolean hasSecondFloor, int secondFloorHeight) {
        int interiorStartX = 1;
        int interiorEndX = width - 1;
        int interiorStartZ = 1;
        int interiorEndZ = depth - 1;
        
        // Only decorate if there's enough interior space
        if (interiorEndX - interiorStartX < 3 || interiorEndZ - interiorStartZ < 3) return;
        
        int centerX = width / 2;
        int centerZ = depth / 2;
        
        // Floor 1 decorations
        int floor1Height = hasSecondFloor ? secondFloorHeight : totalHeight;
        
        // Central table or feature
        blocks.add(new BlockPlacement(start.offset(centerX, 1, centerZ), Blocks.OAK_PLANKS));
        blocks.add(new BlockPlacement(start.offset(centerX, 2, centerZ), Blocks.FLOWER_POT));
        
        // Wall decorations - use decorative blocks (signs, banners, or wall-mounted lights)
        Block[] wallDecorations = {Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN};
        for (int x = interiorStartX; x < interiorEndX; x += 3) {
            if (x != centerX) {
                blocks.add(new BlockPlacement(start.offset(x, 2, interiorStartZ), 
                    wallDecorations[random.nextInt(wallDecorations.length)]));
            }
        }
        
        // Lighting - chandeliers or wall sconces
        blocks.add(new BlockPlacement(start.offset(centerX, floor1Height - 1, centerZ), Blocks.LANTERN));
        blocks.add(new BlockPlacement(start.offset(2, 2, 2), Blocks.WALL_TORCH));
        blocks.add(new BlockPlacement(start.offset(width - 3, 2, 2), Blocks.WALL_TORCH));
        blocks.add(new BlockPlacement(start.offset(2, 2, depth - 3), Blocks.WALL_TORCH));
        blocks.add(new BlockPlacement(start.offset(width - 3, 2, depth - 3), Blocks.WALL_TORCH));
        
        // Furniture - crafting table, chests, furnace
        blocks.add(new BlockPlacement(start.offset(2, 1, depth - 3), Blocks.CRAFTING_TABLE));
        blocks.add(new BlockPlacement(start.offset(3, 1, depth - 3), Blocks.CHEST));
        blocks.add(new BlockPlacement(start.offset(width - 3, 1, 2), Blocks.FURNACE));
        
        // Additional chests for storage
        blocks.add(new BlockPlacement(start.offset(width - 3, 1, 3), Blocks.CHEST));
        
        // Flower pots and plants
        blocks.add(new BlockPlacement(start.offset(2, 1, 2), Blocks.FLOWER_POT));
        blocks.add(new BlockPlacement(start.offset(width - 3, 1, depth - 3), Blocks.POTTED_OAK_SAPLING));
        
        // Bookshelves for library feel
        if (width >= 8) {
            for (int z = 2; z < depth - 2 && z < 5; z++) {
                blocks.add(new BlockPlacement(start.offset(2, 1, z), Blocks.BOOKSHELF));
                blocks.add(new BlockPlacement(start.offset(2, 2, z), Blocks.BOOKSHELF));
            }
        }
        
        // Carpets for decoration - central area
        Block[] carpetColors = {Blocks.RED_CARPET, Blocks.BLUE_CARPET, Blocks.GREEN_CARPET, Blocks.YELLOW_CARPET};
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                if (x != centerX || z != centerZ) { // Don't overwrite central table
                    blocks.add(new BlockPlacement(start.offset(x, 1, z), carpetColors[random.nextInt(carpetColors.length)]));
                }
            }
        }
        
        // Second floor decorations
        if (hasSecondFloor) {
            int floor2Y = secondFloorHeight;
            
            // Bed with proper placement - place foot of bed first, then head
            int bedX = width - 3;
            int bedZ = 2;
            // Bed foot (lower part)
            blocks.add(new BlockPlacement(start.offset(bedX, floor2Y + 1, bedZ), Blocks.RED_BED));
            // Bed head (upper part) - player sleeps with head at higher Z
            blocks.add(new BlockPlacement(start.offset(bedX, floor2Y + 1, bedZ + 1), Blocks.RED_BED));
            
            // Nightstand with lantern next to bed
            blocks.add(new BlockPlacement(start.offset(bedX + 1, floor2Y + 1, bedZ), Blocks.CHEST));
            blocks.add(new BlockPlacement(start.offset(bedX + 1, floor2Y + 2, bedZ), Blocks.LANTERN));
            
            // Additional chest for storage
            blocks.add(new BlockPlacement(start.offset(bedX, floor2Y + 1, bedZ + 2), Blocks.CHEST));
            
            // Window flower boxes
            blocks.add(new BlockPlacement(start.offset(2, floor2Y + 1, 1), Blocks.FLOWER_POT));
            blocks.add(new BlockPlacement(start.offset(width - 3, floor2Y + 1, 1), Blocks.FLOWER_POT));
            
            // Second floor lighting
            blocks.add(new BlockPlacement(start.offset(centerX, floor2Y + 2, centerZ), Blocks.LANTERN));
            blocks.add(new BlockPlacement(start.offset(2, floor2Y + 2, 2), Blocks.WALL_TORCH));
            blocks.add(new BlockPlacement(start.offset(width - 3, floor2Y + 2, 2), Blocks.WALL_TORCH));
            
            // Small seating area on second floor
            blocks.add(new BlockPlacement(start.offset(2, floor2Y + 1, depth - 3), Blocks.OAK_STAIRS));
            blocks.add(new BlockPlacement(start.offset(3, floor2Y + 1, depth - 3), Blocks.OAK_STAIRS));
        }
    }
    
    private static void buildExteriorDecorations(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth,
            int doorX, Block lanternMaterial, Block fenceMaterial) {
        // Exterior lanterns
        blocks.add(new BlockPlacement(start.offset(doorX - 1, 2, -1), lanternMaterial));
        blocks.add(new BlockPlacement(start.offset(doorX + 1, 2, -1), lanternMaterial));
        
        // Flower boxes under windows
        for (int x = 2; x < width - 2; x += 3) {
            if (x != doorX) {
                blocks.add(new BlockPlacement(start.offset(x, 1, -1), Blocks.FLOWER_POT));
            }
        }
        for (int x = 2; x < width - 2; x += 3) {
            blocks.add(new BlockPlacement(start.offset(x, 1, depth), Blocks.FLOWER_POT));
        }
        
        // Side garden patches
        blocks.add(new BlockPlacement(start.offset(-1, 0, 2), Blocks.GRASS_BLOCK));
        blocks.add(new BlockPlacement(start.offset(-1, 1, 2), Blocks.OAK_SAPLING));
        blocks.add(new BlockPlacement(start.offset(width, 0, 2), Blocks.GRASS_BLOCK));
        blocks.add(new BlockPlacement(start.offset(width, 1, 2), Blocks.BIRCH_SAPLING));
        
        // Path to door
        for (int z = -3; z < 0; z++) {
            blocks.add(new BlockPlacement(start.offset(doorX, 0, z), Blocks.STONE_BRICKS));
            blocks.add(new BlockPlacement(start.offset(doorX - 1, 0, z), Blocks.COBBLESTONE));
            blocks.add(new BlockPlacement(start.offset(doorX + 1, 0, z), Blocks.COBBLESTONE));
        }
        
        // Welcome mat
        blocks.add(new BlockPlacement(start.offset(doorX, 0, 0), Blocks.OAK_PRESSURE_PLATE));
    }
    
    private static Block selectWallPattern(Block[] materials, int x, int y, int z, boolean isCorner) {
        if (isCorner) return materials[1];
        // Checkerboard or stripe pattern
        if ((x + y + z) % 3 == 0) return materials[0];
        if ((x + y + z) % 3 == 1) return materials[1];
        return materials[2];
    }
    
    private static boolean shouldPlaceWindow(int x, int y, int z, int width, int height, int depth, boolean isSecondFloor, boolean hasSecondFloor) {
        int windowLevel = hasSecondFloor ? height / 2 : height / 2;
        if (y != windowLevel && y != windowLevel + 1) return false;
        
        // Place windows at regular intervals
        if (z == 0 || z == depth - 1) {
            return x % 3 == 1 && x > 1 && x < width - 2;
        }
        if (x == 0 || x == width - 1) {
            return z % 3 == 1 && z > 1 && z < depth - 2;
        }
        return false;
    }
    
    private static void buildPorch(List<BlockPlacement> blocks, BlockPos start, int doorX, int width, Block material, Block fence, Block lantern) {
        int porchWidth = Math.min(5, width - 2);
        int porchStart = Math.max(0, doorX - porchWidth / 2);
        
        // Porch floor
        for (int x = porchStart; x < porchStart + porchWidth && x < width; x++) {
            for (int z = -2; z <= 0; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), material));
            }
        }
        
        // Porch pillars
        blocks.add(new BlockPlacement(start.offset(porchStart, 1, -2), fence));
        blocks.add(new BlockPlacement(start.offset(porchStart, 2, -2), fence));
        blocks.add(new BlockPlacement(start.offset(porchStart + porchWidth - 1, 1, -2), fence));
        blocks.add(new BlockPlacement(start.offset(porchStart + porchWidth - 1, 2, -2), fence));
        
        // Porch roof
        for (int x = porchStart - 1; x <= porchStart + porchWidth && x < width + 1; x++) {
            for (int z = -3; z <= 0; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 3, z), slabMaterial(material)));
            }
        }
        
        // Lanterns
        blocks.add(new BlockPlacement(start.offset(porchStart, 2, -2), lantern));
        blocks.add(new BlockPlacement(start.offset(porchStart + porchWidth - 1, 2, -2), lantern));
    }
    
    private static void buildDeck(List<BlockPlacement> blocks, BlockPos start, int width, int depth, Block material, Block fence) {
        int deckZ = depth;
        int deckWidth = Math.min(width - 4, 4);
        int deckStart = (width - deckWidth) / 2;
        
        for (int x = deckStart; x < deckStart + deckWidth; x++) {
            for (int z = deckZ; z < deckZ + 3; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 1, z), slabMaterial(material)));
            }
        }
        
        // Railing
        for (int x = deckStart; x < deckStart + deckWidth; x++) {
            blocks.add(new BlockPlacement(start.offset(x, 2, deckZ + 2), fence));
        }
        for (int z = deckZ; z < deckZ + 3; z++) {
            blocks.add(new BlockPlacement(start.offset(deckStart, 2, z), fence));
            blocks.add(new BlockPlacement(start.offset(deckStart + deckWidth - 1, 2, z), fence));
        }
    }
    
    private static void buildChimney(List<BlockPlacement> blocks, BlockPos start, int width, int depth, int height, Block material) {
        int chimneyX = random.nextBoolean() ? 1 : width - 2;
        int chimneyZ = random.nextBoolean() ? 1 : depth - 2;
        
        for (int y = 1; y <= height + 3; y++) {
            blocks.add(new BlockPlacement(start.offset(chimneyX, y, chimneyZ), material));
        }
        
        // Chimney top detail
        blocks.add(new BlockPlacement(start.offset(chimneyX, height + 4, chimneyZ), material));
        blocks.add(new BlockPlacement(start.offset(chimneyX + 1, height + 3, chimneyZ), material));
        blocks.add(new BlockPlacement(start.offset(chimneyX - 1, height + 3, chimneyZ), material));
        blocks.add(new BlockPlacement(start.offset(chimneyX, height + 3, chimneyZ + 1), material));
        blocks.add(new BlockPlacement(start.offset(chimneyX, height + 3, chimneyZ - 1), material));
    }
    
    private static void buildRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material, RoofType type) {
        switch (type) {
            case PYRAMID -> buildPyramidRoof(blocks, start, width, height, depth, material);
            case GABLE -> buildGableRoof(blocks, start, width, height, depth, material);
            case FLAT -> buildFlatRoof(blocks, start, width, height, depth, material);
            case STEEP -> buildSteepRoof(blocks, start, width, height, depth, material);
            case ROUNDED -> buildRoundedRoof(blocks, start, width, height, depth, material);
            case TOWERED -> buildToweredRoof(blocks, start, width, height, depth, material);
        }
    }
    
    private static void buildPyramidRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int roofStart = height + 1;
        int layers = Math.min(width, depth) / 2 + 1;
        
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
    
    private static void buildFlatRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        int y = height + 1;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, y, z), material));
            }
        }
        // Add parapet
        for (int x = 0; x < width; x++) {
            blocks.add(new BlockPlacement(start.offset(x, y + 1, 0), slabMaterial(material)));
            blocks.add(new BlockPlacement(start.offset(x, y + 1, depth - 1), slabMaterial(material)));
        }
        for (int z = 0; z < depth; z++) {
            blocks.add(new BlockPlacement(start.offset(0, y + 1, z), slabMaterial(material)));
            blocks.add(new BlockPlacement(start.offset(width - 1, y + 1, z), slabMaterial(material)));
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
    
    private static void buildRoundedRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        // Simplified rounded roof - stepped pyramid with wider steps
        int roofStart = height + 1;
        int layers = Math.min(width, depth) / 2;
        
        for (int layer = 0; layer < layers; layer++) {
            int y = roofStart + layer;
            int inset = layer;
            boolean wideLayer = layer % 2 == 0;
            
            for (int x = inset; x < width - inset; x++) {
                for (int z = inset; z < depth - inset; z++) {
                    if (wideLayer || x == inset || x == width - 1 - inset || z == inset || z == depth - 1 - inset) {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                    }
                }
            }
        }
    }
    
    private static void buildToweredRoof(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        // Main roof
        buildPyramidRoof(blocks, start, width, height, depth, material);
        
        // Small towers on corners
        int towerHeight = height + 3;
        int[][] corners = {{0, 0}, {width - 1, 0}, {0, depth - 1}, {width - 1, depth - 1}};
        
        for (int[] corner : corners) {
            for (int y = height + 1; y <= towerHeight; y++) {
                blocks.add(new BlockPlacement(start.offset(corner[0], y, corner[1]), material));
            }
        }
    }
    
    // Placeholder methods for other house layouts
    private static void buildLShapedHouse(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth,
            Block[] wallMaterials, Block floorMaterial, Block roofMaterial, Block windowMaterial, Block doorMaterial,
            Block fenceMaterial, Block lanternMaterial, RoofType roofType, boolean hasSecondFloor, boolean hasPorch,
            boolean hasChimney, int doorX) {
        // Build main rectangle
        buildRectangularHouse(blocks, start, width * 2/3, height, depth, wallMaterials, floorMaterial, roofMaterial,
                windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType, hasSecondFloor, hasPorch,
                hasChimney, false, false, doorX);
        // Build wing
        buildRectangularHouse(blocks, start.offset(width * 2/3, 0, 0), width / 3, height, depth * 2/3, wallMaterials,
                floorMaterial, roofMaterial, windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType,
                hasSecondFloor, false, false, false, false, 1);
    }
    
    private static void buildTShapedHouse(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth,
            Block[] wallMaterials, Block floorMaterial, Block roofMaterial, Block windowMaterial, Block doorMaterial,
            Block fenceMaterial, Block lanternMaterial, RoofType roofType, boolean hasSecondFloor, boolean hasPorch,
            boolean hasChimney, int doorX) {
        // Build stem
        buildRectangularHouse(blocks, start, width / 3, height, depth, wallMaterials, floorMaterial, roofMaterial,
                windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType, hasSecondFloor, hasPorch,
                hasChimney, false, false, doorX);
        // Build crossbar
        buildRectangularHouse(blocks, start.offset(0, 0, depth * 2/3), width, height, depth / 3, wallMaterials,
                floorMaterial, roofMaterial, windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType,
                hasSecondFloor, false, false, false, false, doorX);
    }
    
    private static void buildUShapedHouse(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth,
            Block[] wallMaterials, Block floorMaterial, Block roofMaterial, Block windowMaterial, Block doorMaterial,
            Block fenceMaterial, Block lanternMaterial, RoofType roofType, boolean hasSecondFloor, boolean hasPorch,
            boolean hasChimney, int doorX) {
        // Build three sides
        buildRectangularHouse(blocks, start, width, height, depth / 3, wallMaterials, floorMaterial, roofMaterial,
                windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType, hasSecondFloor, hasPorch,
                hasChimney, false, false, doorX);
        buildRectangularHouse(blocks, start.offset(0, 0, depth * 2/3), width, height, depth / 3, wallMaterials,
                floorMaterial, roofMaterial, windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType,
                hasSecondFloor, false, false, false, false, 1);
        buildRectangularHouse(blocks, start.offset(0, 0, depth / 3), width / 4, height, depth / 3, wallMaterials,
                floorMaterial, roofMaterial, windowMaterial, doorMaterial, fenceMaterial, lanternMaterial, roofType,
                hasSecondFloor, false, false, false, false, 1);
    }
    
    private static void buildCourtyardHouse(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth,
            Block[] wallMaterials, Block floorMaterial, Block roofMaterial, Block windowMaterial, Block doorMaterial,
            Block fenceMaterial, Block lanternMaterial, RoofType roofType, boolean hasSecondFloor, boolean hasPorch,
            boolean hasChimney, int doorX) {
        // Build hollow square
        int innerSize = Math.min(width, depth) / 3;
        int outerWidth = width;
        int outerDepth = depth;
        
        // Four walls forming courtyard
        for (int x = 0; x < outerWidth; x++) {
            for (int y = 1; y <= height; y++) {
                // Front and back walls with gap for courtyard
                if (x < (outerWidth - innerSize) / 2 || x >= (outerWidth + innerSize) / 2) {
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), wallMaterials[0]));
                    blocks.add(new BlockPlacement(start.offset(x, y, outerDepth - 1), wallMaterials[0]));
                }
            }
        }
        for (int z = 0; z < outerDepth; z++) {
            for (int y = 1; y <= height; y++) {
                blocks.add(new BlockPlacement(start.offset(0, y, z), wallMaterials[0]));
                blocks.add(new BlockPlacement(start.offset(outerWidth - 1, y, z), wallMaterials[0]));
            }
        }
        
        // Floors
        for (int x = 0; x < outerWidth; x++) {
            for (int z = 0; z < outerDepth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
            }
        }
        
        // Roof
        buildPyramidRoof(blocks, start, outerWidth, height, outerDepth, roofMaterial);
    }

    // ==================== CREATIVE CASTLE ====================
    
    private static List<BlockPlacement> buildCreativeCastle(BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block stoneMaterial = style != null ? style.getPillarBlock() : Blocks.STONE_BRICKS;
        Block wallMaterial = style != null ? style.getWallBlock() : Blocks.COBBLESTONE;
        Block accentMaterial = style != null ? style.getPillarBlock() : Blocks.CHISELED_STONE_BRICKS;
        Block windowMaterial = style != null ? style.getWindowBlock() : Blocks.GLASS_PANE;
        Block lanternMaterial = style != null ? style.getLanternBlock() : Blocks.LANTERN;
        
        // Random castle features
        boolean hasMoat = random.nextBoolean();
        boolean hasDrawbridge = hasMoat || random.nextBoolean();
        boolean hasInnerCourtyard = width >= 12 && depth >= 12;
        boolean hasMultipleTowers = random.nextBoolean();
        int towerSize = 3 + random.nextInt(2);
        int numTowers = hasMultipleTowers ? 4 + random.nextInt(4) : 4;
        
        // Moat
        if (hasMoat) {
            buildMoat(blocks, start, width, depth);
        }
        
        // Drawbridge
        if (hasDrawbridge) {
            buildDrawbridge(blocks, start, width, wallMaterial);
        }
        
        // Main keep or courtyard layout
        if (hasInnerCourtyard) {
            buildCastleWithCourtyard(blocks, start, width, height, depth, stoneMaterial, wallMaterial, 
                    accentMaterial, windowMaterial, lanternMaterial, towerSize);
        } else {
            buildSolidCastle(blocks, start, width, height, depth, stoneMaterial, wallMaterial, 
                    accentMaterial, windowMaterial, lanternMaterial, towerSize, numTowers);
        }
        
        return blocks;
    }
    
    private static void buildMoat(List<BlockPlacement> blocks, BlockPos start, int width, int depth) {
        int moatWidth = 2;
        for (int x = -moatWidth; x < width + moatWidth; x++) {
            for (int z = -moatWidth; z < depth + moatWidth; z++) {
                if (x < 0 || x >= width || z < 0 || z >= depth) {
                    blocks.add(new BlockPlacement(start.offset(x, -1, z), Blocks.WATER));
                    blocks.add(new BlockPlacement(start.offset(x, -2, z), Blocks.DIRT));
                }
            }
        }
    }
    
    private static void buildDrawbridge(List<BlockPlacement> blocks, BlockPos start, int width, Block material) {
        int bridgeWidth = 3;
        int bridgeStart = (width - bridgeWidth) / 2;
        for (int x = bridgeStart; x < bridgeStart + bridgeWidth; x++) {
            for (int z = -3; z < 0; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), material));
            }
        }
        // Chains
        blocks.add(new BlockPlacement(start.offset(bridgeStart, 1, -2), Blocks.OAK_FENCE));
        blocks.add(new BlockPlacement(start.offset(bridgeStart, 2, -2), Blocks.OAK_FENCE));
        blocks.add(new BlockPlacement(start.offset(bridgeStart + bridgeWidth - 1, 1, -2), Blocks.OAK_FENCE));
        blocks.add(new BlockPlacement(start.offset(bridgeStart + bridgeWidth - 1, 2, -2), Blocks.OAK_FENCE));
    }
    
    private static void buildCastleWithCourtyard(List<BlockPlacement> blocks, BlockPos start, int width, int height, 
            int depth, Block stoneMaterial, Block wallMaterial, Block accentMaterial, Block windowMaterial, 
            Block lanternMaterial, int towerSize) {
        int wallThickness = 2;
        int courtyardSize = 4;
        
        // Outer walls
        for (int y = 0; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isOuterEdge = (x < wallThickness || x >= width - wallThickness || 
                                          z < wallThickness || z >= depth - wallThickness);
                    boolean isCourtyard = (x >= (width - courtyardSize) / 2 && x < (width + courtyardSize) / 2 &&
                                          z >= (depth - courtyardSize) / 2 && z < (depth + courtyardSize) / 2);
                    
                    if (isOuterEdge && !isCourtyard) {
                        Block material = (x == 0 || x == width - 1 || z == 0 || z == depth - 1) ? 
                                stoneMaterial : wallMaterial;
                        
                        // Windows
                        if (y > 2 && y < height - 1 && (x + z) % 4 == 0) {
                            material = windowMaterial;
                        }
                        
                        blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                    }
                }
            }
        }
        
        // Corner towers
        int[][] corners = {{0, 0}, {width - towerSize, 0}, {0, depth - towerSize}, {width - towerSize, depth - towerSize}};
        for (int[] corner : corners) {
            buildCastleTower(blocks, start, corner[0], corner[1], height + 4, towerSize, stoneMaterial, windowMaterial, lanternMaterial);
        }
        
        // Gatehouse
        buildGatehouse(blocks, start, width, height, stoneMaterial, wallMaterial, accentMaterial);
    }
    
    private static void buildSolidCastle(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth,
            Block stoneMaterial, Block wallMaterial, Block accentMaterial, Block windowMaterial, 
            Block lanternMaterial, int towerSize, int numTowers) {
        
        // Main keep
        for (int y = 0; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    boolean isCorner = (x <= 2 || x >= width - 3) && (z <= 2 || z >= depth - 3);
                    
                    if (y == 0 || isEdge) {
                        Block material = isCorner ? stoneMaterial : wallMaterial;
                        
                        if (y > 2 && y % 4 == 0 && !isCorner) {
                            material = windowMaterial;
                        }
                        
                        blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                    }
                }
            }
        }
        
        // Towers at corners and possibly midpoints
        int[][] towerPositions = {
            {0, 0}, {width - towerSize, 0}, 
            {0, depth - towerSize}, {width - towerSize, depth - towerSize},
            {width / 2 - towerSize / 2, 0}, {width / 2 - towerSize / 2, depth - towerSize},
            {0, depth / 2 - towerSize / 2}, {width - towerSize, depth / 2 - towerSize / 2}
        };
        
        int towersToBuild = Math.min(numTowers, towerPositions.length);
        for (int i = 0; i < towersToBuild; i++) {
            buildCastleTower(blocks, start, towerPositions[i][0], towerPositions[i][1], 
                    height + 3 + random.nextInt(3), towerSize, stoneMaterial, windowMaterial, lanternMaterial);
        }
        
        // Crenellations
        buildCrenellations(blocks, start, width, height, depth, stoneMaterial);
    }
    
    private static void buildCastleTower(List<BlockPlacement> blocks, BlockPos start, int offsetX, int offsetZ, 
            int height, int size, Block material, Block windowMaterial, Block lanternMaterial) {
        for (int y = 0; y <= height; y++) {
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    boolean isEdge = (dx == 0 || dx == size - 1 || dz == 0 || dz == size - 1);
                    if (y == 0 || isEdge) {
                        Block block = material;
                        if (y > 3 && y % 5 == 0 && (dx == size / 2 || dz == size / 2)) {
                            block = windowMaterial;
                        }
                        blocks.add(new BlockPlacement(start.offset(offsetX + dx, y, offsetZ + dz), block));
                    }
                }
            }
        }
        
        // Tower top
        for (int dx = -1; dx <= size; dx++) {
            for (int dz = -1; dz <= size; dz++) {
                if (dx == -1 || dx == size || dz == -1 || dz == size) {
                    if ((dx + dz) % 2 == 0) {
                        blocks.add(new BlockPlacement(start.offset(offsetX + dx, height + 1, offsetZ + dz), material));
                    }
                }
            }
        }
        
        // Lantern on tower
        blocks.add(new BlockPlacement(start.offset(offsetX + size / 2, height + 2, offsetZ + size / 2), lanternMaterial));
    }
    
    private static void buildGatehouse(List<BlockPlacement> blocks, BlockPos start, int width, int height,
            Block stoneMaterial, Block wallMaterial, Block accentMaterial) {
        int gateX = width / 2 - 1;
        int gateWidth = 3;
        int gateHeight = 4;
        
        // Gate arch
        for (int x = gateX; x < gateX + gateWidth; x++) {
            for (int y = 0; y < gateHeight; y++) {
                if (y == gateHeight - 1 || x == gateX || x == gateX + gateWidth - 1) {
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), stoneMaterial));
                }
            }
        }
        
        // Portcullis
        for (int x = gateX + 1; x < gateX + gateWidth - 1; x++) {
            for (int y = 1; y < gateHeight - 1; y++) {
                blocks.add(new BlockPlacement(start.offset(x, y, 0), Blocks.OAK_FENCE));
            }
        }
        
        // Gatehouse towers
        buildCastleTower(blocks, start, gateX - 2, -1, height + 2, 3, stoneMaterial, Blocks.GLASS_PANE, Blocks.LANTERN);
        buildCastleTower(blocks, start, gateX + gateWidth, -1, height + 2, 3, stoneMaterial, Blocks.GLASS_PANE, Blocks.LANTERN);
    }
    
    private static void buildCrenellations(List<BlockPlacement> blocks, BlockPos start, int width, int height, int depth, Block material) {
        for (int x = 0; x < width; x += 2) {
            blocks.add(new BlockPlacement(start.offset(x, height + 1, 0), material));
            blocks.add(new BlockPlacement(start.offset(x, height + 1, depth - 1), material));
        }
        for (int z = 0; z < depth; z += 2) {
            blocks.add(new BlockPlacement(start.offset(0, height + 1, z), material));
            blocks.add(new BlockPlacement(start.offset(width - 1, height + 1, z), material));
        }
    }

    // ==================== CREATIVE TOWER ====================
    
    private static List<BlockPlacement> buildCreativeTower(BlockPos start, int width, int height, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = style != null ? style.getWallBlock() : Blocks.STONE_BRICKS;
        Block accentMaterial = style != null ? style.getPillarBlock() : Blocks.CHISELED_STONE_BRICKS;
        Block windowMaterial = style != null ? style.getWindowBlock() : Blocks.GLASS_PANE;
        Block lanternMaterial = style != null ? style.getLanternBlock() : Blocks.LANTERN;
        
        // Tower variations
        boolean hasSpiralStaircase = random.nextBoolean();
        boolean hasMultipleDecks = height > 15 && random.nextBoolean();
        boolean hasBeacon = random.nextBoolean();
        int baseWidth = width + random.nextInt(3);
        
        // Tapered tower
        int currentWidth = baseWidth;
        int sectionHeight = height / 3;
        
        for (int section = 0; section < 3; section++) {
            int startY = section * sectionHeight;
            int endY = (section + 1) * sectionHeight;
            
            for (int y = startY; y < endY && y < height; y++) {
                for (int x = 0; x < currentWidth; x++) {
                    for (int z = 0; z < currentWidth; z++) {
                        int offsetX = (baseWidth - currentWidth) / 2;
                        int offsetZ = (baseWidth - currentWidth) / 2;
                        
                        boolean isEdge = (x == 0 || x == currentWidth - 1 || z == 0 || z == currentWidth - 1);
                        boolean isCorner = (x == 0 || x == currentWidth - 1) && (z == 0 || z == currentWidth - 1);
                        
                        if (y == startY || isEdge) {
                            Block material = isCorner ? accentMaterial : wallMaterial;
                            
                            // Windows
                            if ((y - startY) % 4 == 2 && !isCorner && (x == currentWidth / 2 || z == currentWidth / 2)) {
                                material = windowMaterial;
                            }
                            
                            blocks.add(new BlockPlacement(start.offset(offsetX + x, y, offsetZ + z), material));
                        }
                    }
                }
            }
            
            // Deck at section transition
            if (hasMultipleDecks && section < 2) {
                buildTowerDeck(blocks, start, baseWidth, endY, currentWidth, wallMaterial, accentMaterial, lanternMaterial);
            }
            
            currentWidth = Math.max(3, currentWidth - 1);
        }
        
        // Spiral staircase
        if (hasSpiralStaircase) {
            buildSpiralStaircase(blocks, start, baseWidth, height, accentMaterial);
        }
        
        // Top beacon or spire
        if (hasBeacon) {
            int centerX = baseWidth / 2;
            int centerZ = baseWidth / 2;
            for (int y = height; y < height + 5; y++) {
                blocks.add(new BlockPlacement(start.offset(centerX, y, centerZ), accentMaterial));
            }
            blocks.add(new BlockPlacement(start.offset(centerX, height + 5, centerZ), Blocks.LANTERN));
        }
        
        return blocks;
    }
    
    private static void buildTowerDeck(List<BlockPlacement> blocks, BlockPos start, int baseWidth, int y, int towerWidth,
            Block wallMaterial, Block fenceMaterial, Block lanternMaterial) {
        int deckSize = towerWidth + 2;
        int offset = (baseWidth - deckSize) / 2;
        
        for (int x = 0; x < deckSize; x++) {
            for (int z = 0; z < deckSize; z++) {
                if (x == 0 || x == deckSize - 1 || z == 0 || z == deckSize - 1) {
                    blocks.add(new BlockPlacement(start.offset(offset + x, y, offset + z), slabMaterial(wallMaterial)));
                    blocks.add(new BlockPlacement(start.offset(offset + x, y + 1, offset + z), fenceMaterial));
                }
            }
        }
        
        // Lanterns at corners
        blocks.add(new BlockPlacement(start.offset(offset, y + 1, offset), lanternMaterial));
        blocks.add(new BlockPlacement(start.offset(offset + deckSize - 1, y + 1, offset), lanternMaterial));
        blocks.add(new BlockPlacement(start.offset(offset, y + 1, offset + deckSize - 1), lanternMaterial));
        blocks.add(new BlockPlacement(start.offset(offset + deckSize - 1, y + 1, offset + deckSize - 1), lanternMaterial));
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

    // ==================== CREATIVE MODERN HOUSE ====================
    
    private static List<BlockPlacement> buildCreativeModernHouse(BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = Blocks.WHITE_CONCRETE;
        Block accentMaterial = Blocks.GRAY_CONCRETE;
        Block glassMaterial = Blocks.GLASS;
        Block floorMaterial = Blocks.SMOOTH_STONE;
        
        // Modern design features
        boolean hasCantilever = random.nextBoolean();
        boolean hasGlassWalls = random.nextBoolean();
        boolean hasFlatRoofGarden = random.nextBoolean();
        boolean hasPool = width >= 10 && depth >= 10 && random.nextBoolean();
        
        // Main structure with possible cantilever
        int mainWidth = hasCantilever ? width * 2 / 3 : width;
        int cantileverOffset = hasCantilever ? width / 3 : 0;
        
        // Foundation and floors
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
            }
        }
        
        // Second floor for cantilever
        if (hasCantilever) {
            for (int x = cantileverOffset; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    blocks.add(new BlockPlacement(start.offset(x, height / 2, z), floorMaterial));
                }
            }
        }
        
        // Walls
        for (int y = 1; y < height; y++) {
            boolean isSecondFloor = hasCantilever && y >= height / 2;
            int wallStart = isSecondFloor ? cantileverOffset : 0;
            
            for (int x = wallStart; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == wallStart || x == width - 1 || z == 0 || z == depth - 1);
                    
                    if (isEdge) {
                        Block material = wallMaterial;
                        
                        // Large glass windows
                        if (hasGlassWalls && (z == 0 || z == depth - 1) && y > 1 && y < height - 1) {
                            if (x % 2 == 0) {
                                material = glassMaterial;
                            }
                        }
                        
                        // Accent strips
                        if (y == height / 2 || y == 1) {
                            material = accentMaterial;
                        }
                        
                        blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                    }
                }
            }
        }
        
        // Support pillars for cantilever
        if (hasCantilever) {
            for (int y = 1; y < height / 2; y++) {
                blocks.add(new BlockPlacement(start.offset(cantileverOffset, y, 0), accentMaterial));
                blocks.add(new BlockPlacement(start.offset(cantileverOffset, y, depth - 1), accentMaterial));
            }
        }
        
        // Flat roof with optional garden
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, height, z), floorMaterial));
            }
        }
        
        // Roof garden
        if (hasFlatRoofGarden) {
            for (int x = 2; x < width - 2; x++) {
                for (int z = 2; z < depth - 2; z++) {
                    if (random.nextBoolean()) {
                        blocks.add(new BlockPlacement(start.offset(x, height + 1, z), Blocks.GRASS_BLOCK));
                    }
                }
            }
        }
        
        // Pool
        if (hasPool) {
            int poolX = width - 4;
            int poolZ = depth - 4;
            for (int x = poolX; x < width - 1; x++) {
                for (int z = poolZ; z < depth - 1; z++) {
                    blocks.add(new BlockPlacement(start.offset(x, 0, z), Blocks.WATER));
                    blocks.add(new BlockPlacement(start.offset(x, -1, z), Blocks.BLUE_CONCRETE));
                }
            }
        }
        
        return blocks;
    }

    // ==================== CREATIVE BARN ====================
    
    private static List<BlockPlacement> buildCreativeBarn(BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block woodMaterial = style != null ? style.getWallBlock() : Blocks.OAK_PLANKS;
        Block logMaterial = style != null ? style.getPillarBlock() : Blocks.OAK_LOG;
        Block roofMaterial = style != null ? style.getRoofBlock() : Blocks.RED_WOOL;
        Block fenceMaterial = style != null ? style.getFenceBlock() : Blocks.OAK_FENCE;
        
        // Barn variations
        boolean hasHayloft = random.nextBoolean();
        boolean hasStables = random.nextBoolean();
        boolean hasSilo = random.nextBoolean();
        int doorWidth = 2 + random.nextInt(2);
        
        // Floor
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), woodMaterial));
            }
        }
        
        // Walls
        for (int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isSupport = (x == 0 || x == width - 1 || x == width / 2);
                Block material = isSupport ? logMaterial : woodMaterial;
                
                // Large barn doors
                if (x >= (width - doorWidth) / 2 && x < (width + doorWidth) / 2 && y <= 3) {
                    material = (y == 3) ? logMaterial : Blocks.AIR;
                }
                
                blocks.add(new BlockPlacement(start.offset(x, y, 0), material));
                blocks.add(new BlockPlacement(start.offset(x, y, depth - 1), material));
            }
            
            for (int z = 1; z < depth - 1; z++) {
                blocks.add(new BlockPlacement(start.offset(0, y, z), logMaterial));
                blocks.add(new BlockPlacement(start.offset(width - 1, y, z), logMaterial));
            }
        }
        
        // Hayloft
        if (hasHayloft) {
            int loftHeight = height - 2;
            for (int x = 1; x < width - 1; x++) {
                for (int z = 1; z < depth / 2; z++) {
                    blocks.add(new BlockPlacement(start.offset(x, loftHeight, z), woodMaterial));
                    // Hay bales
                    if (random.nextInt(3) == 0) {
                        blocks.add(new BlockPlacement(start.offset(x, loftHeight + 1, z), Blocks.HAY_BLOCK));
                    }
                }
            }
        }
        
        // Stables
        if (hasStables) {
            int stableStart = depth * 2 / 3;
            for (int z = stableStart; z < depth - 1; z++) {
                for (int x = 2; x < width - 2; x += 3) {
                    // Stable dividers
                    for (int y = 1; y < height - 2; y++) {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), fenceMaterial));
                    }
                }
            }
        }
        
        // Silo
        if (hasSilo) {
            int siloX = width + 1;
            int siloZ = depth / 2;
            int siloRadius = 2;
            int siloHeight = height + 3;
            
            for (int y = 0; y < siloHeight; y++) {
                for (int dx = -siloRadius; dx <= siloRadius; dx++) {
                    for (int dz = -siloRadius; dz <= siloRadius; dz++) {
                        if (dx * dx + dz * dz <= siloRadius * siloRadius + 1) {
                            blocks.add(new BlockPlacement(start.offset(siloX + dx, y, siloZ + dz), Blocks.WHITE_WOOL));
                        }
                    }
                }
            }
            // Silo dome
            blocks.add(new BlockPlacement(start.offset(siloX, siloHeight, siloZ), Blocks.WHITE_WOOL));
        }
        
        // Gambrel roof (classic barn roof)
        int roofPeak = height + width / 3;
        for (int x = 0; x < width; x++) {
            int distFromCenter = Math.abs(x - width / 2);
            int roofY;
            
            if (distFromCenter < width / 4) {
                // Steep upper section
                roofY = roofPeak - (distFromCenter * 3 / 2);
            } else {
                // Shallow lower section
                roofY = roofPeak - (width / 4 * 3 / 2) - (distFromCenter - width / 4);
            }
            
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, roofY, z), roofMaterial));
                // Fill below roof
                for (int y = height; y < roofY; y++) {
                    blocks.add(new BlockPlacement(start.offset(x, y, z), roofMaterial));
                }
            }
        }
        
        return blocks;
    }

    // ==================== CREATIVE WALL ====================
    
    private static List<BlockPlacement> buildCreativeWall(BlockPos start, int width, int height, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block baseMaterial = style != null ? style.getWallBlock() : Blocks.STONE_BRICKS;
        Block accentMaterial = style != null ? style.getPillarBlock() : Blocks.COBBLESTONE;
        
        // Wall variations
        boolean hasBattlements = random.nextBoolean();
        boolean hasPillars = random.nextBoolean();
        boolean hasGate = width >= 7 && random.nextBoolean();
        int pillarInterval = 4 + random.nextInt(3);
        
        for (int x = 0; x < width; x++) {
            boolean isPillar = hasPillars && x % pillarInterval == 0;
            boolean isGate = hasGate && x >= width / 2 - 1 && x <= width / 2 + 1;
            
            for (int y = 0; y < height; y++) {
                if (isGate && y < 4) {
                    // Gate opening
                    if (y == 3) {
                        blocks.add(new BlockPlacement(start.offset(x, y, 0), accentMaterial));
                    }
                } else {
                    Block material = isPillar ? accentMaterial : baseMaterial;
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), material));
                    
                    // Make pillars thicker
                    if (isPillar && y < height - 1) {
                        blocks.add(new BlockPlacement(start.offset(x, y, 1), accentMaterial));
                        blocks.add(new BlockPlacement(start.offset(x, y, -1), accentMaterial));
                    }
                }
            }
            
            // Battlements
            if (hasBattlements && !isGate) {
                if (x % 2 == 0) {
                    blocks.add(new BlockPlacement(start.offset(x, height, 0), baseMaterial));
                    blocks.add(new BlockPlacement(start.offset(x, height + 1, 0), baseMaterial));
                }
            }
        }
        
        return blocks;
    }

    // ==================== CREATIVE PLATFORM ====================
    
    private static List<BlockPlacement> buildCreativePlatform(BlockPos start, int width, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block floorMaterial = style != null ? style.getFloorBlock() : Blocks.OAK_PLANKS;
        Block fenceMaterial = style != null ? style.getFenceBlock() : Blocks.OAK_FENCE;
        Block lanternMaterial = style != null ? style.getLanternBlock() : Blocks.LANTERN;
        
        // Platform variations
        boolean hasMultipleLevels = random.nextBoolean();
        boolean hasRailing = random.nextBoolean();
        boolean hasCentralFeature = random.nextBoolean();
        boolean isCircular = random.nextBoolean();
        
        if (isCircular) {
            // Circular platform
            int radius = Math.min(width, depth) / 2;
            int centerX = width / 2;
            int centerZ = depth / 2;
            
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    int distSq = (x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ);
                    if (distSq <= radius * radius) {
                        blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
                        
                        // Railing at edge
                        if (hasRailing && distSq >= (radius - 1) * (radius - 1)) {
                            blocks.add(new BlockPlacement(start.offset(x, 1, z), fenceMaterial));
                        }
                    }
                }
            }
        } else {
            // Rectangular platform with possible multiple levels
            int levels = hasMultipleLevels ? 2 + random.nextInt(2) : 1;
            
            for (int level = 0; level < levels; level++) {
                int levelY = level * 2;
                int inset = level;
                
                for (int x = inset; x < width - inset; x++) {
                    for (int z = inset; z < depth - inset; z++) {
                        blocks.add(new BlockPlacement(start.offset(x, levelY, z), floorMaterial));
                        
                        // Railing on top level
                        if (hasRailing && level == levels - 1 && 
                            (x == inset || x == width - 1 - inset || z == inset || z == depth - 1 - inset)) {
                            blocks.add(new BlockPlacement(start.offset(x, levelY + 1, z), fenceMaterial));
                        }
                    }
                }
            }
        }
        
        // Central feature
        if (hasCentralFeature) {
            int centerX = width / 2;
            int centerZ = depth / 2;
            int featureHeight = 3 + random.nextInt(3);
            
            for (int y = 1; y <= featureHeight; y++) {
                blocks.add(new BlockPlacement(start.offset(centerX, y, centerZ), fenceMaterial));
            }
            blocks.add(new BlockPlacement(start.offset(centerX, featureHeight + 1, centerZ), lanternMaterial));
        }
        
        return blocks;
    }

    // ==================== CREATIVE BOX ====================
    
    private static List<BlockPlacement> buildCreativeBox(BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        
        Block wallMaterial = style != null ? style.getWallBlock() : Blocks.OAK_PLANKS;
        Block accentMaterial = style != null ? style.getPillarBlock() : Blocks.OAK_LOG;
        Block windowMaterial = style != null ? style.getWindowBlock() : Blocks.GLASS_PANE;
        
        // Box variations
        boolean isHollow = random.nextBoolean();
        boolean hasWindows = random.nextBoolean();
        boolean hasPattern = random.nextBoolean();
        int wallThickness = isHollow ? 1 : width; // Solid or hollow
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || y == 0 || y == height - 1 || z == 0 || z == depth - 1);
                    boolean isCorner = ((x == 0 || x == width - 1) && (y == 0 || y == height - 1)) ||
                                      ((x == 0 || x == width - 1) && (z == 0 || z == depth - 1)) ||
                                      ((y == 0 || y == height - 1) && (z == 0 || z == depth - 1));
                    
                    if (isHollow && !isEdge) continue;
                    
                    Block material = wallMaterial;
                    
                    // Corners get accent material
                    if (isCorner) {
                        material = accentMaterial;
                    }
                    // Pattern on faces
                    else if (hasPattern && isEdge && !isCorner) {
                        if ((x + y + z) % 3 == 0) {
                            material = accentMaterial;
                        }
                    }
                    // Windows
                    else if (hasWindows && isEdge && !isCorner && y > 1 && y < height - 2) {
                        if ((x + z) % 4 == 0) {
                            material = windowMaterial;
                        }
                    }
                    
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }
        
        // Interior decoration if hollow
        if (isHollow && width >= 4 && depth >= 4) {
            // Central pillar
            int centerX = width / 2;
            int centerZ = depth / 2;
            for (int y = 1; y < height - 1; y++) {
                blocks.add(new BlockPlacement(start.offset(centerX, y, centerZ), accentMaterial));
            }
        }
        
        return blocks;
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
        if (baseBlock == Blocks.WHITE_CONCRETE) return Blocks.WHITE_CONCRETE_POWDER;
        if (baseBlock == Blocks.GRAY_CONCRETE) return Blocks.GRAY_CONCRETE_POWDER;
        return Blocks.OAK_SLAB;
    }
}
