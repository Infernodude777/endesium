package com.infernodude777.endesium.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

/**
 * The Endesium Guidebook screen. A dark, slate-toned book panel with gold and
 * cyan accents, mirroring the mod's visual language. Pages are drawn from
 * {@link EndesiumGuidebookContent}; the body text is wrapped to fit the page.
 *
 * <p>Category tabs are rendered along the top edge of the book panel. Each tab
 * shows a short label (max 4 characters). Clicking a tab jumps to the first
 * page of that category. Within a category, the left/right buttons and arrow
 * keys advance through the category's pages only, wrapping at the boundaries.
 *
 * <p>Rendering uses {@link RenderSystem} to disable depth-test and enable
 * standard 2D blending so that every fill() and drawString() call lands on
 * crisp integer-pixel boundaries. The background is drawn as a solid black
 * rectangle rather than calling the default blurred vignette.
 */
public class EndesiumGuidebookScreen extends Screen {
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 200;
    private static final int MARGIN = 14;
    private static final int TEXT_LEFT = MARGIN + 4;
    private static final int TEXT_TOP = 34;
    private static final int TEXT_WIDTH = PANEL_WIDTH - MARGIN * 2 - 8;
    private static final int TEXT_HEIGHT = 120;
    private static final int LINE_HEIGHT = 11;
    private static final int TAB_HEIGHT = 14;
    private static final int TAB_GAP = 2;

    private int pageIndex;
    private int activeCategory;
    private Button prevButton;
    private Button nextButton;

    public EndesiumGuidebookScreen() {
        super(Component.literal("Endesium Guidebook"));
    }

