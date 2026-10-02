package dev.hexnowloading.dungeonnowloading.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ExtraCodecs;

/**
 * 1.21 removed NbtUtils.writeGameProfile/readGameProfile. GameProfile NBT now goes
 * through the ResolvableProfile codec.
 */
public final class ProfileNbt {
    private ProfileNbt() {}

    public static Tag write(GameProfile profile) {
        return ExtraCodecs.STORED_GAME_PROFILE.codec()
                .encodeStart(NbtOps.INSTANCE, profile)
                .result()
                .orElseGet(CompoundTag::new);
    }

    public static GameProfile read(CompoundTag tag) {
        return ExtraCodecs.STORED_GAME_PROFILE.codec()
                .parse(NbtOps.INSTANCE, tag)
                .result()
                .orElse(null);
    }
}
