package dev.hexnowloading.dungeonnowloading.entity.client.layer;


import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperBorosBodyModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.FairkeeperBorosBodyRenderer;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import dev.hexnowloading.dungeonnowloading.client.legacy.LivingEntityRenderer;
import dev.hexnowloading.dungeonnowloading.client.legacy.RenderLayer;
import net.minecraft.resources.Identifier;

public class FairkeeperBorosBodyLayer<T extends FairkeeperBorosPartEntity, M extends FairkeeperBorosBodyModel<T>> extends RenderLayer<T, M> {

    private static final Identifier TEXTURE_EMISSIVE = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper_boros/fairkeeper_boros_body_emissive.png");

    public FairkeeperBorosBodyLayer(FairkeeperBorosBodyRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLightIn, FairkeeperBorosPartEntity fairkeeperBoros, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.entityTranslucentEmissive(TEXTURE_EMISSIVE));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(fairkeeperBoros, 0), 0xFFFFFFFF);
    }
}