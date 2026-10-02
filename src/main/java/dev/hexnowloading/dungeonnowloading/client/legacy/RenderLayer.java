package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** 1.21.1-style render layer, driven by {@link LivingEntityRenderer}. */
public abstract class RenderLayer<T extends Entity, M extends EntityModel<T>> {
    private final RenderLayerParent<T, M> renderer;

    public RenderLayer(RenderLayerParent<T, M> renderer) {
        this.renderer = renderer;
    }

    protected static <T extends LivingEntity> void coloredCutoutModelCopyLayerRender(
            EntityModel<T> modelParent, EntityModel<T> model, Identifier textureLocation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
            float partialTick, int color) {
        if (!entity.isInvisible()) {
            modelParent.copyPropertiesTo(model);
            model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            renderColoredCutoutModel(model, textureLocation, poseStack, buffer, packedLight, entity, color);
        }
    }

    protected static <T extends LivingEntity> void renderColoredCutoutModel(
            EntityModel<T> model, Identifier textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, int color) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderTypes.entityCutout(textureLocation));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), color);
    }

    public M getParentModel() {
        return this.renderer.getModel();
    }

    protected Identifier getTextureLocation(T entity) {
        return this.renderer.getTextureLocation(entity);
    }

    public abstract void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing,
                                float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch);
}
