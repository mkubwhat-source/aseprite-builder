package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** 1.21.1-style hierarchical model with keyframe animation helpers (animations are baked lazily per model). */
public abstract class HierarchicalModel<T extends Entity> extends EntityModel<T> {
    private final Map<AnimationDefinition, KeyframeAnimation> bakedAnimations = new IdentityHashMap<>();

    protected HierarchicalModel() {
        super();
    }

    protected HierarchicalModel(Function<Identifier, RenderType> renderType) {
        super(renderType);
    }

    public abstract ModelPart root();

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root().render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    public Optional<ModelPart> getAnyDescendantWithName(String name) {
        if (name.equals("root")) {
            return Optional.of(this.root());
        }
        return this.root().getAllParts().stream().filter(part -> part.hasChild(name)).findFirst().map(part -> part.getChild(name));
    }

    private KeyframeAnimation baked(AnimationDefinition definition) {
        return this.bakedAnimations.computeIfAbsent(definition, def -> def.bake(this.root()));
    }

    protected void animate(AnimationState animationState, AnimationDefinition definition, float ageInTicks) {
        this.animate(animationState, definition, ageInTicks, 1.0F);
    }

    protected void animate(AnimationState animationState, AnimationDefinition definition, float ageInTicks, float speed) {
        this.baked(definition).apply(animationState, ageInTicks, speed);
    }

    protected void animateWalk(AnimationDefinition definition, float limbSwing, float limbSwingAmount, float maxAnimationSpeed, float animationScaleFactor) {
        this.baked(definition).applyWalk(limbSwing, limbSwingAmount, maxAnimationSpeed, animationScaleFactor);
    }

    protected void applyStatic(AnimationDefinition definition) {
        this.baked(definition).applyStatic();
    }
}
