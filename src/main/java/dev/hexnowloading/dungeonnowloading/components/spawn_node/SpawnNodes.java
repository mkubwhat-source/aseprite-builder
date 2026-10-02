package dev.hexnowloading.dungeonnowloading.components.spawn_node;

import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SpawnNodes {

    private static Map<Identifier, SpawnNode> NODES = Collections.emptyMap();

    private SpawnNodes() {}

    public static SpawnNode get(Identifier id) {
        return NODES.get(id);
    }

    public static Map<Identifier, SpawnNode> all() {
        return NODES;
    }

    public static void replaceAll(Map<Identifier, SpawnNode> nodes) {
        NODES = Collections.unmodifiableMap(new HashMap<>(nodes));
    }
}
