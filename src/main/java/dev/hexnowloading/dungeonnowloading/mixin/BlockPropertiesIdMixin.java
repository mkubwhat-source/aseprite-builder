package dev.hexnowloading.dungeonnowloading.mixin;

import dev.hexnowloading.dungeonnowloading.util.PendingRegistration;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesIdMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void dungeonnowloading$applyPendingId(CallbackInfo ci) {
        ResourceKey<Block> key = PendingRegistration.block();
        if (key != null) {
            ((BlockBehaviour.Properties) (Object) this).setId(key);
        }
    }
}
