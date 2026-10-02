package dev.hexnowloading.dungeonnowloading.block.entity;




import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import dev.hexnowloading.dungeonnowloading.util.NbtCompat;
import com.mojang.logging.LogUtils;
import dev.hexnowloading.dungeonnowloading.block.ZoneReceiverBlockEntity;
import dev.hexnowloading.dungeonnowloading.game_event_listener.PreserverBlockDestructionSystem;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class PreserverBlockEntity extends BlockEntity implements GameEventListener.Provider<PreserverBlockDestructionSystem.Listener>, PreserverBlockDestructionSystem, ZoneReceiverBlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    private PreserverBlockDestructionSystem.User user;
    private PreserverBlockDestructionSystem.Listener gameEventListener;

    private final Set<BlockPos> playerPlacedBlocks = new HashSet<>();

    public PreserverBlockEntity(BlockPos pos, BlockState state) {
        this(DNLBlockEntityTypes.PRESERVER_BLOCK.get(), pos, state);
    }

    public PreserverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.user = new PreserverBlockDestructionSystem.User(this.getBlockPos(), new BlockPos(10, 10, 10), new BlockPos(-10, -10, -10), Direction.NORTH);
        this.gameEventListener = new PreserverBlockDestructionSystem.Listener(this);
    }

    @Override
    protected void saveAdditional(ValueOutput compoundTag) {
        super.saveAdditional(compoundTag);

        PreserverBlockDestructionSystem.User.CODEC.encodeStart(NbtOps.INSTANCE, this.user)
                .resultOrPartial(LOGGER::error)
                .ifPresent(tag -> NbtCompat.put(compoundTag, "listener", tag));
        NbtCompat.put(compoundTag, "PlayerPlacedBlocks", saveBlockPosSet(playerPlacedBlocks));
    }

    @Override
    protected void loadAdditional(ValueInput compoundTag) {
        if (NbtCompat.has(compoundTag, "listener")) { // 10 means it's a CompoundTag
            PreserverBlockDestructionSystem.User.CODEC.parse(NbtOps.INSTANCE, NbtCompat.getCompound(compoundTag, "listener"))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(listener -> {
                        this.user = new User(this.getBlockPos(), listener.getCornerA(), listener.getCornerB(), listener.getFacing());
                    });
        }
        if (NbtCompat.has(compoundTag, "PlayerPlacedBlocks")) {
            playerPlacedBlocks.clear();
            playerPlacedBlocks.addAll(loadBlockPosSet(NbtCompat.getList(compoundTag, "PlayerPlacedBlocks")));
        }
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public Listener getListener() {
        return this.gameEventListener;
    }

    public void addPlayerPlacedBlock(BlockPos pos) {
        playerPlacedBlocks.add(pos);
    }

    // Remove when broken
    public void removePlayerPlacedBlock(BlockPos pos) {
        playerPlacedBlocks.remove(pos);
    }

    // Check if the block is player-placed
    public boolean isPlayerPlaced(BlockPos pos) {
        return playerPlacedBlocks.contains(pos);
    }

    public static void spawnPopBurst(ServerLevel level, BlockPos pos) {
        spawnPopBurst(level, Vec3.atCenterOf(pos));
    }

    public static void spawnPopBurst(ServerLevel level, Vec3 center) {
        final int count = 8;
        final int fadeIn = 0;
        final int fadeOut = 8;
        final int lifetime = 12;
        final float speed = 0.22f;

        for (int i = 0; i < count; i++) {
            double rx = level.getRandom().nextDouble() * 2.0 - 1.0;
            double ry = level.getRandom().nextDouble() * 2.0 - 1.0;
            double rz = level.getRandom().nextDouble() * 2.0 - 1.0;
            Vec3 direction = new Vec3(rx, ry, rz);
            if (direction.lengthSqr() < 1.0e-6) {
                direction = new Vec3(0.0D, 1.0D, 0.0D);
            }
            direction = direction.normalize().scale(speed);

            var data = new dev.hexnowloading.dungeonnowloading.particle.type.MendingFadeParticleType.Data(
                    DNLParticleTypes.MENDING_FADE_PARTICLE.get(),
                    (float) direction.x, (float) direction.y, (float) direction.z,
                    fadeIn, fadeOut, lifetime
            );

            double px = center.x + (level.getRandom().nextDouble() - 0.5D) * 0.25D;
            double py = center.y + (level.getRandom().nextDouble() - 0.5D) * 0.25D;
            double pz = center.z + (level.getRandom().nextDouble() - 0.5D) * 0.25D;

            level.sendParticles(data, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static ListTag saveBlockPosSet(Set<BlockPos> positions) {
        ListTag listTag = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            listTag.add(tag);
        }
        return listTag;
    }

    private static Set<BlockPos> loadBlockPosSet(ListTag listTag) {
        Set<BlockPos> positions = new HashSet<>();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompoundOrEmpty(i);
            positions.add(new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0)));
        }
        return positions;
    }

    @Override
    public void setRegion(BlockPos cornerAWorld, BlockPos cornerBWorld, Direction authoredFacing) {
        // store OFFSETS relative to this block
        BlockPos aOff = cornerAWorld.subtract(this.getBlockPos());
        BlockPos bOff = cornerBWorld.subtract(this.getBlockPos());

        Direction facing = authoredFacing == null ? Direction.NORTH : authoredFacing;

        // Update the User (this is what your codec saves/loads already)
        this.user = new PreserverBlockDestructionSystem.User(this.getBlockPos(), aOff, bOff, facing);

        // Optional: if your listener caches anything (usually it shouldn’t), rebuild it
        // this.gameEventListener = new PreserverBlockDestructionSystem.Listener(this);

        setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            BlockState st = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, st, st, 3);
        }
    }
}
