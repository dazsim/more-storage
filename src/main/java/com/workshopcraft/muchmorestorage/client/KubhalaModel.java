package com.workshopcraft.muchmorestorage.client;

import com.workshopcraft.muchmorestorage.block.KubhalaBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.core.Direction.*;

// north facing KUBHÅLA geometry, x 0 is the cube's right as you face it
public final class KubhalaModel {
    private static final int SHADE = 0xFFB8B8B8;
    private static final int DARK = 0xFF9C9C9C;
    public static final float GAP = 0.25F;

    private KubhalaModel() {}

    // which sides join a neighbour, joined sides are 1px so two cubes make one 2px divider
    public record Join(boolean right, boolean left, boolean up, boolean down) {
        public static final Join NONE = new Join(false, false, false, false);

        public static Join of(BlockGetter level, BlockPos pos, BlockState state) {
            Direction f = state.getValue(KubhalaBlock.FACING);
            return new Join(same(level, pos.relative(f.getCounterClockWise()), f), same(level, pos.relative(f.getClockWise()), f),
                    same(level, pos.above(), f), same(level, pos.below(), f));
        }

        private static boolean same(BlockGetter level, BlockPos pos, Direction facing) {
            BlockState s = level.getBlockState(pos);
            return s.getBlock() instanceof KubhalaBlock && s.getValue(KubhalaBlock.ASSEMBLED) && s.getValue(KubhalaBlock.FACING) == facing;
        }

        public float x0() { return right ? 1 : 2; }
        public float x1() { return left ? 15 : 14; }
        public float y0() { return down ? 1 : 2; }
        public float y1() { return up ? 15 : 14; }
    }

    // uv follows the face position so strips of a panel line up
    static void box(Quads q, float x0, float y0, float z0, float x1, float y1, float z1, TextureAtlasSprite sp, int colour, Direction... faces) {
        for (Direction d : faces) {
            float u0, v0, u1, v1;
            switch (d) {
                case NORTH -> { u0 = 16 - x1; u1 = 16 - x0; v0 = 16 - y1; v1 = 16 - y0; }
                case SOUTH -> { u0 = x0; u1 = x1; v0 = 16 - y1; v1 = 16 - y0; }
                case WEST -> { u0 = z0; u1 = z1; v0 = 16 - y1; v1 = 16 - y0; }
                case EAST -> { u0 = 16 - z1; u1 = 16 - z0; v0 = 16 - y1; v1 = 16 - y0; }
                case UP -> { u0 = x0; u1 = x1; v0 = z0; v1 = z1; }
                default -> { u0 = x0; u1 = x1; v0 = 16 - z1; v1 = 16 - z0; }
            }
            q.face(d, x0, y0, z0, x1, y1, z1, sp, u0, v0, u1, v1, colour);
        }
    }

    public static void frame(Quads q, CupboardModel.Body b, Join j) {
        TextureAtlasSprite s = b.side(), in = b.inside();
        float x0 = j.x0(), x1 = j.x1(), y0 = j.y0(), y1 = j.y1();
        int w = 0xFFFFFFFF;
        // the two sides run the full height
        box(q, 0, 0, 0, x0, 16, 16, s, w, NORTH, SOUTH);
        box(q, x1, 0, 0, 16, 16, 16, s, w, NORTH, SOUTH);
        box(q, 0, y0, 0, x0, y1, 15, in, SHADE, EAST);
        box(q, x1, y0, 0, 16, y1, 15, in, SHADE, WEST);
        if (!j.right()) box(q, 0, 0, 0, x0, 16, 16, s, w, WEST);
        if (!j.left()) box(q, x1, 0, 0, 16, 16, 16, s, w, EAST);
        if (!j.up()) { box(q, 0, 0, 0, x0, 16, 16, s, w, UP); box(q, x1, 0, 0, 16, 16, 16, s, w, UP); }
        if (!j.down()) { box(q, 0, 0, 0, x0, 16, 16, s, w, DOWN); box(q, x1, 0, 0, 16, 16, 16, s, w, DOWN); }
        // bottom and top between them
        box(q, x0, 0, 0, x1, y0, 16, s, w, NORTH, SOUTH);
        box(q, x0, 0, 0, x1, y0, 15, in, SHADE, UP);
        if (!j.down()) box(q, x0, 0, 0, x1, y0, 16, s, w, DOWN);
        box(q, x0, y1, 0, x1, 16, 16, s, w, NORTH, SOUTH);
        box(q, x0, y1, 0, x1, 16, 15, in, DARK, DOWN);
        if (!j.up()) box(q, x0, y1, 0, x1, 16, 16, s, w, UP);
        // back panel
        box(q, x0, y0, 15, x1, y1, 16, in, SHADE, NORTH);
        box(q, x0, y0, 15, x1, y1, 16, s, w, SOUTH);
    }

