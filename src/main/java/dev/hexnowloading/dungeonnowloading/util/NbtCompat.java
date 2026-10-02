package dev.hexnowloading.dungeonnowloading.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Bridges the 1.21.1 NBT idioms (contains/getUUID/put of raw tags) onto the 26.x
 * {@link ValueInput}/{@link ValueOutput} and Optional-returning {@link CompoundTag} APIs.
 */
public final class NbtCompat {
    private NbtCompat() {
    }

    public static boolean has(ValueInput input, String key) {
        return input.read(key, ExtraCodecs.NBT).isPresent();
    }

    public static @Nullable UUID getUUID(ValueInput input, String key) {
        return input.read(key, UUIDUtil.CODEC).orElse(null);
    }

    public static boolean hasUUID(ValueInput input, String key) {
        return input.read(key, UUIDUtil.CODEC).isPresent();
    }

    public static void putUUID(ValueOutput output, String key, UUID uuid) {
        output.store(key, UUIDUtil.CODEC, uuid);
    }

    public static CompoundTag getCompound(ValueInput input, String key) {
        return input.read(key, CompoundTag.CODEC).orElseGet(CompoundTag::new);
    }

    public static ListTag getList(ValueInput input, String key) {
        return input.read(key, ExtraCodecs.NBT).flatMap(Tag::asList).orElseGet(ListTag::new);
    }

    public static void put(ValueOutput output, String key, Tag tag) {
        output.store(key, ExtraCodecs.NBT, tag);
    }

    public static <T> void store(ValueOutput output, String key, Codec<T> codec, T value) {
        output.store(key, codec, value);
    }

    public static @Nullable UUID getUUID(CompoundTag tag, String key) {
        return tag.read(key, UUIDUtil.CODEC).orElse(null);
    }

    public static boolean hasUUID(CompoundTag tag, String key) {
        return tag.read(key, UUIDUtil.CODEC).isPresent();
    }

    public static void putUUID(CompoundTag tag, String key, UUID uuid) {
        tag.store(key, UUIDUtil.CODEC, uuid);
    }
}
