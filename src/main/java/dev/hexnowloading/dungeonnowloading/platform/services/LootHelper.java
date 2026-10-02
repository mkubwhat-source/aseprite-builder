package dev.hexnowloading.dungeonnowloading.platform.services;

import net.minecraft.resources.Identifier;

public interface LootHelper {
    /** Rolls {@code injectTable} whenever {@code targetTable} generates loot and adds the result to its drops. */
    void injectLoot(Identifier targetTable, Identifier injectTable);
}