    public static void drawer(Quads q, CupboardModel.Body b, Join j) {
        float x0 = j.x0() + GAP, x1 = j.x1() - GAP, y0 = j.y0() + GAP, y1 = j.y1() - GAP;
        TextureAtlasSprite s = b.side(), in = b.inside();
        q.face(NORTH, x0, y0, 0.1F, x1, y1, 1.1F, b.door(), 0, 0, 16, 16);
        box(q, x0, y0, 0.1F, x1, y1, 1.1F, s, 0xFFFFFFFF, UP, DOWN, WEST, EAST);
        box(q, x0, y0, 0.1F, x1, y1, 1.1F, in, SHADE, SOUTH);
        // the box behind the front, only seen when it slides out
        box(q, x0 + 0.5F, y0, 1.1F, x0 + 1.25F, y1 - 2, 14.5F, s, 0xFFFFFFFF, UP, WEST, EAST, SOUTH);
        box(q, x1 - 1.25F, y0, 1.1F, x1 - 0.5F, y1 - 2, 14.5F, s, 0xFFFFFFFF, UP, WEST, EAST, SOUTH);
        box(q, x0 + 1.25F, y0, 13.75F, x1 - 1.25F, y1 - 2, 14.5F, s, 0xFFFFFFFF, UP, NORTH, SOUTH);
        box(q, x0 + 1.25F, y0, 1.1F, x1 - 1.25F, y0 + 0.75F, 13.75F, in, SHADE, UP);
        float hy = y1 - 3.5F;
        for (Direction d : Direction.values()) {
            if (d != SOUTH) q.face(d, 5.5F, hy, -0.9F, 10.5F, hy + 1, 0.1F, b.handle(), 7, 5, 8, 10);
        }
    }

    public static void door(Quads q, CupboardModel.Body b, Join j) {
        float x0 = j.x0() + GAP, x1 = j.x1() - GAP, y0 = j.y0() + GAP, y1 = j.y1() - GAP;
        q.face(NORTH, x0, y0, 0.1F, x1, y1, 1.1F, b.door(), 0, 0, 16, 16);
        box(q, x0, y0, 0.1F, x1, y1, 1.1F, b.side(), 0xFFFFFFFF, UP, DOWN, WEST, EAST);
        box(q, x0, y0, 0.1F, x1, y1, 1.1F, b.inside(), SHADE, SOUTH);
        float hx = x0 + 1.25F, cy = (y0 + y1) / 2;
        for (Direction d : Direction.values()) {
            if (d != SOUTH) q.face(d, hx, cy - 2.5F, -0.9F, hx + 1, cy + 2.5F, 0.1F, b.handle(), 7, 5, 8, 10);
        }
    }

    // the hinge is on the side away from the handle
    public static float doorHinge(Join j) { return (j.x1() - GAP) / 16F; }

    // the boards of a flat pack, stacked a little untidily
    public static void flatpack(Quads q, CupboardModel.Body b) {
        TextureAtlasSprite s = b.side(), in = b.inside();
        Direction[] all = Direction.values();
        box(q, 1, 0, 2, 15, 1.5F, 14, s, 0xFFFFFFFF, all);
        box(q, 0.5F, 1.5F, 1.5F, 15, 3, 13.5F, s, 0xFFFFFFFF, all);
        box(q, 1.5F, 3, 2.5F, 15.5F, 4, 14.5F, in, 0xFFFFFFFF, all);
        box(q, 2, 4, 3, 14, 4.75F, 13, b.door(), 0xFFFFFFFF, UP);
        box(q, 2, 4, 3, 14, 4.75F, 13, s, 0xFFFFFFFF, NORTH, SOUTH, WEST, EAST);
    }
}
