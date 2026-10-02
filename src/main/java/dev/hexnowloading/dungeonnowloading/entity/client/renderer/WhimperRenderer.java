package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.layer.WhimperLayer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.WhimperModel;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import dev.hexnowloading.dungeonnowloading.client.legacy.MobRenderer;
import net.minecraft.resources.Identifier;

public class WhimperRenderer<T extends WhimperEntity> extends MobRenderer<T, WhimperModel<T>> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/whimper/whimper.png");

    public WhimperRenderer(EntityRendererProvider.Context context) {
        super(context, new WhimperModel<>(context.bakeLayer(WhimperModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new WhimperLayer<>(this));
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        // Base whimper scale
        float baseScale = 1.5F;
        poseStack.scale(baseScale, baseScale, baseScale);

        // Gigantism: double the whimper model when flagged
        if (entity.isGigantic()) {
            poseStack.scale(2.0F, 2.0F, 2.0F);
        }

        super.scale(entity, poseStack, partialTick);
    }

    @Override
    public Identifier getTextureLocation(T t) {
        return TEXTURE;
    }
}