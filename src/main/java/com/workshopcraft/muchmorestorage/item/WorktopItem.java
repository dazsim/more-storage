package com.workshopcraft.muchmorestorage.item;

import net.minecraft.world.item.Item;

public class WorktopItem extends Item {
    public final String top;

    public WorktopItem(String top, Properties props) {
        super(props);
        this.top = top;
    }
    //? if forge {

    /*@Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        com.workshopcraft.muchmorestorage.forge.MuchMoreStorageForgeClient.itemRenderer(consumer);
    }
    *///?}
}
