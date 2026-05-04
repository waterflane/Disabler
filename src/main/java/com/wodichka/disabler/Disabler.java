package com.wodichka.disabler;

import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.event.MobSpawnBlocker;
import com.wodichka.disabler.world.DisablerModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Disabler.MODID)
public class Disabler {
    public static final String MODID = "disabler";

    public Disabler(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, DisablerConfig.SPEC);
        DisablerModifiers.register(modBus);
        NeoForge.EVENT_BUS.register(new MobSpawnBlocker());
    }
}
