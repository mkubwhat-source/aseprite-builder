package dev.hexnowloading.dungeonnowloading.world.features.entities;





import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeatureContext;
import dev.hexnowloading.dungeonnowloading.world.features.DNLFeature;
import com.mojang.serialization.Codec;
import dev.hexnowloading.dungeonnowloading.world.features.configs.ArmorStandConfig;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public class GenericArmorStandFeature extends DNLFeature<ArmorStandConfig> {

    public GenericArmorStandFeature(ArmorStandConfig config) { super(config); }

    @Override
    public boolean place(DNLFeatureContext<ArmorStandConfig> context) {
        ArmorStand armorStand = EntityTypes.ARMOR_STAND.create(context.level().getLevel(), EntitySpawnReason.MOB_SUMMONED);
        armorStand.snapTo(context.origin().below(), 0.0F, 0.0F);
        context.config().heldItem.ifPresent(item -> armorStand.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item)));
        context.config().helmet.ifPresent(item -> armorStand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(item)));
        context.config().chestplate.ifPresent(item -> armorStand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(item)));
        context.config().leggings.ifPresent(item -> armorStand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(item)));
        context.config().boots.ifPresent(item -> armorStand.setItemSlot(EquipmentSlot.FEET, new ItemStack(item)));

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
}
