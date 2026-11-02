package com.workshopcraft.muchmorestorage.client;

import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.block.BokwurmBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import static net.minecraft.core.Direction.*;

// north facing BOKWURM and LADSTAPEL geometry, open to z 0, x 0 is the right as you face it
public final class FurnitureModel {
    private static final int W = 0xFFFFFFFF;
    private static final int SHADE = 0xFFB8B8B8;
    private static final int DARK = 0xFF9C9C9C;
    // book covers, no blues or yellows
    private static final int[] COVERS = {0xFF8E2A2A, 0xFF3F6B35, 0xFF6B4423, 0xFF5A1F2B, 0xFF2E4D2A, 0xFFA08060,
            0xFF2B2B2B, 0xFF6E6E6E, 0xFF9A4E22, 0xFFB03A2E, 0xFF7A5A48, 0xFF4F3A2A};
    public static final int ENCHANTED = 0xFF6A3A8A;
    public static final int WRITTEN = 0xFF4A3426;
    public static final int GUIDE = 0xFFEAE6DC;
    public static final float BOOK_W = 14F / 6F;
    // how far an open LADSTAPEL drawer slides out, in pixels
    public static final float DRAWER_OUT = 9F;
    private static final float GAP = 0.125F;

    private FurnitureModel() {}

    public static TextureAtlasSprite book() { return CupboardModel.sprite(MuchMoreStorage.id("block/bokwurm_book")); }

    public static int cover(int seed) { return COVERS[Math.floorMod(seed, COVERS.length)]; }

    // what each BOKWURM slot looks like, a cover colour and a height, 0 for an empty slot
    public static void fillBooks(BokwurmBlockEntity be, int[] colours, float[] heights) {
        int seed = (int) be.getBlockPos().asLong();
        for (int i = 0; i < colours.length; i++) {
            ItemStack stack = be.getItem(i);
            if (stack.isEmpty()) {
                heights[i] = 0;
                continue;
            }
            var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            int h = seed * 31 + i * 7 + id.hashCode();
            heights[i] = height(h);
            if (stack.is(Items.ENCHANTED_BOOK)) colours[i] = ENCHANTED;
            else if (stack.is(Items.WRITTEN_BOOK) || stack.is(Items.WRITABLE_BOOK)) colours[i] = WRITTEN;
            else if (id.getNamespace().equals("simplebooks")) colours[i] = GUIDE;
            else colours[i] = cover(h);
        }
    }

    public static float height(int seed) { return 4.6F + Math.floorMod(seed >> 3, 4) * 0.4F; }

    // sides, top, bottom and back shared by both
    private static void carcass(Quads q, CupboardModel.Body b) {
        TextureAtlasSprite s = b.side(), in = b.inside();
        box(q, 0, 0, 0, 1, 16, 16, s, W, NORTH, SOUTH, WEST, UP, DOWN);
        box(q, 15, 0, 0, 16, 16, 16, s, W, NORTH, SOUTH, EAST, UP, DOWN);
        box(q, 0, 1, 0, 1, 15, 15, in, SHADE, EAST);
        box(q, 15, 1, 0, 16, 15, 15, in, SHADE, WEST);
        box(q, 1, 0, 0, 15, 1, 16, s, W, NORTH, SOUTH, DOWN);
        box(q, 1, 0, 0, 15, 1, 15, in, SHADE, UP);
        box(q, 1, 15, 0, 15, 16, 16, s, W, NORTH, SOUTH, UP);
        box(q, 1, 15, 0, 15, 16, 15, in, DARK, DOWN);
        box(q, 1, 1, 15, 15, 15, 16, in, SHADE, NORTH);
        box(q, 1, 1, 15, 15, 15, 16, s, W, SOUTH);
    }

    public static void bokwurm(Quads q, CupboardModel.Body b) {
        carcass(q, b);
        box(q, 1, 7.5F, 0, 15, 8.5F, 15, b.side(), W, NORTH);
        box(q, 1, 7.5F, 0, 15, 8.5F, 15, b.inside(), SHADE, UP);
        box(q, 1, 7.5F, 0, 15, 8.5F, 15, b.inside(), DARK, DOWN);
    }

    // one book stood on its shelf, slot 0 is the top shelf at the viewer's left
    public static void book(Quads q, TextureAtlasSprite sp, int slot, int colour, float h) {
        int row = slot / 6, col = slot % 6;
        float x1 = 15 - col * BOOK_W - 0.15F, x0 = 15 - (col + 1) * BOOK_W + 0.15F;
        float y0 = row == 0 ? 8.5F : 1, y1 = y0 + h, z0 = 2.5F, z1 = 14.8F;
        q.face(NORTH, x0, y0, z0, x1, y1, z1, sp, 0, 0, 16, 16, colour);
        box(q, x0, y0, z0, x1, y1, z1, sp, darker(colour), UP, WEST, EAST);
    }

