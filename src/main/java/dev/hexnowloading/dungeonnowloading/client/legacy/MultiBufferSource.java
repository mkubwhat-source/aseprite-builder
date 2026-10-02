package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Stand-in for the 1.21.1 immediate-mode {@code MultiBufferSource}. 26.x renders through
 * {@code SubmitNodeCollector}; {@link RecordingBufferSource} records what legacy code draws and submits it.
 */
public interface MultiBufferSource {
    VertexConsumer getBuffer(RenderType renderType);
}
