package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.services.LootHelper;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashMap;
import java.util.Map;

/**
 * 26.x loot pools reference nested tables through registry holders, which are not available while vanilla
 * tables are being modified. Instead the injected table is rolled with the same context when the target drops.
 */
public class FabricLootHelper implements LootHelper {

    private final Map<ResourceKey<LootTable>, ResourceKey<LootTable>> injections = new HashMap<>();
    private boolean registered;

    @Override
    public void injectLoot(Identifier targetTable, Identifier injectTable) {
        injections.put(ResourceKey.create(Registries.LOOT_TABLE, targetTable), ResourceKey.create(Registries.LOOT_TABLE, injectTable));
        if (!registered) {
            registered = true;
            LootTableEvents.MODIFY_DROPS.register((holder, context, drops) -> holder.unwrapKey()
                    .map(injections::get)
                    .ifPresent(inject -> context.getLevel().getServer().reloadableRegistries()
                            .getLootTable(inject)
                            .getRandomItems(context, drops::add)));
        }
    }
}
