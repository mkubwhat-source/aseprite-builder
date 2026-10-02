package dev.hexnowloading.dungeonnowloading.world.features.entities;




import net.minecraft.world.entity.EntityTypes;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeatureContext;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeature;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityScale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class SkeletonWithWeakFlameBowFeature extends DNLFeature<DNLFeature.None> {

    public SkeletonWithWeakFlameBowFeature() { super(DNLFeature.None.INSTANCE); }

    @Override
    public boolean place(DNLFeatureContext<DNLFeature.None> context) {

        Skeleton skeleton = EntityTypes.SKELETON.create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);
        skeleton.setPersistenceRequired();
        skeleton.snapTo((double)context.origin().getX() + 0.5D, context.origin().getY(), (double)context.origin().getZ() + 0.5D, 0.0F, 0.0F);
        skeleton.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(context.origin()), EntitySpawnReason.STRUCTURE, null);
        EntityScale.scaleMobAttributes(skeleton);
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, weakFlameBow(context.level().registryAccess()));
        skeleton.setItemSlot(EquipmentSlot.HEAD, trimArmor(context.level().registryAccess(), Items.IRON_HELMET));
        skeleton.setItemSlot(EquipmentSlot.CHEST, trimArmor(context.level().registryAccess(), Items.IRON_CHESTPLATE));
        skeleton.setItemSlot(EquipmentSlot.LEGS, trimArmor(context.level().registryAccess(), Items.IRON_LEGGINGS));
        skeleton.setItemSlot(EquipmentSlot.FEET, trimArmor(context.level().registryAccess(), Items.IRON_BOOTS));
        skeleton.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        skeleton.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        skeleton.setDropChance(EquipmentSlot.HEAD, 0.0F);
        skeleton.setDropChance(EquipmentSlot.CHEST, 0.0F);
        skeleton.setDropChance(EquipmentSlot.LEGS, 0.0F);
        skeleton.setDropChance(EquipmentSlot.FEET, 0.0F);
        skeleton.setLeftHanded(context.level().getRandom().nextFloat() < 0.05F);
        skeleton.lootTable = java.util.Optional.of(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "entities/modified/iron_skeleton")));

        context.level().addFreshEntity(skeleton);

        return true;
    }

    private static ItemStack weakFlameBow(net.minecraft.core.HolderLookup.Provider registries) {
        ItemStack itemStack = new ItemStack(Items.BOW);
        itemStack.enchant(DNLEnchantments.holder(registries, Enchantments.POWER), 3);
        itemStack.enchant(DNLEnchantments.holder(registries, Enchantments.FLAME), 1);
        return itemStack;
    }

    private static ItemStack trimArmor(net.minecraft.core.HolderLookup.Provider registries, Item item) {
        ItemStack itemStack = new ItemStack(item);
        dev.hexnowloading.dungeonnowloading.util.ArmorTrimUtil.applyTrim(registries, itemStack, "gold", "wild");
        return itemStack;
    }
}
