package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Registers 1.21.1-style block entity renderers and runs them inside the 26.x submit pipeline. */
public final class LegacyBlockEntityRenderers {
    private static final Map<BlockEntityType<?>, BlockEntityRenderer<?>> INSTANCES = new HashMap<>();

    private LegacyBlockEntityRenderers() {
    }

    public static <T extends BlockEntity> void register(BlockEntityType<? extends T> type, Function<BlockEntityRendererProvider.Context, BlockEntityRenderer<T>> factory) {
        BlockEntityRenderers.register(type, context -> {
            BlockEntityRenderer<T> renderer = factory.apply(context);
            INSTANCES.put(type, renderer);
            return new Adapter<>(renderer);
        });
    }

    /** Replacement for 1.21.1 {@code BlockEntityRenderDispatcher#renderItem}: draws a block entity as an item. */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> void renderItem(T blockEntity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockEntityRenderer<T> renderer = (BlockEntityRenderer<T>) INSTANCES.get(blockEntity.getType());
        if (renderer != null) {
            renderer.render(blockEntity, 0.0F, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    public static class State extends BlockEntityRenderState {
        public BlockEntity blockEntity;
        public float partialTick;
    }

    private record Adapter<T extends BlockEntity>(BlockEntityRenderer<T> renderer)
            implements net.minecraft.client.renderer.blockentity.BlockEntityRenderer<T, State> {

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(T blockEntity, State state, float partialTicks, Vec3 cameraPosition,
                                       ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
            net.minecraft.client.renderer.blockentity.BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
            state.blockEntity = blockEntity;
            state.partialTick = partialTicks;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
            if (state.blockEntity != null) {
                T blockEntity = (T) state.blockEntity;
                RecordingBufferSource.draw(collector, camera, buffers ->
                        this.renderer.render(blockEntity, state.partialTick, poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY));
            }
        }

        @Override
        public boolean shouldRenderOffScreen() {
            return true;
        }

        @Override
        public int getViewDistance() {
            return this.renderer.getViewDistance();
        }

        @Override
        public boolean shouldRender(T blockEntity, Vec3 cameraPosition) {
            return this.renderer.shouldRender(blockEntity, cameraPosition);
        }
    }
}
