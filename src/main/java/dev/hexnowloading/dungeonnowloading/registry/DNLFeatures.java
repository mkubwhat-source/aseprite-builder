package dev.hexnowloading.dungeonnowloading.registry;

import com.mojang.serialization.MapCodec;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.world.features.BrewingStandFeature;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeature;
import dev.hexnowloading.dungeonnowloading.world.features.LabyrinthSurfaceTemplateFeature;
import dev.hexnowloading.dungeonnowloading.world.features.SpawnerFeature;
import dev.hexnowloading.dungeonnowloading.world.features.configs.ArmorStandConfig;
import dev.hexnowloading.dungeonnowloading.world.features.configs.EntityTypeConfig;
import dev.hexnowloading.dungeonnowloading.world.features.configs.PotionConfig;
import dev.hexnowloading.dungeonnowloading.world.features.entities.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.function.Supplier;

/**
 * 26.x: features are data-driven; the mod registers feature <em>types</em> (codecs) and the
 * {@code data/dungeonnowloading/worldgen/feature} JSON files configure them.
 */
public class DNLFeatures {

    public static final Supplier<MapCodec<GenericMobFeature>> GENERIC_MOB = register("generic_mob", GenericMobFeature.class, DNLFeature.codec(EntityTypeConfig.CODEC, GenericMobFeature::new));
    public static final Supplier<MapCodec<GenericArmorStandFeature>> GENERIC_ARMOR_STAND = register("generic_armor_stand", GenericArmorStandFeature.class, DNLFeature.codec(ArmorStandConfig.CODEC, GenericArmorStandFeature::new));
    public static final Supplier<MapCodec<ArmorStandWithRandomEquipmentsFeature>> ARMOR_STAND_WITH_RANDOM_EQUIPMENT = register("armor_stand_with_random_equipments", ArmorStandWithRandomEquipmentsFeature.class, DNLFeature.unitCodec(ArmorStandWithRandomEquipmentsFeature::new));
    public static final Supplier<MapCodec<SkeletonCaveSpiderJokeyFeature>> SKELETON_CAVE_SPIDER_JOKEY = register("skeleton_cave_spider_jockey", SkeletonCaveSpiderJokeyFeature.class, DNLFeature.unitCodec(SkeletonCaveSpiderJokeyFeature::new));
    public static final Supplier<MapCodec<SkeletonSpiderJokeyFeature>> SKELETON_SPIDER_JOKEY = register("skeleton_spider_jockey", SkeletonSpiderJokeyFeature.class, DNLFeature.unitCodec(SkeletonSpiderJokeyFeature::new));
    public static final Supplier<MapCodec<SkeletonWithStrongFlameBowFeature>> SKELETON_WITH_STRONG_FLAME_BOW = register("skeleton_with_strong_flame_bow", SkeletonWithStrongFlameBowFeature.class, DNLFeature.unitCodec(SkeletonWithStrongFlameBowFeature::new));
    public static final Supplier<MapCodec<SkeletonWithWeakFlameBowFeature>> SKELETON_WITH_WEAK_FLAME_BOW = register("skeleton_with_weak_flame_bow", SkeletonWithWeakFlameBowFeature.class, DNLFeature.unitCodec(SkeletonWithWeakFlameBowFeature::new));
    public static final Supplier<MapCodec<SkeletonWithStrongPunchBowFeature>> SKELETON_WITH_STRONG_PUNCH_BOW = register("skeleton_with_strong_punch_bow", SkeletonWithStrongPunchBowFeature.class, DNLFeature.unitCodec(SkeletonWithStrongPunchBowFeature::new));
    public static final Supplier<MapCodec<SkeletonWithWeakPunchBowFeature>> SKELETON_WITH_WEAK_PUNCH_BOW = register("skeleton_with_weak_punch_bow", SkeletonWithWeakPunchBowFeature.class, DNLFeature.unitCodec(SkeletonWithWeakPunchBowFeature::new));
    public static final Supplier<MapCodec<ZombieWithIronAxeFeature>> ZOMBIE_WITH_IRON_AXE = register("zombie_with_iron_axe", ZombieWithIronAxeFeature.class, DNLFeature.unitCodec(ZombieWithIronAxeFeature::new));
    public static final Supplier<MapCodec<ZombieWithDiamondAxeFeature>> ZOMBIE_WITH_DIAMOND_AXE = register("zombie_with_diamond_axe", ZombieWithDiamondAxeFeature.class, DNLFeature.unitCodec(ZombieWithDiamondAxeFeature::new));
    public static final Supplier<MapCodec<ZombieWithGoldSwordFeature>> ZOMBIE_WITH_GOLD_SWORD = register("zombie_with_gold_sword", ZombieWithGoldSwordFeature.class, DNLFeature.unitCodec(ZombieWithGoldSwordFeature::new));
    public static final Supplier<MapCodec<ZombieWithGoldAxeFeature>> ZOMBIE_WITH_GOLD_AXE = register("zombie_with_gold_axe", ZombieWithGoldAxeFeature.class, DNLFeature.unitCodec(ZombieWithGoldAxeFeature::new));
    public static final Supplier<MapCodec<SkeletonHorseFeature>> SKELETON_HORSE = register("skeleton_horse", SkeletonHorseFeature.class, DNLFeature.unitCodec(SkeletonHorseFeature::new));
    public static final Supplier<MapCodec<ZombieHorseFeature>> ZOMBIE_HORSE = register("zombie_horse", ZombieHorseFeature.class, DNLFeature.unitCodec(ZombieHorseFeature::new));
    public static final Supplier<MapCodec<LabyrinthSurfaceTemplateFeature>> LABYRINTH_SURFACE = register("labyrinth_surface", LabyrinthSurfaceTemplateFeature.class, DNLFeature.unitCodec(LabyrinthSurfaceTemplateFeature::new));

    public static final Supplier<MapCodec<SpawnerCarrierFeature>> SPAWNER_CARRIER = register("spawner_carrier", SpawnerCarrierFeature.class, DNLFeature.codec(EntityTypeConfig.CODEC, SpawnerCarrierFeature::new));

    public static final Supplier<MapCodec<SpawnerFeature>> SPAWNER = register("spawner", SpawnerFeature.class, DNLFeature.codec(EntityTypeConfig.CODEC, SpawnerFeature::new));
    public static final Supplier<MapCodec<BrewingStandFeature>> BREWING_STAND = register("brewing_stand", BrewingStandFeature.class, DNLFeature.codec(PotionConfig.CODEC, BrewingStandFeature::new));

    public static <F extends DNLFeature<?>> Supplier<MapCodec<F>> register(String name, Class<F> type, MapCodec<F> codec) {
        DNLFeature.bindCodec(type, codec);
        return Services.REGISTRY.register(BuiltInRegistries.FEATURE_TYPE, name, () -> codec);
    }

    public static void init() {}
}
