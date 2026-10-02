package dev.hexnowloading.dungeonnowloading.world.features.entities;




import net.minecraft.world.entity.EntityTypes;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeatureContext;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeature;
import com.mojang.serialization.Codec;
import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityScale;
import dev.hexnowloading.dungeonnowloading.world.features.configs.EntityTypeConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;

public class SpawnerCarrierFeature extends DNLFeature<EntityTypeConfig> {

    public SpawnerCarrierFeature(EntityTypeConfig config) { super(config); }

    @Override
    public boolean place(DNLFeatureContext<EntityTypeConfig> context) {

        SpawnerCarrierEntity spawnerCarrier = DNLEntityTypes.SPAWNER_CARRIER.get().create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);

        spawnerCarrier.setPersistenceRequired();
        spawnerCarrier.snapTo((double)context.origin().getX() + 0.5D, context.origin().getY(), (double)context.origin().getZ() + 0.5D, 0.0F, 0.0F);
        spawnerCarrier.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(context.origin()), EntitySpawnReason.STRUCTURE, null);
        EntityScale.scaleMobAttributes(spawnerCarrier);
        EntityType<?> entityType = context.config().entityType;
        if (EntityTypes.ZOMBIE.equals(entityType)) {
            //spawnerCarrier.setSummonMobType("Zombie");
        } else if (EntityTypes.SKELETON.equals(entityType)) {
            //spawnerCarrier.setSummonMobType("Skeleton");
        } else if (EntityTypes.SPIDER.equals(entityType)) {
            //spawnerCarrier.setSummonMobType("Spider");
        }

        context.level().addFreshEntity(spawnerCarrier);
        return true;
    }
}
