package com.steve.ai.action.actions;

import com.steve.ai.action.ActionResult;
import com.steve.ai.action.Task;
import com.steve.ai.config.SteveConfig;
import com.steve.ai.entity.SteveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombatAction extends BaseAction {
    private String targetType;
    private LivingEntity target; // Current attack target
    private List<LivingEntity> targetQueue = new ArrayList<>(); // Queue of targets to attack
    private int ticksRunning;
    private int ticksStuck;
    private double lastX, lastZ;
    private static final int MAX_TICKS = 7200; // 6 minutes timeout
    private static final double ATTACK_RANGE = 3.5;
    private static final double GROUND_SEARCH_RANGE = 5.0; // Search for ground within 5 blocks vertically
    private Player protectedPlayer; // The player we're protecting (center of search)
    private double playerSearchRadius; // Configurable search radius

    // Creature aliases: alias -> resolved type or group
    private static final Map<String, String> CREATURE_ALIASES = new HashMap<>() {{
        // General keywords -> hostile group
        put("mob", "hostile");
        put("mobs", "hostile");
        put("hostile", "hostile");
        put("monster", "hostile");
        put("monsters", "hostile");
        put("enemy", "hostile");
        put("enemies", "hostile");
        put("any", "hostile");
        put("all", "hostile");

        // Group mappings
        put("undead", "group:undead");
        put("dead_things", "group:undead");
        put("ranged", "group:ranged");
        put("flying", "group:flying");
        put("flyer", "group:flying");
        put("nether", "group:nether");
        put("nether_mobs", "group:nether");
        put("boss", "group:boss");
        put("raid", "group:raid");
        put("raiders", "group:raid");

        // Common aliases
        put("skelly", "skeleton");
        put("archer", "skeleton");
        put("skeleton_archer", "skeleton");
        put("bones", "skeleton");
        put("zombies", "zombie");
        put("skeletons", "skeleton");
        put("creepers", "creeper");
        put("spiders", "spider");
        put("endermen", "enderman");
        put("phantoms", "phantom");
        put("witches", "witch");
        
        // Slime aliases
        put("slimes", "slime");
        put("cube", "slime");
        put("cubes", "slime");
        put("magma", "magma_cube");
        put("magma_cubes", "magma_cube");
    }};

    // Creature groups: group name -> list of entity type IDs
    private static final Map<String, List<String>> CREATURE_GROUPS = new HashMap<>() {{
        put("undead", Arrays.asList(
            "zombie", "skeleton", "husk", "stray", "drowned",
            "zombie_villager", "wither_skeleton", "zombified_piglin", "phantom", "zoglin"
        ));
        put("ranged", Arrays.asList(
            "skeleton", "stray", "pillager", "blaze", "ghast", "shulker"
        ));
        put("flying", Arrays.asList(
            "phantom", "ghast", "vex", "breeze"
        ));
        put("nether", Arrays.asList(
            "blaze", "ghast", "wither_skeleton", "magma_cube",
            "piglin_brute", "hoglin", "zombified_piglin", "zoglin"
        ));
        put("boss", Arrays.asList(
            "ender_dragon", "wither", "elder_guardian", "warden"
        ));
        put("raid", Arrays.asList(
            "pillager", "vindicator", "evoker", "ravager", "vex"
        ));
        put("slime", Arrays.asList(
            "slime", "magma_cube"
        ));
    }};

    public CombatAction(SteveEntity steve, Task task) {
        super(steve, task);
    }

    @Override
    protected void onStart() {
        targetType = task.getStringParameter("target");
        ticksRunning = 0;
        ticksStuck = 0;
        targetQueue.clear();
        target = null;
        protectedPlayer = null;

        // Make sure we're not flying (in case we were building)
        steve.setFlying(false);

        steve.setInvulnerableBuilding(true);

        // Load configurable search radius
        playerSearchRadius = SteveConfig.COMBAT_SEARCH_RADIUS.get();

        // If Steve is in the air (e.g., was building), teleport to ground first
        ensureSteveOnGround();

        // Find the nearest player to protect (center of monster search)
        findProtectedPlayer();

        findAllTargets();

        if (targetQueue.isEmpty()) {
            if (protectedPlayer != null) {
                com.steve.ai.SteveMod.LOGGER.warn("Steve '{}' no targets near player '{}' in 200x200 area",
                    steve.getSteveName(), protectedPlayer.getName().getString());
            } else {
                com.steve.ai.SteveMod.LOGGER.warn("Steve '{}' no player found to protect, no targets nearby",
                    steve.getSteveName());
            }
        } else {
            String playerName = protectedPlayer != null ? protectedPlayer.getName().getString() : "unknown";
            com.steve.ai.SteveMod.LOGGER.info("Steve '{}' protecting player '{}', found {} targets of type '{}' in 200x200 area",
                steve.getSteveName(), playerName, targetQueue.size(), targetType);
        }
    }

    /**
     * Ensure Steve is on solid ground before starting combat
     * If Steve is in the air, find the ground below and teleport there
     */
    private void ensureSteveOnGround() {
        BlockPos stevePos = steve.blockPosition();
        
        // Check if Steve is on solid ground
        BlockPos belowPos = stevePos.below();
        boolean onSolidGround = steve.level().getBlockState(belowPos).isSolid();
        
        if (!onSolidGround) {
            // Search downward for solid ground
            for (int dy = 0; dy >= -GROUND_SEARCH_RANGE; dy--) {
                BlockPos checkPos = stevePos.offset(0, dy, 0);
                BlockPos groundPos = checkPos.below();
                
                if (steve.level().getBlockState(groundPos).isSolid() && 
                    steve.level().getBlockState(checkPos).isAir()) {
                    // Found solid ground with air above, teleport there
                    steve.teleportTo(checkPos.getX() + 0.5, checkPos.getY(), checkPos.getZ() + 0.5);
                    com.steve.ai.SteveMod.LOGGER.info("Steve '{}' teleported to ground at {} for combat",
                        steve.getSteveName(), checkPos);
                    return;
                }
            }
            
            // If no ground found below, search around horizontally
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    
                    BlockPos checkPos = stevePos.offset(dx, 0, dz);
                    BlockPos groundPos = checkPos.below();
                    
                    if (steve.level().getBlockState(groundPos).isSolid() && 
                        steve.level().getBlockState(checkPos).isAir()) {
                        steve.teleportTo(checkPos.getX() + 0.5, checkPos.getY(), checkPos.getZ() + 0.5);
                        com.steve.ai.SteveMod.LOGGER.info("Steve '{}' teleported to nearby ground at {} for combat",
                            steve.getSteveName(), checkPos);
                        return;
                    }
                }
            }
            
            com.steve.ai.SteveMod.LOGGER.warn("Steve '{}' could not find solid ground, may be stuck in air",
                steve.getSteveName());
        }
    }

    /**
     * Find the nearest player to protect (center of monster search area)
     */
    private void findProtectedPlayer() {
        java.util.List<? extends Player> players = steve.level().players();
        if (players.isEmpty()) {
            protectedPlayer = null;
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

        protectedPlayer = nearest;
    }

    @Override
    protected void onTick() {
        ticksRunning++;

        if (ticksRunning > MAX_TICKS) {
            // Combat complete - clean up and disable invulnerability
            steve.setInvulnerableBuilding(false);
            steve.setSprinting(false);
            steve.getNavigation().stop();
            com.steve.ai.SteveMod.LOGGER.info("Steve '{}' combat timeout, invulnerability disabled",
                steve.getSteveName());
            result = ActionResult.success("Combat timeout");
            return;
        }

        // Periodically re-scan for new targets in the 200x200 area to catch spawned or missed mobs
        // Also refresh the protected player in case they moved or disconnected
        if (ticksRunning % 40 == 0) {
            findProtectedPlayer();
            findAllTargets();
        }

        // Get next target from queue if current target is invalid
        if (target == null || !target.isAlive() || target.isRemoved()) {
            if (!targetQueue.isEmpty()) {
                target = targetQueue.remove(0);
                com.steve.ai.SteveMod.LOGGER.info("Steve '{}' switched to next target: {} ({} remaining in queue)",
                    steve.getSteveName(), target.getType().toString(), targetQueue.size());
            } else {
                // Queue empty after re-scan, check if truly complete
                findAllTargets();
                if (targetQueue.isEmpty()) {
                    // No more targets in 200x200 area around player - all eliminated
                    String playerName = protectedPlayer != null ? protectedPlayer.getName().getString() : "unknown";
                    steve.setInvulnerableBuilding(false);
                    steve.setSprinting(false);
                    steve.getNavigation().stop();
                    com.steve.ai.SteveMod.LOGGER.info("Steve '{}' cleared all targets in 200x200 area around player '{}', invulnerability disabled",
                        steve.getSteveName(), playerName);
                    result = ActionResult.success("All targets eliminated in 200x200 area around player " + playerName);
                    return;
                }
                // Found new targets, get the first one
                target = targetQueue.remove(0);
            }
        }
        
        double distance = steve.distanceTo(target);
        
        steve.setSprinting(true);
        steve.getNavigation().moveTo(target, 2.5); // High speed multiplier for sprinting
        
        double currentX = steve.getX();
        double currentZ = steve.getZ();
        if (Math.abs(currentX - lastX) < 0.1 && Math.abs(currentZ - lastZ) < 0.1) {
            ticksStuck++;
            
            if (ticksStuck > 40 && distance > ATTACK_RANGE) {
                // Teleport 4 blocks closer to target
                double dx = target.getX() - steve.getX();
                double dz = target.getZ() - steve.getZ();
                double dist = Math.sqrt(dx*dx + dz*dz);
                double moveAmount = Math.min(4.0, dist - ATTACK_RANGE);
                
                steve.teleportTo(
                    steve.getX() + (dx/dist) * moveAmount,
                    steve.getY(),
                    steve.getZ() + (dz/dist) * moveAmount
                );
                ticksStuck = 0;
                com.steve.ai.SteveMod.LOGGER.info("Steve '{}' was stuck, teleported closer to target", 
                    steve.getSteveName());
            }
        } else {
            ticksStuck = 0;
        }
        lastX = currentX;
        lastZ = currentZ;
        
        if (distance <= ATTACK_RANGE) {
            steve.doHurtTarget(target);
            steve.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
            
            // Attack 3 times per second (every 6-7 ticks)
            if (ticksRunning % 7 == 0) {
                steve.doHurtTarget(target);
            }
        }
    }

    @Override
    protected void onCancel() {
        steve.setInvulnerableBuilding(false);
        steve.getNavigation().stop();
        steve.setSprinting(false);
        steve.setFlying(false);
        target = null;
        targetQueue.clear();
        com.steve.ai.SteveMod.LOGGER.info("Steve '{}' combat cancelled, invulnerability disabled",
            steve.getSteveName());
    }

    @Override
    public String getDescription() {
        return "Attack " + targetType;
    }

    /**
     * Find all valid targets and populate the target queue
     * Searches in a 200x200 area (100 block radius from the protected player)
     * This ensures we protect the player by clearing all monsters around them
     */
    private void findAllTargets() {
        // Use protected player as center of search, or fall back to Steve's position
        double centerX, centerY, centerZ;
        if (protectedPlayer != null) {
            centerX = protectedPlayer.getX();
            centerY = protectedPlayer.getY();
            centerZ = protectedPlayer.getZ();
        } else {
            centerX = steve.getX();
            centerY = steve.getY();
            centerZ = steve.getZ();
        }

        // Create search box centered on player (configurable radius)
        AABB searchBox = new AABB(
            centerX - playerSearchRadius, centerY - playerSearchRadius, centerZ - playerSearchRadius,
            centerX + playerSearchRadius, centerY + playerSearchRadius, centerZ + playerSearchRadius
        );

        List<Entity> entities = steve.level().getEntities(steve, searchBox);

        targetQueue.clear();

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living && isValidTarget(living)) {
                targetQueue.add(living);
            }
        }

        // Sort by distance to Steve (so Steve attacks closest targets first for efficiency)
        targetQueue.sort((a, b) -> {
            double distA = steve.distanceTo(a);
            double distB = steve.distanceTo(b);
            return Double.compare(distA, distB);
        });

        if (!targetQueue.isEmpty()) {
            String centerName = protectedPlayer != null ? protectedPlayer.getName().getString() : "Steve";
            int area = (int)(playerSearchRadius * 2);
            com.steve.ai.SteveMod.LOGGER.info("Steve '{}' found {} targets of type '{}' in {}x{} area around {}",
                steve.getSteveName(), targetQueue.size(), targetType, area, area, centerName);
        }
    }

    /**
     * Check if an entity is a valid target based on targetType
     */
    private boolean isValidTarget(LivingEntity entity) {
        if (!entity.isAlive() || entity.isRemoved()) return false;
        if (entity instanceof SteveEntity) return false;
        if (entity instanceof Player) return false;

        String targetLower = targetType.toLowerCase().replace(" ", "_");

        // Apply alias mapping
        String resolved = targetLower;
        if (CREATURE_ALIASES.containsKey(targetLower)) {
            resolved = CREATURE_ALIASES.get(targetLower);
        }

        // General match: hostile -> any Monster or Slime (Slime is not a Monster in Minecraft's class hierarchy)
        if (resolved.equals("hostile")) {
            return entity instanceof Monster || entity instanceof Slime;
        }

        // Group matching
        if (resolved.startsWith("group:")) {
            String groupName = resolved.substring(6);
            List<String> types = CREATURE_GROUPS.get(groupName);
            if (types != null) {
                String entityId = getEntityTypeId(entity);
                return types.stream().anyMatch(t -> entityId.contains(t));
            }
            return false;
        }

        // Specific mob matching
        String entityId = getEntityTypeId(entity);
        return entityId.contains(resolved);
    }

    /**
     * Get entity type ID (e.g., "zombie", "skeleton")
     */
    private String getEntityTypeId(Entity entity) {
        return entity.getType().toString().toLowerCase();
    }
}
