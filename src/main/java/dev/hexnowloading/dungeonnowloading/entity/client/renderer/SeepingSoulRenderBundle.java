package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulRenderModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public record SeepingSoulRenderBundle(
        SeepingSoulRenderModel model,
        Identifier baseTexture,
        Identifier eyesTexture
) {
    public RenderType baseRenderType() {
        return RenderType.entityTranslucent(baseTexture);
    }
    public RenderType eyesRenderType() {
        return RenderType.eyes(eyesTexture);
    }
}
