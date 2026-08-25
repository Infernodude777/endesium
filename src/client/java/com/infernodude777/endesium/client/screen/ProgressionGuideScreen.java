package com.infernodude777.endesium.client.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.infernodude777.endesium.menu.LoreBookMenu;

/**
 * The Progression Guide, Aether-style: place any item in the socket and the
 * book tells you everything the Endesium archives know about it - what it is,
 * how to get it, and what it is for. A button in the corner opens the written
 * field-guide pages for the full progression path.
 */
public class ProgressionGuideScreen extends AbstractContainerScreen<LoreBookMenu> {
    private static final int PANEL_WIDTH = 248;
    private static final int PANEL_HEIGHT = 186;
    private static final int COLOR_GOLD = 0xFFC9A227;
    private static final int COLOR_TEXT = 0xFFDEEED6;
    private static final int COLOR_DIM = 0xFF8A80B0;
    private static final int COLOR_PANEL = 0xF0141024;
    private static final int COLOR_PANEL_EDGE = 0xFF3A2E5C;
    private static final int COLOR_SOCKET = 0xFF241C3E;

    private String loreTitle = "Place an item in the socket";
    private final List<FormattedCharSequence> loreLines = new ArrayList<>();

    public ProgressionGuideScreen(LoreBookMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = PANEL_HEIGHT + 4;
        this.addRenderableWidget(Button.builder(Component.literal("Field Guide"), b -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new GuidePagesScreen());
            }
        }).bounds(this.leftPos + PANEL_WIDTH - 78, this.topPos + 5, 72, 14).build());
        refreshLore();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        refreshLore();
    }

    private void refreshLore() {
        ItemStack stack = this.menu.getSlot(0).getItem();
        if (stack.isEmpty()) {
            loreTitle = "Place an item in the socket";
            loreLines.clear();
            loreLines.addAll(this.font.split(Component.literal(
                    "The book is blank until you feed it something."), 160));
            return;
        }
        loreTitle = stack.getHoverName().getString();
        loreLines.clear();
        for (String line : EndesiumLore.forItem(stack.getItem())) {
            loreLines.addAll(this.font.split(Component.literal(line), PANEL_WIDTH - 84));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 6, COLOR_GOLD, false);
        // Item name over the lore panel.
        graphics.drawString(this.font, loreTitle, this.leftPos + 66, this.topPos + 22, COLOR_GOLD, false);
        int y = this.topPos + 34;
        for (FormattedCharSequence line : loreLines) {
            graphics.drawString(this.font, line, this.leftPos + 66, y, COLOR_TEXT, false);
            y += 10;
            if (y > this.topPos + PANEL_HEIGHT - 12) {
                break;
            }
        }
        if (this.menu.getSlot(0).getItem().isEmpty()) {
            graphics.drawString(this.font, "socket", this.leftPos + 26, this.topPos + 52, COLOR_DIM, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Panel body and edge.
        graphics.fill(this.leftPos, this.topPos,
                this.leftPos + PANEL_WIDTH, this.topPos + PANEL_HEIGHT, COLOR_PANEL);
        graphics.fill(this.leftPos, this.topPos,
                this.leftPos + PANEL_WIDTH, this.topPos + 1, COLOR_PANEL_EDGE);
        graphics.fill(this.leftPos, this.topPos + PANEL_HEIGHT - 1,
                this.leftPos + PANEL_WIDTH, this.topPos + PANEL_HEIGHT, COLOR_PANEL_EDGE);
        graphics.fill(this.leftPos, this.topPos,
                this.leftPos + 1, this.topPos + PANEL_HEIGHT, COLOR_PANEL_EDGE);
        graphics.fill(this.leftPos + PANEL_WIDTH - 1, this.topPos,
                this.leftPos + PANEL_WIDTH, this.topPos + PANEL_HEIGHT, COLOR_PANEL_EDGE);
        // The item socket: an inset square behind the slot.
        int sx = this.leftPos + 30 - 1;
        int sy = this.topPos + 34 - 1;
        graphics.fill(sx - 1, sy - 1, sx + 19, sy + 19, COLOR_SOCKET);
        // Divider between the socket page and the lore page.
        graphics.fill(this.leftPos + 60, this.topPos + 16,
                this.leftPos + 61, this.topPos + PANEL_HEIGHT - 10, COLOR_PANEL_EDGE);
    }
}
