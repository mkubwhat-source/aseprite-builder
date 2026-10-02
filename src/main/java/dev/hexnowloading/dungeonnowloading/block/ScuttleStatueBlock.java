package dev.hexnowloading.dungeonnowloading.block;




import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.server.level.ServerLevel;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import dev.hexnowloading.dungeonnowloading.block.entity.ScuttleStatueBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

public class ScuttleStatueBlock extends BaseEntityBlock implements EntityBlock {



    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public boolean playerDestroyed;
    private boolean hasBeenSummoned;

    public ScuttleStatueBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(HALF);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        Direction direction = blockPlaceContext.getHorizontalDirection().getOpposite();
        return blockPos.getY() < (level.getMaxY() + 1) - 1 && level.getBlockState(blockPos.above()).canBeReplaced(blockPlaceContext) ? this.defaultBlockState().setValue(FACING, direction).setValue(HALF, DoubleBlockHalf.LOWER) : null;
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos1 = blockPos.below();
        return this.mayPlaceOn(levelReader.getBlockState(blockPos1), levelReader, blockPos1);
    }

    private boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return !blockState.getCollisionShape(blockGetter, blockPos).getFaceShape(Direction.UP).isEmpty() || blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean b) {
        if (level.isClientSide()) {
            return;
        }
        if (!level.hasNeighborSignal(blockPos)) {
            return;
        }
        ScuttleStatueBlockEntity blockEntity = (ScuttleStatueBlockEntity) level.getBlockEntity(blockPos);
        if (blockEntity != null) {
            BlockPos summonPos = blockPos;
            if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER) {
                summonPos = blockPos.below();
            }
            blockEntity.alert(summonPos, blockEntity);
        }
        if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            level.destroyBlock(blockPos.below(), false);
        }
        if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            level.destroyBlock(blockPos.above(), true);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        BlockPos upperBlockPos = blockPos.above();
        Direction direction = blockState.getValue(FACING);
        level.setBlock(upperBlockPos, this.defaultBlockState().setValue(FACING, direction).setValue(HALF, DoubleBlockHalf.UPPER), 3);
        level.neighborChanged(upperBlockPos, this, null);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide()) {
            this.playerDestroyed = !player.getAbilities().instabuild;
            if (playerDestroyed) {
                ItemStack heldItem = player.getMainHandItem();
                this.playerDestroyed = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(level, Enchantments.SILK_TOUCH), heldItem) < 1;
            }
        }
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos blockPos, boolean isMoving) {
        if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            level.destroyBlock(blockPos.below(), false);
        }
        if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            level.destroyBlock(blockPos.above(), true);
        }
        super.affectNeighborsAfterRemoval(blockState, level, blockPos, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ScuttleStatueBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState $blockState0) {
        return RenderShape.MODEL;
    }
}
