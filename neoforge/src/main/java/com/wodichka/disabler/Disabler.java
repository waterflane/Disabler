package com.wodichka.disabler;

import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.event.DimensionTravelBlocker;
import com.wodichka.disabler.event.MobSpawnBlocker;
import com.wodichka.disabler.world.DisablerModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Disabler.MODID)
public class Disabler {
    public static final String MODID = "disabler";

    public Disabler(IEventBus modBus, ModContainer container) {
        DisablerConfig.load(FMLPaths.CONFIGDIR.get());
        DisablerModifiers.register(modBus);
        NeoForge.EVENT_BUS.register(new MobSpawnBlocker());
        NeoForge.EVENT_BUS.register(new DimensionTravelBlocker());
    }
}
