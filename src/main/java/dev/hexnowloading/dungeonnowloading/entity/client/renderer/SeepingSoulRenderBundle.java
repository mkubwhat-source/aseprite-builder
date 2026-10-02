package dev.hexnowloading.dungeonnowloading.entity.client.renderer;


import net.minecraft.client.renderer.rendertype.RenderTypes;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulRenderModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public record SeepingSoulRenderBundle(
        SeepingSoulRenderModel model,
        Identifier baseTexture,
        Identifier eyesTexture
) {
    public RenderType baseRenderType() {
        return RenderTypes.entityTranslucent(baseTexture);
    }
    public RenderType eyesRenderType() {
        return RenderTypes.eyes(eyesTexture);
    }
}
