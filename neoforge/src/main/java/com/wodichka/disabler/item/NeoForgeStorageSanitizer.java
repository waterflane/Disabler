package com.wodichka.disabler.item;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public final class NeoForgeStorageSanitizer {
    private NeoForgeStorageSanitizer() {}

    public static boolean sanitize(BlockEntity blockEntity) {
        Set<IItemHandler> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        boolean changed = sanitize(query(blockEntity, null), visited);
        for (Direction direction : Direction.values()) {
            changed |= sanitize(query(blockEntity, direction), visited);
        }
        return changed;
    }

    private static IItemHandler query(BlockEntity blockEntity, Direction direction) {
        return blockEntity.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK,
                blockEntity.getBlockPos(),
                blockEntity.getBlockState(),
                blockEntity,
                direction);
    }

    private static boolean sanitize(IItemHandler handler, Set<IItemHandler> visited) {
        if (handler == null || !visited.add(handler)) {
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
                changed |= extract(handler, slot, stack.getCount());
            }
        }
        return changed;
    }

    private static boolean extract(IItemHandler handler, int slot, int amount) {
        boolean changed = false;
        for (int attempts = 0; amount > 0 && attempts < 64; attempts++) {
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
