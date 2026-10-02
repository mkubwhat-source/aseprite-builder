package dev.hexnowloading.dungeonnowloading.world.features.entities;





import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeatureContext;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeature;
import com.mojang.serialization.Codec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ArmorStandWithRandomEquipmentsFeature extends DNLFeature<DNLFeature.None> {

    public ArmorStandWithRandomEquipmentsFeature() { super(DNLFeature.None.INSTANCE); }

    private final Item[] DIAMOND_EQUIPMENTS = new Item[]{Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE};
    private final Item[] IRON_AND_CHAINMAIL_EQUIPMENTS = new Item[]{Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_PICKAXE, Items.BOW, Items.CROSSBOW};

    @Override
    public boolean place(DNLFeatureContext<DNLFeature.None> context) {
        ArmorStand armorStand = EntityTypes.ARMOR_STAND.create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);
        armorStand.snapTo(context.origin().below(), 0.0F, 0.0F);

        float armorTypeChance = context.random().nextFloat();

        if (armorTypeChance < 0.03F) {
            this.equipDiamondEquipments(armorStand, context);
        } else if (armorTypeChance < 0.25F) {
            this.equipIronEquipments(armorStand, context);
        } else {
            this.equipChainmailEquipments(armorStand, context);
        }

        BlockState blockState = context.level().getBlockState(context.origin());

        if (blockState.is(Blocks.DISPENSER)) {
            switch (blockState.getValue(DispenserBlock.FACING)) {
                case NORTH -> armorStand.setYRot(180.0F);
                case EAST -> armorStand.setYRot(-90.0F);
                case WEST -> armorStand.setYRot(90.0F);
            }
            context.level().setBlock(context.origin(), Blocks.AIR.defaultBlockState(), 2);
        }
        context.level().addFreshEntity(armorStand);
        return true;
    }

    private void equipChainmailEquipments(ArmorStand armorStand, DNLFeatureContext<DNLFeature.None> context) {
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET)); }
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE)); }
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS)); }
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS)); }
        if (context.random().nextFloat() < 0.5F) {
            armorStand.setShowArms(true);
            armorStand.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IRON_AND_CHAINMAIL_EQUIPMENTS[context.random().nextInt(IRON_AND_CHAINMAIL_EQUIPMENTS.length)]));
        }
    }

    private void equipIronEquipments(ArmorStand armorStand, DNLFeatureContext<DNLFeature.None> context) {
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET)); }
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE)); }
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS)); }
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS)); }
        if (context.random().nextFloat() < 0.5F) {
            armorStand.setShowArms(true);
            armorStand.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(IRON_AND_CHAINMAIL_EQUIPMENTS[context.random().nextInt(IRON_AND_CHAINMAIL_EQUIPMENTS.length)]));
        }
    }

    private void equipDiamondEquipments(ArmorStand armorStand, DNLFeatureContext<DNLFeature.None> context) {
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET)); }
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE)); }
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS)); }
        if (context.random().nextFloat() < 0.5F) { armorStand.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS)); }
        if (context.random().nextFloat() < 0.5F) {
            armorStand.setShowArms(true);
            armorStand.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DIAMOND_EQUIPMENTS[context.random().nextInt(DIAMOND_EQUIPMENTS.length)]));
        }
    }
}
