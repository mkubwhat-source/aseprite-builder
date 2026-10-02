package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.BallistaGolemModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SpawnerCarrierModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.BallistaArrowEntity;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import dev.hexnowloading.dungeonnowloading.client.legacy.MobRenderer;
import net.minecraft.resources.Identifier;

public class BallistaGolemRenderer<T extends BallistaGolemEntity> extends MobRenderer<T, BallistaGolemModel<T>> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/ballista_golem/ballista_golem.png");

    public BallistaGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BallistaGolemModel<>(renderManager.bakeLayer(BallistaGolemModel.LAYER_LOCATION)), 1.5F);
    }

    @Override
    public void render(BallistaGolemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Render only if the entity is past its first tick
        if (!entity.isSlumbering()) {
            super.render((T) entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    public Identifier getTextureLocation(BallistaGolemEntity ballistaGolemEntity) {
        return TEXTURE;
    }
}
