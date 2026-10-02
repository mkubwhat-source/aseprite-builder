package dev.hexnowloading.dungeonnowloading.world.features;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 26.x replaced {@code Feature<FC>} + {@code ConfiguredFeature} with data-driven {@link Feature} instances that carry
 * their own configuration. This base class keeps the 1.21.1 "config + place(context)" shape of the mod's features.
 */
public abstract class DNLFeature<C> implements Feature {

    /** Stand-in for the removed {@code NoneFeatureConfiguration}. */
    public static final class None {
        public static final None INSTANCE = new None();

        private None() {
        }
    }

    private static final Map<Class<?>, MapCodec<? extends Feature>> CODECS = new HashMap<>();

    protected final C config;

    protected DNLFeature(C config) {
        this.config = config;
    }

    public C config() {
        return this.config;
    }

    public abstract boolean place(DNLFeatureContext<C> context);

    /** Associates a feature class with the codec it was registered under (see DNLFeatures). */
    public static <F extends DNLFeature<?>> void bindCodec(Class<F> type, MapCodec<F> codec) {
        CODECS.put(type, codec);
    }

    @Override
    public MapCodec<? extends Feature> codec() {
        MapCodec<? extends Feature> codec = CODECS.get(this.getClass());
        if (codec == null) {
            throw new IllegalStateException("Unregistered feature type " + this.getClass().getName());
        }
        return codec;
    }

    /** Replacement for the removed {@code Feature.isReplaceable(tag)}. */
    public static Predicate<BlockState> isReplaceable(TagKey<Block> cannotReplace) {
        return state -> !state.is(cannotReplace);
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return this.place(new DNLFeatureContext<>(level, chunkGenerator, random, origin, this.config));
    }

    public static <C, F extends DNLFeature<C>> MapCodec<F> codec(MapCodec<C> configCodec, Function<C, F> factory) {
        return configCodec.xmap(factory, DNLFeature::config);
    }

    public static <F extends DNLFeature<None>> MapCodec<F> unitCodec(Supplier<F> factory) {
        return MapCodec.unit(factory);
    }
}
