package dev.hexnowloading.dungeonnowloading.world.features.entities;



import dev.hexnowloading.dungeonnowloading.world.features.DNLFeatureContext;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeature;
import com.mojang.serialization.Codec;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityScale;
import dev.hexnowloading.dungeonnowloading.world.features.configs.EntityTypeConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;

public class GenericMobFeature extends DNLFeature<EntityTypeConfig> {

    public GenericMobFeature(EntityTypeConfig config) { super(config); }

    @Override
    public boolean place(DNLFeatureContext<EntityTypeConfig> context) {

        Mob mob = (Mob) context.config().entityType.create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);
        mob.setPersistenceRequired();
        mob.snapTo((double)context.origin().getX() + 0.5D, context.origin().getY(), (double)context.origin().getZ() + 0.5D, 0.0F, 0.0F);
        mob.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(context.origin()), EntitySpawnReason.STRUCTURE, null);
        EntityScale.scaleMobAttributes(mob);

        context.level().addFreshEntity(mob);
        return true;
    }
}
