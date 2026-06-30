package com.wodichka.disabler.event;

import com.wodichka.disabler.config.DisablerConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

public final class ConfigReloader {
    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        DisablerConfig.load(FMLPaths.CONFIGDIR.get());
    }
}
