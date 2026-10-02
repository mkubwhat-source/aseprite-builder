package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

/**
 * 26.x armor materials are plain records (no registry). The worn texture is looked up through the
 * equipment asset {@code assets/dungeonnowloading/equipment/spawner.json}, and repairs use the
 * {@code dungeonnowloading:repairs_spawner_armor} item tag.
 */
public final class DNLArmorMaterial {
    private DNLArmorMaterial() {}

    /** Durability multiplier the old enum carried (BASE_DURABILITY * 26). */
    public static final int SPAWNER_DURABILITY_MULTIPLIER = 26;

    public static final TagKey<Item> REPAIRS_SPAWNER_ARMOR =
            TagKey.create(Registries.ITEM, DungeonNowLoading.id("repairs_spawner_armor"));

    public static final ResourceKey<EquipmentAsset> SPAWNER_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, DungeonNowLoading.id("spawner"));

    public static final ArmorMaterial SPAWNER = new ArmorMaterial(
            SPAWNER_DURABILITY_MULTIPLIER,
            defense(3, 6, 8, 3, 8),
            10,                                   // enchantmentValue
            SoundEvents.ARMOR_EQUIP_IRON,
            2.0F,                                 // toughness
            0.0F,                                 // knockbackResistance
            REPAIRS_SPAWNER_ARMOR,
            SPAWNER_ASSET
    );

    private static Map<ArmorType, Integer> defense(int boots, int leggings, int chestplate, int helmet, int body) {
        EnumMap<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, boots);
        map.put(ArmorType.LEGGINGS, leggings);
        map.put(ArmorType.CHESTPLATE, chestplate);
        map.put(ArmorType.HELMET, helmet);
        map.put(ArmorType.BODY, body);
        return map;
    }

    /** Kept for call-site compatibility; the material no longer needs registering. */
    public static void init() {}
}
