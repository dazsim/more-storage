package com.workshopcraft.muchmorestorage.block;

import net.minecraft.util.StringRepresentable;

// what sits in a KUBHÅLA cube, box is kept for the SAKKSÄKK fabric box
public enum Insert implements StringRepresentable {
    NONE, DRAWER, DOOR, BOX;

    @Override
    public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }

    public boolean closes() { return this == DRAWER || this == DOOR; }
}
