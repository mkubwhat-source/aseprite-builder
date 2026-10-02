package dev.hexnowloading.dungeonnowloading.block.client.renderer;



import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.world.level.BlockGetter;
import java.util.ArrayList;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.MendingAuraBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import dev.hexnowloading.dungeonnowloading.client.legacy.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MendingAuraBlockEntityRenderer implements BlockEntityRenderer<MendingAuraBlockEntity> {

    private static final int U_OFFSET = 4;
    private static final int V_OFFSET = 5;
    private static final int VERTEX_STRIDE = 8;
    private static final int VERTEX_COUNT = 4;
    private static final int MAX_MASKED_PIXELS_PER_QUAD = 4096;
    private static final float SHAPE_OVERLAY_EPSILON = 0.001F;
    public MendingAuraBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MendingAuraBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState storedState = blockEntity.getStoredBlockState();
        if (storedState == null || storedState.getBlock() instanceof MendingAuraBlock) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        BlockStateModel storedModel = minecraft.getModelManager().getBlockStateModelSet().get(storedState);
        TextureAtlasSprite auraSprite = minecraft.getModelManager().getBlockStateModelSet().getParticleMaterial(DNLBlocks.MENDING_AURA.get().defaultBlockState()).sprite();

        if (blockEntity.getLevel() == null) {
            return;
        }

        if (needsInteractionShapeOverlay(storedState, storedModel, blockEntity)) {
            renderInteractionShapeOverlay(storedState, blockEntity, poseStack, buffer.getBuffer(RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)), auraSprite, packedOverlay);
        }
    }

    private static boolean needsInteractionShapeOverlay(BlockState state, BlockStateModel model, MendingAuraBlockEntity blockEntity) {
        return state.getRenderShape() != net.minecraft.world.level.block.RenderShape.MODEL || !hasAnyBakedQuads(state, model, blockEntity.getBlockPos());
    }

    private static boolean hasAnyBakedQuads(BlockState state, BlockStateModel model, BlockPos pos) {
        RandomSource random = RandomSource.create(state.getSeed(pos));
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);
        for (BlockStateModelPart part : parts) {
            for (Direction direction : Direction.values()) {
                if (!part.getQuads(direction).isEmpty()) {
                    return true;
                }
            }
            if (!part.getQuads(null).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void renderInteractionShapeOverlay(BlockState storedState, MendingAuraBlockEntity blockEntity, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite auraSprite, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        VoxelShape shape = storedState.getInteractionShape(blockEntity.getLevel(), blockEntity.getBlockPos());
        if (shape.isEmpty()) {
            shape = storedState.getShape(blockEntity.getLevel(), blockEntity.getBlockPos(), CollisionContext.empty());
        }
        if (shape.isEmpty()) {
            shape = Shapes.block();
        }

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Set<Direction> culledDirections = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (!AuraTextureModel.shouldRenderAgainstNeighborAura(storedState, blockEntity.getLevel(), blockEntity.getBlockPos(), direction)) {
                culledDirections.add(direction);
            }
        }
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> renderBox(
                consumer,
                matrix,
                pose,
                auraSprite,
                packedOverlay,
                culledDirections,
                inflateBox(minX, minY, minZ, maxX, maxY, maxZ)
        ));
    }

    private static AABB inflateBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AABB(
                Math.max(0.0D, minX - SHAPE_OVERLAY_EPSILON),
                Math.max(0.0D, minY - SHAPE_OVERLAY_EPSILON),
                Math.max(0.0D, minZ - SHAPE_OVERLAY_EPSILON),
                Math.min(1.0D, maxX + SHAPE_OVERLAY_EPSILON),
                Math.min(1.0D, maxY + SHAPE_OVERLAY_EPSILON),
                Math.min(1.0D, maxZ + SHAPE_OVERLAY_EPSILON)
        );
    }

    private static void renderBox(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose, TextureAtlasSprite auraSprite, int packedOverlay, Set<Direction> culledDirections, AABB box) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        if (minX > 0.0F || !culledDirections.contains(Direction.WEST)) renderFace(consumer, matrix, pose, auraSprite, packedOverlay, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, Direction.WEST, minZ, maxZ, minY, maxY);
        if (maxX < 1.0F || !culledDirections.contains(Direction.EAST)) renderFace(consumer, matrix, pose, auraSprite, packedOverlay, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, Direction.EAST, minZ, maxZ, minY, maxY);
        if (minZ > 0.0F || !culledDirections.contains(Direction.NORTH)) renderFace(consumer, matrix, pose, auraSprite, packedOverlay, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, Direction.NORTH, minX, maxX, minY, maxY);
        if (maxZ < 1.0F || !culledDirections.contains(Direction.SOUTH)) renderFace(consumer, matrix, pose, auraSprite, packedOverlay, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, Direction.SOUTH, minX, maxX, minY, maxY);
        if (maxY < 1.0F || !culledDirections.contains(Direction.UP)) renderFace(consumer, matrix, pose, auraSprite, packedOverlay, minX, maxY, maxZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, Direction.UP, minX, maxX, minZ, maxZ);
        if (minY > 0.0F || !culledDirections.contains(Direction.DOWN)) renderFace(consumer, matrix, pose, auraSprite, packedOverlay, minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, Direction.DOWN, minX, maxX, minZ, maxZ);
    }

    private static void renderFace(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose, TextureAtlasSprite auraSprite, int packedOverlay, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, Direction direction, float minUBlock, float maxUBlock, float minVBlock, float maxVBlock) {
        float u0 = auraSprite.getU(minUBlock);
        float u1 = auraSprite.getU(maxUBlock);
        float v0 = auraSprite.getV(minVBlock);
        float v1 = auraSprite.getV(maxVBlock);
        vertex(consumer, matrix, pose, packedOverlay, direction, x1, y1, z1, u0, v1);
        vertex(consumer, matrix, pose, packedOverlay, direction, x2, y2, z2, u0, v0);
        vertex(consumer, matrix, pose, packedOverlay, direction, x3, y3, z3, u1, v0);
        vertex(consumer, matrix, pose, packedOverlay, direction, x4, y4, z4, u1, v1);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose, int packedOverlay, Direction direction, float x, float y, float z, float u, float v) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }


    /** Neighbour culling between adjacent aura blocks (shared by the chunk model and this renderer). */
    public static final class AuraTextureModel {
        private AuraTextureModel() {
        }

        public static boolean shouldRenderAgainstNeighborAura(BlockState storedState, BlockGetter level, BlockPos pos, Direction direction) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(neighborPos);
            if (!(blockEntity instanceof MendingAuraBlockEntity mendingAuraBlockEntity)) {
                return true;
            }

            BlockState neighborStoredState = mendingAuraBlockEntity.getStoredBlockState();
            if (neighborStoredState == null || neighborStoredState.getBlock() instanceof MendingAuraBlock) {
                return true;
            }

            if (storedState.skipRendering(neighborStoredState, direction)) {
                return false;
            }
            if (!neighborStoredState.canOcclude()) {
                return true;
            }

            VoxelShape shape = storedState.getFaceOcclusionShape(direction);
            if (shape.isEmpty()) {
                return true;
            }

            VoxelShape neighborShape = neighborStoredState.getFaceOcclusionShape(direction.getOpposite());
            return Shapes.joinIsNotEmpty(shape, neighborShape, BooleanOp.ONLY_FIRST);
        }
    }
}
