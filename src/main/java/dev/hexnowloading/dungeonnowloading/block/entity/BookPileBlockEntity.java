package dev.hexnowloading.dungeonnowloading.block.entity;




import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import dev.hexnowloading.dungeonnowloading.util.NbtCompat;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class BookPileBlockEntity extends BlockEntity {

    @Nullable
    private Identifier lootTable;
    private long lootTableSeed;

    public BookPileBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.BOOK_PILE.get(), pos, state);
    }

    @Nullable
    public Identifier getLootTable() {
        return lootTable;
    }

    public void setLootTable(@Nullable Identifier id, long seed) {
        this.lootTable = id;
        this.lootTableSeed = seed;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);

        if (lootTable != null) {
            tag.putString("LootTable", lootTable.toString());
            tag.putLong("LootTableSeed", lootTableSeed);
        }
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);

        if (NbtCompat.has(tag, "LootTable")) {
            this.lootTable = Identifier.parse(tag.getStringOr("LootTable", ""));
            this.lootTableSeed = tag.getLongOr("LootTableSeed", 0L);
        } else {
            this.lootTable = null;
            this.lootTableSeed = 0L;
        }
    }
}

