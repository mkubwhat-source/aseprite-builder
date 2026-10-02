package dev.hexnowloading.dungeonnowloading.client.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * 1.21.1-style sprite particle: constructed without a sprite and assigned one with {@link #pickSprite}/{@link #setSprite}.
 * 26.x sprite particles must have a sprite from construction, so the missing sprite is used until then.
 */
public abstract class TextureSheetParticle extends SingleQuadParticle {

    protected TextureSheetParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, placeholderSprite());
    }

    protected TextureSheetParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd, placeholderSprite());
    }

    private static TextureAtlasSprite placeholderSprite() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.PARTICLES).missingSprite();
    }

    public void pickSprite(SpriteSet sprites) {
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public void setSprite(TextureAtlasSprite sprite) {
        super.setSprite(sprite);
    }
}
