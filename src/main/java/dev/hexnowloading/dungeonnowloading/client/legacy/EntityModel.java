package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

/**
 * 1.21.1-style entity model ({@code setupAnim(entity, ...)} + {@code renderToBuffer}) used by the mod's
 * renderers through {@link LivingEntityRenderer}. It does not extend the 26.x model hierarchy.
 */
public abstract class EntityModel<T extends Entity> {
    protected final Function<Identifier, RenderType> renderType;
    public float attackTime;
    public boolean riding;
    public boolean young;

    protected EntityModel() {
        this(texture -> RenderTypes.entityCutout(texture));
    }

    protected EntityModel(Function<Identifier, RenderType> renderType) {
        this.renderType = renderType;
    }

    public final RenderType renderType(Identifier texture) {
        return this.renderType.apply(texture);
    }

    public abstract void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch);

    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
    }

    public abstract void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color);

    public final void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        this.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, -1);
    }

    public void copyPropertiesTo(EntityModel<T> other) {
        other.attackTime = this.attackTime;
        other.riding = this.riding;
        other.young = this.young;
    }
}
