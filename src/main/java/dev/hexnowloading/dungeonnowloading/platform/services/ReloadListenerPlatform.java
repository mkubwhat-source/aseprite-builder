package dev.hexnowloading.dungeonnowloading.platform.services;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface ReloadListenerPlatform {
    void registerDataReloadListener(Identifier id, PreparableReloadListener listener);
}