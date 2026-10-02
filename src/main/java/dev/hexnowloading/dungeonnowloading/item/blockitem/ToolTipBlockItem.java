package dev.hexnowloading.dungeonnowloading.item.blockitem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/** 26.x removed Block#appendHoverText; block tooltips now live on the block item. */
public class ToolTipBlockItem extends BlockItem {

    private final String toolTipKey;

    public ToolTipBlockItem(Block block, Properties properties, String toolTipKey) {
        super(block, properties);
        this.toolTipKey = toolTipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
        tooltip.accept(Component.translatable(this.toolTipKey).withStyle(ChatFormatting.GRAY));
    }
}
