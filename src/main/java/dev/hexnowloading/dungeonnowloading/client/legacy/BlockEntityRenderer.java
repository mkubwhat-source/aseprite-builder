package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** 1.21.1-style block entity renderer; registered through {@link LegacyBlockEntityRenderers}. */
public interface BlockEntityRenderer<T extends BlockEntity> {
    void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay);

    default boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    default int getViewDistance() {
        return 64;
    }

    default boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPos, this.getViewDistance());
    }
}
