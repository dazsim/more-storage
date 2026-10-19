package com.workshopcraft.morestorage.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

// keep in sync with FINISHES in tools/gen_resources.py
public enum Finish {
    WHITE("white", MapColor.SNOW, SoundType.WOOD),
    OAK("oak", MapColor.WOOD, SoundType.WOOD),
    SPRUCE("spruce", MapColor.PODZOL, SoundType.WOOD),
    DARK_OAK("dark_oak", MapColor.COLOR_BROWN, SoundType.WOOD);

    public final String name;
    public final MapColor mapColor;
    public final SoundType sound;

    Finish(String name, MapColor mapColor, SoundType sound) {
        this.name = name;
        this.mapColor = mapColor;
        this.sound = sound;
    }

    public String blockId() {
        return name + "_cupboard";
    }
}
