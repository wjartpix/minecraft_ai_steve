package com.steve.ai.action.actions;

import com.steve.ai.action.ActionResult;
import com.steve.ai.action.Task;
import com.steve.ai.entity.SteveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class FollowPlayerAction extends BaseAction {
    private String playerName;
    private Player targetPlayer;
    private int ticksRunning;
    private int ticksSinceLastNavUpdate;
    private static final int MAX_TICKS = 6000; // 5 minutes
    private static final double FOLLOW_DISTANCE = 3.0;  // Start following beyond this distance
    private static final double STOP_DISTANCE = 2.0;    // Stop moving when closer than this
    private static final double TELEPORT_DISTANCE = 50.0; // Teleport if further than this
    private static final int NAV_UPDATE_INTERVAL = 10;   // Recalculate path every N ticks (0.5s)
    private static final double NAV_UPDATE_MIN_DELTA = 2.0; // Or if target moved this far since last path

    private double lastNavTargetX;
    private double lastNavTargetY;
    private double lastNavTargetZ;

    public FollowPlayerAction(SteveEntity steve, Task task) {
        super(steve, task);
    }

    @Override
    protected void onStart() {
        playerName = task.getStringParameter("player");
        ticksRunning = 0;
        ticksSinceLastNavUpdate = 0;
        lastNavTargetX = Double.NaN;
        lastNavTargetY = Double.NaN;
        lastNavTargetZ = Double.NaN;

        // Handle special placeholder from LLM prompt
        if (playerName == null ||
            playerName.equalsIgnoreCase("nearest") ||
            playerName.equalsIgnoreCase("me") ||
            playerName.equals("USE_NEARBY_PLAYER_NAME") ||
            playerName.equals("PLAYER_NAME") ||
            playerName.equals("NEAREST_PLAYER")) {
            playerName = null; // Will trigger nearest player search
        }

        findPlayer();

        if (targetPlayer == null) {
            result = ActionResult.failure("Player not found: " + (playerName != null ? playerName : "(no specific player specified)"));
        } else {
            com.steve.ai.SteveMod.LOGGER.info("Steve '{}' started following player '{}'",
                steve.getSteveName(), targetPlayer.getName().getString());
        }
    }

    @Override
    protected void onTick() {
        ticksRunning++;
        ticksSinceLastNavUpdate++;

        if (ticksRunning > MAX_TICKS) {
            result = ActionResult.success("Stopped following");
            return;
        }

        if (targetPlayer == null || !targetPlayer.isAlive() || targetPlayer.isRemoved()) {
            findPlayer();
            if (targetPlayer == null) {
                result = ActionResult.failure("Lost track of player");
                return;
            }
        }

        double distance = steve.distanceTo(targetPlayer);
        double horizontalDistance = Math.sqrt(
            Math.pow(steve.getX() - targetPlayer.getX(), 2) +
            Math.pow(steve.getZ() - targetPlayer.getZ(), 2)
        );
        double verticalDistance = Math.abs(steve.getY() - targetPlayer.getY());

        // Teleport if way too far away
        if (distance > TELEPORT_DISTANCE) {
            teleportNearPlayer();
            return;
        }

        if (distance > FOLLOW_DISTANCE) {
            // Player is on a different Y level - try to teleport closer
            if (verticalDistance > 3.0 && horizontalDistance < 10.0) {
                teleportToPlayerLevel();
            } else {
                // Only recalculate navigation path when needed (not every tick!)
                // This prevents path computation from being reset constantly
                updateNavigation();
            }
        } else if (distance < STOP_DISTANCE) {
            steve.getNavigation().stop();
            resetNavTarget();
        } else {
            // Within comfortable range (STOP_DISTANCE .. FOLLOW_DISTANCE)
            // Let current navigation continue, or stand still if already stopped
            if (steve.getNavigation().isDone()) {
                // Navigation finished and we're at comfortable distance, just stand
            }
        }
    }

    /**
     * Update navigation to target player.
     * Only recalculates the path when the interval has elapsed OR the target
     * has moved significantly since the last path was set.
     * This prevents the path from being reset every tick, which would stop
     * the mob from ever completing a path and actually moving.
     */
    private void updateNavigation() {
        if (targetPlayer == null) return;

        double targetX = targetPlayer.getX();
        double targetY = targetPlayer.getY();
        double targetZ = targetPlayer.getZ();

        boolean needsUpdate = false;

        // Check if enough ticks have passed since last update
        if (ticksSinceLastNavUpdate >= NAV_UPDATE_INTERVAL) {
            needsUpdate = true;
        }

        // Check if target has moved significantly since last path was set
        if (!Double.isNaN(lastNavTargetX)) {
            double delta = Math.sqrt(
                Math.pow(targetX - lastNavTargetX, 2) +
                Math.pow(targetY - lastNavTargetY, 2) +
                Math.pow(targetZ - lastNavTargetZ, 2)
            );
            if (delta > NAV_UPDATE_MIN_DELTA) {
                needsUpdate = true;
            }
        } else {
            // First time, always set navigation
            needsUpdate = true;
        }

        // Also update if navigation is done (mob arrived but target moved again)
        if (steve.getNavigation().isDone()) {
            needsUpdate = true;
        }

        if (needsUpdate) {
            steve.getNavigation().moveTo(targetPlayer, 1.0);
            lastNavTargetX = targetX;
            lastNavTargetY = targetY;
            lastNavTargetZ = targetZ;
            ticksSinceLastNavUpdate = 0;
        }
    }

    private void resetNavTarget() {
        lastNavTargetX = Double.NaN;
        lastNavTargetY = Double.NaN;
        lastNavTargetZ = Double.NaN;
        ticksSinceLastNavUpdate = 0;
    }

    /**
     * Teleport Steve near the target player when too far away.
     */
    private void teleportNearPlayer() {
        if (targetPlayer == null) return;

        double offsetX = (Math.random() - 0.5) * 6;
        double offsetZ = (Math.random() - 0.5) * 6;
        double targetX = targetPlayer.getX() + offsetX;
        double targetY = targetPlayer.getY();
        double targetZ = targetPlayer.getZ() + offsetZ;

        net.minecraft.core.BlockPos checkPos = new net.minecraft.core.BlockPos((int)targetX, (int)targetY, (int)targetZ);
        for (int i = 0; i < 10; i++) {
            net.minecraft.core.BlockPos groundPos = checkPos.below(i);
            if (!steve.level().getBlockState(groundPos).isAir() &&
                steve.level().getBlockState(groundPos.above()).isAir()) {
                targetY = groundPos.above().getY();
                break;
            }
        }

        steve.teleportTo(targetX, targetY, targetZ);
        steve.getNavigation().stop();
        resetNavTarget();

        com.steve.ai.SteveMod.LOGGER.info("Steve '{}' teleported to player (was {} blocks away)",
            steve.getSteveName(), (int)steve.distanceTo(targetPlayer));
    }
    
    private void teleportToPlayerLevel() {
        if (targetPlayer == null) return;
        
        BlockPos playerPos = targetPlayer.blockPosition();
        
        // Try to find a safe position near the player
        // First try same Y level as player
        for (int dy = 0; dy >= -5; dy--) {
            BlockPos checkPos = playerPos.offset(0, dy, 0);
            BlockPos belowPos = checkPos.below();
            
            // Check if this position is safe (solid ground below, air at position)
            if (steve.level().getBlockState(belowPos).isSolid() && 
                steve.level().getBlockState(checkPos).isAir() &&
                steve.level().getBlockState(checkPos.above()).isAir()) {
                
                // Teleport Steve to this position
                steve.teleportTo(checkPos.getX() + 0.5, checkPos.getY(), checkPos.getZ() + 0.5);
                com.steve.ai.SteveMod.LOGGER.info("Steve '{}' teleported to player level at {}", 
                    steve.getSteveName(), checkPos);
                return;
            }
        }
        
        // If no safe spot found near player Y level, try horizontal positions
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                
                for (int dy = 0; dy >= -3; dy--) {
                    BlockPos checkPos = playerPos.offset(dx, dy, dz);
                    BlockPos belowPos = checkPos.below();
                    
                    if (steve.level().getBlockState(belowPos).isSolid() && 
                        steve.level().getBlockState(checkPos).isAir() &&
                        steve.level().getBlockState(checkPos.above()).isAir()) {
                        
                        steve.teleportTo(checkPos.getX() + 0.5, checkPos.getY(), checkPos.getZ() + 0.5);
                        com.steve.ai.SteveMod.LOGGER.info("Steve '{}' teleported to player level at {}", 
                            steve.getSteveName(), checkPos);
                        return;
                    }
                }
            }
        }
        
        // Fallback: use normal navigation if teleportation failed
        steve.getNavigation().moveTo(targetPlayer, 1.0);
    }

    @Override
    protected void onCancel() {
        steve.getNavigation().stop();
    }

    @Override
    public String getDescription() {
        return "Follow player " + playerName;
    }

    private void findPlayer() {
        java.util.List<? extends Player> players = steve.level().players();
        
        // First try exact name match
        for (Player player : players) {
            if (player.getName().getString().equalsIgnoreCase(playerName)) {
                targetPlayer = player;
                return;
            }
        }
        
        // If no exact match found, check for special keywords or fallback to nearest player
        boolean shouldFindNearest = playerName == null || 
            playerName.isEmpty() ||
            playerName.equalsIgnoreCase("me") || 
            playerName.equalsIgnoreCase("you") ||
            playerName.contains("PLAYER") || 
            playerName.contains("NAME");
        
        // Also find nearest if specified player is not online (fallback behavior)
        if (targetPlayer == null) {
            shouldFindNearest = true;
        }
        
        if (shouldFindNearest && !players.isEmpty()) {
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
            
            if (nearest != null) {
                targetPlayer = nearest;
                String originalName = playerName;
                playerName = nearest.getName().getString(); // Update to actual name
                if (originalName != null && !originalName.equalsIgnoreCase(playerName)) {
                    com.steve.ai.SteveMod.LOGGER.info("Steve '{}' following nearest player '{}' (original target '{}' not found or was generic)", 
                        steve.getSteveName(), playerName, originalName);
                } else {
                    com.steve.ai.SteveMod.LOGGER.info("Steve '{}' following nearest player: {}", 
                        steve.getSteveName(), playerName);
                }
            }
        }
    }
}

