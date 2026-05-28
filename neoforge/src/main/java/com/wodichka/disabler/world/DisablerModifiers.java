package com.wodichka.disabler.world;

import com.mojang.serialization.MapCodec;
import com.wodichka.disabler.Disabler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class DisablerModifiers {
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Disabler.MODID);
    private static final DeferredRegister<MapCodec<? extends StructureModifier>> STRUCTURE_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS, Disabler.MODID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<ConfigDrivenBiomeModifier>> CONFIG_SPAWN_BLOCKER = BIOME_MODIFIER_SERIALIZERS.register("config_spawn_blocker", () -> MapCodec.unit(ConfigDrivenBiomeModifier.INSTANCE));
    public static final DeferredHolder<MapCodec<? extends StructureModifier>, MapCodec<ConfigDrivenStructureModifier>> CONFIG_STRUCTURE_BLOCKER = STRUCTURE_MODIFIER_SERIALIZERS.register("config_structure_blocker", () -> MapCodec.unit(ConfigDrivenStructureModifier.INSTANCE));

    private DisablerModifiers() {}

    public static void register(IEventBus modBus) {
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
        STRUCTURE_MODIFIER_SERIALIZERS.register(modBus);
    }
}