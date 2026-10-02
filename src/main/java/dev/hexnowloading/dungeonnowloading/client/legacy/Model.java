package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

/** 1.21.1-style non-entity model (block entity and item models). */
public abstract class Model {
    protected final Function<Identifier, RenderType> renderType;

    protected Model(Function<Identifier, RenderType> renderType) {
        this.renderType = renderType;
    }

    public final RenderType renderType(Identifier texture) {
        return this.renderType.apply(texture);
    }

    public abstract void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color);

    public final void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        this.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, -1);
    }
}
