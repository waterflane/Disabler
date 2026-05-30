package com.wodichka.disabler.world;

import com.mojang.serialization.Codec;
import com.wodichka.disabler.Disabler;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class DisablerModifiers {
    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Disabler.MODID);
    private static final DeferredRegister<Codec<? extends StructureModifier>> STRUCTURE_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS, Disabler.MODID);

    public static final RegistryObject<Codec<ConfigDrivenBiomeModifier>> CONFIG_SPAWN_BLOCKER = BIOME_MODIFIER_SERIALIZERS.register("config_spawn_blocker", () -> Codec.unit(ConfigDrivenBiomeModifier.INSTANCE));
    public static final RegistryObject<Codec<ConfigDrivenStructureModifier>> CONFIG_STRUCTURE_BLOCKER = STRUCTURE_MODIFIER_SERIALIZERS.register("config_structure_blocker", () -> Codec.unit(ConfigDrivenStructureModifier.INSTANCE));

    private DisablerModifiers() {}

    public static void register() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
        STRUCTURE_MODIFIER_SERIALIZERS.register(modBus);
    }
}
