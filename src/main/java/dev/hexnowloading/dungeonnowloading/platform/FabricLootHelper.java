package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.services.LootHelper;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class FabricLootHelper implements LootHelper {

    private final Set<Identifier> injected = new HashSet<>();

    @Override
    public void injectLoot(Identifier id, LootPool pool) {
        if (!injected.contains(id)) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, id);
            LootTableEvents.MODIFY.register((tableKey, builder, source, registries) -> {
                if (tableKey.equals(key)) {
                    builder.pool(pool);
                }
            });
            injected.add(id);
        }
    }
}
