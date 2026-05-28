package com.wodichka.disabler;

import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.event.DimensionTravelBlockerForge;
import com.wodichka.disabler.event.MobSpawnBlockerForge;
import com.wodichka.disabler.world.DisablerModifiersForge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(DisablerForge.MODID)
public class DisablerForge {
    public static final String MODID = "disabler";

    public DisablerForge() {
        DisablerConfig.load(FMLPaths.CONFIGDIR.get());
        DisablerModifiersForge.register();
        MinecraftForge.EVENT_BUS.register(new MobSpawnBlockerForge());
        MinecraftForge.EVENT_BUS.register(new DimensionTravelBlockerForge());
    }
}
