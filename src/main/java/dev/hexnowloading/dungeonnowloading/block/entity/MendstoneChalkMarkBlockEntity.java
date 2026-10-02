package dev.hexnowloading.dungeonnowloading.block.entity;



import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class MendstoneChalkMarkBlockEntity extends PreserverBlockEntity{

    public static final int MAX_USES = 160;

    private int damage;

    public MendstoneChalkMarkBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DNLBlockEntityTypes.MENDSTONE_CHALK_MARK.get(), blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput compoundTag) {
        super.saveAdditional(compoundTag, registries);

        compoundTag.putInt("Damage", this.damage);
    }

    @Override
    protected void loadAdditional(ValueInput compoundTag) {
        super.loadAdditional(compoundTag, registries);

        this.damage = compoundTag.getIntOr("Damage", 0);
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = Mth.clamp(damage, 0, MAX_USES);
    }
}
