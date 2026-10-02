package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

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

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return this.buffers.computeIfAbsent(renderType, type -> new Recorder());
    }

    public void submit(SubmitNodeCollector collector) {
        PoseStack identity = new PoseStack();
        this.buffers.forEach((type, recorder) -> {
            if (!recorder.ops.isEmpty()) {
                collector.submitCustomGeometry(identity, type, (pose, consumer) -> recorder.replay(consumer));
            }
        });
        this.buffers.clear();
    }

    /** Runs legacy drawing code and submits whatever it drew. */
    public static void draw(SubmitNodeCollector collector, Consumer<MultiBufferSource> drawing) {
        RecordingBufferSource source = new RecordingBufferSource();
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
}
