package com.infernodude777.endesium.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

/**
 * The written reference pages of the Progression Guide (opened from the lore book). Slate panel, gold trim, category tabs across
 * the top, and pages rendered verbatim from {@link ProgressionGuideContent}.
 */
public class GuidePagesScreen extends Screen {
    private static final int PANEL_WIDTH = 340;
    private static final int PANEL_HEIGHT = 216;
    private static final int MARGIN = 14;
    private static final int TEXT_LEFT = MARGIN + 6;
    private static final int TEXT_TOP = 56;
    private static final int TEXT_WIDTH = PANEL_WIDTH - MARGIN * 2 - 8;
    private static final int LINE_HEIGHT = 10;
    private static final int COLOR_GOLD = 0xFFC9A227;
    private static final int COLOR_CYAN = 0xFF6DC2CA;
    private static final int COLOR_TEXT = 0xFFDEEED6;
    private static final int COLOR_DIM = 0xFF8A80B0;

    private record Tab(String label, int x, int width) {
    }

    private final List<Tab> tabs = new ArrayList<>();
    private List<ProgressionGuideContent.Entry> visible = List.of();
    private int tab;
    private int page;

    public GuidePagesScreen() {
        super(Component.literal("Endesium Progression Guide"));
    }

    @Override
    protected void init() {
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;
        int buttonY = panelTop + PANEL_HEIGHT - 26;
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> turnPage(-1))
                .bounds(panelLeft + 8, buttonY, 20, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> turnPage(1))
                .bounds(panelLeft + PANEL_WIDTH - 28, buttonY, 20, 18).build());
        refresh();
    }

    private void refresh() {
        String cat = ProgressionGuideContent.CATEGORIES.get(this.tab);
        this.visible = ProgressionGuideContent.PAGES.stream()
                .filter(entry -> entry.category().equals(cat))
                .toList();
        if (this.page >= this.visible.size()) {
            this.page = 0;
        }
    }

    private void turnPage(int dir) {
        if (this.visible.isEmpty()) {
            return;
        }
        this.page = Math.floorMod(this.page + dir, this.visible.size());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        graphics.fill(left - 3, top - 3, left + PANEL_WIDTH + 3, top + PANEL_HEIGHT + 3, 0xCC000000);
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF14161C);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 2, COLOR_GOLD);
        graphics.fill(left, top + PANEL_HEIGHT - 2, left + PANEL_WIDTH, top + PANEL_HEIGHT, COLOR_GOLD);

        // Category tabs along the top edge.
        this.tabs.clear();
        int tx = left + MARGIN;
        String active = ProgressionGuideContent.CATEGORIES.get(this.tab);
        for (String cat : ProgressionGuideContent.CATEGORIES) {
            int w = this.font.width(cat) + 10;
            graphics.fill(tx, top + 8, tx + w, top + 20, cat.equals(active) ? 0xFF2A2440 : 0xFF1D1A28);
            graphics.drawString(this.font, cat, tx + 5, top + 11, cat.equals(active) ? COLOR_CYAN : COLOR_DIM);
            this.tabs.add(new Tab(cat, tx, w));
            tx += w + 2;
        }

        if (!this.visible.isEmpty()) {
            ProgressionGuideContent.Entry entry = this.visible.get(this.page);
            graphics.drawCenteredString(this.font, entry.title(), this.width / 2, top + 32, COLOR_GOLD);

            int y = top + TEXT_TOP;
            for (String raw : entry.body()) {
                for (FormattedCharSequence line : this.font.split(Component.literal(raw), TEXT_WIDTH)) {
                    graphics.drawString(this.font, line, left + TEXT_LEFT, y, COLOR_TEXT);
                    y += LINE_HEIGHT;
                }
                y += 2;
            }

            String counter = (this.page + 1) + " / " + this.visible.size();
            graphics.drawCenteredString(this.font, counter, this.width / 2, top + PANEL_HEIGHT - 22, COLOR_DIM);
        }
        RenderSystem.enableDepthTest();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int top = (this.height - PANEL_HEIGHT) / 2;
        if (button == 0 && mouseY >= top + 8 && mouseY <= top + 20) {
            for (int i = 0; i < this.tabs.size(); i++) {
                Tab t = this.tabs.get(i);
                if (mouseX >= t.x() && mouseX <= t.x() + t.width()) {
                    this.tab = i;
                    this.page = 0;
                    refresh();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            turnPage(-1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            turnPage(1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
