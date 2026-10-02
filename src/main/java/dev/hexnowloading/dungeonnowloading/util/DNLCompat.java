package dev.hexnowloading.dungeonnowloading.util;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Small replacements for 1.21.1 helpers that no longer exist in 26.x. */
public final class DNLCompat {
    private DNLCompat() {
    }

    /** 1.21.1 {@code Player#disableShield()}: drop the raised shield and put shields on a 5 second cooldown. */
    public static void disableShield(Player player) {
        player.getCooldowns().addCooldown(new ItemStack(Items.SHIELD), 100);
        player.stopUsingItem();
        player.level().broadcastEntityEvent(player, (byte) 30);
    }

    /** 1.21.1 {@code BlockState#blocksMotion()}. */
    public static boolean blocksMotion(BlockState state) {
        return state.isSolid();
    }

    /** 1.21.1 {@code OwnableEntity#getOwnerUUID()}. */
    public static @Nullable UUID ownerUUID(OwnableEntity ownable) {
        EntityReference<?> reference = ownable.getOwnerReference();
        return reference != null ? reference.getUUID() : null;
    }
}
