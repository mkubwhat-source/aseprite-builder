package dev.hexnowloading.dungeonnowloading.particle;



import net.minecraft.client.particle.SingleQuadParticle;
import dev.hexnowloading.dungeonnowloading.client.legacy.TextureSheetParticle;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableAxisParticleType;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RedstoneHazardIndicatorParticle extends TextureSheetParticle {

    private SpriteSet spriteSet;
    private int axis;
    private float degree;
    private float scale;

    protected RedstoneHazardIndicatorParticle(ClientLevel clientLevel, double x, double y, double z, int axis, float degree, float scale, SpriteSet spriteSet) {
        super(clientLevel, x, y, z);
        this.quadSize = 0.5F;
        this.gravity = 0.0F;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.spriteSet = spriteSet;
        this.lifetime = 50;
        this.axis = axis;
        this.degree = degree;
        this.scale = scale;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        int sprite = Mth.clamp(4 * this.age / this.lifetime, 0, 4);
        this.setSprite(spriteSet.get(0, 4));
        if (sprite > 0) {
            this.xd = 0;
            this.yd = 0;
            this.zd = 0;
        }
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
        }
    }

    @Override
    public float getQuadSize(float f) {
        float progress = ((float) this.age + f) / (float) this.lifetime;

        // Use a very small exponent to make the growth extremely sharp at the start
        float exponent = 0.2f; // Smaller values = sharper expansion at the start
        float growthFactor = (float) Math.pow(progress, exponent);

        return this.quadSize * this.scale * Mth.clamp(growthFactor, 0.0f, 1.0f);
    }



    @Override
    public void extract(net.minecraft.client.renderer.state.level.QuadParticleRenderState particleRenderState, Camera camera, float partialTick) {
        float progress = ((float) this.age + partialTick) / (float) this.lifetime;
        float fadeStart = 0.8f;

        if (progress < fadeStart) {
            this.alpha = 1.0F;
        } else {
            float fadeProgress = (progress - fadeStart) / (1.0f - fadeStart);
            this.alpha = 1.0F - Mth.clamp(fadeProgress, 0.0f, 1.0f);
        }

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.renderRotatedParticle(particleRenderState, camera, partialTick, this.axis, this.degree);

        this.rCol = 0.1f;
        this.gCol = 0.1f;
        this.renderRotatedParticle(particleRenderState, camera, partialTick, this.axis, this.degree + 180F);
    }

    private void renderRotatedParticle(net.minecraft.client.renderer.state.level.QuadParticleRenderState particleRenderState, Camera camera, float partialTick, int axis, float degree) {
        Quaternionf quaternion =
                switch (axis) {
                    default -> Axis.XP.rotationDegrees(degree);
                    case 1 -> Axis.YP.rotationDegrees(degree);
                    case 2 -> Axis.ZP.rotationDegrees(degree);
                };
        this.extractRotatedQuad(particleRenderState, camera, quaternion, partialTick);
    }

    @Override
    public int getLightCoords(float f) {
        return 240;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<ScalableAxisParticleType.ScalableAxisParticleData> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        public Particle createParticle(ScalableAxisParticleType.ScalableAxisParticleData data, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, net.minecraft.util.RandomSource random) {
            RedstoneHazardIndicatorParticle particle = new RedstoneHazardIndicatorParticle(clientLevel, x, y, z, data.getAxis() , data.getDegree(), data.getScale(), this.sprites);
            particle.setSprite(sprites.get(0, 1));
            particle.setAlpha(1.0F);
            return particle;
        }
    }
}
