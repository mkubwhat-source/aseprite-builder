package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.network.packets.S2CStructureDetectionPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;


@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    private BlockPos lastCheckedPos = BlockPos.ZERO;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerMove(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        BlockPos currentPos = player.blockPosition();

        if (currentPos.equals(lastCheckedPos)) return; // Skip check if still in the same block

        lastCheckedPos = currentPos;
        // ✅ Get the structure at the player's positionetResourceKey(Identifier.fromNamespaceAndPath("dungeonnowloading", "temple_of_duality"));

        boolean isInTemple = player.level().structureManager().getStructureWithPieceAt(currentPos, DNLTags.TEMPLE_OF_DUALITY).isValid();

        Services.NETWORK.sendToPlayer(new S2CStructureDetectionPacket(isInTemple, player.getId()), player);
    }

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/util/Prediction;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"))
    private void beforeItemDrop(ItemStack itemStack, boolean thrownFromHand, Prediction prediction, CallbackInfoReturnable<ItemEntity> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (itemStack.getItem() instanceof DNLAnimatedItem<?> animatedItem) {
            long gameTime = player.level().getGameTime();
            if (ItemAnimationState.isAnimating(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_ACTIVATED.getName(), gameTime) || ItemAnimationState.isAnimating(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                animatedItem.playDroppedAnimation(player, itemStack);
            }
        }
    }
}
