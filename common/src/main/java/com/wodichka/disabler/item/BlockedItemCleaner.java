package com.wodichka.disabler.item;

import com.wodichka.disabler.config.DisablerConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class BlockedItemCleaner {
    private BlockedItemCleaner() {}

    public static boolean isBlocked(ItemStack stack) {
        return !stack.isEmpty() && DisablerConfig.isBlockedItem(stack.getItem());
    }

    public static boolean sanitizePlayer(ServerPlayer player) {
        if (!DisablerConfig.hasBlockedItems()) {
            return false;
        }

        boolean changed = false;
        changed |= sanitizeList(player.getInventory().items);
        changed |= sanitizeList(player.getInventory().armor);
        changed |= sanitizeList(player.getInventory().offhand);
        changed |= sanitizeContainer(player.getEnderChestInventory());
        changed |= sanitizeMenu(player.inventoryMenu);

        if (player.containerMenu != player.inventoryMenu) {
            changed |= sanitizeMenu(player.containerMenu);
        }

        if (changed) {
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            player.containerMenu.broadcastChanges();
        }

        return changed;
    }

    public static boolean sanitizeContainer(Container container) {
        boolean changed = false;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (isBlocked(container.getItem(slot))) {
                container.setItem(slot, ItemStack.EMPTY);
                changed = true;
            }
        }
        if (changed) {
            container.setChanged();
        }
        return changed;
    }

    public static boolean sanitizeMenu(AbstractContainerMenu menu) {
        boolean changed = false;

        if (isBlocked(menu.getCarried())) {
            menu.setCarried(ItemStack.EMPTY);
            changed = true;
        }

        for (Slot slot : menu.slots) {
            if (isBlocked(slot.getItem())) {
                slot.set(ItemStack.EMPTY);
                slot.setChanged();
                changed = true;
            }
        }

        if (changed) {
            menu.broadcastChanges();
        }
        return changed;
    }

    private static boolean sanitizeList(NonNullList<ItemStack> stacks) {
        boolean changed = false;
        for (int i = 0; i < stacks.size(); i++) {
            if (isBlocked(stacks.get(i))) {
                stacks.set(i, ItemStack.EMPTY);
                changed = true;
            }
        }
        return changed;
    }
}
