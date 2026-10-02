package dev.hexnowloading.dungeonnowloading.mixin;

import dev.hexnowloading.dungeonnowloading.util.PendingRegistration;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.Properties.class)
public abstract class ItemPropertiesIdMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void dungeonnowloading$applyPendingId(CallbackInfo ci) {
        ResourceKey<Item> key = PendingRegistration.item();
        if (key != null) {
            Item.Properties self = (Item.Properties) (Object) this;
            self.setId(key);
            if (PendingRegistration.isBlockItem(key)) {
                self.useBlockDescriptionPrefix();
            }
        }
    }
}
