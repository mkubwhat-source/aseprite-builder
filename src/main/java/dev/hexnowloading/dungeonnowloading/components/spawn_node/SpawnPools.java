package dev.hexnowloading.dungeonnowloading.components.spawn_node;

import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SpawnPools {
    private static Map<Identifier, SpawnPool> POOLS = Collections.emptyMap();

    private SpawnPools() {}

    public static SpawnPool get(Identifier id) {
        return POOLS.get(id);
    }

    public static Map<Identifier, SpawnPool> all() {
        return POOLS;
    }

    static void replaceAll(Map<Identifier, SpawnPool> pools) {
        POOLS = Collections.unmodifiableMap(new HashMap<>(pools));
    }
}
