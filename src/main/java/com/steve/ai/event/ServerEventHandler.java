package com.steve.ai.event;

import com.steve.ai.SteveMod;
import com.steve.ai.entity.SteveEntity;
import com.steve.ai.entity.SteveManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SteveMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {
    private static boolean stevesInitialized = false;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            SteveManager manager = SteveMod.getSteveManager();

            if (!stevesInitialized) {
                // Re-register any Steve entities that were restored from world save
                int restored = 0;
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof SteveEntity steve) {
                        if (manager.registerExistingSteve(steve)) {
                            restored++;
                        }
                    }
                }
                if (restored > 0) {
                    SteveMod.LOGGER.info("Restored {} Steve entities from world save", restored);
                } else {
                    SteveMod.LOGGER.info("No existing Steve entities found. Use '/steve spawn <name>' to create one.");
                }
                stevesInitialized = true;
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        stevesInitialized = false;
        SteveMod.getSteveManager().clearAllSteves();
        SteveMod.LOGGER.info("Server stopping - cleared Steve manager state");
    }
}

