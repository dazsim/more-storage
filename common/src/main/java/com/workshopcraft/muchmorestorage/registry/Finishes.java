package com.workshopcraft.muchmorestorage.registry;

import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

// keep in sync with tools/gen_resources.py
public final class Finishes {
    public static final List<String> WOODS = List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped");
    public static final List<String> COLOURS = List.of("white", "orange", "magenta", "light_blue", "yellow", "lime",
            "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black");
    public static final List<String> STONES = List.of("smooth_stone", "granite", "polished_granite", "polished_diorite", "polished_andesite");
    public static final List<String> BODIES = new ArrayList<>();
    public static final List<String> TOPS = new ArrayList<>();

    static {
        BODIES.addAll(WOODS);
        COLOURS.forEach(c -> BODIES.add(c + "_concrete"));
        TOPS.addAll(WOODS);
        TOPS.addAll(STONES);
        COLOURS.forEach(c -> TOPS.add(c + "_concrete"));
    }

    private Finishes() {}

    public static boolean isWood(String body) {
        return WOODS.contains(body);
    }

    public static Identifier bodySprite(String body, String part) {
        return MuchMoreStorage.id("block/" + body + "_body_" + part);
    }

    public static Identifier topSprite(String top) {
        if (WOODS.contains(top) || top.equals("granite")) return MuchMoreStorage.id("block/" + top + "_worktop");
        return Identifier.withDefaultNamespace("block/" + top);
    }
}
