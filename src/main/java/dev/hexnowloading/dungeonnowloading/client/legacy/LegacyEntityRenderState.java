package dev.hexnowloading.dungeonnowloading.client.legacy;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

/** Render state that carries the entity so 1.21.1-style render code can run at submit time. */
public class LegacyEntityRenderState extends EntityRenderState {
    public Entity entity;
    public float partialTick;
    public float entityYaw;
    public int packedLight;
}
