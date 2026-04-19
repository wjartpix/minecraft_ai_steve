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
    private static final int MAX_TICKS = 6000; // 5 minutes

    public FollowPlayerAction(SteveEntity steve, Task task) {
        super(steve, task);
    }

    @Override
    protected void onStart() {
        playerName = task.getStringParameter("player");
        ticksRunning = 0;
        
        // Handle special placeholder from LLM prompt
        if (playerName == null || 
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
        
        if (distance > 3.0) {
            // Check if player is significantly above or below Steve (e.g., on roof or in pit)
            if (verticalDistance > 3.0 && horizontalDistance < 10.0) {
                // Player is on different level - try to teleport closer
                teleportToPlayerLevel();
            } else {
                steve.getNavigation().moveTo(targetPlayer, 1.0);
            }
        } else if (distance < 2.0) {
            steve.getNavigation().stop();
        }
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

