package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.world.processors.BookPileRandomizerProcessor;
import dev.hexnowloading.dungeonnowloading.world.processors.StatueProcessor;
import dev.hexnowloading.dungeonnowloading.world.processors.WaterloggingFixProcessor;
import dev.hexnowloading.dungeonnowloading.world.processors.WeightedListProcessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public class DNLProcessors {

    public static final Supplier<MapCodec<WaterloggingFixProcessor>> WATERLOGGING_FIX_PROCESSOR = register("waterlogging_fix_processor", WaterloggingFixProcessor.CODEC);
    public static final Supplier<MapCodec<WeightedListProcessor>> WEIGHTED_LIST_PROCESSOR = register("weighted_list_processor", WeightedListProcessor.CODEC);
    public static final Supplier<MapCodec<StatueProcessor>> STATUE_PROCESSOR = register("statue_processor", StatueProcessor.CODEC);
    public static final Supplier<MapCodec<BookPileRandomizerProcessor>> BOOK_PILE_RANDOMIZER_PROCESSOR = register("book_pile_randomizer_processor", BookPileRandomizerProcessor.CODEC);

    public static <T extends StructureProcessor> Supplier<MapCodec<T>> register(String name, MapCodec<T> codec) {
        return Services.REGISTRY.register(BuiltInRegistries.STRUCTURE_PROCESSOR, name, () -> codec);
    }

    public static void init() {}
}
