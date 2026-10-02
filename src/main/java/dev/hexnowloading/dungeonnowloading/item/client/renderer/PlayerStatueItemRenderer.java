package dev.hexnowloading.dungeonnowloading.item.client.renderer;

import dev.hexnowloading.dungeonnowloading.util.StackNbt;
import dev.hexnowloading.dungeonnowloading.util.ProfileNbt;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.block.PlayerStatueBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import dev.hexnowloading.dungeonnowloading.client.legacy.BlockEntityWithoutLevelRenderer;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerStatueItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final PlayerStatueItemRenderer INSTANCE =
            new PlayerStatueItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()
            );

    public PlayerStatueItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        // Build block state for the dummy BE
        BlockState state = DNLBlocks.PLAYER_STATUE.get().defaultBlockState();
        CompoundTag tag = StackNbt.getTag(stack);

        // Keep a deterministic statue yaw for items (or read NBT if you like).
        // Using 8 (180°) or 14 (225°) are common; pick one and stick to it.
        int rot16 = 8;
        if (tag != null && tag.contains("Rotation")) {
            rot16 = Mth.clamp(tag.getIntOr("Rotation", 0), 0, 15);
        }
        state = state.setValue(PlayerStatueBlock.ROTATION, rot16);

        // Build a throwaway BE populated from NBT so the BER renders the right skin/pose/notch
        PlayerStatueBlockEntity be = new PlayerStatueBlockEntity(BlockPos.ZERO, state);

        GameProfile gp = null;
        if (tag != null) {
            if (tag.contains("Owner")) gp = ProfileNbt.read(tag.getCompoundOrEmpty("Owner"));
            else if (tag.contains("SkullOwner")) gp = new GameProfile(null, tag.getStringOr("SkullOwner", ""));
            if (tag.contains("DNL_Pose"))  be.setPoseVariant(tag.getIntOr("DNL_Pose", 0));
            if (tag.contains("DNL_Notch")) be.setNotchTier(
                    dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity.NotchTier.fromString(tag.getStringOr("DNL_Notch", "")));
        }
        be.setOwner(gp);

        pose.pushPose();

        // 🔑 Neutralize the BER's internal center-translate so JSON 'display' stays perfectly centered.
        // Your BER does: pose.translate(0.5, 0, 0.5) ... so cancel it here:
        //pose.translate(-0.5f, 0f, -0.5f);

        // ⛔ No additional rotate/scale/translate here — let the JSON 'display' handle all contexts.
        dev.hexnowloading.dungeonnowloading.client.legacy.LegacyBlockEntityRenderers.renderItem(be, pose, buf, light, overlay);

        pose.popPose();
    }

    public static PlayerStatueItemRenderer getInstance() {
        return INSTANCE;
    }
}
