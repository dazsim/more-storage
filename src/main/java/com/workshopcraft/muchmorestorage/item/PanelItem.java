package com.workshopcraft.muchmorestorage.item;

import net.minecraft.world.item.Item;

// a flat-pack panel, three of them and a screwdriver make a KLADKAOS
public class PanelItem extends Item {
    public final String body;

    public PanelItem(String body, Properties props) {
        super(props);
        this.body = body;
    }
    //? if forge {

    /*@Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        com.workshopcraft.muchmorestorage.forge.MuchMoreStorageForgeClient.itemRenderer(consumer);
    }
    *///?}
}
