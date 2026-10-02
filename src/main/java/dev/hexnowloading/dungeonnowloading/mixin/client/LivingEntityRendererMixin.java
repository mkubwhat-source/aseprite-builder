package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.entity.monster.BrokenGarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidMobRenderer.class)
public abstract class LivingEntityRendererMixin {

    // Riders of a Garhold stand on its back instead of sitting.
    @Inject(method = "extractHumanoidRenderState", at = @At("TAIL"))
    private static void dnl$noSittingWhenRidingGarhold(LivingEntity entity, HumanoidRenderState state, float partialTicks, ItemModelResolver resolver, CallbackInfo ci) {
        if ((entity.getVehicle() instanceof GarholdEntity || entity.getVehicle() instanceof BrokenGarholdEntity)) {
            state.isPassenger = false;
        }
    }
}
