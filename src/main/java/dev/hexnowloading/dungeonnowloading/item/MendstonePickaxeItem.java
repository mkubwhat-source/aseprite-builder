package dev.hexnowloading.dungeonnowloading.item;



import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MendstonePickaxeItem extends PickaxeItem {

    public MendstonePickaxeItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(net.minecraft.world.item.DiggerItem.createAttributes(Tiers.IRON, 1, -2.8F)));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext level, TooltipDisplay tooltipDisplay, Consumer<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, tooltipDisplay, components, tooltipFlag);
        components.accept(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
        components.accept(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.ability_description1").withStyle(ChatFormatting.DARK_GRAY));
        components.accept(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.disclaimer").withStyle(ChatFormatting.DARK_GRAY));
    }

    // 1.21 removed Item.getRarity(ItemStack); rarity is set via Properties.rarity(...) at registration.

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairItem) {
        return false;
    }

    // Prevent enchanting entirely (enchanting table + "enchantability" weight)
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }
}