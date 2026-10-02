package dev.hexnowloading.dungeonnowloading.world.features.entities;




import net.minecraft.world.entity.EntityTypes;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeatureContext;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeature;
import com.mojang.serialization.Codec;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;

public class ZombieHorseFeature extends DNLFeature<DNLFeature.None> {

    public ZombieHorseFeature() { super(DNLFeature.None.INSTANCE); }

    @Override
    public boolean place(DNLFeatureContext<DNLFeature.None> context) {
        ZombieHorse zombieHorse = EntityTypes.ZOMBIE_HORSE.create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);
        zombieHorse.setPersistenceRequired();
        zombieHorse.snapTo((double)context.origin().getX() + 0.5D, context.origin().getY(), (double)context.origin().getZ() + 0.5D, 0.0F, 0.0F);
        zombieHorse.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(context.origin()), EntitySpawnReason.STRUCTURE, null);
        zombieHorse.setTamed(true);
        zombieHorse.setItemSlot(net.minecraft.world.entity.EquipmentSlot.SADDLE, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SADDLE));

        context.level().addFreshEntity(zombieHorse);
        return true;
    }
}
