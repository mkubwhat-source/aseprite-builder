package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Port of the 1.21.1 {@code LivingEntityRenderer#render} pipeline on top of {@link EntityRenderer}. */
public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
    protected M model;
    protected final List<RenderLayer<T, M>> layers = new ArrayList<>();

    public LivingEntityRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context);
        this.model = model;
        this.shadowRadius = shadowRadius;
    }

    public final boolean addLayer(RenderLayer<T, M> layer) {
        return this.layers.add(layer);
    }

    @Override
    public M getModel() {
        return this.model;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        this.model.attackTime = entity.getSwingAnimation(partialTicks);
        boolean shouldSit = entity.isPassenger() && entity.getVehicle() != null;
        this.model.riding = shouldSit;
        this.model.young = entity.isBaby();
        float bodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = headRot - bodyRot;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity vehicle) {
            bodyRot = Mth.rotLerp(partialTicks, vehicle.yBodyRotO, vehicle.yBodyRot);
            netHeadYaw = headRot - bodyRot;
            float clamped = Mth.wrapDegrees(netHeadYaw);
            if (clamped < -85.0F) {
                clamped = -85.0F;
            }
            if (clamped >= 85.0F) {
                clamped = 85.0F;
            }
            bodyRot = headRot - clamped;
            if (clamped * clamped > 2500.0F) {
                bodyRot += clamped * 0.2F;
            }
            netHeadYaw = headRot - bodyRot;
        }

        float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (isEntityUpsideDown(entity)) {
            headPitch *= -1.0F;
            netHeadYaw *= -1.0F;
        }
        netHeadYaw = Mth.wrapDegrees(netHeadYaw);

        if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float eyeOffset = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                poseStack.translate(-direction.getStepX() * eyeOffset, 0.0F, -direction.getStepZ() * eyeOffset);
            }
        }

        float scale = entity.getScale();
        poseStack.scale(scale, scale, scale);
        float ageInTicks = this.getBob(entity, partialTicks);
        this.setupRotations(entity, poseStack, ageInTicks, bodyRot, partialTicks, scale);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!shouldSit && entity.isAlive()) {
            limbSwingAmount = entity.walkAnimation.speed(partialTicks);
            limbSwing = entity.walkAnimation.position(partialTicks);
            if (entity.isBaby()) {
                limbSwing *= 3.0F;
            }
            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
        }

        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        Minecraft minecraft = Minecraft.getInstance();
        boolean bodyVisible = this.isBodyVisible(entity);
        boolean translucent = !bodyVisible && minecraft.player != null && !entity.isInvisibleTo(minecraft.player);
        boolean glowing = minecraft.shouldEntityAppearGlowing(entity);
        RenderType renderType = this.getRenderType(entity, bodyVisible, translucent, glowing);
        if (renderType != null) {
            VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
            int overlay = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
            this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, overlay, translucent ? 0x26FFFFFF : -1);
        }

        if (!entity.isSpectator()) {
            for (RenderLayer<T, M> layer : this.layers) {
                layer.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    protected @Nullable RenderType getRenderType(T livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        net.minecraft.resources.Identifier texture = this.getTextureLocation(livingEntity);
        if (translucent) {
            return RenderTypes.itemTranslucent(texture);
        } else if (bodyVisible) {
            return this.model.renderType(texture);
        } else {
            return glowing ? RenderTypes.outline(texture) : null;
        }
    }

    public static int getOverlayCoords(LivingEntity livingEntity, float whiteOverlayProgress) {
        return OverlayTexture.pack(OverlayTexture.u(whiteOverlayProgress), OverlayTexture.v(livingEntity.hurtTime > 0 || livingEntity.deathTime > 0));
    }

    protected boolean isBodyVisible(T livingEntity) {
        return !livingEntity.isInvisible();
    }

    private static float sleepDirectionToRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90.0F;
            case WEST -> 0.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

    protected boolean isShaking(T entity) {
        return entity.isFullyFrozen();
    }

    protected void setupRotations(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        if (this.isShaking(entity)) {
            yBodyRot += (float) (Math.cos(entity.tickCount * 3.25) * Math.PI * 0.4F);
        }

        if (!entity.hasPose(Pose.SLEEPING)) {
            poseStack.rotate(Axis.YP.rotationDegrees(180.0F - yBodyRot));
        }

        if (entity.deathTime > 0) {
            float flip = (entity.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
            flip = Mth.sqrt(flip);
            if (flip > 1.0F) {
                flip = 1.0F;
            }
            poseStack.rotate(Axis.ZP.rotationDegrees(flip * this.getFlipDegrees(entity)));
        } else if (entity.isAutoSpinAttack()) {
            poseStack.rotate(Axis.XP.rotationDegrees(-90.0F - entity.getXRot()));
            poseStack.rotate(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * -75.0F));
        } else if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.getBedOrientation();
            float rotation = direction != null ? sleepDirectionToRotation(direction) : yBodyRot;
            poseStack.rotate(Axis.YP.rotationDegrees(rotation));
            poseStack.rotate(Axis.ZP.rotationDegrees(this.getFlipDegrees(entity)));
            poseStack.rotate(Axis.YP.rotationDegrees(270.0F));
        } else if (isEntityUpsideDown(entity)) {
            poseStack.translate(0.0F, (entity.getBbHeight() + 0.1F) / scale, 0.0F);
            poseStack.rotate(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    protected float getAttackAnim(T livingBase, float partialTickTime) {
        return livingBase.getSwingAnimation(partialTickTime);
    }

    protected float getBob(T livingBase, float partialTick) {
        return livingBase.tickCount + partialTick;
    }

    protected float getFlipDegrees(T livingEntity) {
        return 90.0F;
    }

    protected float getWhiteOverlayProgress(T livingEntity, float partialTicks) {
        return 0.0F;
    }

    protected void scale(T livingEntity, PoseStack poseStack, float partialTickTime) {
    }

    public static boolean isEntityUpsideDown(LivingEntity entity) {
        if (entity instanceof Player || entity.hasCustomName()) {
            String name = ChatFormatting.stripFormatting(entity.getName().getString());
            if ("Dinnerbone".equals(name) || "Grumm".equals(name)) {
                return !(entity instanceof Player player) || player.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.CAPE);
            }
        }
        return false;
    }

    @Override
    protected boolean shouldShowName(T entity, double distanceToCameraSq) {
        return super.shouldShowName(entity, distanceToCameraSq) && entity != Minecraft.getInstance().getCameraEntity() && !entity.isInvisible();
    }

    protected static boolean isEntityFullyLoaded(Entity entity) {
        return entity != null;
    }
}
