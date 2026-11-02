package com.workshopcraft.muchmorestorage;

import net.minecraft.resources.Identifier;

public final class MuchMoreStorage {
    public static final String MOD_ID = "muchmorestorage";

    private MuchMoreStorage() {}

    public static Identifier id(String path) {
        return id(MOD_ID, path);
    }

    public static Identifier id(String namespace, String path) {
        //? if >=1.21 {
        return Identifier.fromNamespaceAndPath(namespace, path);
        //?} else
        /*return new Identifier(namespace, path);*/
    }
}
