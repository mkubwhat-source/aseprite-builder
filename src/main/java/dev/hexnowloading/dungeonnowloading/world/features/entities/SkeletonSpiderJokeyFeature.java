package dev.hexnowloading.dungeonnowloading.world.features.entities;




import net.minecraft.world.entity.EntityTypes;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeatureContext;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeature;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityScale;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;

public class SkeletonSpiderJokeyFeature extends DNLFeature<DNLFeature.None> {

    public SkeletonSpiderJokeyFeature() { super(DNLFeature.None.INSTANCE); }

    @Override
    public boolean place(DNLFeatureContext<DNLFeature.None> context) {

        Spider spider = EntityTypes.SPIDER.create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);
        spider.setPersistenceRequired();
        spider.snapTo((double)context.origin().getX() + 0.5D, context.origin().getY(), (double)context.origin().getZ() + 0.5D, 0.0F, 0.0F);
        spider.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(context.origin()), EntitySpawnReason.STRUCTURE, null);
        EntityScale.scaleMobAttributes(spider);

        Skeleton skeleton = EntityTypes.SKELETON.create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);
        skeleton.snapTo((double)context.origin().getX() + 0.5D, context.origin().getY() + 1, (double)context.origin().getZ() + 0.5D, 0.0F, 0.0F);
        skeleton.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(context.origin()), EntitySpawnReason.STRUCTURE, null);
        skeleton.setPersistenceRequired();
        EntityScale.scaleMobAttributes(skeleton);

        skeleton.startRiding(spider);

        context.level().addFreshEntityWithPassengers(spider);
        return true;
    }
}
