package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FirstPersonHandsAndItems.class)
public class ItemInHandRendererMixin {

    // DNLAnimatedItems (e.g. the Scorcher) rewrite their CUSTOM_DATA every tick (animation
    // StartTime, heat, fuel). Treat the same animated item type as "unchanged" so the per-tick
    // data churn does not re-trigger the first-person re-equip animation.
    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("RETURN"), cancellable = true)
    private void ignoreAnimationNBT(ItemStack currentlyVisibleItem, ItemStack expectedItem, LocalPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (currentlyVisibleItem.getItem() instanceof DNLAnimatedItem<?> && currentlyVisibleItem.is(expectedItem.getItem())) {
            cir.setReturnValue(true);
        }
    }
}
