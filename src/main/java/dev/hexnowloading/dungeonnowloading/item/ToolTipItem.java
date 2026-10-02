package dev.hexnowloading.dungeonnowloading.item;



import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToolTipItem extends Item {

    private final String toolTipKey;

    public ToolTipItem(Properties properties, String string) {
        super(properties);
        this.toolTipKey = string;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext world, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(this.toolTipKey).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, world, tooltipDisplay, tooltip, flag);
    }
}
