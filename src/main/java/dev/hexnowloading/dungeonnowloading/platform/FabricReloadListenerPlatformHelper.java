package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.services.ReloadListenerPlatform;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public class FabricReloadListenerPlatformHelper implements ReloadListenerPlatform {

    @Override
    public void registerDataReloadListener(Identifier id, PreparableReloadListener listener) {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(id, listener);
    }
}
