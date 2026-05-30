package com.wodichka.disabler;

import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.event.DimensionTravelBlocker;
import com.wodichka.disabler.event.MobSpawnBlocker;
import com.wodichka.disabler.world.DisablerModifiers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(Disabler.MODID)
public class Disabler {
    public static final String MODID = "disabler";

    public Disabler() {
        DisablerConfig.load(FMLPaths.CONFIGDIR.get());
        DisablerModifiers.register();
        MinecraftForge.EVENT_BUS.register(new MobSpawnBlocker());
        MinecraftForge.EVENT_BUS.register(new DimensionTravelBlocker());
    }
}
