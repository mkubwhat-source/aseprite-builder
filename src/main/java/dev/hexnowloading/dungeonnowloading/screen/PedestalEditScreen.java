package dev.hexnowloading.dungeonnowloading.screen;

import dev.hexnowloading.dungeonnowloading.block.client.renderer.PlayerStatueRenderer;
import dev.hexnowloading.dungeonnowloading.network.packets.C2SPedestalUpdatePacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.List;

public class PedestalEditScreen extends Screen {
    private static final Identifier PREVIEW_TEX =
            Identifier.fromNamespaceAndPath("dungeonnowloading", "textures/block/player_statue_pedestal_side.png");

    private static final int MAX_CHARS = 16; // ⬅︎ hard username-length cap

    private final BlockPos pos;
    private final DyeColor color;
    private final boolean glowing;

    private String line = "";
    private TextFieldHelper field;
    private int frame;
    private int maxTextPixels; // will match renderer’s budget

    private boolean sent = false;


    public PedestalEditScreen(BlockPos pos, List<Component> lines, DyeColor color, boolean glowing) {
        super(Component.translatable("screen.dnl.pedestal_edit"));
        this.pos = pos.immutable();
        this.color = (color != null) ? color : DyeColor.BLACK;
        this.glowing = glowing;
        if (lines != null && !lines.isEmpty() && lines.get(0) != null) {
            this.line = lines.get(0).getString();
        }
    }

    @Override
    protected void init() {
        int btnW = 100, btnH = 20;
        int y = this.height / 4 + 144;

        // matches renderer’s width budget
        this.maxTextPixels = PlayerStatueRenderer.pedestalMaxTextPixels(this.font);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
            sendUpdate();
            this.minecraft.gui.setScreen(null);
        }).bounds(this.width / 2 - btnW - 4, y, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> {
            this.minecraft.gui.setScreen(null);
        }).bounds(this.width / 2 + 4, y, btnW, btnH).build());

        this.field = new TextFieldHelper(
                () -> this.line,
                this::setMessage,
                TextFieldHelper.createClipboardGetter(this.minecraft),
                TextFieldHelper.createClipboardSetter(this.minecraft),
                s -> s.length() <= MAX_CHARS // ⬅︎ hard char limit; scaling will handle width
        );
        this.field.setCursorToEnd();
    }

    private void sendUpdate() {
        if (sent) return;
        sent = true;
        System.out.println("[PedestalEditScreen] sending line=\"" + line + "\" to server @ " + pos);
        Services.NETWORK.sendToServer(new C2SPedestalUpdatePacket(
                pos,
                List.of(Component.literal(line)),
                color,
                glowing
        ));
    }

    @Override
    public void removed() {
        if (sent) return;
        sent = true;
        System.out.println("[PedestalEditScreen] sending line=\"" + line + "\" to server @ " + pos);
        Services.NETWORK.sendToServer(new C2SPedestalUpdatePacket(
                pos,
                java.util.List.of(Component.literal(line)), // 1 line
                color,
                glowing
        ));
    }

    @Override
    public void tick() { ++frame; }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isConfirmation()) {
            sendUpdate();
            this.minecraft.gui.setScreen(null);
            return true;
        }
        if (event.isEscape()) {
            sendUpdate();
            if (this.minecraft != null) this.minecraft.gui.setScreen(null);
            return true;
        }
        return field.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        field.charTyped(event);
        return true;
    }

    private void setMessage(String s) {
        // clamp to 16 chars (defensive, also handles pastes)
        if (s.length() > MAX_CHARS) s = s.substring(0, MAX_CHARS);
        this.line = s;

        // live preview (client-side BE only)
        var mc = this.minecraft;
        if (mc != null && mc.level != null) {
            var be = mc.level.getBlockEntity(this.pos);
            if (be instanceof dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity statue) {
                java.util.List<Component> lines = new java.util.ArrayList<>(4);
                lines.add(Component.literal(this.line));
                lines.add(Component.empty());
                lines.add(Component.empty());
                lines.add(Component.empty());
                statue.setAllText(lines, this.color, this.glowing);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.centeredText(this.font, this.title, this.width / 2, 40, 0xFFFFFFFF);

        g.pose().pushMatrix();
        {
            int cx = this.width / 2;
            int cy = 90;

            int texW = 16, texH = 16;
            int drawW = texW * 5;
            int drawH = texH * 5;
            g.blit(RenderPipelines.GUI_TEXTURED, PREVIEW_TEX, cx - drawW / 2, cy - drawH / 2, 0, 0, drawW, drawH, texW, texH, texW, texH);

            // text overlay – scale to fit maxTextPixels, center
            g.pose().pushMatrix();
            g.pose().translate(cx, cy);

            String toDraw = this.font.isBidirectional() ? this.font.bidirectionalShaping(line) : line;
            int rawW = this.font.width(toDraw);

            float fit = 1.0f;
            if (rawW > this.maxTextPixels) {
                fit = (float) this.maxTextPixels / (float) rawW;
            }
            g.pose().scale(fit, fit);

            int textColor = 0xFF000000 | this.color.getTextColor();
            boolean caretBlink = (frame / 6) % 2 == 0;
            int cursor = field.getCursorPos();
            int sel = field.getSelectionPos();

            int baseY = -this.font.lineHeight / 2;
            int x0 = -rawW / 2;

            g.text(this.font, toDraw, x0, baseY, textColor, false);

            if (caretBlink && cursor >= 0 && cursor == toDraw.length()) {
                int xCaret = this.font.width(toDraw) - rawW / 2;
                g.text(this.font, "_", xCaret, baseY, textColor, false);
            }

            if (cursor != sel) {
                int a = Math.min(cursor, sel);
                int b = Math.max(cursor, sel);
                int xA = this.font.width(toDraw.substring(0, a)) - rawW / 2;
                int xB = this.font.width(toDraw.substring(0, b)) - rawW / 2;
                int yTop = baseY - 1;
                int yBot = baseY + this.font.lineHeight;
                g.textHighlight(xA, yTop, xB, yBot, true);
            }

            g.pose().popMatrix();
        }
        g.pose().popMatrix();

        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        // Do NOT send here; let removed() handle it (like AbstractSignEditScreen)
        super.onClose();
    }

    private void onDone() {
        // Just close; removed() will send
        this.minecraft.gui.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
