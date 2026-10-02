package dev.hexnowloading.dungeonnowloading.client.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Bridges 1.21.1 builtin item renderers to 26.x special item models. Each registered item gets a special model
 * type with the item's id, used by {@code assets/<ns>/items/<item>.json} ({@code "type": "minecraft:special"}).
 */
public final class LegacyItemRenderers {
    /** Display context of the item model currently being resolved (set by {@code SpecialModelWrapperMixin}). */
    public static final ThreadLocal<ItemDisplayContext> CURRENT_CONTEXT = ThreadLocal.withInitial(() -> ItemDisplayContext.NONE);

    private LegacyItemRenderers() {
    }

    @FunctionalInterface
    public interface Renderer {
        void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
    }

    public record Argument(ItemStack stack, ItemDisplayContext displayContext) {
    }

    public static void register(Item item, Renderer renderer) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        Unbaked unbaked = new Unbaked(renderer);
        MapCodec<Unbaked> codec = MapCodec.unit(unbaked);
        unbaked.codec = codec;
        SpecialModelRenderers.ID_MAPPER.put(id, codec);
    }

    private static final class Unbaked implements SpecialModelRenderer.Unbaked<Argument> {
        private final Renderer renderer;
        private MapCodec<Unbaked> codec;

        private Unbaked(Renderer renderer) {
            this.renderer = renderer;
        }

        @Override
        public SpecialModelRenderer<Argument> bake(SpecialModelRenderer.BakingContext context) {
            return new Baked(this.renderer);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Argument>> type() {
            return this.codec;
        }
    }

    private record Baked(Renderer renderer) implements SpecialModelRenderer<Argument> {
        @Override
        public void submit(@Nullable Argument argument, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument != null) {
                RecordingBufferSource.draw(collector, buffers ->
                        this.renderer.renderByItem(argument.stack(), argument.displayContext(), poseStack, buffers, lightCoords, overlayCoords));
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            output.accept(new Vector3f(0.0F, 0.0F, 0.0F));
            output.accept(new Vector3f(1.0F, 1.0F, 1.0F));
        }

        @Override
        public @Nullable Argument extractArgument(ItemStack stack) {
            return new Argument(stack.copy(), CURRENT_CONTEXT.get());
        }
    }
}
