package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.util.StackNbt;
import dev.hexnowloading.dungeonnowloading.block.ZoneReceiverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ZoneWandItem extends Item {

    private static final String CORNER_A = "CornerA";
    private static final String CORNER_B = "CornerB";

    public ZoneWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        var player = context.getPlayer();
        var stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        CompoundTag tag = StackNbt.getOrCreateTag(stack);

        // Sneak-right-click: clear wand corners
        if (player.isCrouching()) {
            tag.remove(CORNER_A);
            tag.remove(CORNER_B);
            tag.remove("NextCornerIsB");
            StackNbt.setTag(stack, tag);
            player.sendOverlayMessage(Component.literal("Corners cleared from wand."));
            return InteractionResult.CONSUME;
        }

        // If clicked block is a zone receiver: apply corners to its BE (do NOT overwrite wand)
        BlockEntity be = level.getBlockEntity(clickedPos);
        if (be instanceof ZoneReceiverBlockEntity receiver) {
            if (tag.contains(CORNER_A) && tag.contains(CORNER_B)) {
                BlockPos a = readPos(tag.getCompoundOrEmpty(CORNER_A));
                BlockPos b = readPos(tag.getCompoundOrEmpty(CORNER_B));

                // Choose an authored facing.
                // If your blocks use BlockStateProperties.FACING, grab it; otherwise default.
                Direction authoredFacing = Direction.NORTH;
                var state = level.getBlockState(clickedPos);
                if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
                    authoredFacing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
                }

                receiver.setRegion(a, b, authoredFacing);

                // if you want clients to see any BE-rendered change immediately:
                be.setChanged();
                level.sendBlockUpdated(clickedPos, state, state, 3);

                player.sendOverlayMessage(Component.literal("Zone applied to block at " + clickedPos.toShortString() + " (wand corners kept)."));
            } else {
                player.sendOverlayMessage(Component.literal("Wand needs Corner A and Corner B first."));
            }
            return InteractionResult.CONSUME;
        }

        // Otherwise: we are setting/updating the wand corners (same as your old behavior)
        boolean nextCornerIsB = tag.getBooleanOr("NextCornerIsB", false);

        if (!tag.contains(CORNER_A)) {
            tag.put(CORNER_A, writePos(clickedPos));
            tag.putBoolean("NextCornerIsB", true);
            player.sendOverlayMessage(Component.literal("Corner A set at " + clickedPos.toShortString()));

        } else if (!tag.contains(CORNER_B)) {
            tag.put(CORNER_B, writePos(clickedPos));
            tag.putBoolean("NextCornerIsB", false);
            player.sendOverlayMessage(Component.literal("Corner B set at " + clickedPos.toShortString()));

        } else {
            if (nextCornerIsB) {
                tag.put(CORNER_B, writePos(clickedPos));
                tag.putBoolean("NextCornerIsB", false);
                player.sendOverlayMessage(Component.literal("Corner B updated at " + clickedPos.toShortString()));
            } else {
                tag.put(CORNER_A, writePos(clickedPos));
                tag.putBoolean("NextCornerIsB", true);
                player.sendOverlayMessage(Component.literal("Corner A updated at " + clickedPos.toShortString()));
            }
        }

        StackNbt.setTag(stack, tag);
        return InteractionResult.CONSUME;
    }

    private static CompoundTag writePos(BlockPos pos) {
        CompoundTag t = new CompoundTag();
        t.putInt("X", pos.getX());
        t.putInt("Y", pos.getY());
        t.putInt("Z", pos.getZ());
        return t;
    }

    private static BlockPos readPos(CompoundTag tag) {
        return new BlockPos(tag.getIntOr("X", 0), tag.getIntOr("Y", 0), tag.getIntOr("Z", 0));
    }
}
