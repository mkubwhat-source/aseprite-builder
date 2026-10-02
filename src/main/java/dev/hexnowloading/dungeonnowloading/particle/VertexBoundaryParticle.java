package dev.hexnowloading.dungeonnowloading.particle;



import net.minecraft.client.particle.SingleQuadParticle;
import dev.hexnowloading.dungeonnowloading.client.legacy.TextureSheetParticle;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.particle.type.AxisParticleType;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VertexBoundaryParticle extends TextureSheetParticle {

    private SpriteSet spriteSet;
    private int axis;
    private float degree;

    protected VertexBoundaryParticle(ClientLevel clientLevel, double x, double y, double z, int axis, float degree, SpriteSet spriteSet) {
        super(clientLevel, x, y, z);
        this.quadSize = 0.5F;
        this.gravity = 0.0F;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.spriteSet = spriteSet;
        this.lifetime = 70;
        this.axis = axis;
        this.degree = degree;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.xd = 0;
        this.yd = 0.02F;
        this.zd = 0;

        this.move(this.xd, this.yd, this.zd);

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }



    @Override
    public float getQuadSize(float f) {
        return this.quadSize;
    }

    @Override
    public void extract(net.minecraft.client.renderer.state.level.QuadParticleRenderState particleRenderState, Camera camera, float partialTick) {
        float fadeStart = this.lifetime * (2.0F / 3.0F); // Fade starts at 2/3 of lifetime

        if (this.age >= fadeStart) {
            float fadeProgress = (this.age + partialTick - fadeStart) / (this.lifetime - fadeStart);
            this.alpha = 1.0F - Mth.clamp(fadeProgress, 0.0F, 1.0F); // Gradually reduce alpha
        } else {
            this.alpha = 1.0F; // Fully visible before fade starts
        }

        this.rCol = 1f;
        this.gCol = 1f;
        this.renderRotatedParticle(particleRenderState, camera, partialTick, this.axis, this.degree);
        this.rCol = 0.8f;
        this.gCol = 0.8f;
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

    public static class Factory implements ParticleProvider<AxisParticleType.AxisParticleData> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        public Particle createParticle(AxisParticleType.AxisParticleData data, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, net.minecraft.util.RandomSource random) {
            VertexBoundaryParticle particle = new VertexBoundaryParticle(clientLevel, x, y, z, data.getAxis() , data.getDegree(), this.sprites);
            particle.setSprite(sprites.get(0, 1));
            particle.setAlpha(1.0F);
            return particle;
        }
    }
}

