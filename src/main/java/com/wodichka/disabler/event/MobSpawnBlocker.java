package com.wodichka.disabler.event;

import com.wodichka.disabler.config.DisablerConfig;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

public final class MobSpawnBlocker {
    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (DisablerConfig.isBlockedMob(event.getEntity().getType())) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()) {
            return;
        }

        if (event.getEntity() instanceof Mob mob && DisablerConfig.isBlockedMob(mob.getType())) {
            event.setCanceled(true);
        }
    }
}