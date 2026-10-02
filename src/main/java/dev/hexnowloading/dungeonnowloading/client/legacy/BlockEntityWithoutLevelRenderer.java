package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** 1.21.1-style custom item renderer; registered through {@link LegacyItemRenderers}. */
public abstract class BlockEntityWithoutLevelRenderer {
    protected BlockEntityWithoutLevelRenderer(Object... ignored) {
    }

    public abstract void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
}
