package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Records vertices written by legacy (1.21.1 style) rendering code and replays them as custom geometry
 * submissions. Vertices are recorded already transformed by the caller's pose stack, so they are replayed as-is.
 */
public final class RecordingBufferSource implements MultiBufferSource {
    private final Map<RenderType, Recorder> buffers = new LinkedHashMap<>();
    private final @Nullable SubmitNodeCollector collector;
    private final @Nullable CameraRenderState camera;
    /** Glowing-effect outline colour of the entity being drawn (0 = no outline). */
    private int outlineColor;

    public RecordingBufferSource() {
        this(null, null);
    }

    public RecordingBufferSource(@Nullable SubmitNodeCollector collector, @Nullable CameraRenderState camera) {
        this.collector = collector;
        this.camera = camera;
    }

    /**
     * 1.21.1 {@code EntityRenderDispatcher#render(entity, 0, 0, 0, yaw, partialTick, poseStack, buffer, light)} for
     * entities drawn inside legacy render code (e.g. a mob preview inside a spawner).
     */
    public static void renderEntity(MultiBufferSource buffer, Entity entity, float partialTick, PoseStack poseStack, int packedLight) {
        if (buffer instanceof RecordingBufferSource source && source.collector != null && source.camera != null) {
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            EntityRenderState state = dispatcher.extractEntity(entity, partialTick);
            state.lightCoords = packedLight;
            dispatcher.submit(state, source.camera, 0.0, 0.0, 0.0, poseStack, source.collector);
        }
    }

    private static final net.minecraft.client.renderer.block.model.BlockDisplayContext BLOCK_DISPLAY_CONTEXT =
            net.minecraft.client.renderer.block.model.BlockDisplayContext.create();

    /** Replacement for 1.21.1 {@code BlockRenderDispatcher#renderSingleBlock}/{@code ModelBlockRenderer#renderModel} in legacy render code. */
    public static void renderBlock(MultiBufferSource buffer, net.minecraft.world.level.block.state.BlockState state, PoseStack poseStack, int packedLight, int packedOverlay) {
        if (buffer instanceof RecordingBufferSource source && source.collector != null) {
            net.minecraft.client.renderer.block.BlockModelRenderState renderState = new net.minecraft.client.renderer.block.BlockModelRenderState();
            new net.minecraft.client.renderer.block.BlockModelResolver(Minecraft.getInstance().getModelManager()).update(renderState, state, BLOCK_DISPLAY_CONTEXT);
            renderState.submit(poseStack, source.collector, packedLight, packedOverlay, 0);
        }
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return this.buffers.computeIfAbsent(renderType, type -> new Recorder());
    }

    /** Replacement for {@code Font#drawInBatch}: submits text through the collector (outlineColor 0 = no outline). */
    public static void renderText(MultiBufferSource buffer, PoseStack poseStack, net.minecraft.util.FormattedCharSequence text, float x, float y,
                                  int color, int outlineColor, int packedLight) {
        if (buffer instanceof RecordingBufferSource source && source.collector != null) {
            source.collector.submitText(poseStack, x, y, text, false, net.minecraft.client.gui.Font.DisplayMode.POLYGON_OFFSET, packedLight, color, 0, outlineColor);
        }
    }

    public void submit(SubmitNodeCollector collector) {
        PoseStack identity = new PoseStack();
        this.buffers.forEach((type, recorder) -> {
            if (!recorder.ops.isEmpty()) {
                collector.submitCustomGeometry(identity, type, (pose, consumer) -> recorder.replay(consumer));
                // Glowing effect: replay the same geometry into the outline pass, coloured with the outline colour.
                if (this.outlineColor != 0 && !type.isOutline() && type.outline().isPresent()) {
                    int color = this.outlineColor;
                    collector.submitCustomGeometry(identity, type.outline().get(), (pose, consumer) -> recorder.replay(new OutlineConsumer(consumer, color)));
                }
            }
        });
        this.buffers.clear();
    }

    /** Runs legacy drawing code and submits whatever it drew. */
    public static void draw(SubmitNodeCollector collector, Consumer<MultiBufferSource> drawing) {
        draw(collector, null, drawing);
    }

    public static void draw(SubmitNodeCollector collector, @Nullable CameraRenderState camera, Consumer<MultiBufferSource> drawing) {
        draw(collector, camera, 0, drawing);
    }

    public static void draw(SubmitNodeCollector collector, @Nullable CameraRenderState camera, int outlineColor, Consumer<MultiBufferSource> drawing) {
        RecordingBufferSource source = new RecordingBufferSource(collector, camera);
        source.outlineColor = outlineColor;
        drawing.accept(source);
        source.submit(collector);
    }

    private static final class Recorder implements VertexConsumer {
        private final List<Consumer<VertexConsumer>> ops = new ArrayList<>();

        void replay(VertexConsumer target) {
            for (Consumer<VertexConsumer> op : this.ops) {
                op.accept(target);
            }
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.ops.add(c -> c.addVertex(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            this.ops.add(c -> c.setColor(r, g, b, a));
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            this.ops.add(c -> c.setColor(color));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.ops.add(c -> c.setUv(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.ops.add(c -> c.setUv1(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.ops.add(c -> c.setUv2(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            this.ops.add(c -> c.setUv3(u, v));
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.ops.add(c -> c.setNormal(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            this.ops.add(c -> c.setLineWidth(width));
            return this;
        }
    }

    /** Forwards geometry to an outline buffer, forcing every vertex to the outline colour. */
    private record OutlineConsumer(VertexConsumer delegate, int color) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.delegate.addVertex(x, y, z).setColor(this.color);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }
}
