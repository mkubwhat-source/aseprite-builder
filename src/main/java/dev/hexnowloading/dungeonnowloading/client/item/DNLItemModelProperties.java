package dev.hexnowloading.dungeonnowloading.client.item;

import com.mojang.serialization.MapCodec;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.item.CopperDetonatorItem;
import dev.hexnowloading.dungeonnowloading.item.RepulsorItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 26.x replaced {@code ItemProperties} predicates with item model definitions. The vertex bow uses the vanilla
 * {@code minecraft:using_item}/{@code minecraft:use_duration} properties; the two mod-specific ones are here.
 */
public final class DNLItemModelProperties {
    private DNLItemModelProperties() {
    }

    public static void register() {
        ConditionalItemModelProperties.ID_MAPPER.put(DungeonNowLoading.id("mode_switch"), ModeSwitch.MAP_CODEC);
        ConditionalItemModelProperties.ID_MAPPER.put(DungeonNowLoading.id("golden_mode"), GoldenMode.MAP_CODEC);
    }

    /** Copper detonator held long enough to switch modes. */
    public record ModeSwitch() implements ConditionalItemModelProperty {
        public static final MapCodec<ModeSwitch> MAP_CODEC = MapCodec.unit(new ModeSwitch());

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
            if (owner == null || owner.getUseItem() != stack) {
                return false;
            }
            int useTime = stack.getUseDuration(owner) - owner.getUseItemRemainingTicks();
            return useTime > CopperDetonatorItem.MODE_SWITCH_TIMING;
        }

        @Override
        public MapCodec<ModeSwitch> type() {
            return MAP_CODEC;
        }
    }

    /** Repulsor in golden mode. */
    public record GoldenMode() implements ConditionalItemModelProperty {
        public static final MapCodec<GoldenMode> MAP_CODEC = MapCodec.unit(new GoldenMode());

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
            return RepulsorItem.isGoldenMode(stack);
        }

        @Override
        public MapCodec<GoldenMode> type() {
            return MAP_CODEC;
        }
    }
}
