package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.block.entity.DungeonDirectorBlockEntity;
import net.minecraft.client.Minecraft;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import dev.hexnowloading.dungeonnowloading.client.legacy.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class DungeonDirectorRenderer implements BlockEntityRenderer<DungeonDirectorBlockEntity> {

    public DungeonDirectorRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(DungeonDirectorBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // ✅ creative only
        if (!player.getAbilities().instabuild) return;

        BlockState state = be.getBlockState();

        // Render the block model directly (the block itself uses RenderShape.INVISIBLE)
        dev.hexnowloading.dungeonnowloading.client.legacy.RecordingBufferSource.renderBlock(buffer, state, poseStack, packedLight, packedOverlay);
    }
}