    @Override
    protected void init() {
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;
        int buttonY = panelTop + PANEL_HEIGHT - 24;
        this.prevButton = Button.builder(Component.literal("\u25C0"), button -> turnPage(-1))
                .bounds(panelLeft + 8, buttonY, 20, 18).build();
        this.nextButton = Button.builder(Component.literal("\u25B6"), button -> turnPage(1))
                .bounds(panelLeft + PANEL_WIDTH - 28, buttonY, 20, 18).build();
        this.addRenderableWidget(this.prevButton);
        this.addRenderableWidget(this.nextButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Solid black background -- no blurred vignette
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);

        // Set up 2D rendering state for crisp pixel art
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;

        // Soft outer shadow
        graphics.fill(panelLeft - 3, panelTop - 3, panelLeft + PANEL_WIDTH + 3, panelTop + PANEL_HEIGHT + 3, 0xCC000000);
        // Book panel background (dark slate)
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFF14161C);
        // Gold border
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + 2, 0xFFC9A227);
        graphics.fill(panelLeft, panelTop + PANEL_HEIGHT - 2, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFFC9A227);
        graphics.fill(panelLeft, panelTop, panelLeft + 2, panelTop + PANEL_HEIGHT, 0xFFC9A227);
        graphics.fill(panelLeft + PANEL_WIDTH - 2, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFFC9A227);

        // -- Category tabs ------------------------------------------------
        List<EndesiumGuidebookContent.Category> cats = EndesiumGuidebookContent.CATEGORIES;
        int tabWidth = (PANEL_WIDTH - MARGIN * 2) / cats.size();
        for (int i = 0; i < cats.size(); i++) {
            EndesiumGuidebookContent.Category cat = cats.get(i);
            int tx = panelLeft + MARGIN + i * tabWidth;
            int ty = panelTop + 3;

            boolean active = (i == this.activeCategory);
            int bgColor = active ? 0xFF1E2130 : 0xFF0C0E14;
            graphics.fill(tx, ty, tx + tabWidth - TAB_GAP, ty + TAB_HEIGHT, bgColor);

            // Active tab gold underline
            if (active) {
                graphics.fill(tx + 1, ty + TAB_HEIGHT - 2, tx + tabWidth - TAB_GAP - 1, ty + TAB_HEIGHT, 0xFFC9A227);
            }

            // Tab label text (short, fits in the narrow tab)
            String label = cat.tabLabel();
            int textW = this.font.width(label);
            int textX = tx + Math.max(1, (tabWidth - TAB_GAP - textW) / 2);
            int textColor = active ? 0xFFE8C96A : 0xFF6A6F78;
            graphics.drawString(this.font, label, textX, ty + 3, textColor, true);
        }

        // Cyan accent line under tabs
        int contentTop = panelTop + 3 + TAB_HEIGHT + 4;
        graphics.fill(panelLeft + MARGIN, contentTop - 2, panelLeft + PANEL_WIDTH - MARGIN, contentTop - 1, 0xFF7EA7A6);

        // Inner page area
        int pageBottom = panelTop + PANEL_HEIGHT - 24;
        graphics.fill(panelLeft + MARGIN, contentTop, panelLeft + PANEL_WIDTH - MARGIN, pageBottom, 0xFF1A1D25);

        // -- Page content -------------------------------------------------
        EndesiumGuidebookContent.Page page = EndesiumGuidebookContent.PAGES.get(this.pageIndex);
        EndesiumGuidebookContent.Category cat = cats.get(this.activeCategory);

        // Category display name (top-right of content area)
        String catName = cat.displayName();
        graphics.drawString(this.font, catName,
                panelLeft + PANEL_WIDTH - MARGIN - 4 - this.font.width(catName),
                contentTop + 2, 0xFF5A6068, true);

        // Title in gold, with shadow for crispness
        graphics.drawString(this.font, page.title(), panelLeft + TEXT_LEFT, contentTop + 2, 0xFFE8C96A, true);

        // Body text, wrapped to fit the page, in a soft light gray
        List<FormattedCharSequence> lines = this.font.split(Component.literal(page.body()), TEXT_WIDTH);
        int y = contentTop + TEXT_TOP - 10;
        for (FormattedCharSequence line : lines) {
            if (y > contentTop + TEXT_TOP + TEXT_HEIGHT) {
                break;
            }
            graphics.drawString(this.font, line, panelLeft + TEXT_LEFT, y, 0xFFC9CDD4, true);
            y += LINE_HEIGHT;
        }

        // Page indicator (within category)
        int localPage = this.pageIndex - cat.firstPage() + 1;
        int localTotal = cat.lastPage() - cat.firstPage() + 1;
        String indicator = localPage + " / " + localTotal;
        graphics.drawString(this.font, indicator,
                panelLeft + PANEL_WIDTH / 2 - this.font.width(indicator) / 2,
                panelTop + PANEL_HEIGHT - 20, 0xFF8A8F98, true);

        // Render navigation buttons on top of the panel
        this.prevButton.render(graphics, mouseX, mouseY, partialTick);
        this.nextButton.render(graphics, mouseX, mouseY, partialTick);

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelTop = (this.height - PANEL_HEIGHT) / 2;

        // Check tab clicks
        List<EndesiumGuidebookContent.Category> cats = EndesiumGuidebookContent.CATEGORIES;
        int tabWidth = (PANEL_WIDTH - MARGIN * 2) / cats.size();
        int tabTop = panelTop + 3;
        if (mouseY >= tabTop && mouseY <= tabTop + TAB_HEIGHT) {
            for (int i = 0; i < cats.size(); i++) {
                int tx = panelLeft + MARGIN + i * tabWidth;
                if (mouseX >= tx && mouseX <= tx + tabWidth - TAB_GAP) {
                    this.activeCategory = i;
                    this.pageIndex = cats.get(i).firstPage();
                    return true;
                }
            }
        }

        // Page click navigation (within content area)
        boolean inside = mouseX >= panelLeft && mouseX <= panelLeft + PANEL_WIDTH
                && mouseY >= panelTop + TAB_HEIGHT && mouseY <= panelTop + PANEL_HEIGHT;
        if (inside) {
            if (button == 0) {
                turnPage(1);
                return true;
            } else if (button == 1) {
                turnPage(-1);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            turnPage(1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            turnPage(-1);
            return true;
        }
        // Tab switching with number keys 1-0
        List<EndesiumGuidebookContent.Category> cats = EndesiumGuidebookContent.CATEGORIES;
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_0 + 10) {
            int idx = keyCode - GLFW.GLFW_KEY_1;
            if (idx < cats.size()) {
                this.activeCategory = idx;
                this.pageIndex = cats.get(idx).firstPage();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void turnPage(int delta) {
        EndesiumGuidebookContent.Category cat = EndesiumGuidebookContent.CATEGORIES.get(this.activeCategory);
        int next = this.pageIndex + delta;
        if (next < cat.firstPage() || next > cat.lastPage()) {
            return;
        }
        this.pageIndex = next;
    }
}
