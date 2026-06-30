package com.wodichka.disabler.event;

import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.item.BlockedItemCleaner;
import com.wodichka.disabler.item.NeoForgeStorageSanitizer;
import com.wodichka.disabler.item.StorageInventoryScanner;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class ItemBlocker {
    private static final int INVENTORY_SWEEP_INTERVAL_TICKS = 100;

    private final StorageInventoryScanner storageScanner =
            new StorageInventoryScanner(NeoForgeStorageSanitizer::sanitize);
    private int ticksUntilSweep = INVENTORY_SWEEP_INTERVAL_TICKS;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        storageScanner.tick(event.getServer());
        if (!DisablerConfig.hasBlockedItems()) {
            ticksUntilSweep = INVENTORY_SWEEP_INTERVAL_TICKS;
            return;
        }
        if (--ticksUntilSweep > 0) {
            return;
        }
        ticksUntilSweep = INVENTORY_SWEEP_INTERVAL_TICKS;
        event.getServer().getPlayerList().getPlayers().forEach(BlockedItemCleaner::sanitizePlayer);
    }

    @SubscribeEvent public void onLogin(PlayerEvent.PlayerLoggedInEvent event) { sanitize(event); }
    @SubscribeEvent public void onRespawn(PlayerEvent.PlayerRespawnEvent event) { sanitize(event); }
    @SubscribeEvent public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) { sanitize(event); }
    @SubscribeEvent public void onCrafted(PlayerEvent.ItemCraftedEvent event) { sanitize(event); }
    @SubscribeEvent public void onSmelted(PlayerEvent.ItemSmeltedEvent event) { sanitize(event); }

    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        BlockedItemCleaner.sanitizeMenu(event.getContainer());
        sanitize(event);
    }

    @SubscribeEvent
    public void onContainerClose(PlayerContainerEvent.Close event) {
        BlockedItemCleaner.sanitizeMenu(event.getContainer());
        sanitize(event);
    }

    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (BlockedItemCleaner.isBlocked(event.getItemEntity().getItem())) {
            event.setCanPickup(TriState.FALSE);
            event.getItemEntity().discard();
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (BlockedItemCleaner.isBlocked(event.getEntity().getItem())) {
            event.setCanceled(true);
            event.getEntity().discard();
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()
                && event.getEntity() instanceof ItemEntity item
                && BlockedItemCleaner.isBlocked(item.getItem())) {
            event.setCanceled(true);
        }
    }

    private static void sanitize(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BlockedItemCleaner.sanitizePlayer(player);
        }
    }
}
