package dev.hexnowloading.dungeonnowloading.entity.client.renderer;


import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.VertexPillarProjectileModel;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexPillarProjectileEntity;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import dev.hexnowloading.dungeonnowloading.client.legacy.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class VertexPillarProjectileRenderer<T extends VertexPillarProjectileEntity> extends EntityRenderer<VertexPillarProjectileEntity> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/vertex_pillar.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucent(TEXTURE);
    private VertexPillarProjectileModel model;

    public VertexPillarProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new VertexPillarProjectileModel(context.bakeLayer(VertexPillarProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(VertexPillarProjectileEntity entity, float v, float v1, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.scale(-0.99f, -0.99F, 0.99F);
        poseStack.translate(0.0f, -entity.getBbHeight() + 0.5F, 0.0f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
        super.render(entity, v, v1, poseStack, multiBufferSource, i);
    }

    @Override
    public Identifier getTextureLocation(VertexPillarProjectileEntity entity) {
        return TEXTURE;
    }
}
