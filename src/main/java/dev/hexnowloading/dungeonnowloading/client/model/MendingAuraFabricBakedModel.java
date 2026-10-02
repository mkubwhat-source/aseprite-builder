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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.model.geom.builders.UVPair;
import org.joml.Vector3f;

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

        List<BlockStateModelPart> parts = new ArrayList<>();
        storedModel.collectParts(random, parts);
        for (BlockStateModelPart part : parts) {
            for (Direction cullFace : CULL_FACES) {
                if (cullFace != null && (cullTest.test(cullFace)
                        || !MendingAuraBlockEntityRenderer.AuraTextureModel.shouldRenderAgainstNeighborAura(storedState, level, pos, cullFace))) {
                    continue;
                }
                for (BakedQuad quad : part.getQuads(cullFace)) {
                    for (Vector3f[] corners : maskedQuads(quad)) {
                        emitAuraQuad(emitter, corners, quad.direction(), cullFace, auraSprite);
                    }
                }
            }
        }
    }

    private static final Direction[] CULL_FACES = {null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final int MAX_MASKED_PIXELS_PER_QUAD = 4096;
    /** Opaque-pixel rectangles per source quad; chunk meshes are built off-thread, hence the synchronized map. */
    private static final Map<BakedQuad, List<Vector3f[]>> MASK_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private static void emitAuraQuad(QuadEmitter emitter, Vector3f[] corners, Direction face, @Nullable Direction cullFace, TextureAtlasSprite auraSprite) {
        for (int vertex = 0; vertex < 4; vertex++) {
            Vector3f p = corners[vertex];
            float u;
            float v;
            switch (face) {
                case UP, DOWN -> { u = p.x; v = p.z; }
                case NORTH, SOUTH -> { u = p.x; v = 1.0F - p.y; }
                default -> { u = p.z; v = 1.0F - p.y; }
            }
            emitter.pos(vertex, p.x, p.y, p.z);
            emitter.uv(vertex, auraSprite.getU(wrap(u)), auraSprite.getV(wrap(v)));
            emitter.color(vertex, -1);
        }
        emitter.nominalFace(face);
        emitter.cullFace(cullFace);
        emitter.tintIndex(-1);
        emitter.chunkLayer(ChunkSectionLayer.TRANSLUCENT);
        emitter.emit();
    }

    /**
     * The aura only covers the pixels of the mimicked texture that are visible: a quad whose texture has transparent
     * pixels (leaves, glass panes, flowers, ...) is split into rectangles of opaque pixels.
     */
    private static List<Vector3f[]> maskedQuads(BakedQuad quad) {
        List<Vector3f[]> cached = MASK_CACHE.get(quad);
        if (cached != null) {
            return cached;
        }
        List<Vector3f[]> result = computeMaskedQuads(quad);
        MASK_CACHE.put(quad, result);
        return result;
    }

    private static List<Vector3f[]> computeMaskedQuads(BakedQuad quad) {
        TextureAtlasSprite sprite = quad.materialInfo().sprite();
        SpriteContents contents = sprite.contents();
        float[] su = new float[4];
        float[] sv = new float[4];
        float minU = Float.MAX_VALUE, maxU = -Float.MAX_VALUE, minV = Float.MAX_VALUE, maxV = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            long uv = quad.packedUV(i);
            su[i] = (UVPair.unpackU(uv) - sprite.getU0()) / (sprite.getU1() - sprite.getU0());
            sv[i] = (UVPair.unpackV(uv) - sprite.getV0()) / (sprite.getV1() - sprite.getV0());
            minU = Math.min(minU, su[i]); maxU = Math.max(maxU, su[i]);
            minV = Math.min(minV, sv[i]); maxV = Math.max(maxV, sv[i]);
        }
        Vector3f[] original = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            original[i] = new Vector3f(quad.position(i));
        }
        if (maxU - minU < 1.0E-6F || maxV - minV < 1.0E-6F) {
            return Collections.singletonList(original);
        }

        int xStart = Math.max(0, (int) Math.floor(minU * contents.width() + 1.0E-4F));
        int xEnd = Math.min(contents.width(), (int) Math.ceil(maxU * contents.width() - 1.0E-4F));
        int yStart = Math.max(0, (int) Math.floor(minV * contents.height() + 1.0E-4F));
        int yEnd = Math.min(contents.height(), (int) Math.ceil(maxV * contents.height() - 1.0E-4F));
        int w = xEnd - xStart;
        int h = yEnd - yStart;
        if (w <= 0 || h <= 0 || w * h > MAX_MASKED_PIXELS_PER_QUAD) {
            return Collections.singletonList(original);
        }

        boolean[][] opaque = new boolean[h][w];
        boolean anyTransparent = false;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                opaque[y][x] = !contents.isTransparent(0, xStart + x, yStart + y);
                anyTransparent |= !opaque[y][x];
            }
        }
        if (!anyTransparent) {
            return Collections.singletonList(original);
        }

        // Corners of the source quad in texture space, used to map texture rectangles back to positions.
        Vector3f c00 = nearest(original, su, sv, minU, minV);
        Vector3f c10 = nearest(original, su, sv, maxU, minV);
        Vector3f c11 = nearest(original, su, sv, maxU, maxV);
        Vector3f c01 = nearest(original, su, sv, minU, maxV);

        List<Vector3f[]> rects = new ArrayList<>();
        boolean[][] used = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!opaque[y][x] || used[y][x]) {
                    continue;
                }
                int rw = 0;
                while (x + rw < w && opaque[y][x + rw] && !used[y][x + rw]) {
                    rw++;
                }
                int rh = 1;
                grow:
                while (y + rh < h) {
                    for (int xx = x; xx < x + rw; xx++) {
                        if (!opaque[y + rh][xx] || used[y + rh][xx]) {
                            break grow;
                        }
                    }
                    rh++;
                }
                for (int yy = y; yy < y + rh; yy++) {
                    for (int xx = x; xx < x + rw; xx++) {
                        used[yy][xx] = true;
                    }
                }
                float ru0 = (xStart + x) / (float) contents.width();
                float ru1 = (xStart + x + rw) / (float) contents.width();
                float rv0 = (yStart + y) / (float) contents.height();
                float rv1 = (yStart + y + rh) / (float) contents.height();
                Vector3f[] corners = new Vector3f[4];
                for (int i = 0; i < 4; i++) {
                    // keep the source winding: each vertex moves to the rectangle corner on its side
                    float tu = Math.abs(su[i] - maxU) < Math.abs(su[i] - minU) ? ru1 : ru0;
                    float tv = Math.abs(sv[i] - maxV) < Math.abs(sv[i] - minV) ? rv1 : rv0;
                    float s = (tu - minU) / (maxU - minU);
                    float t = (tv - minV) / (maxV - minV);
                    Vector3f top = new Vector3f(c00).lerp(c10, s);
                    Vector3f bottom = new Vector3f(c01).lerp(c11, s);
                    corners[i] = top.lerp(bottom, t);
                }
                rects.add(corners);
            }
        }
        return rects;
    }

    private static Vector3f nearest(Vector3f[] positions, float[] su, float[] sv, float u, float v) {
        int best = 0;
        float bestDistance = Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            float distance = Math.abs(su[i] - u) + Math.abs(sv[i] - v);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return positions[best];
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
