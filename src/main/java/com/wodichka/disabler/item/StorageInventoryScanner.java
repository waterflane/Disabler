package com.wodichka.disabler.item;

import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.mixin.ChunkMapAccessor;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public final class StorageInventoryScanner {
    private final Queue<BlockEntity> pendingBlockEntities = new ArrayDeque<>();
    private int ticksUntilScan = 1;

    public void tick(MinecraftServer server) {
        if (!DisablerConfig.hasBlockedItems() || !DisablerConfig.shouldScanStorageInventories()) {
            pendingBlockEntities.clear();
            ticksUntilScan = 1;
            return;
        }

        if (pendingBlockEntities.isEmpty()) {
            if (--ticksUntilScan > 0) {
                return;
            }
            collectLoadedBlockEntities(server);
            ticksUntilScan = DisablerConfig.getStorageScanIntervalTicks();
        }

        processPendingBlockEntities(DisablerConfig.getStorageScanBlockEntitiesPerTick());
    }

    private void collectLoadedBlockEntities(MinecraftServer server) {
        pendingBlockEntities.clear();

        for (ServerLevel level : server.getAllLevels()) {
            Iterable<ChunkHolder> chunkHolders = ((ChunkMapAccessor) level.getChunkSource().chunkMap).disabler$getChunks();
            for (ChunkHolder chunkHolder : chunkHolders) {
                LevelChunk chunk = chunkHolder.getTickingChunk();
                if (chunk == null) {
                    continue;
                }
                pendingBlockEntities.addAll(chunk.getBlockEntities().values());
            }
        }
    }

    private void processPendingBlockEntities(int limit) {
        int processed = 0;
        while (processed < limit && !pendingBlockEntities.isEmpty()) {
            BlockEntity blockEntity = pendingBlockEntities.poll();
            if (blockEntity != null && !blockEntity.isRemoved() && blockEntity.hasLevel()) {
                sanitizeBlockEntity(blockEntity);
            }
            processed++;
        }
    }

    private static void sanitizeBlockEntity(BlockEntity blockEntity) {
        if (shouldSkipBlockEntity(blockEntity)) {
            return;
        }

        boolean changed = false;

        if (blockEntity instanceof Container container) {
            changed |= BlockedItemCleaner.sanitizeContainer(container);
        }

        Set<IItemHandler> visitedHandlers = Collections.newSetFromMap(new IdentityHashMap<>());
        IItemHandler handler = blockEntity.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK,
                blockEntity.getBlockPos(),
                blockEntity.getBlockState(),
                blockEntity,
                null);
        changed |= sanitizeHandler(handler, visitedHandlers);

        for (Direction direction : Direction.values()) {
            handler = blockEntity.getLevel().getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    blockEntity.getBlockPos(),
                    blockEntity.getBlockState(),
                    blockEntity,
                    direction);
            changed |= sanitizeHandler(handler, visitedHandlers);
        }

        if (changed) {
            blockEntity.setChanged();
        }
    }

    private static boolean sanitizeHandler(IItemHandler handler, Set<IItemHandler> visitedHandlers) {
        if (handler == null || !visitedHandlers.add(handler)) {
            return false;
        }

        boolean changed = false;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!BlockedItemCleaner.isBlocked(stack)) {
                continue;
            }

            if (handler instanceof IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(slot, ItemStack.EMPTY);
                changed = true;
            } else {
                changed |= extractBlockedStack(handler, slot, stack.getCount());
            }
        }
        return changed;
    }

    private static boolean shouldSkipBlockEntity(BlockEntity blockEntity) {
        Set<String> skippedNamespaces = DisablerConfig.getStorageScanSkippedNamespaces();
        if (skippedNamespaces.isEmpty()) {
            return false;
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock());
        if (blockId != null && skippedNamespaces.contains(blockId.getNamespace())) {
            return true;
        }

        ResourceLocation blockEntityTypeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
        if (blockEntityTypeId != null && skippedNamespaces.contains(blockEntityTypeId.getNamespace())) {
            return true;
        }

        String className = blockEntity.getClass().getName().toLowerCase(java.util.Locale.ROOT);
        for (String namespace : skippedNamespaces) {
            if (className.contains("." + namespace + ".") || className.startsWith(namespace + ".")) {
                return true;
            }
        }
        return false;
    }

    private static boolean extractBlockedStack(IItemHandler handler, int slot, int amount) {
        boolean changed = false;
        int attempts = 0;

        while (amount > 0 && attempts++ < 64) {
            ItemStack extracted = handler.extractItem(slot, amount, false);
            if (extracted.isEmpty()) {
                break;
            }
            changed |= BlockedItemCleaner.isBlocked(extracted);

            ItemStack remaining = handler.getStackInSlot(slot);
            if (!BlockedItemCleaner.isBlocked(remaining)) {
                break;
            }
            amount = remaining.getCount();
        }

        return changed;
    }
}
