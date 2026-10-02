package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.entity.monster.BrokenGarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    // Block the auto-dismount-on-sneak while riding the Garhold, so players can't accidentally
    // dismount it mid-combat/motion. Player.wantsToStopRiding() still exists in 1.21 (it's on
    // Player, not Entity) — gate it on the vehicle type.
    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    private void dnl$blockDismountOnGarhold(CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player)(Object) this;
        Entity vehicle = self.getVehicle();

        if ((vehicle instanceof GarholdEntity || vehicle instanceof BrokenGarholdEntity) && !self.getAbilities().instabuild) {
            cir.setReturnValue(false);
        }
    }
}
