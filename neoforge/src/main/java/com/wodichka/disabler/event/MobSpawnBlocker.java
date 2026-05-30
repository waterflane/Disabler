package com.wodichka.disabler.event;

import com.wodichka.disabler.config.DisablerConfig;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MobSpawnBlocker {
    @SubscribeEvent
    public void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
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
