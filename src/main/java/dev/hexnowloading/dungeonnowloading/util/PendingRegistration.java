package dev.hexnowloading.dungeonnowloading.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * Since 1.21.2, {@link Block} and {@link Item} constructors require their registry id to be set on their
 * properties. The 1.21.1 code builds properties inside registration suppliers, so the key of the entry being
 * registered is published here while its supplier runs; mixins on the properties constructors pick it up.
 */
public final class PendingRegistration {
    private static final ThreadLocal<ResourceKey<?>> PENDING = new ThreadLocal<>();

    private PendingRegistration() {
    }

    public static void set(@Nullable ResourceKey<?> key) {
        if (key == null) {
            PENDING.remove();
        } else {
            PENDING.set(key);
        }
    }

    @SuppressWarnings("unchecked")
    public static @Nullable ResourceKey<Block> block() {
        ResourceKey<?> key = PENDING.get();
        return key != null && key.isFor(Registries.BLOCK) ? (ResourceKey<Block>) key : null;
    }

    @SuppressWarnings("unchecked")
    public static @Nullable ResourceKey<Item> item() {
        ResourceKey<?> key = PENDING.get();
        return key != null && key.isFor(Registries.ITEM) ? (ResourceKey<Item>) key : null;
    }

    /** Items that share their id with a block are block items and use the block's translation key. */
    public static boolean isBlockItem(ResourceKey<Item> key) {
        return BuiltInRegistries.BLOCK.containsKey(key.identifier());
    }
}
