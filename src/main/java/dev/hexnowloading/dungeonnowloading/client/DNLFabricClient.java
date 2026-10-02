package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.DNLClient;
import dev.hexnowloading.dungeonnowloading.block.DungeonBannerBlock;
import dev.hexnowloading.dungeonnowloading.client.model.MendingAuraFabricBakedModel;
import dev.hexnowloading.dungeonnowloading.block.client.model.*;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.*;
import dev.hexnowloading.dungeonnowloading.entity.client.model.*;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepButlerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulChaosSpawnerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulSerpentCallerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.*;
import dev.hexnowloading.dungeonnowloading.item.CopperDetonatorItem;
import dev.hexnowloading.dungeonnowloading.item.RepulsorItem;
import dev.hexnowloading.dungeonnowloading.item.client.model.ScorcherModel;
import dev.hexnowloading.dungeonnowloading.item.client.renderer.PlayerStatueItemRenderer;
import dev.hexnowloading.dungeonnowloading.item.client.renderer.ScorcherRenderer;
import dev.hexnowloading.dungeonnowloading.particle.*;
import dev.hexnowloading.dungeonnowloading.registry.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import dev.hexnowloading.dungeonnowloading.client.legacy.LegacyItemRenderers;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import dev.hexnowloading.dungeonnowloading.client.legacy.LegacyBlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.Map;

