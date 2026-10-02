package dev.hexnowloading.dungeonnowloading.entity.util;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;

import java.util.Optional;
import java.util.UUID;

/** 26.x removed {@code DNLDataSerializers.OPTIONAL_UUID}; the mod keeps its own equivalent. */
public final class DNLDataSerializers {
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID =
            EntityDataSerializer.forValueType(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional));

    private DNLDataSerializers() {
    }
}
