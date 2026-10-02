package dev.hexnowloading.dungeonnowloading.client.model;

import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.MendingAuraBlockEntityRenderer;
import dev.hexnowloading.dungeonnowloading.block.entity.MendingAuraBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Chunk model of the mending aura block: renders the mimicked block's geometry re-textured with the aura sprite.
 * The aura texture is projected onto each face by block-space position, so it tiles seamlessly across faces.
 */
public class MendingAuraFabricBakedModel extends WrapperBlockStateModel {

    public MendingAuraFabricBakedModel(BlockStateModel wrapped) {
        super(wrapped);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@org.jspecify.annotations.Nullable Direction> cullTest) {
        BlockState storedState = getStoredBlockState(level, pos);
        if (storedState == null) {
            return;
        }

        var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        BlockStateModel storedModel = modelSet.get(storedState);
        TextureAtlasSprite auraSprite = modelSet.getParticleMaterial(DNLBlocks.MENDING_AURA.get().defaultBlockState()).sprite();

        emitter.pushTransform(quad -> {
            Direction cullFace = quad.cullFace();
            if (cullFace != null && !MendingAuraBlockEntityRenderer.AuraTextureModel.shouldRenderAgainstNeighborAura(storedState, level, pos, cullFace)) {
                return false;
            }
            Direction face = quad.nominalFace();
            for (int vertex = 0; vertex < 4; vertex++) {
                float x = quad.x(vertex);
                float y = quad.y(vertex);
                float z = quad.z(vertex);
                float u;
                float v;
                switch (face == null ? Direction.UP : face) {
                    case UP, DOWN -> { u = x; v = z; }
                    case NORTH, SOUTH -> { u = x; v = 1.0F - y; }
                    default -> { u = z; v = 1.0F - y; }
                }
                quad.uv(vertex, auraSprite.getU(wrap(u)), auraSprite.getV(wrap(v)));
                quad.color(vertex, -1);
            }
            quad.tintIndex(-1);
            quad.chunkLayer(ChunkSectionLayer.TRANSLUCENT);
            return true;
        });
        storedModel.emitQuads(emitter, level, pos, storedState, random, cullTest);
        emitter.popTransform();
    }

    private static float wrap(float value) {
        float wrapped = value - (float) Math.floor(value);
        return value != 0.0F && wrapped == 0.0F ? 1.0F : wrapped;
    }

    @Nullable
    private static BlockState getStoredBlockState(BlockAndTintGetter blockView, BlockPos pos) {
        BlockEntity blockEntity = blockView.getBlockEntity(pos);
        if (blockEntity instanceof MendingAuraBlockEntity mendingAuraBlockEntity) {
            BlockState storedState = mendingAuraBlockEntity.getStoredBlockState();
            if (storedState != null && !(storedState.getBlock() instanceof MendingAuraBlock)) {
                return storedState;
            }
        }
        return null;
    }
}
