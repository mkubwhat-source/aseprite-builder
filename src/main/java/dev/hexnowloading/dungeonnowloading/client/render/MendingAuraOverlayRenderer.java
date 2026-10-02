package dev.hexnowloading.dungeonnowloading.client.render;


import dev.hexnowloading.dungeonnowloading.client.legacy.LegacyBlockEntityRenderers;
import dev.hexnowloading.dungeonnowloading.client.model.MendingAuraFabricBakedModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
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
                VertexConsumer consumer = new AlphaVertexConsumer(translucentConsumer, overlay.alpha());
                boolean drawn = state.getRenderShape() == RenderShape.MODEL
                        && renderModelOverlay(state, pos, poseStack, consumer, auraSprite);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null && LegacyBlockEntityRenderers.has(blockEntity)) {
                    // the mod's own block entity renderers: redraw their geometry with the aura texture
                    LegacyBlockEntityRenderers.renderItem(blockEntity, poseStack, type -> new AuraRetextureConsumer(consumer, auraSprite, new Vector3f((float) (pos.getX() - cameraPos.x), (float) (pos.getY() - cameraPos.y), (float) (pos.getZ() - cameraPos.z))), LightCoordsUtil.FULL_BRIGHT, 0);
                    drawn = true;
                }
                if (!drawn) {
                    renderShapeOverlay(state, level, pos, poseStack, consumer, auraSprite);
                }
                poseStack.popPose();
            }
        });
    }

    /** Re-draws the block's own model with the aura texture (masked to the opaque texture pixels), slightly inflated. */
    private static boolean renderModelOverlay(BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite auraSprite) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(state.getSeed(pos)), parts);
        PoseStack.Pose pose = poseStack.last();
        boolean any = false;
        for (BlockStateModelPart part : parts) {
            for (Direction cullFace : MendingAuraFabricBakedModel.CULL_FACES) {
                for (BakedQuad quad : part.getQuads(cullFace)) {
                    any = true;
                    Direction face = quad.direction();
                    float ox = face.getStepX() * MODEL_OVERLAY_OFFSET;
                    float oy = face.getStepY() * MODEL_OVERLAY_OFFSET;
                    float oz = face.getStepZ() * MODEL_OVERLAY_OFFSET;
                    for (Vector3f[] corners : MendingAuraFabricBakedModel.maskedQuads(quad)) {
                        for (Vector3f p : corners) {
                            consumer.addVertex(pose, p.x + ox, p.y + oy, p.z + oz)
                                    .setColor(255, 255, 255, 255)
                                    .setUv(MendingAuraFabricBakedModel.auraU(auraSprite, face, p.x, p.y, p.z), MendingAuraFabricBakedModel.auraV(auraSprite, face, p.x, p.y, p.z))
                                    .setOverlay(0)
                                    .setLight(LightCoordsUtil.FULL_BRIGHT)
                                    .setNormal(pose, face.getStepX(), face.getStepY(), face.getStepZ());
                        }
                    }
                }
            }
        }
        return any;
    }

    /**
     * Takes geometry from a legacy block entity renderer and replaces its texture with the aura, projected from the
     * vertex position (relative to the block, which the pose stack already translated to) and its normal.
     */
    private static final class AuraRetextureConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final TextureAtlasSprite auraSprite;
        private final Vector3f blockOffset;
        private float x;
        private float y;
        private float z;

        private AuraRetextureConsumer(VertexConsumer delegate, TextureAtlasSprite auraSprite, Vector3f blockOffset) {
            this.delegate = delegate;
            this.auraSprite = auraSprite;
            this.blockOffset = blockOffset;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            // positions arrive camera-relative; project the texture in block space
            this.x = x - this.blockOffset.x;
            this.y = y - this.blockOffset.y;
            this.z = z - this.blockOffset.z;
            this.delegate.addVertex(x, y, z).setColor(-1).setOverlay(0).setLight(LightCoordsUtil.FULL_BRIGHT);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float nx, float ny, float nz) {
            // the normal arrives after the position: pick the projection plane from the dominant normal axis
            Direction face = Direction.getApproximateNearest(nx, ny, nz);
            this.delegate.setUv(MendingAuraFabricBakedModel.auraU(this.auraSprite, face, this.x, this.y, this.z),
                    MendingAuraFabricBakedModel.auraV(this.auraSprite, face, this.x, this.y, this.z));
            this.delegate.setNormal(nx, ny, nz);
            return this;
        }
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
            this.delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            this.delegate.setColor(red, green, blue, Math.round(alpha * this.alpha));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.delegate.setNormal(x, y, z);
            return this;
        }
    }
}
