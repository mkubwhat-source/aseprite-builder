package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * 1.21.1-style entity renderer: subclasses implement {@link #render} against a {@link MultiBufferSource};
 * the drawing is recorded and submitted through the 26.x pipeline. Leashes and name tags stay vanilla.
 */
public abstract class EntityRenderer<T extends Entity> extends net.minecraft.client.renderer.entity.EntityRenderer<T, LegacyEntityRenderState> {

    protected EntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public abstract Identifier getTextureLocation(T entity);

    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    }

    @Override
    public LegacyEntityRenderState createRenderState() {
        return new LegacyEntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, LegacyEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
        state.partialTick = partialTick;
        state.entityYaw = entity.getYRot(partialTick);
        state.packedLight = this.getPackedLightCoords(entity, partialTick);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void submit(LegacyEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.entity != null) {
            T entity = (T) state.entity;
            RecordingBufferSource.draw(collector, camera, state.outlineColor, buffers -> this.render(entity, state.entityYaw, state.partialTick, poseStack, buffers, state.packedLight));
        }
        super.submit(state, poseStack, collector, camera);
    }
}
