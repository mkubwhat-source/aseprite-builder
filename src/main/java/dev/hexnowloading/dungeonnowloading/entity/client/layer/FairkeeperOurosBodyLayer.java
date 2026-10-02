package dev.hexnowloading.dungeonnowloading.entity.client.layer;


import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.model.FairkeeperOurosBodyModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.FairkeeperOurosBodyRenderer;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import dev.hexnowloading.dungeonnowloading.client.legacy.LivingEntityRenderer;
import dev.hexnowloading.dungeonnowloading.client.legacy.RenderLayer;
import net.minecraft.resources.Identifier;

public class FairkeeperOurosBodyLayer<T extends FairkeeperOurosPartEntity, M extends FairkeeperOurosBodyModel<T>> extends RenderLayer<T, M> {

    private static final Identifier TEXTURE_EMISSIVE = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/fairkeeper_ouros/fairkeeper_ouros_body_emissive.png");

    public FairkeeperOurosBodyLayer(FairkeeperOurosBodyRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLightIn, FairkeeperOurosPartEntity body, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.entityTranslucentEmissive(TEXTURE_EMISSIVE));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(body, 0), 0xFFFFFFFF);
    }
}
