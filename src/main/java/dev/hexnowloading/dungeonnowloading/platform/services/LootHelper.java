package dev.hexnowloading.dungeonnowloading.platform.services;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;

public interface LootHelper {
    void injectLoot(Identifier id, LootPool pool);
}
