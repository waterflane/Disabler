package com.wodichka.disabler.event;

import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.item.BlockedItemCleaner;
import com.wodichka.disabler.item.FabricStorageSanitizer;
import com.wodichka.disabler.item.StorageInventoryScanner;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public final class FabricItemBlocker {
    private static final int INVENTORY_SWEEP_INTERVAL_TICKS = 100;
    private static final StorageInventoryScanner STORAGE_SCANNER =
            new StorageInventoryScanner(FabricStorageSanitizer::sanitize);
    private static int ticksUntilSweep = INVENTORY_SWEEP_INTERVAL_TICKS;

    private FabricItemBlocker() {}

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                DisablerConfig.load(FabricLoader.getInstance().getConfigDir()));
        ServerTickEvents.END_SERVER_TICK.register(FabricItemBlocker::onServerTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                BlockedItemCleaner.sanitizePlayer(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                BlockedItemCleaner.sanitizePlayer(newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                BlockedItemCleaner.sanitizePlayer(player));
    }

    private static void onServerTick(MinecraftServer server) {
        STORAGE_SCANNER.tick(server);
        if (!DisablerConfig.hasBlockedItems()) {
            ticksUntilSweep = INVENTORY_SWEEP_INTERVAL_TICKS;
            return;
        }
        if (--ticksUntilSweep > 0) {
            return;
        }
        ticksUntilSweep = INVENTORY_SWEEP_INTERVAL_TICKS;
        server.getPlayerList().getPlayers().forEach(BlockedItemCleaner::sanitizePlayer);
    }
}
