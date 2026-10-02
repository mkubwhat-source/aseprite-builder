package dev.hexnowloading.dungeonnowloading.entity.client.renderer;


import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.HollowTransparentLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.HollowModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.HollowEntity;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import dev.hexnowloading.dungeonnowloading.client.legacy.LivingEntityRenderer;
import dev.hexnowloading.dungeonnowloading.client.legacy.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class HollowRenderer<T extends HollowEntity> extends MobRenderer<T, HollowModel<T>> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/hollow.png");

    public HollowRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HollowModel<>(renderManager.bakeLayer(HollowModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new HollowTransparentLayer<>(this));
    }

    /*@Override
    public void render(T entity, float v, float v1, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        super.render(entity, v, v1, poseStack, multiBufferSource, i);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.entityTranslucentEmissive(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), net.minecraft.util.ARGB.colorFromFloat(0.5F, 1.0F, 1.0F, 1.0F));
    }*/

    @Override
    public Identifier getTextureLocation(HollowEntity hollowEntity) {
        return TEXTURE;
    }
}
