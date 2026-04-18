package com.steve.ai.action;

import com.steve.ai.SteveMod;
import com.steve.ai.structure.BlockPlacement;
import net.minecraft.core.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages collaborative building where multiple Steves work on DIFFERENT SECTIONS of the same structure
 */
public class CollaborativeBuildManager {
    
    public static class CollaborativeBuild {
        public final String structureId;
        public final String structureType;
        public final List<BlockPlacement> buildPlan;
        private final List<BuildSection> sections;
        private final Map<String, Integer> steveToSectionMap;
        private final AtomicInteger nextSectionIndex;
        public final Set<String> participatingSteves;
        public final BlockPos startPos;
        private String styleName;  // 建筑风格名称

        public CollaborativeBuild(String structureId, String structureType, List<BlockPlacement> buildPlan, BlockPos startPos) {
            this(structureId, structureType, buildPlan, startPos, null);
        }

        public CollaborativeBuild(String structureId, String structureType, List<BlockPlacement> buildPlan, BlockPos startPos, String styleName) {
            this.structureId = structureId;
            this.structureType = structureType;
            this.buildPlan = new ArrayList<>(buildPlan);
            this.participatingSteves = ConcurrentHashMap.newKeySet();
            this.startPos = startPos;
            this.steveToSectionMap = new ConcurrentHashMap<>();
            this.nextSectionIndex = new AtomicInteger(0);
            this.styleName = styleName;
            this.sections = divideBuildIntoSections(buildPlan);
            
            SteveMod.LOGGER.info("Divided '{}' into {} sections for collaborative building", 
                structureId, sections.size());
        }
        
        /**
         * Divide the build into 4 QUADRANTS (NW, NE, SW, SE)
         * Each quadrant is sorted BOTTOM-TO-TOP so each Steve builds their quadrant from the ground up
         */
        private List<BuildSection> divideBuildIntoSections(List<BlockPlacement> plan) {
            if (plan.isEmpty()) {
                return new ArrayList<>();
            }
            
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            
            for (BlockPlacement placement : plan) {
                minX = Math.min(minX, placement.pos.getX());
                maxX = Math.max(maxX, placement.pos.getX());
                minZ = Math.min(minZ, placement.pos.getZ());
                maxZ = Math.max(maxZ, placement.pos.getZ());
            }
            
            int centerX = (minX + maxX) / 2;
            int centerZ = (minZ + maxZ) / 2;
            
            List<BlockPlacement> northWest = new ArrayList<>();
            List<BlockPlacement> northEast = new ArrayList<>();
            List<BlockPlacement> southWest = new ArrayList<>();
            List<BlockPlacement> southEast = new ArrayList<>();
            
            for (BlockPlacement placement : plan) {
                int x = placement.pos.getX();
                int z = placement.pos.getZ();

                if (x < centerX) {
                    if (z < centerZ) {
                        northWest.add(placement);
                    } else {
                        southWest.add(placement);
                    }
                } else {
                    if (z < centerZ) {
                        northEast.add(placement);
                    } else {
                        southEast.add(placement);
                    }
                }
            }
            Comparator<BlockPlacement> bottomToTop = Comparator.comparingInt(p -> p.pos.getY());
            northWest.sort(bottomToTop);
            northEast.sort(bottomToTop);
            southWest.sort(bottomToTop);
            southEast.sort(bottomToTop);
            
            List<BuildSection> sectionList = new ArrayList<>();
            if (!northWest.isEmpty()) sectionList.add(new BuildSection(0, northWest, "NORTH-WEST"));
            if (!northEast.isEmpty()) sectionList.add(new BuildSection(1, northEast, "NORTH-EAST"));
            if (!southWest.isEmpty()) sectionList.add(new BuildSection(2, southWest, "SOUTH-WEST"));
            if (!southEast.isEmpty()) sectionList.add(new BuildSection(3, southEast, "SOUTH-EAST"));
            
            SteveMod.LOGGER.info("Divided structure into {} quadrants (BOTTOM-TO-TOP): NW={}, NE={}, SW={}, SE={} blocks", 
                sectionList.size(), northWest.size(), northEast.size(), southWest.size(), southEast.size());
            
            return sectionList;
        }
        
        public int getTotalBlocks() {
            return buildPlan.size();
        }
        
        public int getBlocksPlaced() {
            int total = 0;
            for (BuildSection section : sections) {
                total += section.getBlocksPlaced();
            }
            return total;
        }
        
