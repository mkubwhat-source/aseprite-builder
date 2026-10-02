package dev.hexnowloading.dungeonnowloading.mixin.fabric.structures;

import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(StructureTemplatePool.class)
public class StructurePoolMixin {

    /**
     * Increases the weight limit that mojang slapped on that was a workaround for "https://bugs.mojang.com/browse/MC-203131"
     * @author - TelepathicGrunt
     * @return - The higher weight that is a more reasonable limit.
     */
    @ModifyConstant(
            // 26.x is unobfuscated: the 150 weight-limit lives in the DIRECT_CODEC builder lambda.
            method = "lambda$static$0",
            constant = @Constant(intValue = 150),
            require = 0
    )
    private static int dungeonnowloading_increaseWeightLimit(int constant) {
        return 5000;
    }
}
