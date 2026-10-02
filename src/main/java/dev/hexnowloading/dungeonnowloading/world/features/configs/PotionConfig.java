package dev.hexnowloading.dungeonnowloading.world.features.configs;


import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;

public class PotionConfig {
    public static final MapCodec<PotionConfig> CODEC = RecordCodecBuilder.mapCodec((configInstance) -> configInstance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("potion_item").forGetter(config -> config.type),
            BuiltInRegistries.POTION.holderByNameCodec().fieldOf("potion").forGetter(config -> config.potion)
    ).apply(configInstance, PotionConfig::new));

    public final Item type;
    public final Holder<Potion> potion;

    public PotionConfig(Item type, Holder<Potion> potion) {
        this.type = type;
        this.potion = potion;
    }
}
