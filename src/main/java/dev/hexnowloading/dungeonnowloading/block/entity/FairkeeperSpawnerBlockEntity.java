package dev.hexnowloading.dungeonnowloading.block.entity;






import net.minecraft.util.random.Weighted;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import dev.hexnowloading.dungeonnowloading.util.NbtCompat;
import net.minecraft.world.entity.EntitySpawnReason;
import com.mojang.logging.LogUtils;
import dev.hexnowloading.dungeonnowloading.block.FairkeeperSpawnerBlock;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityScale;
import dev.hexnowloading.dungeonnowloading.entity.util.SlumberingEntity;
import dev.hexnowloading.dungeonnowloading.entity.util.SpawnMobUtil;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLProperties;
import dev.hexnowloading.dungeonnowloading.util.ArmorTrimMaterial;
import dev.hexnowloading.dungeonnowloading.util.ArmorTrimPattern;
import dev.hexnowloading.dungeonnowloading.util.WeightedRandomBag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.ai.behavior.ShufflingList.WeightedEntry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class FairkeeperSpawnerBlockEntity extends BlockEntity {

    private static final int SPAWN_RANGE = 4;
    private static final int SPAWN_POS_TRIES = 10;
    private static final Logger LOGGER = LogUtils.getLogger();
    private WeightedList<SpawnData> spawnPotentials = WeightedList.of();
    private SpawnData nextSpawnData;
    private Entity displayEntity;
    private int remainingStoredMobs;
    private int spawnDelay;
    private int startUpTick;
    private int destroyTick;
    private int requiredPlayerRange = 16;
    private double spin;
    private double oSpin;
    private boolean disabled;

    public FairkeeperSpawnerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DNLBlockEntityTypes.FAIRKEEPER_SPAWNER.get(), blockPos, blockState);
        this.spawnDelay = 0;
        this.startUpTick = 40;
        this.destroyTick = -1;
        this.remainingStoredMobs = 0;
        this.disabled = false;
    }

    @Override
    protected void saveAdditional(ValueOutput compoundTag) {
        compoundTag.putInt("RemainingStoredMobs", this.remainingStoredMobs);
        compoundTag.putInt("StartUpTick", this.startUpTick);
        compoundTag.putInt("SpawnDelay", this.spawnDelay);
        compoundTag.putBoolean("Disabled", this.disabled);
        if (this.nextSpawnData != null) {
            NbtCompat.put(compoundTag, "SpawnData", (Tag)SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, this.nextSpawnData).result().orElseThrow(() -> {
                return new IllegalStateException("Invalid SpawnData");
            }));
        }

        NbtCompat.put(compoundTag, "SpawnPotentials", (Tag)SpawnData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.spawnPotentials).result().orElseThrow());
        super.saveAdditional(compoundTag);
    }

    @Override
    protected void loadAdditional(ValueInput compoundTag) {
        this.remainingStoredMobs = compoundTag.getIntOr("RemainingStoredMobs", 0);
        this.startUpTick = compoundTag.getIntOr("StartUpTick", 0);
        this.spawnDelay = compoundTag.getIntOr("SpawnDelay", 0);
        this.disabled = compoundTag.getBooleanOr("Disabled", false);
        boolean b0 = NbtCompat.has(compoundTag, "SpawnData");
        if (b0) {
            SpawnData spawnData = (SpawnData)SpawnData.CODEC.parse(NbtOps.INSTANCE, NbtCompat.getCompound(compoundTag, "SpawnData")).resultOrPartial(($$0x) -> {
                LOGGER.warn("Invalid SpawnData: {}", $$0x);
            }).orElseGet(SpawnData::new);
            this.setNextSpawnData(this.level, this.getBlockPos(), spawnData);
        }
        boolean b1 = NbtCompat.has(compoundTag, "SpawnPotentials");
        if (b1) {
            ListTag $$6 = NbtCompat.getList(compoundTag, "SpawnPotentials");
            this.spawnPotentials = (WeightedList)SpawnData.LIST_CODEC.parse(NbtOps.INSTANCE, $$6).resultOrPartial(($$0x) -> {
                LOGGER.warn("Invalid SpawnPotentials list: {}", $$0x);
            }).orElseGet(WeightedList::of);
        } else {
            this.spawnPotentials = WeightedList.of(this.nextSpawnData != null ? this.nextSpawnData : new SpawnData());
        }

        this.displayEntity = null;
        super.loadAdditional(compoundTag);
    }

    protected void setNextSpawnData(Level level, BlockPos blockPos, SpawnData spawnData) {
        this.nextSpawnData = spawnData;
        if (level != null) {
            BlockState blockState = level.getBlockState(blockPos);
            level.sendBlockUpdated(blockPos, blockState, blockState, 4);
        }
    }

    // For Rendering the inside entity, but dunno how to do it yet...
    public Entity getOrCreateDisplayEntity(Level level, RandomSource randomSource, BlockPos blockPos) {
        if (this.displayEntity == null) {
            CompoundTag $$3 = this.getOrCreateNextSpawnData(level, randomSource, blockPos).getEntityToSpawn();
            if (!$$3.contains("id")) {
                return null;
            }

            this.displayEntity = NbtCompat.loadEntityRecursive($$3, level, EntitySpawnReason.SPAWNER, Function.identity());
            if ($$3.size() == 1 && this.displayEntity instanceof Mob) {
            }
        }

        return this.displayEntity;
    }

    private SpawnData getOrCreateNextSpawnData(@Nullable Level level, RandomSource randomSource, BlockPos blockPos) {
        if (this.nextSpawnData != null) {
            return this.nextSpawnData;
        } else {
            this.setNextSpawnData(level, blockPos, this.spawnPotentials.getRandom(randomSource).map(Weighted::value).orElseGet(SpawnData::new));
            return this.nextSpawnData;
        }
    }

    private boolean isNearPlayer(Level level, BlockPos blockPos) {
        return level.hasNearbyAlivePlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, this.requiredPlayerRange);
    }

    public void setDisabled(boolean b) {
        this.disabled = b;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FairkeeperSpawnerBlockEntity blockEntity) {
        if (state.getValue(DNLProperties.FAIRKEEPER_ALERT) == Boolean.TRUE) {
            RandomSource randomSource = level.getRandom();
            double d = (double)pos.getX() + randomSource.nextDouble();
            double e = (double)pos.getY() + randomSource.nextDouble();
            double f = (double)pos.getZ() + randomSource.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, d, e, f, 0.0, 0.0, 0.0);
        }
        if (!blockEntity.isNearPlayer(level, pos)) {
            blockEntity.oSpin = blockEntity.spin;
        } else if (blockEntity.displayEntity != null) {
            blockEntity.oSpin = blockEntity.spin;
            blockEntity.spin = (blockEntity.spin + (double)(1000.0f / ((float)blockEntity.spawnDelay + 200.0f))) % 360.0;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FairkeeperSpawnerBlockEntity blockEntity) {
        if (blockEntity.disabled) {
            if (blockEntity.destroyTick < 0) {
                blockEntity.destroyTick = 20 + level.getRandom().nextInt(40);
            }
            blockEntity.destroyTick--;
            if (blockEntity.destroyTick == 0) {
                level.destroyBlock(pos, false);
                double d = (double) pos.getX() + 0.5D;
                double e = (double) pos.getY() + 0.5D;
                double f = (double) pos.getZ() + 0.5D;
                ((ServerLevel) level).sendParticles(DustParticleOptions.REDSTONE, d, e, f, 10, 0.0D, 0.5, 0.5, 0.5);
            }
        }
        if (state.getValue(DNLProperties.FAIRKEEPER_ALERT)) {
            if (blockEntity.startUpTick > 0) {
                if (blockEntity.startUpTick == 40) {
                    level.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.WITHER_SHOOT, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.2F + 0.8F);
                }
                blockEntity.startUpTick--;
            } else {
                if(blockEntity.remainingStoredMobs > 0) {
                    if (blockEntity.spawnDelay > 0) {
                        blockEntity.spawnDelay--;
                    } else {
                        blockEntity.spawnDelay = 20;
                        blockEntity.remainingStoredMobs--;
                        blockEntity.randomMobSummon((ServerLevel) level);
                        level.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.2F + 0.8F);
                    }
                } else {
                    level.destroyBlock(pos, false);
                }
            }
        }
    }

    public void alert(int playerCount, BlockPos blockPos, FairkeeperSpawnerBlockEntity blockEntity) {
        if (blockEntity.getBlockState().getValue(DNLProperties.FAIRKEEPER_ALERT) == Boolean.FALSE) {
            FairkeeperSpawnerBlock.setFairkeeperAlert(blockEntity.level, blockPos, Boolean.TRUE);
            blockEntity.remainingStoredMobs = Math.min(playerCount + 1, 4);
        }
    }

    public void destroyed() {
        AABB aabb = new AABB(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), this.getBlockPos().getX() + 1, this.getBlockPos().getY() + 1, this.getBlockPos().getZ()).inflate(32.0D);
        List<Player> nearbyPlayers = this.level.getEntitiesOfClass(Player.class, aabb);
        int playerCount = Math.min(nearbyPlayers.size() + 1, 4);
        for (int i = 0; i < playerCount; i++) {
            randomMobSummon((ServerLevel) this.level);
        }
    }

    public void randomMobSummon(ServerLevel level) {
        RandomSource randomSource = level.getRandom();
        SpawnData spawnData = this.getOrCreateNextSpawnData(level, randomSource, this.getBlockPos());
        CompoundTag compoundTag = spawnData.getEntityToSpawn();
        Optional<EntityType<?>> entityType = EntityType.by(compoundTag);
        if (entityType.isEmpty()) return;
        if (!entityType.get().getCategory().isFriendly() && level.getDifficulty() == Difficulty.PEACEFUL) return;
        for (int i = 0; i < SPAWN_POS_TRIES; i++) {
            double x = (double)this.getBlockPos().getX() + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * (double)this.SPAWN_RANGE + 0.5;
            double y = (double)(this.getBlockPos().getY() + level.getRandom().nextInt(3) - 1);
            double z = (double)this.getBlockPos().getZ() + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * (double)this.SPAWN_RANGE + 0.5;
            BlockPos blockPos = BlockPos.containing(x, y, z);
            Entity mob = NbtCompat.loadEntityRecursive(compoundTag, level, EntitySpawnReason.SPAWNER, (a) -> {
                a.snapTo(x, y, z, a.getYRot(), a.getXRot());
                return a;
            });
            if (mob == null) break;
            mob.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0f, 0.0f);
            if (mob instanceof Mob mob1 && level.noCollision(mob1, entityType.get().getSpawnAABB(mob1.getX(), mob1.getY(), mob1.getZ())) && mob1.checkSpawnObstruction(level)) {
                EntityScale.scaleMobAttributes(mob1);
                mob1.setPersistenceRequired();
                if (spawnData.getEntityToSpawn().size() == 1 && spawnData.getEntityToSpawn().contains("id")) { // adding this part ignores equipment attachment from the finalized spawn, which is good, but don't know why it happens...
                    ((Mob)mob).finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.SPAWNER, (SpawnGroupData)null);
                }
                /*Collection<MobEffectInstance> reapplyMobEffects = mob1.getActiveEffects();
                mob1.removeAllEffects();
                for (MobEffectInstance mobEffectInstance : reapplyMobEffects) {
                    mob1.addEffect(mobEffectInstance);
                }*/
            } else {
                continue;
            }
            if (!level.tryAddFreshEntityWithPassengers(mob1)) break;
            level.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, this.getBlockPos(), 0);
            level.gameEvent(mob1, GameEvent.ENTITY_PLACE, blockPos);
            mob1.spawnAnim();
            this.spawnPotentials.getRandom(randomSource).ifPresent((b) -> {
                this.setNextSpawnData(level, blockPos, b.data());
            });
            break;
        }
    }

    public double getSpin() {
        return this.spin;
    }

    public double getoSpin() {
        return this.oSpin;
    }
}
