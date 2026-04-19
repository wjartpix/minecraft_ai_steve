package com.steve.ai.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for procedural structure generation.
 * Contains algorithms for generating various building types.
 */
public class StructureGenerators {
    
    private static final Random random = new Random();

    public static List<BlockPlacement> generate(String structureType, BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        // Use creative generators for more variety
        return CreativeStructureGenerators.generate(structureType, start, width, height, depth, style, materials);
    }

    private static Block getMaterial(List<Block> materials, int index) {
        if (materials == null || materials.isEmpty()) return Blocks.OAK_PLANKS;
        return materials.get(index % materials.size());
    }

    private static Block getMaterialOrDefault(BuildingStyle style, Block styleBlock, List<Block> materials, int index, Block defaultBlock) {
        if (style != null) {
            return styleBlock;
        }
        if (materials == null || materials.isEmpty()) {
            return defaultBlock;
        }
        return materials.get(index % materials.size());
    }

    private static List<BlockPlacement> buildAdvancedHouse(BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        Block floorMaterial = getMaterialOrDefault(style, style != null ? style.getFloorBlock() : null, materials, 0, Blocks.OAK_PLANKS);
        Block wallMaterial = getMaterialOrDefault(style, style != null ? style.getWallBlock() : null, materials, 1, Blocks.OAK_PLANKS);
        Block roofMaterial = getMaterialOrDefault(style, style != null ? style.getRoofBlock() : null, materials, 2, Blocks.OAK_PLANKS);
        Block windowMaterial = style != null ? style.getWindowBlock() : Blocks.GLASS_PANE;
        Block doorMaterial = style != null ? style.getDoorBlock() : Blocks.OAK_DOOR;
        Block fenceMaterial = style != null ? style.getFenceBlock() : Blocks.OAK_FENCE;
        Block lanternMaterial = style != null ? style.getLanternBlock() : Blocks.LANTERN;
        Block trapdoorMaterial = style != null ? style.getTrapdoorBlock() : Blocks.OAK_TRAPDOOR;
        Block chimneyMaterial = style != null ? style.getChimneyBlock() : Blocks.COBBLESTONE;

        if (roofMaterial == Blocks.GLASS || roofMaterial == Blocks.GLASS_PANE) {
            roofMaterial = Blocks.OAK_PLANKS;
        }

        // Random variations for creative buildings
        boolean hasChimney = random.nextBoolean();
        boolean hasBalcony = width >= 7 && random.nextBoolean();
        boolean hasPorch = width >= 7 && random.nextBoolean();
        boolean hasDormer = random.nextBoolean();
        int doorX = width / 2 + (random.nextBoolean() ? 0 : (random.nextBoolean() ? 1 : -1));

        // Floor
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
            }
        }

        // Porch/Entrance platform
        if (hasPorch) {
            for (int x = doorX - 1; x <= doorX + 1; x++) {
                for (int z = -1; z >= -2; z--) {
                    blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
                }
            }
            // Porch pillars
            blocks.add(new BlockPlacement(start.offset(doorX - 1, 1, -2), fenceMaterial));
            blocks.add(new BlockPlacement(start.offset(doorX - 1, 2, -2), fenceMaterial));
            blocks.add(new BlockPlacement(start.offset(doorX + 1, 1, -2), fenceMaterial));
            blocks.add(new BlockPlacement(start.offset(doorX + 1, 2, -2), fenceMaterial));
            // Porch roof
            for (int x = doorX - 2; x <= doorX + 2; x++) {
                for (int z = -3; z <= 0; z++) {
                    blocks.add(new BlockPlacement(start.offset(x, 3, z), slabMaterial(roofMaterial)));
                }
            }
        }

        // Walls with windows and door
        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                // Front wall with door
                if (x == doorX && y <= 2) {
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), doorMaterial));
                } else if (y >= 2 && y <= height - 1 && (x == 2 || x == width - 3 || x == doorX - 2 || x == doorX + 2)) {
                    // Windows with shutters (trapdoors)
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), windowMaterial));
                    if (y == 2) {
                        blocks.add(new BlockPlacement(start.offset(x, y, -1), trapdoorMaterial));
                    }
                } else {
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), wallMaterial));
                }

                // Back wall with more windows
                if (y >= 2 && y <= height - 1 && (x == 2 || x == width / 2 || x == width - 3)) {
                    blocks.add(new BlockPlacement(start.offset(x, y, depth - 1), windowMaterial));
                } else {
                    blocks.add(new BlockPlacement(start.offset(x, y, depth - 1), wallMaterial));
                }
            }

            // Side walls with windows
            for (int z = 1; z < depth - 1; z++) {
                if (y >= 2 && y <= height - 1 && (z % 3 == 1)) {
                    blocks.add(new BlockPlacement(start.offset(0, y, z), windowMaterial));
                    blocks.add(new BlockPlacement(start.offset(width - 1, y, z), windowMaterial));
                } else {
                    blocks.add(new BlockPlacement(start.offset(0, y, z), wallMaterial));
                    blocks.add(new BlockPlacement(start.offset(width - 1, y, z), wallMaterial));
                }
            }
        }

        // Balcony on upper floor
        if (hasBalcony) {
            int balconyZ = depth / 2;
            int balconyY = height - 1;
            // Balcony floor
            for (int x = 2; x < width - 2; x++) {
                blocks.add(new BlockPlacement(start.offset(x, balconyY, balconyZ), slabMaterial(wallMaterial)));
            }
            // Balcony railing
            for (int x = 2; x < width - 2; x++) {
                blocks.add(new BlockPlacement(start.offset(x, balconyY + 1, balconyZ), fenceMaterial));
            }
            // Door to balcony
            blocks.add(new BlockPlacement(start.offset(width / 2, balconyY, balconyZ - 1), Blocks.AIR));
            blocks.add(new BlockPlacement(start.offset(width / 2, balconyY, balconyZ), doorMaterial));
        }

        // Chimney
        if (hasChimney) {
            int chimneyX = width - 2;
            int chimneyZ = depth - 2;
            for (int y = 1; y <= height + 4; y++) {
                blocks.add(new BlockPlacement(start.offset(chimneyX, y, chimneyZ), chimneyMaterial));
            }
            // Chimney top detail
            blocks.add(new BlockPlacement(start.offset(chimneyX, height + 5, chimneyZ), chimneyMaterial));
            blocks.add(new BlockPlacement(start.offset(chimneyX + 1, height + 4, chimneyZ), chimneyMaterial));
            blocks.add(new BlockPlacement(start.offset(chimneyX - 1, height + 4, chimneyZ), chimneyMaterial));
            blocks.add(new BlockPlacement(start.offset(chimneyX, height + 4, chimneyZ + 1), chimneyMaterial));
            blocks.add(new BlockPlacement(start.offset(chimneyX, height + 4, chimneyZ - 1), chimneyMaterial));
        }

        // Pyramid roof with variations
        int roofStartHeight = height + 1;
        int roofLayers = Math.max(width, depth) / 2 + 1;

        for (int layer = 0; layer < roofLayers; layer++) {
            int currentHeight = roofStartHeight + layer;
            int inset = layer;

            if (width - 2 * inset <= 0 || depth - 2 * inset <= 0) {
                break;
            }

            boolean isTopLayer = (width - 2 * inset <= 2 || depth - 2 * inset <= 2);

            for (int x = inset; x < width - inset; x++) {
                for (int z = inset; z < depth - inset; z++) {
                    if (isTopLayer || x == inset || x == width - 1 - inset ||
                        z == inset || z == depth - 1 - inset) {
                        blocks.add(new BlockPlacement(start.offset(x, currentHeight, z), roofMaterial));
                    }
                }
            }
        }

        // Dormer windows
        if (hasDormer && roofLayers > 2) {
            int dormerY = roofStartHeight + 1;
            int dormerX = width / 2;
            // Front dormer
            blocks.add(new BlockPlacement(start.offset(dormerX - 1, dormerY, 0), wallMaterial));
            blocks.add(new BlockPlacement(start.offset(dormerX, dormerY, 0), windowMaterial));
            blocks.add(new BlockPlacement(start.offset(dormerX + 1, dormerY, 0), wallMaterial));
            blocks.add(new BlockPlacement(start.offset(dormerX - 1, dormerY + 1, 0), wallMaterial));
            blocks.add(new BlockPlacement(start.offset(dormerX, dormerY + 1, 0), wallMaterial));
            blocks.add(new BlockPlacement(start.offset(dormerX + 1, dormerY + 1, 0), wallMaterial));
            // Dormer roof
            for (int x = dormerX - 1; x <= dormerX + 1; x++) {
                blocks.add(new BlockPlacement(start.offset(x, dormerY + 2, 0), roofMaterial));
                blocks.add(new BlockPlacement(start.offset(x, dormerY + 2, 1), roofMaterial));
            }
        }

        // Exterior decorations - lanterns
        blocks.add(new BlockPlacement(start.offset(doorX - 1, 2, -1), lanternMaterial));
        blocks.add(new BlockPlacement(start.offset(doorX + 1, 2, -1), lanternMaterial));

        // Flower boxes under windows
        for (int x = 2; x < width - 2; x += 3) {
            if (x != doorX) {
                blocks.add(new BlockPlacement(start.offset(x, 1, -1), Blocks.FLOWER_POT));
            }
        }

        return blocks;
    }
    
    private static Block slabMaterial(Block baseBlock) {
        // Simple mapping for common blocks to their slab variants
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
        return Blocks.OAK_SLAB;
    }

    private static List<BlockPlacement> buildCastle(BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        Block stoneMaterial = style != null ? style.getPillarBlock() : Blocks.STONE_BRICKS;
        Block wallMaterial = style != null ? style.getWallBlock() : Blocks.COBBLESTONE;
        Block windowMaterial = style != null ? style.getWindowBlock() : Blocks.GLASS_PANE;

        // Main structure
        for (int y = 0; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1);
                    boolean isCorner = (x <= 2 || x >= width - 3) && (z <= 2 || z >= depth - 3);

                    if (y == 0) {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), stoneMaterial));
                    } else if (isEdge && !isCorner) {
                        if (x == width / 2 && z == 0 && y <= 3) {
                            if (y >= 1 && y <= 3 && x >= width / 2 - 1 && x <= width / 2 + 1) {
                                blocks.add(new BlockPlacement(start.offset(x, y, 0), Blocks.AIR));
                            }
                        } else if (y % 4 == 2 && !isCorner) {
                            blocks.add(new BlockPlacement(start.offset(x, y, z), windowMaterial));
                        } else {
                            blocks.add(new BlockPlacement(start.offset(x, y, z), wallMaterial));
                        }
                    }
                }
            }
        }

        // Corner towers
        int towerHeight = height + 6;
        int towerSize = 3;
        int[][] corners = {{0, 0}, {width - towerSize, 0}, {0, depth - towerSize}, {width - towerSize, depth - towerSize}};

        for (int[] corner : corners) {
            for (int y = 0; y <= towerHeight; y++) {
                for (int dx = 0; dx < towerSize; dx++) {
                    for (int dz = 0; dz < towerSize; dz++) {
                        boolean isTowerEdge = (dx == 0 || dx == towerSize - 1 || dz == 0 || dz == towerSize - 1);

                        if (y == 0 || isTowerEdge) {
                            blocks.add(new BlockPlacement(start.offset(corner[0] + dx, y, corner[1] + dz), stoneMaterial));
                        }

                        if (y % 5 == 3 && isTowerEdge && (dx == towerSize / 2 || dz == towerSize / 2)) {
                            blocks.add(new BlockPlacement(start.offset(corner[0] + dx, y, corner[1] + dz), windowMaterial));
                        }
                    }
                }
            }

            // Tower crenellations
            for (int dx = 0; dx < towerSize; dx++) {
                for (int dz = 0; dz < towerSize; dz++) {
                    if (dx % 2 == 0 || dz % 2 == 0) {
                        blocks.add(new BlockPlacement(start.offset(corner[0] + dx, towerHeight + 1, corner[1] + dz), stoneMaterial));
                    }
                }
            }
        }

        // Wall crenellations
        for (int x = 0; x < width; x += 2) {
            blocks.add(new BlockPlacement(start.offset(x, height + 1, 0), stoneMaterial));
            blocks.add(new BlockPlacement(start.offset(x, height + 2, 0), stoneMaterial));
            blocks.add(new BlockPlacement(start.offset(x, height + 1, depth - 1), stoneMaterial));
            blocks.add(new BlockPlacement(start.offset(x, height + 2, depth - 1), stoneMaterial));
        }

        for (int z = 0; z < depth; z += 2) {
            blocks.add(new BlockPlacement(start.offset(0, height + 1, z), stoneMaterial));
            blocks.add(new BlockPlacement(start.offset(0, height + 2, z), stoneMaterial));
            blocks.add(new BlockPlacement(start.offset(width - 1, height + 1, z), stoneMaterial));
            blocks.add(new BlockPlacement(start.offset(width - 1, height + 2, z), stoneMaterial));
        }

        return blocks;
    }

    private static List<BlockPlacement> buildAdvancedTower(BlockPos start, int width, int height, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        Block wallMaterial = style != null ? style.getWallBlock() : Blocks.STONE_BRICKS;
        Block accentMaterial = style != null ? style.getPillarBlock() : Blocks.CHISELED_STONE_BRICKS;
        Block windowMaterial = style != null ? style.getWindowBlock() : Blocks.GLASS_PANE;
        Block roofMaterial = style != null ? style.getRoofBlock() : Blocks.DARK_OAK_STAIRS;
        Block lanternMaterial = style != null ? style.getLanternBlock() : Blocks.LANTERN;
        Block fenceMaterial = style != null ? style.getFenceBlock() : Blocks.OAK_FENCE;

        // Random tower variations
        boolean hasMultipleLevels = height > 12 && random.nextBoolean();
        boolean hasObservationDeck = height > 15 && random.nextBoolean();
        boolean hasSpire = random.nextBoolean();
        int levels = hasMultipleLevels ? 3 : 1;
        int levelHeight = height / levels;

        // Main tower body with levels
        for (int level = 0; level < levels; level++) {
            int levelStartY = level * levelHeight;
            int levelEndY = (level + 1) * levelHeight;
            int levelWidth = Math.max(3, width - level);
            
            for (int y = levelStartY; y < levelEndY && y < height; y++) {
                for (int x = 0; x < levelWidth; x++) {
                    for (int z = 0; z < levelWidth; z++) {
                        boolean isEdge = (x == 0 || x == levelWidth - 1 || z == 0 || z == levelWidth - 1);
                        boolean isCorner = (x == 0 || x == levelWidth - 1) && (z == 0 || z == levelWidth - 1);
                        int actualX = x + (width - levelWidth) / 2;
                        int actualZ = z + (width - levelWidth) / 2;

                        if (y == levelStartY) {
                            blocks.add(new BlockPlacement(start.offset(actualX, y, actualZ), wallMaterial));
                        } else if (isEdge) {
                            if ((y - levelStartY) % 4 == 2 && !isCorner && 
                                (x == levelWidth / 2 || z == levelWidth / 2)) {
                                blocks.add(new BlockPlacement(start.offset(actualX, y, actualZ), windowMaterial));
                            } else if (isCorner) {
                                blocks.add(new BlockPlacement(start.offset(actualX, y, actualZ), accentMaterial));
                            } else {
                                blocks.add(new BlockPlacement(start.offset(actualX, y, actualZ), wallMaterial));
                            }
                        }
                    }
                }
            }
            
            // Level separator/overhang
            if (level < levels - 1) {
                int separatorY = levelEndY;
                int sepWidth = levelWidth + 2;
                for (int x = 0; x < sepWidth; x++) {
                    for (int z = 0; z < sepWidth; z++) {
                        int actualX = x + (width - sepWidth) / 2;
                        int actualZ = z + (width - sepWidth) / 2;
                        if (x == 0 || x == sepWidth - 1 || z == 0 || z == sepWidth - 1) {
                            blocks.add(new BlockPlacement(start.offset(actualX, separatorY, actualZ), accentMaterial));
                        }
                    }
                }
            }
        }

        // Observation deck
        if (hasObservationDeck) {
            int deckY = height - 2;
            int deckWidth = width + 2;
            for (int x = 0; x < deckWidth; x++) {
                for (int z = 0; z < deckWidth; z++) {
                    int actualX = x - 1;
                    int actualZ = z - 1;
                    if (x == 0 || x == deckWidth - 1 || z == 0 || z == deckWidth - 1) {
                        blocks.add(new BlockPlacement(start.offset(actualX, deckY, actualZ), slabMaterial(wallMaterial)));
                        blocks.add(new BlockPlacement(start.offset(actualX, deckY + 1, actualZ), fenceMaterial));
                    }
                }
            }
            blocks.add(new BlockPlacement(start.offset(-1, deckY + 1, -1), lanternMaterial));
            blocks.add(new BlockPlacement(start.offset(width, deckY + 1, -1), lanternMaterial));
            blocks.add(new BlockPlacement(start.offset(-1, deckY + 1, width), lanternMaterial));
            blocks.add(new BlockPlacement(start.offset(width, deckY + 1, width), lanternMaterial));
        }

        // Pyramid roof or spire
        if (hasSpire) {
            int spireHeight = height + width;
            int centerX = width / 2;
            int centerZ = width / 2;
            for (int y = height; y < spireHeight; y++) {
                int shrink = (y - height) / 2;
                if (centerX - shrink >= 0 && centerZ - shrink >= 0) {
                    blocks.add(new BlockPlacement(start.offset(centerX - shrink, y, centerZ - shrink), accentMaterial));
                    blocks.add(new BlockPlacement(start.offset(centerX + shrink, y, centerZ + shrink), accentMaterial));
                    blocks.add(new BlockPlacement(start.offset(centerX - shrink, y, centerZ + shrink), accentMaterial));
                    blocks.add(new BlockPlacement(start.offset(centerX + shrink, y, centerZ - shrink), accentMaterial));
                }
            }
            blocks.add(new BlockPlacement(start.offset(centerX, spireHeight, centerZ), lanternMaterial));
        } else {
            for (int i = 0; i < width / 2 + 1; i++) {
                for (int x = i; x < width - i; x++) {
                    for (int z = i; z < width - i; z++) {
                        if (x == i || x == width - 1 - i || z == i || z == width - 1 - i) {
                            blocks.add(new BlockPlacement(start.offset(x, height + i, z), roofMaterial));
                        }
                    }
                }
            }
        }

        return blocks;
    }

    private static List<BlockPlacement> buildModernHouse(BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        Block wallMaterial = Blocks.QUARTZ_BLOCK;
        Block floorMaterial = Blocks.SMOOTH_STONE;
        Block glassMaterial = Blocks.GLASS;
        Block roofMaterial = Blocks.DARK_OAK_PLANKS;

        // Floor
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
            }
        }

        // Modern walls with lots of glass
        for (int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x % 2 == 0 || y > 1) {
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), glassMaterial));
                } else {
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), wallMaterial));
                }

                blocks.add(new BlockPlacement(start.offset(x, y, depth - 1), wallMaterial));
            }

            for (int z = 1; z < depth - 1; z++) {
                if (z % 3 == 1 && y == 2) {
                    blocks.add(new BlockPlacement(start.offset(0, y, z), glassMaterial));
                    blocks.add(new BlockPlacement(start.offset(width - 1, y, z), glassMaterial));
                } else {
                    blocks.add(new BlockPlacement(start.offset(0, y, z), wallMaterial));
                    blocks.add(new BlockPlacement(start.offset(width - 1, y, z), wallMaterial));
                }
            }
        }

        // Flat roof
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, height, z), roofMaterial));
            }
        }

        return blocks;
    }

    private static List<BlockPlacement> buildBarn(BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        Block woodMaterial = style != null ? style.getWallBlock() : Blocks.OAK_PLANKS;
        Block logMaterial = style != null ? style.getPillarBlock() : Blocks.OAK_LOG;
        Block roofMaterial = style != null ? style.getRoofBlock() : Blocks.SPRUCE_PLANKS;

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

                if (x >= width / 3 && x <= 2 * width / 3 && y <= 2) {
                    continue; // Large door opening
                }

                blocks.add(new BlockPlacement(start.offset(x, y, 0), material));
                blocks.add(new BlockPlacement(start.offset(x, y, depth - 1), material));
            }

            for (int z = 1; z < depth - 1; z++) {
                blocks.add(new BlockPlacement(start.offset(0, y, z), logMaterial));
                blocks.add(new BlockPlacement(start.offset(width - 1, y, z), logMaterial));
            }
        }

        // Peaked roof
        int roofPeakHeight = height + width / 2;
        for (int x = 0; x < width; x++) {
            int distFromCenter = Math.abs(x - width / 2);
            int roofY = roofPeakHeight - distFromCenter;

            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, roofY, z), roofMaterial));
            }
        }

        return blocks;
    }

    private static List<BlockPlacement> buildWall(BlockPos start, int width, int height, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        Block material = style != null ? style.getWallBlock() : getMaterial(materials, 0);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                blocks.add(new BlockPlacement(start.offset(x, y, 0), material));
            }
        }

        return blocks;
    }

    private static List<BlockPlacement> buildPlatform(BlockPos start, int width, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        Block material = style != null ? style.getFloorBlock() : getMaterial(materials, 0);

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), material));
            }
        }

        return blocks;
    }

    private static List<BlockPlacement> buildBox(BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        List<BlockPlacement> blocks = new ArrayList<>();
        Block material = style != null ? style.getWallBlock() : getMaterial(materials, 0);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    blocks.add(new BlockPlacement(start.offset(x, y, z), material));
                }
            }
        }

        return blocks;
    }
}
