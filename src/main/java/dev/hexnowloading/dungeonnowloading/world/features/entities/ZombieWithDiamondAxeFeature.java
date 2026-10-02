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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class ZombieWithDiamondAxeFeature extends DNLFeature<DNLFeature.None> {

    public ZombieWithDiamondAxeFeature() { super(DNLFeature.None.INSTANCE); }

    @Override
    public boolean place(DNLFeatureContext<DNLFeature.None> context) {

        Zombie zombie = EntityTypes.ZOMBIE.create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);
        zombie.setPersistenceRequired();
        zombie.snapTo((double)context.origin().getX() + 0.5D, context.origin().getY(), (double)context.origin().getZ() + 0.5D, 0.0F, 0.0F);
        zombie.finalizeSpawn(context.level(), context.level().getCurrentDifficultyAt(context.origin()), EntitySpawnReason.STRUCTURE, null);
        EntityScale.scaleMobAttributes(zombie);
        zombie.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3F);
        zombie.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
        zombie.setItemSlot(EquipmentSlot.MAINHAND, diamondAxe(context.level().registryAccess()));
        zombie.setItemSlot(EquipmentSlot.HEAD, trimArmor(context.level().registryAccess(), Items.DIAMOND_HELMET));
        zombie.setItemSlot(EquipmentSlot.CHEST, trimArmor(context.level().registryAccess(), Items.DIAMOND_CHESTPLATE));
        zombie.setItemSlot(EquipmentSlot.LEGS, trimArmor(context.level().registryAccess(), Items.DIAMOND_LEGGINGS));
        zombie.setItemSlot(EquipmentSlot.FEET, trimArmor(context.level().registryAccess(), Items.DIAMOND_BOOTS));
        zombie.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        zombie.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        zombie.setDropChance(EquipmentSlot.HEAD, 0.0F);
        zombie.setDropChance(EquipmentSlot.CHEST, 0.0F);
        zombie.setDropChance(EquipmentSlot.LEGS, 0.0F);
        zombie.setDropChance(EquipmentSlot.FEET, 0.0F);
        zombie.lootTable = java.util.Optional.of(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "entities/modified/diamond_zombie")));

        zombie.setLeftHanded(context.level().getRandom().nextFloat() < 0.05F);
        context.level().addFreshEntityWithPassengers(zombie);
        return true;
    }

    private static ItemStack diamondAxe(net.minecraft.core.HolderLookup.Provider registries) {
        ItemStack itemStack = new ItemStack(Items.DIAMOND_AXE);
        itemStack.enchant(DNLEnchantments.holder(registries, Enchantments.SHARPNESS), 5);
        itemStack.enchant(DNLEnchantments.holder(registries, Enchantments.KNOCKBACK), 5);
        return itemStack;
    }

    private void lootTable(LivingEntity entity) {
        if (entity instanceof net.minecraft.world.entity.Mob lootMob) {
            lootMob.lootTable = java.util.Optional.of(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, net.minecraft.resources.Identifier.parse("dungeonnowloading:entities/modified/diamond_zombie")));
        }
    }

    private static ItemStack trimArmor(net.minecraft.core.HolderLookup.Provider registries, Item item) {
        ItemStack itemStack = new ItemStack(item);
        dev.hexnowloading.dungeonnowloading.util.ArmorTrimUtil.applyTrim(registries, itemStack, "netherite", "wild");
        return itemStack;
    }
}
