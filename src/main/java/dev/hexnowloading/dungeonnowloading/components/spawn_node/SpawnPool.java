package dev.hexnowloading.dungeonnowloading.components.spawn_node;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import java.util.List;

public class SpawnPool {
    public final Identifier id;
    public final List<Entry> entries;

    public SpawnPool(Identifier id, List<Entry> entries) {
        this.id = id;
        this.entries = (entries == null) ? List.of() : List.copyOf(entries);
    }

    public Identifier pickNodeId(RandomSource random) {
        if (entries.isEmpty()) return null;

        int total = 0;
        for (Entry e : entries) total += e.weight;

        if (total <= 0) return null; // all weights 0 => empty

        int roll = random.nextInt(total);
        int acc = 0;
        for (Entry e : entries) {
            acc += e.weight;
            if (roll < acc) return e.nodeId;
        }
        return entries.get(entries.size() - 1).nodeId;
    }

    public static class Entry {
        public final int weight;
        public final Identifier nodeId;

        public Entry(int weight, Identifier nodeId) {
            this.weight = Math.max(0, weight);
            this.nodeId = nodeId;
        }
    }
}