    private static int darker(int c) {
        int r = (c >> 16 & 255) * 3 / 4, g = (c >> 8 & 255) * 3 / 4, bl = (c & 255) * 3 / 4;
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }

    // the drawers split the inside, 1 to 15 pixels, top drawer first
    public static float drawerBottom(int d) { return 1 + (2 - d) * 14F / 3F; }

    public static float drawerFloor(int d) { return drawerBottom(d) + GAP + 0.6F; }

    public static void ladstapel(Quads q, CupboardModel.Body b) {
        carcass(q, b);
        // a dark gap behind the drawer fronts
        box(q, 1, 1, 13.5F, 15, 15, 15, b.inside(), DARK, NORTH);
    }

    // the item form, a few books already on the shelves
    public static void bokwurmItem(Quads q, CupboardModel.Body b) {
        bokwurm(q, b);
        TextureAtlasSprite sp = book();
        int[] slots = {0, 1, 2, 4, 6, 7, 9, 10, 11};
        for (int i = 0; i < slots.length; i++) book(q, sp, slots[i], i == 3 ? ENCHANTED : cover(i * 5 + 1), height(i * 13));
    }

    public static void ladstapelItem(Quads q, CupboardModel.Body b) {
        ladstapel(q, b);
        for (int d = 0; d < 3; d++) drawer(q, b, d);
    }

    public static void drawer(Quads q, CupboardModel.Body b, int d) {
        float x0 = 1 + GAP, x1 = 15 - GAP, y0 = drawerBottom(d) + GAP, y1 = drawerBottom(d) + 14F / 3F - GAP;
        TextureAtlasSprite s = b.side(), in = b.inside();
        q.face(NORTH, x0, y0, 0, x1, y1, 1, b.door(), 0, 0, 16, 16);
        box(q, x0, y0, 0, x1, y1, 1, s, W, UP, DOWN, WEST, EAST);
        box(q, x0, y0, 0, x1, y1, 1, in, SHADE, SOUTH);
        float top = y1 - 1;
        box(q, x0 + 0.5F, y0, 1, x0 + 1.25F, top, 13.25F, s, W, UP, WEST, EAST, SOUTH);
        box(q, x1 - 1.25F, y0, 1, x1 - 0.5F, top, 13.25F, s, W, UP, WEST, EAST, SOUTH);
        box(q, x0 + 1.25F, y0, 12.5F, x1 - 1.25F, top, 13.25F, s, W, UP, NORTH, SOUTH);
        box(q, x0 + 1.25F, y0, 1, x1 - 1.25F, y0 + 0.6F, 12.5F, in, SHADE, UP);
        box(q, x0 + 1.25F, y0, 1, x1 - 1.25F, y0 + 0.6F, 12.5F, s, DARK, DOWN);
        float hy = (y0 + y1) / 2 - 0.5F;
        for (Direction dir : Direction.values()) {
            if (dir != SOUTH) q.face(dir, 5.5F, hy, -0.9F, 10.5F, hy + 1, 0, b.handle(), 7, 5, 8, 10);
        }
    }

    // the KLADKAOS wardrobe, two blocks tall, its doors are drawn on their own
    public static void kladkaos(Quads q, CupboardModel.Body b) {
        TextureAtlasSprite s = b.side(), in = b.inside();
        tall(q, 0, 0, 0, 1, 32, 16, s, W, NORTH, SOUTH, WEST, UP, DOWN);
        tall(q, 15, 0, 0, 16, 32, 16, s, W, NORTH, SOUTH, EAST, UP, DOWN);
        tall(q, 0, 1, 0, 1, 31, 15, in, SHADE, EAST);
        tall(q, 15, 1, 0, 16, 31, 15, in, SHADE, WEST);
        box(q, 1, 0, 0, 15, 1, 16, s, W, NORTH, SOUTH, DOWN);
        box(q, 1, 0, 0, 15, 1, 15, in, SHADE, UP);
        tall(q, 1, 31, 0, 15, 32, 16, s, W, NORTH, SOUTH, UP);
        tall(q, 1, 31, 0, 15, 32, 15, in, DARK, DOWN);
        tall(q, 1, 1, 15, 15, 31, 16, in, SHADE, NORTH);
        tall(q, 1, 1, 15, 15, 31, 16, s, W, SOUTH);
        // a shoe shelf and the hanging rail
        box(q, 1, 6, 1, 15, 7, 15, s, W, NORTH);
        box(q, 1, 6, 1, 15, 7, 15, in, SHADE, UP);
        box(q, 1, 6, 1, 15, 7, 15, in, DARK, DOWN);
        for (Direction dir : new Direction[]{NORTH, SOUTH, UP, DOWN}) q.face(dir, 1, 27, 7.5F, 15, 28, 8.5F, b.handle(), 7, 5, 8, 10);
    }

