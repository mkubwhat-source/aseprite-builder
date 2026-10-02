package dev.hexnowloading.dungeonnowloading.client.legacy;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public interface RenderLayerParent<T extends Entity, M extends EntityModel<T>> {
    M getModel();

    Identifier getTextureLocation(T entity);
}
