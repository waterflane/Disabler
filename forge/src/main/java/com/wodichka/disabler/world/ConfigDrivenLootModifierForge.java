package com.wodichka.disabler.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wodichka.disabler.item.BlockedItemCleaner;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public final class ConfigDrivenLootModifierForge extends LootModifier {
    public static final Codec<ConfigDrivenLootModifierForge> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, ConfigDrivenLootModifierForge::new));

    public ConfigDrivenLootModifierForge(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        loot.removeIf(BlockedItemCleaner::isBlocked);
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return DisablerModifiersForge.CONFIG_LOOT_BLOCKER.get();
    }
}
