package dev.hexnowloading.dungeonnowloading.world.features;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

/** Mirrors the removed 1.21.1 {@code FeaturePlaceContext}. */
public record DNLFeatureContext<C>(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin, C config) {
}
