package dev.hexnowloading.dungeonnowloading.mixin.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevel.class)
public interface LevelRendererAccessor {
    @Accessor("destroyingBlocks")
    Int2ObjectMap<BlockDestructionProgress> dungeonnowloading$getDestroyingBlocks();
}
