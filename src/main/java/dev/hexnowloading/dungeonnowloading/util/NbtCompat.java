package dev.hexnowloading.dungeonnowloading.util;


import dev.hexnowloading.dungeonnowloading.util.NbtCompat;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

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

    /** Replacement for {@code NbtCompat.saveEntity(entity)}. */
    public static CompoundTag saveEntity(Entity entity) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveWithoutId(output);
        return output.buildResult();
    }

    /** Replacement for {@code entity.load(tag)}. */
    public static void loadEntity(Entity entity, CompoundTag tag) {
        entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), tag));
    }

    /** Replacement for {@code NbtCompat.loadBlockEntity(blockEntity, tag, registries)}. */
    public static void loadBlockEntity(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider registries) {
        blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
    }

    /** Replacement for the 1.21.1 {@code NbtCompat.loadEntityRecursive(tag, level, EntitySpawnReason.SPAWNER, fn)}. */
    public static @Nullable Entity loadEntityRecursive(CompoundTag tag, Level level, EntitySpawnReason reason, Function<Entity, Entity> postLoad) {
        return EntityType.loadEntityRecursive(tag, level, new EntitySpawnRequest(reason, false), postLoad::apply);
    }

    public static Optional<EntityType<?>> entityTypeOf(CompoundTag tag) {
        return tag.getString("id").flatMap(EntityType::byString);
    }
}
