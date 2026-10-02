package dev.hexnowloading.dungeonnowloading.world.features.entities;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SkeletonHorseFeature extends Feature<NoneFeatureConfiguration> {

    public SkeletonHorseFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        SkeletonHorse skeletonHorse = EntityType.SKELETON_HORSE.create(context.level().getLevel());
        skeletonHorse.setPersistenceRequired();
        skeletonHorse.snapTo((double)context.origin().getX() + 0.5D, context.origin().getY(), (double)context.origin().getZ() + 0.5D, 0.0F, 0.0F);
        skeletonHorse.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(context.origin()), EntitySpawnReason.STRUCTURE, null);
        skeletonHorse.setTamed(true);
        skeletonHorse.equipSaddle(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SADDLE), SoundSource.NEUTRAL);

        context.level().addFreshEntity(skeletonHorse);
        return true;
    }
}
