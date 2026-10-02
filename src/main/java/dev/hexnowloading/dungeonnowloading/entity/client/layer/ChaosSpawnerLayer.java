package dev.hexnowloading.dungeonnowloading.entity.client.layer;


import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.ChaosSpawnerModel;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.ChaosSpawnerRenderer;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import dev.hexnowloading.dungeonnowloading.client.legacy.LivingEntityRenderer;
import dev.hexnowloading.dungeonnowloading.client.legacy.RenderLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AnimationState;

public class ChaosSpawnerLayer<T extends ChaosSpawnerEntity, M extends ChaosSpawnerModel<T>> extends RenderLayer<T, M> {

    private static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/chaos_spawner/chaos_spawner_eyes.png");
    private static final Identifier TEXTURE_CHAINED = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/chaos_spawner/chaos_spawner_chained.png");
    private static final Identifier TEXTURE_SHOCKWAVE = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/chaos_spawner/chaos_spawner_shockwave.png");
    private static final Identifier TEXTURE_CHAOS_HEXAHEDRON = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/chaos_spawner/chaos_spawner_chaos_hexahedron.png");

    public ChaosSpawnerLayer(ChaosSpawnerRenderer renderer) {
        super(renderer);
    }

    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, ChaosSpawnerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entitylivingbaseIn.smashAttackAnimationState.isStarted()) {
            VertexConsumer shockwaveVertexConsumer = bufferIn.getBuffer(RenderTypes.entityTranslucentEmissive(TEXTURE_SHOCKWAVE));
            this.getParentModel().renderToBuffer(matrixStackIn, shockwaveVertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(entitylivingbaseIn, 0), 0xFFFFFFFF);
        } else if (entitylivingbaseIn.rangeAttackAnimationState.isStarted() || entitylivingbaseIn.rangeBurstAttackAnimationState.isStarted()) {
            VertexConsumer chaosHexahedronVertexConsumer = bufferIn.getBuffer(RenderTypes.entityTranslucentEmissive(TEXTURE_CHAOS_HEXAHEDRON));
            this.getParentModel().renderToBuffer(matrixStackIn, chaosHexahedronVertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(entitylivingbaseIn, 0), net.minecraft.util.ARGB.colorFromFloat(0.8F, 1.0F, 1.0F, 1.0F));
        } else if (entitylivingbaseIn.getState() == ChaosSpawnerEntity.State.SLEEPING || entitylivingbaseIn.getAwakeningTick() > 100) {
            VertexConsumer chainVertexConsumer = bufferIn.getBuffer(RenderTypes.entityTranslucentEmissive(TEXTURE_CHAINED));
            this.getParentModel().renderToBuffer(matrixStackIn, chainVertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(entitylivingbaseIn, 0), net.minecraft.util.ARGB.colorFromFloat(0.8F, 1.0F, 1.0F, 1.0F));
        } else {
            VertexConsumer eyesVertexConsumer = bufferIn.getBuffer(RenderTypes.entityTranslucentEmissive(TEXTURE_EYES));
            this.getParentModel().renderToBuffer(matrixStackIn, eyesVertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(entitylivingbaseIn, 0), 0xFFFFFFFF);
        }
    }
}