        public boolean isComplete() {
            for (BuildSection section : sections) {
                if (!section.isComplete()) {
                    return false;
                }
            }
            return true;
        }
        
        public int getProgressPercentage() {
            if (buildPlan.isEmpty()) {
                return 100;
            }
            return (getBlocksPlaced() * 100) / buildPlan.size();
        }

        /**
         * Remove a Steve from this build
         */
        public void removeSteve(String steveName) {
            steveToSectionMap.remove(steveName);
            participatingSteves.remove(steveName);
        }

        /**
         * Get the style name of this build
         */
        public String getStyleName() {
            return styleName;
        }

        /**
         * Set the style name of this build
         */
        public void setStyleName(String styleName) {
            this.styleName = styleName;
        }
    }
    
    /**
     * A section of the build that one Steve works on (represents a spatial quadrant)
     */
    public static class BuildSection {
        public final int yLevel; // Used as section ID
        public final String sectionName;
        private final List<BlockPlacement> blocks;
        private final AtomicInteger nextBlockIndex;
        
        public BuildSection(int sectionId, List<BlockPlacement> blocks, String sectionName) {
            this.yLevel = sectionId;
            this.sectionName = sectionName;
            this.blocks = blocks;
            this.nextBlockIndex = new AtomicInteger(0);
        }
        
        public BlockPlacement getNextBlock() {
            int index = nextBlockIndex.getAndIncrement();
            if (index < blocks.size()) {
                return blocks.get(index);
            }
            return null;
        }
        
        public int getBlocksPlaced() {
            return Math.min(nextBlockIndex.get(), blocks.size());
        }
        
        public boolean isComplete() {
            return nextBlockIndex.get() >= blocks.size();
        }
        
        public int getTotalBlocks() {
            return blocks.size();
        }
    }

    private static final Map<String, CollaborativeBuild> activeBuilds = new ConcurrentHashMap<>();
    
    /**
     * Register a new collaborative build project (with style)
     */
    public static CollaborativeBuild registerBuild(String structureType, List<BlockPlacement> buildPlan, BlockPos startPos, String styleName) {
        String structureId = structureType + "_" + System.currentTimeMillis();
        CollaborativeBuild build = new CollaborativeBuild(structureId, structureType, buildPlan, startPos, styleName);
        activeBuilds.put(structureId, build);
        
        SteveMod.LOGGER.info("Registered collaborative build '{}' at {} with {} blocks, style: {}", 
            structureType, startPos, buildPlan.size(), styleName != null ? styleName : "default");
        
        return build;
    }

    /**
     * Register a new collaborative build project (backward compatible, no style specified)
     */
    public static CollaborativeBuild registerBuild(String structureType, List<BlockPlacement> buildPlan, BlockPos startPos) {
        return registerBuild(structureType, buildPlan, startPos, null);
    }
    
    /**
     * Get the next block for a Steve to place (each Steve works on their own section)
     * Returns null if Steve's section is complete
     */
    public static BlockPlacement getNextBlock(CollaborativeBuild build, String steveName) {
        if (build.isComplete()) {
            return null;
        }

        build.participatingSteves.add(steveName);

        synchronized(build) {
            // Assign Steve to a section if not already assigned
            Integer sectionIndex = build.steveToSectionMap.get(steveName);
            if (sectionIndex == null) {
                sectionIndex = assignSteveToSection(build, steveName);
                if (sectionIndex == null) {
                    // No sections available
                    return null;
                }
            }

            BuildSection section = build.sections.get(sectionIndex);
            BlockPlacement block = section.getNextBlock();

            // If current section is complete, try to find another incomplete section
            if (block == null) {
                SteveMod.LOGGER.info("Steve '{}' completed section {} ({}), looking for another section",
                    steveName, sectionIndex, section.sectionName);

                // Search for another incomplete section
                for (int i = 0; i < build.sections.size(); i++) {
                    BuildSection otherSection = build.sections.get(i);
                    if (!otherSection.isComplete()) {
                        // Reassign Steve to this section
                        build.steveToSectionMap.put(steveName, i);
                        SteveMod.LOGGER.info("Reassigned Steve '{}' from section {} to section {} ({}) - {} blocks remaining",
                            steveName, sectionIndex, i, otherSection.sectionName,
                            otherSection.getTotalBlocks() - otherSection.getBlocksPlaced());

                        // Get block from the new section
                        block = otherSection.getNextBlock();
                        if (block != null) {
                            return block;
                        }
                    }
                }

                // All sections are complete
                SteveMod.LOGGER.info("Steve '{}' has no more sections to work on - build is complete", steveName);
            }

            return block;
        }
    }
    
