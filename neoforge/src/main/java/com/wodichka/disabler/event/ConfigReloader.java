package com.wodichka.disabler.event;

import com.wodichka.disabler.config.DisablerConfig;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLPaths;

public final class ConfigReloader {
    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        DisablerConfig.load(FMLPaths.CONFIGDIR.get());
    }
}