    // hinge x for each door, the right one swings on x 1 and the left on x 15
    public static float kladkaosHinge(boolean left) { return left ? 15 : 1; }

    public static void kladkaosDoor(Quads q, CupboardModel.Body b, boolean left) {
        float x0 = left ? 8 + GAP / 2 : 1, x1 = left ? 15 : 8 - GAP / 2;
        tall(q, x0, 1, 0, x1, 31, 1, b.door(), W, NORTH);
        tall(q, x0, 1, 0, x1, 31, 1, b.inside(), SHADE, SOUTH);
        tall(q, x0, 1, 0, x1, 31, 1, b.side(), W, WEST, EAST, UP, DOWN);
        float hx = left ? x0 + 0.8F : x1 - 1.8F;
        for (Direction dir : Direction.values()) {
            if (dir != SOUTH) q.face(dir, hx, 13, -0.9F, hx + 1, 19, 0, b.handle(), 7, 5, 8, 10);
        }
    }

    // the item form, scaled to one block by the item renderer
    public static void kladkaosItem(Quads q, CupboardModel.Body b) {
        kladkaos(q, b);
        kladkaosDoor(q, b, false);
        kladkaosDoor(q, b, true);
    }

    // one KLADKAOS panel, a thin board
    public static void panel(Quads q, CupboardModel.Body b) {
        box(q, 1, 0, 2, 15, 1.5F, 14, b.door(), W, UP);
        box(q, 1, 0, 2, 15, 1.5F, 14, b.side(), W, NORTH, SOUTH, WEST, EAST, DOWN);
        for (Direction dir : new Direction[]{UP, NORTH, SOUTH, WEST, EAST}) q.face(dir, 3, 1.5F, 4, 9, 2, 5, b.handle(), 7, 5, 8, 10);
    }

    // TRULLBORG, a small chest on four wheels with the handle on the south side, lift raises the lid a little
    public static void trullborg(Quads q, CupboardModel.Body b, float lift) {
        TextureAtlasSprite s = b.side(), d = b.door(), h = b.handle();
        for (float[] w : new float[][]{{2, 3}, {2, 12}, {13, 3}, {13, 12}}) {
            for (Direction dir : Direction.values()) q.face(dir, w[0], 0, w[1], w[0] + 1, 3, w[1] + 1, h, 7, 5, 8, 10);
        }
        box(q, 1.5F, 3, 1.5F, 14.5F, 4, 14.5F, s, DARK, Direction.values());
        box(q, 2, 4, 2, 14, 10, 14, d, W, NORTH, SOUTH);
        box(q, 2, 4, 2, 14, 10, 14, s, SHADE, WEST, EAST);
        box(q, 1.5F, 10 + lift, 1.5F, 14.5F, 12 + lift, 14.5F, s, W, Direction.values());
        for (Direction dir : new Direction[]{NORTH, SOUTH, WEST, EAST, UP}) q.face(dir, 7, 12 + lift, 7.5F, 9, 12.5F + lift, 8.5F, h, 7, 5, 8, 10);
        for (float x : new float[]{3, 12}) {
            for (Direction dir : Direction.values()) q.face(dir, x, 5, 14, x + 1, 16, 15, h, 7, 5, 8, 10);
        }
        for (Direction dir : Direction.values()) q.face(dir, 4, 15, 14, 12, 16, 15, h, 7, 5, 8, 10);
    }

    // a box that may run above one block, the upper part maps the texture again
    private static void tall(Quads q, float x0, float y0, float z0, float x1, float y1, float z1, TextureAtlasSprite sp, int colour, Direction... faces) {
        for (Direction d : faces) {
            if (d.getAxis() == Direction.Axis.Y || y1 <= 16) {
                box(q, x0, y0, z0, x1, y1, z1, sp, colour, d);
                continue;
            }
            if (y0 < 16) box(q, x0, y0, z0, x1, 16, z1, sp, colour, d);
            float lo = Math.max(y0, 16);
            float u0 = switch (d) { case NORTH -> 16 - x1; case SOUTH -> x0; case WEST -> z0; default -> 16 - z1; };
            float u1 = switch (d) { case NORTH -> 16 - x0; case SOUTH -> x1; case WEST -> z1; default -> 16 - z0; };
            q.face(d, x0, lo, z0, x1, y1, z1, sp, u0, 32 - y1, u1, 32 - lo, colour);
        }
    }

    private static void box(Quads q, float x0, float y0, float z0, float x1, float y1, float z1, TextureAtlasSprite sp, int colour, Direction... faces) {
        KubhalaModel.box(q, x0, y0, z0, x1, y1, z1, sp, colour, faces);
    }
}
