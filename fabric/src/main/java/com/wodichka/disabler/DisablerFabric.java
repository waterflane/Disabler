package com.wodichka.disabler;

import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.event.FabricItemBlocker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class DisablerFabric implements ModInitializer {
    public static final String MODID = "disabler";

    @Override
    public void onInitialize() {
        DisablerConfig.load(FabricLoader.getInstance().getConfigDir());
        FabricItemBlocker.register();
    }
}
