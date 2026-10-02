package dev.hexnowloading.dungeonnowloading.item;





import net.minecraft.server.level.ServerLevel;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item;
import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnerSword extends Item {

    public SpawnerSword(Properties properties, float attackSpeed) {
        // 26.x: swords are plain items configured through properties; repairs use the spawner blade or diamonds.
        super(properties.sword(ToolMaterial.DIAMOND, 3.0F, attackSpeed).repairable(DNLTags.REPAIRS_SPAWNER_WEAPONS));
    }

    @Override
    public void hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(itemStack, target, attacker);
        boolean result = true;
        if (result && !target.level().isClientSide()) {
            int recklessLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(attacker.level(), DNLEnchantments.RECKLESS), attacker.getMainHandItem());
            float selfDamage = 1.0F + recklessLevel;

            // Don't self-damage in creative/spectator.
            if (attacker instanceof Player player && (player.getAbilities().instabuild || player.isSpectator())) {
                return;
            }

            // If self damage would kill the attacker, do nothing (no self damage and no bonus damage).
            // This matches: "shouldn't kill the user, but also shouldn't apply the bonus damage".
            if (recklessLevel > 0 && attacker.getHealth() <= selfDamage) {
                return;
            }

            // Always apply: 1 + reckless level.
            attacker.hurtOrSimulate(attacker.damageSources().magic(), selfDamage);
        }
        return;
    }

    public static float soulDispersionEffect(LivingEntity attacker, LivingEntity target, float damage) {
        return attacker.getHealth() > 1 ? damage + 3.0F : damage;
    }

    public static float onLivingDamage(LivingEntity attacker, LivingEntity target, float damage) {
        int recklessLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(attacker.level(), DNLEnchantments.RECKLESS), attacker.getMainHandItem());
        if (recklessLevel > 0 && damage > 0.0F) {
            float selfDamage = 1.0F + recklessLevel;
            // If applying reckless self-damage would kill the attacker, don't grant the bonus damage.
            if (attacker.getHealth() <= selfDamage) {
                return damage;
            }
            // Increase outgoing damage: +2 * level
            damage += 2.0F * recklessLevel;
        }
        return damage;
    }


    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext level, TooltipDisplay tooltipDisplay, Consumer<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, tooltipDisplay, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.accept(Component.translatable("item.dungeonnowloading.spawner_sword.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
            components.accept(Component.translatable("item.dungeonnowloading.spawner_sword.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}