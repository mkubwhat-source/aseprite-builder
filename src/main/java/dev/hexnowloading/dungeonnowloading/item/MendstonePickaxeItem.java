package dev.hexnowloading.dungeonnowloading.item;





import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ToolMaterial;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MendstonePickaxeItem extends Item {

    public MendstonePickaxeItem(Properties properties) {
        // Not repairable and not enchantable, matching the 1.21.1 overrides.
        super(properties.pickaxe(ToolMaterial.IRON, 1.0F, -2.8F)
                .component(DataComponents.REPAIRABLE, null)
                .component(DataComponents.ENCHANTABLE, null));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext level, TooltipDisplay tooltipDisplay, Consumer<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, tooltipDisplay, components, tooltipFlag);
        components.accept(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
        components.accept(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.ability_description1").withStyle(ChatFormatting.DARK_GRAY));
        components.accept(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.disclaimer").withStyle(ChatFormatting.DARK_GRAY));
    }

    // 1.21 removed Item.getRarity(ItemStack); rarity is set via Properties.rarity(...) at registration.


    // Prevent enchanting entirely (enchanting table + "enchantability" weight)

}