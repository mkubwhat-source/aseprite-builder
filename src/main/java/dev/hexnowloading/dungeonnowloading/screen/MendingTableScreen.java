package dev.hexnowloading.dungeonnowloading.screen;

import dev.hexnowloading.dungeonnowloading.menu.MendingTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MendingTableScreen extends AbstractContainerScreen<MendingTableMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("dungeonnowloading", "textures/gui/mending_table_gui.png");
    private static final Identifier PICKAXE_OUTLINE = Identifier.fromNamespaceAndPath("dungeonnowloading", "textures/gui/pickaxe_outline_gui.png");
    private static final Identifier DURITE_OUTLINE = Identifier.fromNamespaceAndPath("dungeonnowloading", "textures/gui/durite_outline_gui.png");
    private static final Identifier DURITE_NOTCH = Identifier.fromNamespaceAndPath("dungeonnowloading", "textures/gui/mending_table_gui_durite_notch.png");
    private static final Identifier GOLD_NOTCH = Identifier.fromNamespaceAndPath("dungeonnowloading", "textures/gui/mending_table_gui_golden_notch.png");
    private static final int NOTCH_START_X = 113;
    private static final int NOTCH_ROW1_Y = 69;
    private static final int NOTCH_ROW2_Y = 75; // keep given y even though height is 3 (gap visually larger)
    private static final int NOTCH_SPACING = 2; // updated to 2 pixels between notches
    private static final int NOTCH_WIDTH = 6;  // updated per user
    private static final int NOTCH_HEIGHT = 3; // updated per user

    public MendingTableScreen(MendingTableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 175, 186);
        this.titleLabelX = 14;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        // Base background
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        if (!menu.getSlot(MendingTableMenu.PICKAXE_SLOT).hasItem()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, PICKAXE_OUTLINE, x + 30, y + 45, 0, 0, 16, 16, 16, 16);
        }
        if (!menu.getSlot(MendingTableMenu.DURITE_SLOT_1).hasItem()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, DURITE_OUTLINE, x + 56, y + 33, 0, 0, 16, 16, 16, 16);
        }
        if (!menu.getSlot(MendingTableMenu.DURITE_SLOT_2).hasItem()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, DURITE_OUTLINE, x + 56, y + 57, 0, 0, 16, 16, 16, 16);
        }
        renderNotches(graphics, x, y);
    }

    private void renderNotches(GuiGraphicsExtractor g, int guiLeft, int guiTop) {
        if (menu.slots.size() <= 0 || !menu.getSlot(0).hasItem()) return;
        int base = menu.getBasePercent();
        int bonus = menu.getBonusPercent();
        if (base <= 0 && bonus <= 0) return;
        // Each notch = 10% contribution of that category. Do not auto-fill when fully repaired.
        int baseNotches = Math.min(10, (base + 9) / 10); // ceil division
        int bonusNotches = Math.min(10 - baseNotches, (bonus + 9) / 10);
        for (int i = 0; i < baseNotches; i++) {
            int row = i / 5;
            int col = i % 5;
            int drawX = guiLeft + NOTCH_START_X + col * (NOTCH_WIDTH + NOTCH_SPACING);
            int drawY = guiTop + (row == 0 ? NOTCH_ROW1_Y : NOTCH_ROW2_Y);
            g.blit(RenderPipelines.GUI_TEXTURED, DURITE_NOTCH, drawX, drawY, 0, 0, NOTCH_WIDTH, NOTCH_HEIGHT, NOTCH_WIDTH, NOTCH_HEIGHT);
        }
        for (int i = 0; i < bonusNotches; i++) {
            int idx = baseNotches + i;
            if (idx >= 10) break;
            int row = idx / 5;
            int col = idx % 5;
            int drawX = guiLeft + NOTCH_START_X + col * (NOTCH_WIDTH + NOTCH_SPACING);
            int drawY = guiTop + (row == 0 ? NOTCH_ROW1_Y : NOTCH_ROW2_Y);
            g.blit(RenderPipelines.GUI_TEXTURED, GOLD_NOTCH, drawX, drawY, 0, 0, NOTCH_WIDTH, NOTCH_HEIGHT, NOTCH_WIDTH, NOTCH_HEIGHT);
        }
    }

}
