package com.wodichka.disabler.event;

import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.item.BlockedItemCleaner;
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

    private int ticksUntilSweep = INVENTORY_SWEEP_INTERVAL_TICKS;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (!DisablerConfig.hasBlockedItems()) {
            ticksUntilSweep = INVENTORY_SWEEP_INTERVAL_TICKS;
            return;
        }

        if (--ticksUntilSweep > 0) {
            return;
        }

        ticksUntilSweep = INVENTORY_SWEEP_INTERVAL_TICKS;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            BlockedItemCleaner.sanitizePlayer(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sanitizePlayer(event);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sanitizePlayer(event);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        sanitizePlayer(event);
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        sanitizePlayer(event);
    }

    @SubscribeEvent
    public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        sanitizePlayer(event);
    }

    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        BlockedItemCleaner.sanitizeMenu(event.getContainer());
        sanitizePlayer(event);
    }

    @SubscribeEvent
    public void onContainerClose(PlayerContainerEvent.Close event) {
        BlockedItemCleaner.sanitizeMenu(event.getContainer());
        sanitizePlayer(event);
    }

    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (!DisablerConfig.hasBlockedItems() || !BlockedItemCleaner.isBlocked(event.getItemEntity().getItem())) {
            return;
        }

        event.setCanPickup(TriState.FALSE);
        event.getItemEntity().discard();
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (!DisablerConfig.hasBlockedItems() || !BlockedItemCleaner.isBlocked(event.getEntity().getItem())) {
            return;
        }

        event.setCanceled(true);
        event.getEntity().discard();
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !DisablerConfig.hasBlockedItems()) {
            return;
        }

        if (event.getEntity() instanceof ItemEntity itemEntity && BlockedItemCleaner.isBlocked(itemEntity.getItem())) {
            event.setCanceled(true);
        }
    }

    private static void sanitizePlayer(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            BlockedItemCleaner.sanitizePlayer(serverPlayer);
        }
    }
}
