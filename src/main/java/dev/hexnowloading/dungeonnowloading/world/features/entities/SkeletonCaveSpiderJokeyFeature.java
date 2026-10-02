package dev.hexnowloading.dungeonnowloading.world.features.entities;




import net.minecraft.world.entity.EntityTypes;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeatureContext;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeature;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityScale;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.skeleton.Skeleton;

public class SkeletonCaveSpiderJokeyFeature extends DNLFeature<DNLFeature.None> {

    public SkeletonCaveSpiderJokeyFeature() { super(DNLFeature.None.INSTANCE); }

    @Override
    public boolean place(DNLFeatureContext<DNLFeature.None> context) {
        CaveSpider caveSpider = EntityTypes.CAVE_SPIDER.create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);
        caveSpider.setPersistenceRequired();
        caveSpider.snapTo((double)context.origin().getX() + 0.5D, context.origin().getY(), (double)context.origin().getZ() + 0.5D, 0.0F, 0.0F);
        caveSpider.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(context.origin()), EntitySpawnReason.STRUCTURE, null);
        EntityScale.scaleMobAttributes(caveSpider);

        Skeleton skeleton = EntityTypes.SKELETON.create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);
        skeleton.setPersistenceRequired();
        skeleton.snapTo((double)context.origin().getX() + 0.5D, context.origin().getY() + 1, (double)context.origin().getZ() + 0.5D, 0.0F, 0.0F);
        skeleton.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(context.origin()), EntitySpawnReason.STRUCTURE, null);
        EntityScale.scaleMobAttributes(skeleton);

        skeleton.startRiding(caveSpider);

        context.level().addFreshEntityWithPassengers(caveSpider);
        return true;
    }
}
