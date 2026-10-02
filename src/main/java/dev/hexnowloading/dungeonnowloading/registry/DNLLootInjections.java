package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.resources.Identifier;

public class DNLLootInjections {
    public static void setup() {
        //chests
        injectLootTableRef(Identifier.fromNamespaceAndPath("minecraft", "chests/jungle_temple"), Identifier.fromNamespaceAndPath("dungeonnowloading", "vanilla/chests/jungle_temple"));
        injectLootTableRef(Identifier.fromNamespaceAndPath("minecraft", "chests/simple_dungeon"), Identifier.fromNamespaceAndPath("dungeonnowloading", "vanilla/chests/simple_dungeon"));

        //blocks
        injectLootTableRef(Identifier.fromNamespaceAndPath("minecraft", "blocks/spawner"), Identifier.fromNamespaceAndPath("dungeonnowloading", "vanilla/blocks/spawner"));
    }

    private static void injectLootTableRef(Identifier targetLootTable, Identifier injectTable) {
        Services.LOOT.injectLoot(targetLootTable, injectTable);
    }
}
