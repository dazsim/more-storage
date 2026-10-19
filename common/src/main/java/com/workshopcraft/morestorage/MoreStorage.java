package com.workshopcraft.morestorage;

import net.minecraft.resources.Identifier;

public final class MoreStorage {
    public static final String MOD_ID = "morestorage";

    private MoreStorage() {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
