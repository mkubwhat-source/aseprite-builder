package dev.hexnowloading.dungeonnowloading.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRule;

/** 26.x game rules live on the server level only; client-side callers get the rule's default-off answer. */
public final class DNLGameRules {
    private DNLGameRules() {
    }

    public static boolean get(Level level, GameRule<Boolean> rule) {
        return level instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(rule);
    }
}
