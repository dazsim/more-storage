package com.workshopcraft.morestorage.registry;

import net.minecraft.world.level.material.MapColor;

// keep in sync with FINISHES in tools/gen_resources.py
public enum Finish {
    WHITE("white", "white", true, MapColor.SNOW),
    OAK("oak", "oak", false, MapColor.WOOD),
    SPRUCE("spruce", "spruce", false, MapColor.PODZOL),
    DARK_OAK("dark_oak", "dark_oak", false, MapColor.COLOR_BROWN),
    STONE_TOP_OAK("stone_top_oak", "oak", true, MapColor.WOOD),
    STONE_TOP_SPRUCE("stone_top_spruce", "spruce", true, MapColor.PODZOL),
    STONE_TOP_DARK_OAK("stone_top_dark_oak", "dark_oak", true, MapColor.COLOR_BROWN);

    public final String name;
    public final String panels;
    public final boolean stoneTop;
    public final MapColor mapColor;

    Finish(String name, String panels, boolean stoneTop, MapColor mapColor) {
        this.name = name;
        this.panels = panels;
        this.stoneTop = stoneTop;
        this.mapColor = mapColor;
    }

    public String blockId() {
        return name + "_cupboard";
    }
}
