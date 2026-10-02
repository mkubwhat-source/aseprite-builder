package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.platform.services.ConfigHelper;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class FabricConfigHelper implements ConfigHelper {
    @Override
    public void registerConfig(ConfigType type, ModConfigSpec spec) {
        ConfigRegistry.INSTANCE.register(DungeonNowLoading.MOD_ID, toModConfigType(type), spec);
    }

    private static ModConfig.Type toModConfigType(ConfigType type) {
        return switch (type) {
            case CLIENT -> ModConfig.Type.CLIENT;
            case COMMON -> ModConfig.Type.COMMON;
            case SERVER -> ModConfig.Type.SERVER;
        };
    }
}