    /**
     * Assign a Steve to a section (quadrant) that needs work
     * Prioritizes unassigned sections, but allows helping on large sections
     * Returns the section index, or null if all sections are complete
     */
    private static synchronized Integer assignSteveToSection(CollaborativeBuild build, String steveName) {
        // First pass: Find a section that isn't complete and isn't already assigned
        for (int i = 0; i < build.sections.size(); i++) {
            BuildSection section = build.sections.get(i);
            if (!section.isComplete()) {
                boolean alreadyAssigned = build.steveToSectionMap.containsValue(i);

                if (!alreadyAssigned) {
                    build.steveToSectionMap.put(steveName, i);
                    SteveMod.LOGGER.info("Assigned Steve '{}' to {} quadrant - will build {} blocks BOTTOM-TO-TOP",
                        steveName, section.sectionName, section.getTotalBlocks());
                    return i;
                }
            }
        }

        // Second pass: Help with any incomplete section (even if assigned to someone else)
        for (int i = 0; i < build.sections.size(); i++) {
            BuildSection section = build.sections.get(i);
            if (!section.isComplete()) {
                build.steveToSectionMap.put(steveName, i);
                SteveMod.LOGGER.info("Steve '{}' helping with {} quadrant ({} blocks remaining)",
                    steveName, section.sectionName, section.getTotalBlocks() - section.getBlocksPlaced());
                return i;
            }
        }

        // All sections complete
        return null;
    }
    
    /**
     * Get an active build by ID
     */
    public static CollaborativeBuild getBuild(String structureId) {
        return activeBuilds.get(structureId);
    }
    
    /**
     * Complete and remove a build
     */
    public static void completeBuild(String structureId) {
        CollaborativeBuild build = activeBuilds.remove(structureId);
        if (build != null) {
            SteveMod.LOGGER.info("Collaborative build '{}' completed - {} Steves participated, {} blocks placed",
                structureId, build.participatingSteves.size(), build.getBlocksPlaced());
            synchronized(build) {
                build.steveToSectionMap.clear();
                build.participatingSteves.clear();
                build.sections.clear();
            }
        }
    }
    
    /**
     * Check if there's an active build of a structure type
     */
    public static CollaborativeBuild findActiveBuild(String structureType) {
        for (CollaborativeBuild build : activeBuilds.values()) {
            if (build.structureType.equalsIgnoreCase(structureType) && !build.isComplete()) {
                return build;
            }
        }
        return null;
    }
    
    /**
     * Clean up completed builds
     */
    public static void cleanupCompletedBuilds() {
        activeBuilds.entrySet().removeIf(entry -> entry.getValue().isComplete());
    }

    /**
     * Remove a Steve from all active collaborative builds
     * Called when a Steve is removed/despawned
     */
    public static void removeSteveFromAllBuilds(String steveName) {
        for (CollaborativeBuild build : activeBuilds.values()) {
            synchronized(build) {
                build.removeSteve(steveName);
                // If no participants remain, clean up the build
                if (build.participatingSteves.isEmpty()) {
                    activeBuilds.remove(build.structureId);
                    SteveMod.LOGGER.info("Collaborative build '{}' abandoned - no participants remaining", build.structureId);
                }
            }
        }
    }

    /**
     * Remove a Steve from a specific collaborative build
     */
    public static void removeSteveFromBuild(CollaborativeBuild build, String steveName) {
        if (build != null) {
            synchronized(build) {
                build.removeSteve(steveName);
                if (build.participatingSteves.isEmpty()) {
                    activeBuilds.remove(build.structureId);
                    SteveMod.LOGGER.info("Collaborative build '{}' abandoned - no participants remaining", build.structureId);
                }
            }
        }
    }

    /**
     * Get all used styles from active builds
     */
    public static Set<String> getUsedStyles() {
        Set<String> usedStyles = new HashSet<>();
        for (CollaborativeBuild build : activeBuilds.values()) {
            if (build.getStyleName() != null) {
                usedStyles.add(build.getStyleName());
            }
        }
        return usedStyles;
    }
}

