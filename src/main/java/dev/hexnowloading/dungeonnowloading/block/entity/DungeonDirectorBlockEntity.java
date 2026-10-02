package dev.hexnowloading.dungeonnowloading.block.entity;




import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import dev.hexnowloading.dungeonnowloading.util.NbtCompat;
import dev.hexnowloading.dungeonnowloading.block.DungeonDirectorBlock;
import dev.hexnowloading.dungeonnowloading.block.ZoneReceiverBlockEntity;
import dev.hexnowloading.dungeonnowloading.components.spawn_node.*;
import dev.hexnowloading.dungeonnowloading.entity.util.EntityScale;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class DungeonDirectorBlockEntity extends BlockEntity implements ZoneReceiverBlockEntity {

    private static final int CHECK_INTERVAL = 20;

    private BlockPos cornerAOffset = BlockPos.ZERO;
    private BlockPos cornerBOffset = BlockPos.ZERO;
    private Direction authoredFacing = Direction.NORTH;
    private boolean regionSet = false;

    // Authoring: baked data stored inside director
    private boolean baked = false;
    private final List<StoredSpawnNode> storedNodes = new ArrayList<>();

    // Runtime encounter state
    private boolean triggered = false;
    private float triggerRangeMultiplier = 0.0f;
    private boolean cleared = false;
    private final Set<UUID> spawnedMobs = new HashSet<>();
    private int tickCounter = 0;

    private final List<SpawnTask> pendingTasks = new ArrayList<>();
    private boolean spawnsScheduled = false;


    public DungeonDirectorBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.DUNGEON_DIRECTOR.get(), pos, state);
    }

    // =========================
    // Tick / Trigger
    // =========================
    public static void serverTick(Level level, BlockPos pos, BlockState state, DungeonDirectorBlockEntity be) {
        if (be.cleared) return;
        if (!be.hasRegion()) return;

        // 1) Trigger check (rate-limited)
        if (!be.triggered) {
            be.tickCounter++;
            if (be.tickCounter % CHECK_INTERVAL != 0) return;

            if (be.isAnySurvivalPlayerInsideRegion()) {
                be.triggered = true;

                int scheduled = be.scheduleFromStoredNodes((ServerLevel) level);
                if (scheduled <= 0) {
                    be.triggered = false; // don't lock
                    return;
                }

                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        // 2) Triggered: tick spawn tasks EVERY TICK
        ServerLevel server = (ServerLevel) level;

        be.tickSpawnTasks(server);
        be.pruneDeadSpawnedMobs(server);

        // 3) Clear ONLY when tasks done AND mobs dead
        if (be.pendingTasks.isEmpty() && be.spawnsScheduled) {
            be.cleared = true;
            be.setChanged();

            if (state.getValue(DungeonDirectorBlock.REMOVE_AFTER_SUMMON)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return;
            }

            level.sendBlockUpdated(pos, state, state, 3);
        }
    }


    private boolean isAnySurvivalPlayerInsideRegion() {
        if (!(level instanceof ServerLevel server)) return false;
        AABB box = getRegionAabbInflated(triggerRangeMultiplier);
        return !server.getEntitiesOfClass(Player.class, box,
                p -> !p.isSpectator() && !p.getAbilities().instabuild
        ).isEmpty();
    }

    private AABB getRegionAabbInflated(double inflate) {
        BlockPos a = worldPosition.offset(rotateOffset(cornerAOffset));
        BlockPos b = worldPosition.offset(rotateOffset(cornerBOffset));

        BlockPos min = new BlockPos(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        );
        BlockPos max = new BlockPos(
                Math.max(a.getX(), b.getX()) + 1,
                Math.max(a.getY(), b.getY()) + 1,
                Math.max(a.getZ(), b.getZ()) + 1
        );

        return new AABB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()).inflate(inflate);
    }

    private void tickSpawnTasks(ServerLevel server) {
        if (pendingTasks.isEmpty()) return;
        pendingTasks.removeIf(task -> task.tick(server, this));
        setChanged();
    }

    private int scheduleFromStoredNodes(ServerLevel server) {
        if (storedNodes.isEmpty()) return 0;
        if (!baked) return 0;

        pendingTasks.clear();
        int scheduled = 0;

        for (StoredSpawnNode entry : storedNodes) {
            BlockPos basePos = worldPosition.offset(rotateOffset(entry.relPos));

            Identifier poolId;
            try {
                poolId = Identifier.parse(entry.poolId);
            } catch (Exception e) {
                continue;
            }

            SpawnPool pool = SpawnPools.get(poolId);
            if (pool == null) continue;

            Identifier nodeId = pool.pickNodeId(server.getRandom());
            if (nodeId == null) continue;

            SpawnNode nodeDef = SpawnNodes.get(nodeId);
            if (nodeDef == null) continue;

            SpawnEntry picked = nodeDef.pickEntry(server.getRandom());

            CompoundTag patch = picked.combinedPatchCopy();

            // Create a resolved single-mode node for the spawn task pipeline
            SpawnNode resolved = new SpawnNode(
                    nodeDef.id,
                    picked.entityType,
                    picked.count,
                    picked.chance,
                    picked.spawnEffect,
                    picked.nbtPatch,
                    picked.snbtPatch
            );

            SpawnRequest req = new SpawnRequest(resolved, patch, basePos);
            SpawnTask task = DNLSpawnEffects.createTask(resolved.spawnEffect, req);
            pendingTasks.add(task);

            scheduled++;
        }

        spawnsScheduled = true;
        setChanged();
        return scheduled;
    }



    public boolean spawnOne(ServerLevel server, SpawnNode def, CompoundTag patch, BlockPos pos) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(def.entityType).toString());

        // Create entity (+ passengers if your patch includes Passengers)
        if (patch != null) nbt.merge(patch);

        Entity loaded = NbtCompat.loadEntityRecursive(nbt, server, EntitySpawnReason.SPAWNER, e -> {
            e.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, e.getYRot(), e.getXRot());
            return e;
        });

        if (!(loaded instanceof Mob mob)) {
            if (loaded != null) loaded.discard();
            return false;
        }

        // 1) Vanilla setup FIRST (may overwrite gear)
        mob.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);

        // 2) Then FORCE our custom NBT LAST (restores enchanted bow, armor, etc.)
        if (patch != null && !patch.isEmpty()) {
            CompoundTag full = NbtCompat.saveEntity(mob);
            NbtMerge.mergeCompound(full, patch);
            NbtCompat.loadEntity(mob, full);
        }

        // 3) Add root + passengers
        server.addFreshEntityWithPassengers(mob);

        trackSpawnTree(mob);
        scaleSpawnTree(mob);
        return true;
    }


    private void trackSpawnTree(Entity root) {
        spawnedMobs.add(root.getUUID());
        for (Entity p : root.getPassengers()) {
            trackSpawnTree(p);
        }
    }

    private void scaleSpawnTree(Entity root) {
        if (root instanceof Mob mob) {
            EntityScale.scaleMobAttributes(mob);
        }
        for (Entity p : root.getPassengers()) {
            scaleSpawnTree(p);
        }
    }

    private boolean rollChance(ServerLevel level, double chance) {
        if (chance >= 1.0) return true;
        if (chance <= 0.0) return false;
        return level.getRandom().nextDouble() < chance;
    }

    private void pruneDeadSpawnedMobs(ServerLevel server) {
        spawnedMobs.removeIf(uuid -> {
            Entity e = server.getEntity(uuid);
            return e == null || !e.isAlive();
        });
    }

    // =========================
    // Bake / Unbake (authoring)
    // =========================
    public boolean isBaked() { return baked; }

    public int bakeFromWorldSpawnNodes() {
        if (!(level instanceof ServerLevel server)) return 0;
        if (!hasRegion()) return 0;

        AABB box = getRegionAabbInflated(0.0);

// Convert AABB to integer block bounds
        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);
        int maxX = (int) Math.ceil(box.maxX) - 1;
        int maxY = (int) Math.ceil(box.maxY) - 1;
        int maxZ = (int) Math.ceil(box.maxZ) - 1;

        storedNodes.clear();

        int count = 0;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);

                    if (!server.getBlockState(p).is(DNLBlocks.SPAWN_NODE.get())) continue;

                    BlockEntity be = server.getBlockEntity(p);
                    if (be instanceof SpawnNodeBlockEntity nodeBe) {
                        // IMPORTANT: store UNROTATED local offset, relative to the director
                        BlockPos rel = p.subtract(worldPosition);

                        storedNodes.add(new StoredSpawnNode(rel, nodeBe.getSpawnPool()));
                        count++;

                        server.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        baked = true;
        resetEncounterState();
        setChanged();
        return count;
    }

    public int restoreSpawnNodesToWorld() {
        if (!(level instanceof ServerLevel server)) return 0;

        int placed = 0;
        for (StoredSpawnNode entry : storedNodes) {
            BlockPos p = worldPosition.offset(rotateOffset(entry.relPos));

            if (!server.getBlockState(p).isAir()) continue;

            server.setBlock(p, DNLBlocks.SPAWN_NODE.get().defaultBlockState(), 3);

            BlockEntity be = server.getBlockEntity(p);
            if (be instanceof SpawnNodeBlockEntity nodeBe) {
                nodeBe.setSpawnPool(entry.poolId);
                nodeBe.setChanged();

                BlockState st = server.getBlockState(p);
                server.sendBlockUpdated(p, st, st, 3);
                placed++;
            }
        }

        baked = false;
        resetEncounterState();
        setChanged();
        return placed;
    }

    public void resetEncounterState() {
        triggered = false;
        cleared = false;
        spawnedMobs.clear();
        tickCounter = 0;
        pendingTasks.clear();
        spawnsScheduled = false;
    }


    // =========================
    // ZoneReceiverBlockEntity
    // =========================
    @Override
    public void setRegion(BlockPos cornerAWorld, BlockPos cornerBWorld, Direction authoredFacing) {
        this.regionSet = true;
        // store offsets relative to director position
        this.cornerAOffset = cornerAWorld.subtract(this.worldPosition);
        this.cornerBOffset = cornerBWorld.subtract(this.worldPosition);

        // store the facing used during authoring (usually the block’s facing at the time)
        this.authoredFacing = authoredFacing == null ? Direction.NORTH : authoredFacing;

        // if region changes, reset baked/encounter state (recommended)
        this.baked = false;
        this.storedNodes.clear();
        resetEncounterState();

        setChanged();
    }

    public boolean hasRegion() { return regionSet; }

    private BlockPos rotateOffset(BlockPos offset) {
        if (level == null) return offset;

        Direction currentFacing = getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);

        int currentIndex = switch (currentFacing) {
            default -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int authoredIndex = switch (authoredFacing) {
            default -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int diff = currentIndex - authoredIndex;

        return switch (diff) {
            default -> offset;
            case 1, -3 -> offset.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
            case -1, 3 -> offset.rotate(net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90);
            case -2, 2 -> offset.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_180);
        };
    }

    public void setAuthoredFacing(Direction facing) {
        this.authoredFacing = (facing == null) ? Direction.NORTH : facing;
        setChanged();
    }

    // =========================
    // NBT Save/Load
    // =========================
    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);

        tag.putFloat("TriggerRangeMultiplier", triggerRangeMultiplier);
        NbtCompat.put(tag, "CornerA", writePos(cornerAOffset));
        NbtCompat.put(tag, "CornerB", writePos(cornerBOffset));
        tag.putInt("AuthoredFacing", authoredFacing.get3DDataValue());

        tag.putBoolean("RegionSet", regionSet);
        tag.putBoolean("Baked", baked);

        ListTag stored = new ListTag();
        for (StoredSpawnNode e : storedNodes) {
            CompoundTag t = new CompoundTag();
            t.putInt("dx", e.relPos.getX());
            t.putInt("dy", e.relPos.getY());
            t.putInt("dz", e.relPos.getZ());
            t.putString("PoolId", e.poolId);
            stored.add(t);
        }
        NbtCompat.put(tag, "StoredNodes", stored);

        tag.putBoolean("Triggered", triggered);
        tag.putBoolean("Cleared", cleared);

        ListTag uuids = new ListTag();
        for (UUID id : spawnedMobs) {
            CompoundTag t = new CompoundTag();
            NbtCompat.putUUID(t, "Id", id);
            uuids.add(t);
        }
        NbtCompat.put(tag, "SpawnedMobs", uuids);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);

        this.triggerRangeMultiplier = NbtCompat.has(tag, "TriggerRangeMultiplier")
                ? tag.getFloatOr("TriggerRangeMultiplier", 0.0F)
                : 10.0F;
        this.cornerAOffset = NbtCompat.has(tag, "CornerA") ? readPos(NbtCompat.getCompound(tag, "CornerA")) : BlockPos.ZERO;
        this.cornerBOffset = NbtCompat.has(tag, "CornerB") ? readPos(NbtCompat.getCompound(tag, "CornerB")) : BlockPos.ZERO;
        this.authoredFacing = NbtCompat.has(tag, "AuthoredFacing")
                ? Direction.from3DDataValue(tag.getIntOr("AuthoredFacing", 0))
                : Direction.NORTH;

        this.regionSet = tag.getBooleanOr("RegionSet", false);
        this.baked = tag.getBooleanOr("Baked", false);

        this.storedNodes.clear();
        if (NbtCompat.has(tag, "StoredNodes")) {
            ListTag list = NbtCompat.getList(tag, "StoredNodes");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompoundOrEmpty(i);
                BlockPos rel = new BlockPos(t.getIntOr("dx", 0), t.getIntOr("dy", 0), t.getIntOr("dz", 0));
                String poolId = t.getStringOr("PoolId", "");
                storedNodes.add(new StoredSpawnNode(rel, poolId));
            }
        }

        this.triggered = tag.getBooleanOr("Triggered", false);
        this.cleared = tag.getBooleanOr("Cleared", false);

        this.spawnedMobs.clear();
        if (NbtCompat.has(tag, "SpawnedMobs")) {
            ListTag list = NbtCompat.getList(tag, "SpawnedMobs");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompoundOrEmpty(i);
                if (NbtCompat.hasUUID(t, "Id")) spawnedMobs.add(NbtCompat.getUUID(t, "Id"));
            }
        }
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

    public static class StoredSpawnNode {
        public final BlockPos relPos;
        public final String poolId;

        public StoredSpawnNode(BlockPos relPos, String poolId) {
            this.relPos = relPos;
            this.poolId = poolId;
        }
    }
}
