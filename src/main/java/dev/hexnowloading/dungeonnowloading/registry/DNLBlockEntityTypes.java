package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.block.entity.*;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class DNLBlockEntityTypes {
    public static final Supplier<BlockEntityType<BookPileBlockEntity>> BOOK_PILE = register("book_pile", () -> new BlockEntityType<>(BookPileBlockEntity::new, java.util.Set.of(DNLBlocks.BOOK_PILE.get())));
    public static final Supplier<BlockEntityType<FairkeeperChestBlockEntity>> FAIRKEEPER_CHEST = register("fairkeeper_chest", () -> new BlockEntityType<>(FairkeeperChestBlockEntity::new, java.util.Set.of(DNLBlocks.FAIRKEEPER_CHEST.get())));
    public static final Supplier<BlockEntityType<FairkeeperSpawnerBlockEntity>> FAIRKEEPER_SPAWNER = register("fairkeeper_spawner", () -> new BlockEntityType<>(FairkeeperSpawnerBlockEntity::new, java.util.Set.of(DNLBlocks.FAIRKEEEPER_SPAWNER.get())));
    public static final Supplier<BlockEntityType<ScuttleStatueBlockEntity>> SCUTTLE_STATUE = register("scuttle_statue", () -> new BlockEntityType<>(ScuttleStatueBlockEntity::new, java.util.Set.of(DNLBlocks.SCUTTLE_STATUE.get())));
    public static final Supplier<BlockEntityType<BallistaGolemStatueBlockEntity>> BALLISTA_GOLEM_STATUE = register("ballista_golem_statue", () -> new BlockEntityType<>(BallistaGolemStatueBlockEntity::new, java.util.Set.of(DNLBlocks.BALLISTA_GOLEM_STATUE.get())));
    public static final Supplier<BlockEntityType<VertexPillarBlockEntity>> VERTEX_PILLAR = register("vertex_pillar", () -> new BlockEntityType<>(VertexPillarBlockEntity::new, java.util.Set.of(DNLBlocks.VERTEX_PILLAR.get())));
    public static final Supplier<BlockEntityType<DisabledFairkeeperChestBlockEntity>> DISABLED_FAIRKEEPER_CHEST = register("disabled_fairkeeper_chest", () -> new BlockEntityType<>(DisabledFairkeeperChestBlockEntity::new, java.util.Set.of(DNLBlocks.WISE_FAIRKEEPER_CHEST.get(), DNLBlocks.FIERCE_FAIRKEEPER_CHEST.get())));
    public static final Supplier<BlockEntityType<PreserverBlockEntity>> PRESERVER_BLOCK = register("preserver", () -> new BlockEntityType<>(PreserverBlockEntity::new, java.util.Set.of(DNLBlocks.STONE_PRESERVER.get())));
    public static final Supplier<BlockEntityType<MendingAuraBlockEntity>> MENDING_AURA = register("mending_aura", () -> new BlockEntityType<>(MendingAuraBlockEntity::new, java.util.Set.of(DNLBlocks.MENDING_AURA.get())));
    public static final Supplier<BlockEntityType<MendingTableBlockEntity>> MENDING_TABLE = register("mending_table", () -> new BlockEntityType<>(MendingTableBlockEntity::new, java.util.Set.of(DNLBlocks.MENDING_TABLE.get())));
    public static final Supplier<BlockEntityType<DuriteQuellerBlockEntity>> DURITE_QUELLER = register("durite_queller", () -> new BlockEntityType<>(DuriteQuellerBlockEntity::new, java.util.Set.of(DNLBlocks.DURITE_QUELLER.get())));
    public static final Supplier<BlockEntityType<DungeonDirectorBlockEntity>> DUNGEON_DIRECTOR = register("dungeon_director", () -> new BlockEntityType<>(DungeonDirectorBlockEntity::new, java.util.Set.of(DNLBlocks.DUNGEON_DIRECTOR.get())));
    public static final Supplier<BlockEntityType<SpawnNodeBlockEntity>> SPAWN_NODE = register("spawn_node", () -> new BlockEntityType<>(SpawnNodeBlockEntity::new, java.util.Set.of(DNLBlocks.SPAWN_NODE.get())));

    public static final Supplier<BlockEntityType<PlayerStatueBlockEntity>> PLAYER_STATUE = register("player_statue", () -> new BlockEntityType<>(PlayerStatueBlockEntity::new, java.util.Set.of(DNLBlocks.PLAYER_STATUE.get())));
    public static final Supplier<BlockEntityType<MendstoneChalkMarkBlockEntity>> MENDSTONE_CHALK_MARK = register("mendstone_chalk_mark", () -> new BlockEntityType<>(MendstoneChalkMarkBlockEntity::new, java.util.Set.of(DNLBlocks.MENDSTONE_CHALK_MARK.get())));
    public static final Supplier<BlockEntityType<DungeonBannerBlockEntity>> DUNGEON_BANNER = register("dungeon_banner", () -> new BlockEntityType<>(DungeonBannerBlockEntity::new, java.util.Set.of(DNLBlocks.DUNGEON_BANNER_SPAWNER_MAGENTA.get(), DNLBlocks.DUNGEON_BANNER_SPAWNER_BLACK.get(), DNLBlocks.DUNGEON_BANNER_SPAWNER_BLUE.get(), DNLBlocks.DUNGEON_BANNER_SPAWNER_PURPLE.get(), DNLBlocks.DUNGEON_BANNER_SPAWNER_GREEN.get(), DNLBlocks.DUNGEON_BANNER_HOLLOW.get(), DNLBlocks.DUNGEON_BANNER_SPAWNER_CARRIER.get(), DNLBlocks.DUNGEON_BANNER_EXPERIENCE_BOTTLE.get(), DNLBlocks.DUNGEON_BANNER_CHAOS_SPAWNER.get(), DNLBlocks.DUNGEON_BANNER_WHIMPER_LANTERN.get(), DNLBlocks.DUNGEON_BANNER_GARHOLD_UPSIDEDOWN.get(), DNLBlocks.DUNGEON_BANNER_SKULL_OF_CHAOS.get())));

    //public static final RegistryObject<BlockEntityType<WindAlterBlockEntity>> WIND_ALTER = BLOCK_ENTITY_TYPES.register("wind_alter", () -> new BlockEntityType<>(WindAlterBlockEntity::new, java.util.Set.of(SkyislandBlocks.WIND_ALTER.get())));

    private static <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String name, Supplier<BlockEntityType<T>> type) {
        return Services.REGISTRY.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, name, type);
    }

    public static void init() {}
}
