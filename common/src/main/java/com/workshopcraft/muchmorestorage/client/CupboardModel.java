package com.workshopcraft.muchmorestorage.client;

import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.registry.Finishes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import static net.minecraft.core.Direction.*;

// north facing, shared by the block entity renderer and the item renderer
public final class CupboardModel {
    private static final int SHADE = 0xFFB8B8B8;

    private CupboardModel() {}

    public static TextureAtlasSprite sprite(Identifier id) {
        return Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, id));
    }

    public record Body(TextureAtlasSprite door, TextureAtlasSprite side, TextureAtlasSprite inside, TextureAtlasSprite handle) {
        public static Body of(String body) {
            return new Body(sprite(Finishes.bodySprite(body, "door")), sprite(Finishes.bodySprite(body, "side")),
                    sprite(Finishes.bodySprite(body, "inside")), sprite(MuchMoreStorage.id("block/cupboard_handle")));
        }
    }

    public static void carcass(Quads q, Body b) {
        TextureAtlasSprite s = b.side, in = b.inside;
        q.face(WEST, 0, 2, 2, 1, 14, 16, s, 2, 2, 16, 14);
        q.face(EAST, 0, 2, 2, 1, 14, 16, in, 2, 2, 16, 14, SHADE);
        q.face(NORTH, 0, 2, 2, 1, 14, 16, s, 0, 2, 1, 14);
        q.face(SOUTH, 0, 2, 2, 1, 14, 16, s, 15, 2, 16, 14);
        q.face(DOWN, 0, 2, 2, 1, 14, 16, s, 0, 0, 1, 14);
        q.face(UP, 0, 2, 2, 1, 14, 16, s, 0, 2, 1, 16);

        q.face(EAST, 15, 2, 2, 16, 14, 16, s, 0, 2, 14, 14);
        q.face(WEST, 15, 2, 2, 16, 14, 16, in, 0, 2, 14, 14, SHADE);
        q.face(NORTH, 15, 2, 2, 16, 14, 16, s, 15, 2, 16, 14);
        q.face(SOUTH, 15, 2, 2, 16, 14, 16, s, 0, 2, 1, 14);
        q.face(DOWN, 15, 2, 2, 16, 14, 16, s, 15, 0, 16, 14);
        q.face(UP, 15, 2, 2, 16, 14, 16, s, 15, 2, 16, 16);

        q.face(NORTH, 1, 2, 15, 15, 14, 16, in, 1, 2, 15, 14, SHADE);
        q.face(SOUTH, 1, 2, 15, 15, 14, 16, s, 1, 2, 15, 14);
        q.face(UP, 1, 2, 15, 15, 14, 16, s, 1, 15, 15, 16);

        q.face(UP, 1, 2, 2, 15, 3, 15, in, 1, 2, 15, 15, SHADE);
        q.face(NORTH, 1, 2, 2, 15, 3, 15, s, 1, 13, 15, 14);
        q.face(DOWN, 1, 2, 2, 15, 3, 15, s, 1, 1, 15, 14);

        q.face(UP, 1, 8, 3, 15, 9, 15, in, 1, 3, 15, 15, SHADE);
        q.face(DOWN, 1, 8, 3, 15, 9, 15, in, 1, 1, 15, 13, SHADE);
        q.face(NORTH, 1, 8, 3, 15, 9, 15, in, 1, 7, 15, 8, SHADE);

        q.face(NORTH, 0, 0, 3, 16, 2, 16, s, 0, 14, 16, 16);
        q.face(SOUTH, 0, 0, 3, 16, 2, 16, s, 0, 14, 16, 16);
        q.face(EAST, 0, 0, 3, 16, 2, 16, s, 0, 14, 13, 16);
        q.face(WEST, 0, 0, 3, 16, 2, 16, s, 3, 14, 16, 16);
        q.face(DOWN, 0, 0, 3, 16, 2, 16, s, 0, 0, 16, 13);
    }

    public static void door(Quads q, Body b, boolean rightHinge) {
        TextureAtlasSprite s = b.side;
        float hx = rightHinge ? 14F : 1F;
        q.face(NORTH, 0, 2, 1, 16, 14, 2, b.door, 0, 2, 16, 14);
        q.face(SOUTH, 0, 2, 1, 16, 14, 2, b.inside, 0, 2, 16, 14, SHADE);
        q.face(UP, 0, 2, 1, 16, 14, 2, s, 0, 0, 16, 1);
        q.face(DOWN, 0, 2, 1, 16, 14, 2, s, 0, 15, 16, 16);
        q.face(WEST, 0, 2, 1, 16, 14, 2, s, 0, 2, 1, 14);
        q.face(EAST, 0, 2, 1, 16, 14, 2, s, 15, 2, 16, 14);
        for (Direction dir : Direction.values()) {
            if (dir != SOUTH) q.face(dir, hx, 6, 0, hx + 1, 11, 1, b.handle, 7, 5, 8, 10);
        }
    }

    public static void worktop(Quads q, TextureAtlasSprite top) {
        q.face(UP, 0, 14, 0, 16, 16, 16, top, 0, 0, 16, 16);
        q.face(DOWN, 0, 14, 0, 16, 16, 16, top, 0, 0, 16, 16, SHADE);
        q.face(NORTH, 0, 14, 0, 16, 16, 16, top, 0, 0, 16, 2);
        q.face(SOUTH, 0, 14, 0, 16, 16, 16, top, 0, 14, 16, 16);
        q.face(EAST, 0, 14, 0, 16, 16, 16, top, 0, 0, 16, 2);
        q.face(WEST, 0, 14, 0, 16, 16, 16, top, 0, 0, 16, 2);
    }
}
