package com.steve.ai.action.actions;

import com.steve.ai.action.ActionResult;
import com.steve.ai.action.Task;
import com.steve.ai.entity.SteveEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
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
    private static final int MAX_TICKS = 2400; // 2 minutes timeout for multi-target combat
    private static final double ATTACK_RANGE = 3.5;

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

        // Make sure we're not flying (in case we were building)
        steve.setFlying(false);

        steve.setInvulnerableBuilding(true);

        findAllTargets();

        if (targetQueue.isEmpty()) {
            com.steve.ai.SteveMod.LOGGER.warn("Steve '{}' no targets nearby", steve.getSteveName());
        } else {
            com.steve.ai.SteveMod.LOGGER.info("Steve '{}' found {} targets of type '{}'",
                steve.getSteveName(), targetQueue.size(), targetType);
        }
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

        // Get next target from queue if current target is invalid
        if (target == null || !target.isAlive() || target.isRemoved()) {
            if (!targetQueue.isEmpty()) {
                target = targetQueue.remove(0);
                com.steve.ai.SteveMod.LOGGER.info("Steve '{}' switched to next target: {} ({} remaining)",
                    steve.getSteveName(), target.getType().toString(), targetQueue.size());
            } else {
                // Queue empty, try to find more targets
                if (ticksRunning % 20 == 0) {
                    findAllTargets();
                }
                if (targetQueue.isEmpty()) {
                    // No more targets - all eliminated
                    steve.setInvulnerableBuilding(false);
                    steve.setSprinting(false);
                    steve.getNavigation().stop();
                    com.steve.ai.SteveMod.LOGGER.info("Steve '{}' all targets eliminated, invulnerability disabled",
                        steve.getSteveName());
                    result = ActionResult.success("All targets eliminated");
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
     */
    private void findAllTargets() {
        AABB searchBox = steve.getBoundingBox().inflate(32.0);
        List<Entity> entities = steve.level().getEntities(steve, searchBox);

        targetQueue.clear();

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living && isValidTarget(living)) {
                targetQueue.add(living);
            }
        }

        // Sort by distance (closest first)
        targetQueue.sort((a, b) -> {
            double distA = steve.distanceTo(a);
            double distB = steve.distanceTo(b);
            return Double.compare(distA, distB);
        });

        if (!targetQueue.isEmpty()) {
            com.steve.ai.SteveMod.LOGGER.info("Steve '{}' found {} targets of type '{}'",
                steve.getSteveName(), targetQueue.size(), targetType);
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

        // General match: hostile -> any Monster
        if (resolved.equals("hostile")) {
            return entity instanceof Monster;
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
