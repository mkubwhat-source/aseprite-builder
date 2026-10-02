package dev.hexnowloading.dungeonnowloading.entity.client.layer;


import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WhimperModel;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import dev.hexnowloading.dungeonnowloading.client.legacy.LivingEntityRenderer;
import dev.hexnowloading.dungeonnowloading.client.legacy.RenderLayerParent;
import dev.hexnowloading.dungeonnowloading.client.legacy.RenderLayer;
import net.minecraft.resources.Identifier;

public class WhimperLayer<T extends WhimperEntity, M extends WhimperModel<T>> extends RenderLayer<T, M> {

    private static final Identifier TEXTURE_EMISSIVE = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/whimper/whimper_emissive.png");

    public WhimperLayer(RenderLayerParent<T, M> $$0) {
        super($$0);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T t, float v, float v1, float v2, float v3, float v4, float v5) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.entityTranslucentEmissive(TEXTURE_EMISSIVE));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(t, 0), 0xFFFFFFFF);
    }
}
