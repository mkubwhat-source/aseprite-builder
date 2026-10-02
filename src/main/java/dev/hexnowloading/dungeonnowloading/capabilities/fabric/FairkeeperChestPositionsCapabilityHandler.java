package dev.hexnowloading.dungeonnowloading.capabilities.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

import java.util.ArrayList;
import java.util.List;

// 1.21 / CCA 6.1.3: PlayerComponent -> RespawnableComponent; NBT methods take HolderLookup.Provider.
// Also fixed: the old readFromNbt read hardcoded indices getInt(0/1/2) (re-read entry 0) while
// writeToNbt stored a list-of-int-lists; the serialization now round-trips correctly. Removed the
// broken equals(){return false;} (it defeated AutoSyncedComponent change detection) and a stray
// mixin Profiler.setActive debug leftover.
public class FairkeeperChestPositionsCapabilityHandler implements IFairkeeperChestPositionsCapability, RespawnableComponent<FairkeeperChestPositionsCapabilityHandler> {

    private List<BlockPos> fairkeeperPosList;

    public FairkeeperChestPositionsCapabilityHandler() {
        this(new ArrayList<>());
    }

    public FairkeeperChestPositionsCapabilityHandler(List<BlockPos> blockPosList) {
        this.fairkeeperPosList = blockPosList;
    }

    @Override
    public List<BlockPos> getList() {
        if (this.fairkeeperPosList == null) {
            this.fairkeeperPosList = new ArrayList<>();
        }
        return this.fairkeeperPosList;
    }

    @Override
    public void addBlock(BlockPos blockPos) {
        if (this.fairkeeperPosList == null) {
            this.fairkeeperPosList = new ArrayList<>();
        }
        if (!this.fairkeeperPosList.contains(blockPos)) {
            this.fairkeeperPosList.add(blockPos);
        }
    }

    @Override
    public void copyList(List<BlockPos> list) {
        this.fairkeeperPosList = list;
    }

    @Override
    public void readData(ValueInput input) {
        this.fairkeeperPosList = new ArrayList<>();
        input.listOrEmpty("FairkeeperChestPositions", BlockPos.CODEC).stream().forEach(this.fairkeeperPosList::add);
    }

    @Override
    public void writeData(ValueOutput output) {
        ValueOutput.TypedOutputList<BlockPos> list = output.list("FairkeeperChestPositions", BlockPos.CODEC);
        getList().forEach(list::add);
    }

    @Override
    public void copyFrom(FairkeeperChestPositionsCapabilityHandler original, HolderLookup.Provider registries) {
        this.fairkeeperPosList = new ArrayList<>(original.fairkeeperPosList);
    }

    @Override
    public boolean shouldCopyForRespawn(boolean lossless, boolean keepInventory, boolean sameCharacter) {
        return lossless || keepInventory;
    }
}