public class DNLFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Packets are registered by common's DungeonNowLoading.init() (via FabricNetworkHelper);
        // do NOT re-register here — doing so double-registers the payload codec/receiver.

        DNLClient.registerItemModels();
        DNLClient.registerMenuScreens();
        net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(context ->
                dev.hexnowloading.dungeonnowloading.client.render.MendingAuraOverlayRenderer.render(
                        context.poseStack(), context.submitNodeCollector(), context.levelState().cameraRenderState.pos,
                        net.minecraft.client.Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)));
        registerItemModelLayers();
        registerItemRenderers();
        registerBlockRenderers();
        registerModelModifiers();
        registerModelLayers();
        registerRenderers();
        registerParticleFactories();
        MendstonePickaxeParticleHandlerFabric.register();

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> addDnlEnchantmentDescriptions(stack, lines));
    }

    private void registerItemModelLayers() {
        ModelLayerRegistry.registerModelLayer(ScorcherModel.LAYER_LOCATION, ScorcherModel::createBodyLayer);
    }

    private void registerItemRenderers() {

        // Block
        LegacyItemRenderers.register(DNLItems.FAIRKEEPER_CHEST.get(), FairkeeperChestItemRenderer.getInstance()::renderByItem);
        LegacyItemRenderers.register(DNLItems.WISE_FAIRKEEPER_CHEST.get(), WiseFairkeeperChestItemRenderer.getInstance()::renderByItem);
        LegacyItemRenderers.register(DNLItems.FIERCE_FAIRKEEPER_CHEST.get(), FierceFairkeeperChestItemRenderer.getInstance()::renderByItem);
        LegacyItemRenderers.register(DNLItems.PLAYER_STATUE.get(), PlayerStatueItemRenderer.getInstance()::renderByItem);
        for (DungeonBannerBlock.DungeonBannerVariant variant : DungeonBannerBlock.DungeonBannerVariant.values()) {
            LegacyItemRenderers.register(
                    DNLItems.getBannerItem(variant).get(),
                    (stack, displayContext, poseStack, buffer, light, overlay) -> {
                        DungeonBannerBlockItemRenderer.getInstance()
                                .renderByItem(stack, displayContext, poseStack, buffer, light, overlay);
                    }
            );
        }


        // Item
        LegacyItemRenderers.register(DNLItems.SCORCHER.get(), ScorcherRenderer.getInstance()::renderByItem);
        LegacyItemRenderers.register(DNLItems.SOUL_SCORCHER.get(), ScorcherRenderer.getInstance()::renderByItem);
        //LegacyItemRenderers.register(DNLItems.SCORCHER.get(), new DifferentProspectiveItemRenderer(DNLClientRegistry.SCORCHER_3D_MODEL, DNLClientRegistry.SCORCHER_3D_MODEL));

    }

    private void registerBlockRenderers() {
    }

    private void registerModelModifiers() {
        ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake().register((model, modifierContext) -> {
            if (model != null && modifierContext.state().is(DNLBlocks.MENDING_AURA.get())) {
                return new MendingAuraFabricBakedModel(model);
            }
            return model;
        }));
    }

    private static boolean isMendingAuraModel(String path) {
        return path.equals("mending_aura") || path.startsWith("block/mending_aura_");
    }

    private void registerRenderers() {
        // Bosses
        EntityRendererRegistry.register(DNLEntityTypes.CHAOS_SPAWNER.get(), ChaosSpawnerRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FAIRKEEPER_BOROS.get(), FairkeeperBorosRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FAIRKEEPER_BOROS_PART.get(), FairkeeperBorosBodyRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FAIRKEEPER_OUROS.get(), FairkeeperOurosRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FAIRKEEPER_OUROS_PART.get(), FairkeeperOurosBodyRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FAIRKEEPER_SERPENT_CALLER.get(), FairkeeperSerpentCallerRenderer::new);

        // Monsters
        EntityRendererRegistry.register(DNLEntityTypes.HOLLOW.get(), HollowRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.SPAWNER_CARRIER.get(), SpawnerCarrierRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.SCUTTLE.get(), ScuttleRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.BALLISTA_GOLEM.get(), BallistaGolemRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.GARHOLD.get(), GarholdRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.BROKEN_GARHOLD.get(), BrokenGarholdRenderer::new);

        // Passive
        EntityRendererRegistry.register(DNLEntityTypes.SEALED_CHAOS.get(), SealedChaosRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.WHIMPER.get(), WhimperRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.COPPER_CREEP.get(), CopperCreepRenderer::new);


        // Projectiles
        EntityRendererRegistry.register(DNLEntityTypes.CHAOS_SPAWNER_PROJECTILE.get(), ChaosSpawnerProjectileRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FLAME_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.VERTEX_ARROW_PROJECTILE.get(), VertexArrowProjectileRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.VERTEX_PILLAR_PROJECTILE.get(), VertexPillarProjectileRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.BALLISTA_ARROW.get(), BallistaArrowRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.VERTEX_ORB_PROJECTILE.get(), VertexOrbProjectileRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.VERTEX_DOMAIN_PROJECTILE.get(), VertexDomainProjectileRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.BORUS_ARROW.get(), BorusArrowRenderer::new);

        // Misc
        EntityRendererRegistry.register(DNLEntityTypes.SPECIAL_ITEM_ENTITY.get(), SpecialItemEntityRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.GREAT_EXPERIENCE_BOTTLE.get(), (context) -> new ThrownItemRenderer<>(context, 1.25F, false));
        EntityRendererRegistry.register(DNLEntityTypes.REPULSOR.get(), RepulsorRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.SEEPING_SOUL.get(), SeepingSoulRenderer::new);
        // Block Entities
        LegacyBlockEntityRenderers.register(DNLBlockEntityTypes.FAIRKEEPER_CHEST.get(), FairkeeperChestBlockRenderer::new);
        LegacyBlockEntityRenderers.register(DNLBlockEntityTypes.DISABLED_FAIRKEEPER_CHEST.get(), DisabledFairkeeperChestBlockRenderer::new);
        LegacyBlockEntityRenderers.register(DNLBlockEntityTypes.PLAYER_STATUE.get(), PlayerStatueRenderer::new);
        LegacyBlockEntityRenderers.register(DNLBlockEntityTypes.DUNGEON_DIRECTOR.get(), DungeonDirectorRenderer::new);
        LegacyBlockEntityRenderers.register(DNLBlockEntityTypes.DUNGEON_BANNER.get(), DungeonBannerBlockRenderer::new);
        LegacyBlockEntityRenderers.register(DNLBlockEntityTypes.MENDING_AURA.get(), MendingAuraBlockEntityRenderer::new);

        // Item model properties (26.x: data-driven, see assets/dungeonnowloading/items)
        dev.hexnowloading.dungeonnowloading.client.item.DNLItemModelProperties.register();
    }

    private void registerModelLayers() {
        // Bosses
        ModelLayerRegistry.registerModelLayer(ChaosSpawnerModel.LAYER_LOCATION, ChaosSpawnerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(FairkeeperBorosModel.LAYER_LOCATION, FairkeeperBorosModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(FairkeeperBorosBodyModel.LAYER_LOCATION, FairkeeperBorosBodyModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(FairkeeperOurosModel.LAYER_LOCATION, FairkeeperOurosModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(FairkeeperOurosBodyModel.LAYER_LOCATION, FairkeeperOurosBodyModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(FairkeeperSerpentCallerModel.LAYER_LOCATION, FairkeeperSerpentCallerModel::createBodyLayer);

        // Monsters
        ModelLayerRegistry.registerModelLayer(HollowModel.LAYER_LOCATION, HollowModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(SpawnerCarrierModel.LAYER_LOCATION, SpawnerCarrierModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ScuttleModel.LAYER_LOCATION, ScuttleModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(BallistaGolemModel.LAYER_LOCATION, BallistaGolemModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(GarholdModel.LAYER_LOCATION, GarholdModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(BrokenGarholdModel.LAYER_LOCATION, BrokenGarholdModel::createBodyLayer);

        // Passive
        ModelLayerRegistry.registerModelLayer(SealedChaosModel.LAYER_LOCATION, SealedChaosModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(WhimperModel.LAYER_LOCATION, WhimperModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(CopperCreepModel.LAYER_LOCATION, CopperCreepModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(CopperCreepButlerModel.LAYER_LOCATION, CopperCreepButlerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(RepulsorModel.LAYER_LOCATION, RepulsorModel::createBodyLayer);

        //Projectiles
        ModelLayerRegistry.registerModelLayer(ChaosSpawnerProjectileModel.LAYER_LOCATION, ChaosSpawnerProjectileModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(VertexArrowProjectileModel.LAYER_LOCATION, VertexArrowProjectileModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(VertexPillarProjectileModel.LAYER_LOCATION, VertexPillarProjectileModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(BallistaArrowModel.LAYER_LOCATION, BallistaArrowModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(VertexOrbProjectileModel.LAYER_LOCATION, VertexOrbProjectileModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(VertexDomainProjectileModel.LAYER_LOCATION, VertexDomainProjectileModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(BorusArrowModel.LAYER_LOCATION, BorusArrowModel::createBodyLayer);
        // Block Entities
        ModelLayerRegistry.registerModelLayer(FairkeeperChestModel.LAYER_LOCATION, FairkeeperChestModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(DisabledFairkeeperChestModel.LAYER_LOCATION, DisabledFairkeeperChestModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PlayerStatueModel.LAYER_LOCATION, PlayerStatueModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PlayerStatuePedestalModel.LAYER_LOCATION, PlayerStatuePedestalModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(DungeonBannerBlockModel.LAYER_LOCATION, DungeonBannerBlockModel::createBodyLayer);

        // Misc
        ModelLayerRegistry.registerModelLayer(SeepingSoulChaosSpawnerModel.LAYER_LOCATION, SeepingSoulChaosSpawnerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(SeepingSoulSerpentCallerModel.LAYER_LOCATION, SeepingSoulSerpentCallerModel::createBodyLayer);
    }

    private static void registerParticleFactories() {
        ParticleProviderRegistry registry = ParticleProviderRegistry.getInstance();
        registry.register(DNLParticleTypes.LARGE_FLAME_PARTICLE.get(), LargeFlameParticle.Factory::new);
        registry.register(DNLParticleTypes.LARGE_SOUL_FLAME_PARTICLE.get(), LargeSoulFlameParticle.Factory::new);
        registry.register(DNLParticleTypes.SCORCHER_FLAME_PARTICLE.get(), ScorcherFlameParticle.Factory::new);
        registry.register(DNLParticleTypes.VERTEX_SPARK_PARTICLE.get(), VertexSparkParticle.Factory::new);
        registry.register(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), FairkeeperBoundaryParticle.Factory::new);
        registry.register(DNLParticleTypes.VERTEX_BOUNDARY_PARTICLE.get(), VertexBoundaryParticle.Factory::new);
        registry.register(DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(), RedstoneShockwaveParticle.Factory::new);
        registry.register(DNLParticleTypes.REDSTONE_HAZARD_INDICATOR_PARTICLE.get(), RedstoneHazardIndicatorParticle.Factory::new);
        registry.register(DNLParticleTypes.WHITE_SHOCKWAVE_PARTICLE.get(), WhiteShockwaveParticle.Factory::new);
        registry.register(DNLParticleTypes.WHITE_SHOCKWAVE_MEDIUM_PARTICLE.get(), WhiteShockwaveParticle.Factory::new);
        registry.register(DNLParticleTypes.ARROW_HAZARD_INDICATOR.get(), ArrowHazardIndicatorParticle.Factory::new);
        registry.register(DNLParticleTypes.MENDING_POP_AND_RUNE_PARTICLE.get(), MendingPopAndRuneParticle.Factory::new);
        registry.register(DNLParticleTypes.MENDING_RUNE_PARTICLE.get(), MendingRuneParticle.Factory::new);
        registry.register(DNLParticleTypes.MENDING_RUNE_SHORT_PARTICLE.get(), MendingRuneShortParticle.Factory::new);
        registry.register(DNLParticleTypes.MENDING_FADE_PARTICLE.get(), MendingFadeParticle.Factory::new);
        registry.register(DNLParticleTypes.MENDING_POP_PARTICLE.get(), MendingPopParticle.Factory::new);
        registry.register(DNLParticleTypes.SNIFFER_TRAIL_PARTICLE.get(), SnifferTrailParticle.Factory::new);
    }

    private static void addDnlEnchantmentDescriptions(ItemStack stack, List<Component> lines) {
        // 1.21: enchantments are holder-based via the ENCHANTMENTS component (not EnchantmentHelper.getEnchantments).
        for (var holder : stack.getEnchantments().keySet()) {
            var key = holder.unwrapKey().orElse(null);
            if (key != null && DungeonNowLoading.MOD_ID.equals(key.identifier().getNamespace())) {
                String descKey = "enchantment." + key.identifier().getNamespace() + "." + key.identifier().getPath() + ".desc";
                lines.add(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
