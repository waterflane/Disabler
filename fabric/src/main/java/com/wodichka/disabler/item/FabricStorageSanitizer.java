package com.wodichka.disabler.item;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class FabricStorageSanitizer {
    private FabricStorageSanitizer() {}

    public static boolean sanitize(BlockEntity blockEntity) {
        Set<Storage<ItemVariant>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        boolean changed = sanitize(query(blockEntity, null), visited);
        for (Direction direction : Direction.values()) {
            changed |= sanitize(query(blockEntity, direction), visited);
        }
        return changed;
    }

    private static Storage<ItemVariant> query(BlockEntity blockEntity, Direction direction) {
        return ItemStorage.SIDED.find(
                blockEntity.getLevel(),
                blockEntity.getBlockPos(),
                blockEntity.getBlockState(),
                blockEntity,
                direction);
    }

    private static boolean sanitize(Storage<ItemVariant> storage, Set<Storage<ItemVariant>> visited) {
        if (storage == null || !visited.add(storage)) {
            return false;
        }

        boolean changed = false;
        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant resource = view.getResource();
                if (!resource.isBlank() && com.wodichka.disabler.config.DisablerConfig.isBlockedItem(resource.getItem())) {
                    changed |= storage.extract(resource, view.getAmount(), transaction) > 0;
                }
            }
            if (changed) {
                transaction.commit();
            }
        }
        return changed;
    }
}
