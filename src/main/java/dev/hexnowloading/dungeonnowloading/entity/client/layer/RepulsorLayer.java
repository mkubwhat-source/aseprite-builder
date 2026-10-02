package dev.hexnowloading.dungeonnowloading.entity.client.layer;


import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.RepulsorModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.RepulsorRenderer;
import dev.hexnowloading.dungeonnowloading.entity.misc.RepulsorEntity;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import dev.hexnowloading.dungeonnowloading.client.legacy.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class RepulsorLayer<T extends RepulsorEntity, M extends RepulsorModel<T>> extends RenderLayer<T, M> {
    private static final Identifier EMISSIVE = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/repulsor/repulsor_emissive.png");

    public RepulsorLayer(RepulsorRenderer renderer) { super(renderer); }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLightIn, RepulsorEntity repulsorEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.entityTranslucentEmissive(EMISSIVE));
        if (repulsorEntity.getAge() >= 35) {
            if (repulsorEntity.getShieldHealth() <= RepulsorEntity.SHIELD_ALERT_THRESHOLD) {
                float healthRation = repulsorEntity.getShieldHealth() / RepulsorEntity.SHIELD_ALERT_THRESHOLD;
                float blinkCycle = 40 - 35F * (1 - healthRation);
                float alpha = (repulsorEntity.getAge() % blinkCycle < blinkCycle / 2) ? 1.0F : 0.0F;
                this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, net.minecraft.util.ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));
            } else {
                this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            }
        }
    }
}
