package com.wodichka.disabler.world;

import com.mojang.serialization.Codec;
import com.wodichka.disabler.DisablerForge;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class DisablerModifiersForge {
    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, DisablerForge.MODID);
    private static final DeferredRegister<Codec<? extends StructureModifier>> STRUCTURE_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS, DisablerForge.MODID);
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, DisablerForge.MODID);

    public static final RegistryObject<Codec<ConfigDrivenBiomeModifierForge>> CONFIG_SPAWN_BLOCKER = BIOME_MODIFIER_SERIALIZERS.register("config_spawn_blocker", () -> Codec.unit(ConfigDrivenBiomeModifierForge.INSTANCE));
    public static final RegistryObject<Codec<ConfigDrivenStructureModifierForge>> CONFIG_STRUCTURE_BLOCKER = STRUCTURE_MODIFIER_SERIALIZERS.register("config_structure_blocker", () -> Codec.unit(ConfigDrivenStructureModifierForge.INSTANCE));
    public static final RegistryObject<Codec<ConfigDrivenLootModifierForge>> CONFIG_LOOT_BLOCKER = LOOT_MODIFIER_SERIALIZERS.register("config_loot_blocker", () -> ConfigDrivenLootModifierForge.CODEC);

    private DisablerModifiersForge() {}

    public static void register() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
        STRUCTURE_MODIFIER_SERIALIZERS.register(modBus);
        LOOT_MODIFIER_SERIALIZERS.register(modBus);
    }
}
