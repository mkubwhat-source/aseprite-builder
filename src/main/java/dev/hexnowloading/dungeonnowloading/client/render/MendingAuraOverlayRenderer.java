package dev.hexnowloading.dungeonnowloading.client.render;


import dev.hexnowloading.dungeonnowloading.client.legacy.RecordingBufferSource;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.MendingAuraBlockEntityRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import dev.hexnowloading.dungeonnowloading.client.legacy.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class MendingAuraOverlayRenderer {
    private static final int OVERLAY_TICKS = 40;
    private static final float MODEL_OVERLAY_OFFSET = 0.002F;
    private static final float SHAPE_OVERLAY_EPSILON = 0.001F;
    private static final Identifier MENDING_AURA_SPRITE = Identifier.fromNamespaceAndPath("dungeonnowloading", "block/mending_aura_0");

    private MendingAuraOverlayRenderer() {
    }

    public static void add(BlockPos pos) {
        MendingAuraOverlayClientState.add(pos, OVERLAY_TICKS);
    }

    /** Called from {@code LevelRenderEvents.COLLECT_SUBMITS}; draws a fading aura over recently repaired blocks. */
    public static void render(PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector, Vec3 cameraPos, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) {
            return;
        }

        var overlays = MendingAuraOverlayClientState.getActive(level, partialTick);
        if (overlays.isEmpty()) {
            return;
        }

        TextureAtlasSprite auraSprite = minecraft.getModelManager().getBlockStateModelSet()
                .getParticleMaterial(DNLBlocks.MENDING_AURA.get().defaultBlockState()).sprite();

        RecordingBufferSource.draw(collector, buffers -> {
            VertexConsumer translucentConsumer = buffers.getBuffer(RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
            for (MendingAuraOverlayClientState.ActiveOverlay overlay : overlays) {
                BlockPos pos = overlay.pos();
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.getBlock() instanceof MendingAuraBlock) {
                    continue;
                }

                poseStack.pushPose();
                poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
                renderShapeOverlay(state, level, pos, poseStack, new AlphaVertexConsumer(translucentConsumer, overlay.alpha()), auraSprite);
                poseStack.popPose();
            }
        });
    }

    private static void renderShapeOverlay(BlockState state, Level level, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite auraSprite) {
        VoxelShape shape = state.getInteractionShape(level, pos);
        if (shape.isEmpty()) {
            shape = state.getShape(level, pos, CollisionContext.empty());
        }
        if (shape.isEmpty()) {
            shape = Shapes.block();
        }

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> renderBox(
                consumer,
                matrix,
                pose,
                auraSprite,
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

    private static void renderBox(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose, TextureAtlasSprite auraSprite, AABB box) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        renderFace(consumer, matrix, pose, auraSprite, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, Direction.WEST, minZ, maxZ, minY, maxY);
        renderFace(consumer, matrix, pose, auraSprite, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, Direction.EAST, minZ, maxZ, minY, maxY);
        renderFace(consumer, matrix, pose, auraSprite, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, Direction.NORTH, minX, maxX, minY, maxY);
        renderFace(consumer, matrix, pose, auraSprite, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, Direction.SOUTH, minX, maxX, minY, maxY);
        renderFace(consumer, matrix, pose, auraSprite, minX, maxY, maxZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, Direction.UP, minX, maxX, minZ, maxZ);
        renderFace(consumer, matrix, pose, auraSprite, minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, Direction.DOWN, minX, maxX, minZ, maxZ);
    }

    private static void renderFace(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose, TextureAtlasSprite auraSprite, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, Direction direction, float minUBlock, float maxUBlock, float minVBlock, float maxVBlock) {
        // 1.21: getU/getV take a normalized 0..1 fraction (1.20.1 took 0..16). minUBlock..maxVBlock
        // are already 0..1 block-face extents.
        float u0 = auraSprite.getU(minUBlock);
        float u1 = auraSprite.getU(maxUBlock);
        float v0 = auraSprite.getV(minVBlock);
        float v1 = auraSprite.getV(maxVBlock);
        vertex(consumer, matrix, pose, direction, x1, y1, z1, u0, v1);
        vertex(consumer, matrix, pose, direction, x2, y2, z2, u0, v0);
        vertex(consumer, matrix, pose, direction, x3, y3, z3, u1, v0);
        vertex(consumer, matrix, pose, direction, x4, y4, z4, u1, v1);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose, Direction direction, float x, float y, float z, float u, float v) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    private static class AlphaVertexConsumer implements VertexConsumer {

        @Override
        public VertexConsumer setColor(int color) {
            return this.setColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >>> 24) & 0xFF);
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            return this;
        }
        private final VertexConsumer delegate;
        private final float alpha;

        private AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
            this.delegate = delegate;
            this.alpha = alpha;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return this.delegate.addVertex(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this.delegate.setColor(red, green, blue, Math.round(alpha * this.alpha));
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this.delegate.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this.delegate.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this.delegate.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this.delegate.setNormal(x, y, z);
        }
    }
}
