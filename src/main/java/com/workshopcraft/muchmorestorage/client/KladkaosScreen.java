package com.workshopcraft.muchmorestorage.client;

import com.workshopcraft.muchmorestorage.menu.KladkaosMenu;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else
/*import net.minecraft.client.gui.GuiGraphics;*/
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Inventory;

// the chest screen with the armour rail as a strip on the left
public class KladkaosScreen extends ContainerScreen {
    private static final int W = 30, H = 86;

    public KladkaosScreen(ChestMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    // the screen is a chest screen, so it registers against the menu type as a chest menu
    @SuppressWarnings("unchecked")
    public static MenuType<ChestMenu> type() { return (MenuType<ChestMenu>) (MenuType<?>) ModContent.KLADKAOS_MENU; }

    private int railLeft() { return leftPos + KladkaosMenu.RAIL_X - 7; }

    private int railTop() { return topPos + KladkaosMenu.RAIL_Y - 7; }

    //? if >=26.1 {
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
        rail(g);
        super.extractBackground(g, mx, my, pt);
    }

    private void rail(GuiGraphicsExtractor g) {
    //?} else {
    /*@Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        rail(g);
        super.renderBg(g, pt, mx, my);
    }

    private void rail(GuiGraphics g) {
    *///?}
        int x = railLeft(), y = railTop();
        g.fill(x + 1, y, x + W, y + H, 0xFF000000);
        g.fill(x, y + 1, x + W, y + H - 1, 0xFF000000);
        g.fill(x + 1, y + 1, x + W, y + H - 1, 0xFFC6C6C6);
        g.fill(x + 1, y + 1, x + W, y + 3, 0xFFFFFFFF);
        g.fill(x + 1, y + 1, x + 3, y + H - 3, 0xFFFFFFFF);
        g.fill(x + 3, y + H - 3, x + W, y + H - 1, 0xFF555555);
        for (int i = 0; i < 4; i++) {
            int sx = leftPos + KladkaosMenu.RAIL_X - 1, sy = topPos + KladkaosMenu.RAIL_Y - 1 + i * 18;
            g.fill(sx, sy, sx + 18, sy + 18, 0xFF373737);
            g.fill(sx + 1, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        }
    }

    private boolean onRail(double mx, double my) {
        return mx >= railLeft() && mx < railLeft() + W && my >= railTop() && my < railTop() + H;
    }

    //? if >=26.1 {
    @Override
    protected boolean hasClickedOutside(double mx, double my, int left, int top) {
        return super.hasClickedOutside(mx, my, left, top) && !onRail(mx, my);
    }
    //?} else {
    /*@Override
    protected boolean hasClickedOutside(double mx, double my, int left, int top, int button) {
        return super.hasClickedOutside(mx, my, left, top, button) && !onRail(mx, my);
    }
    *///?}
}
