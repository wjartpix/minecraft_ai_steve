package com.steve.ai.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for procedural structure generation.
 * Contains algorithms for generating various building types.
 */
public class StructureGenerators {

    public static List<BlockPlacement> generate(String structureType, BlockPos start, int width, int height, int depth, BuildingStyle style, List<Block> materials) {
        return switch (structureType.toLowerCase()) {
            case "house", "home" -> buildAdvancedHouse(start, width, height, depth, style, materials);
            case "castle", "catle", "fort" -> buildCastle(start, width, height, depth, style, materials);
            case "tower" -> buildAdvancedTower(start, width, height, style, materials);
            case "wall" -> buildWall(start, width, height, style, materials);
            case "platform" -> buildPlatform(start, width, depth, style, materials);
            case "barn", "shed" -> buildBarn(start, width, height, depth, style, materials);
            case "modern", "modern_house" -> buildModernHouse(start, width, height, depth, style, materials);
            case "box", "cube" -> buildBox(start, width, height, depth, style, materials);
            default -> buildAdvancedHouse(start, Math.max(5, width), Math.max(4, height), Math.max(5, depth), style, materials);
        };
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

        if (roofMaterial == Blocks.GLASS || roofMaterial == Blocks.GLASS_PANE) {
            roofMaterial = Blocks.OAK_PLANKS;
        }

        // Floor
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                blocks.add(new BlockPlacement(start.offset(x, 0, z), floorMaterial));
            }
        }

        // Walls with windows and door
        for (int y = 1; y <= height; y++) {
            for (int x = 0; x < width; x++) {
                // Front wall
                if (x == width / 2 && y <= 2) {
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), doorMaterial));
                } else if (y >= 2 && y <= height - 1 && (x == 2 || x == width - 3)) {
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), windowMaterial));
                } else {
                    blocks.add(new BlockPlacement(start.offset(x, y, 0), wallMaterial));
                }

                // Back wall
                if (y >= 2 && y <= height - 1 && (x == 2 || x == width / 2 || x == width - 3)) {
                    blocks.add(new BlockPlacement(start.offset(x, y, depth - 1), windowMaterial));
                } else {
                    blocks.add(new BlockPlacement(start.offset(x, y, depth - 1), wallMaterial));
                }
            }

            // Side walls
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

        // Pyramid roof
        int roofStartHeight = height + 1;
        int roofLayers = Math.max(width, depth) / 2 + 1;

        for (int layer = 0; layer < roofLayers; layer++) {
            int currentHeight = roofStartHeight + layer;
            int inset = layer;

            // Check if current layer has space to generate blocks
            if (width - 2 * inset <= 0 || depth - 2 * inset <= 0) {
                break;
            }

            // If current layer width or depth is small, fill the entire layer (top cap)
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

        return blocks;
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

        // Main tower body
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < width; z++) {
                    boolean isEdge = (x == 0 || x == width - 1 || z == 0 || z == width - 1);
                    boolean isCorner = (x == 0 || x == width - 1) && (z == 0 || z == width - 1);

                    if (y == 0) {
                        blocks.add(new BlockPlacement(start.offset(x, y, z), wallMaterial));
                    } else if (isEdge) {
                        if (y % 3 == 2 && !isCorner && (x == width / 2 || z == width / 2)) {
                            blocks.add(new BlockPlacement(start.offset(x, y, z), windowMaterial));
                        } else if (isCorner) {
                            blocks.add(new BlockPlacement(start.offset(x, y, z), accentMaterial));
                        } else {
                            blocks.add(new BlockPlacement(start.offset(x, y, z), wallMaterial));
                        }
                    }
                }
            }
        }

        // Pyramid roof
        for (int i = 0; i < width / 2 + 1; i++) {
            for (int x = i; x < width - i; x++) {
                for (int z = i; z < width - i; z++) {
                    if (x == i || x == width - 1 - i || z == i || z == width - 1 - i) {
                        blocks.add(new BlockPlacement(start.offset(x, height + i, z), roofMaterial));
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
