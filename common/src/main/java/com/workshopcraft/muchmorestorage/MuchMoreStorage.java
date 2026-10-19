package com.workshopcraft.muchmorestorage;

import net.minecraft.resources.Identifier;

public final class MuchMoreStorage {
    public static final String MOD_ID = "muchmorestorage";

    private MuchMoreStorage() {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
