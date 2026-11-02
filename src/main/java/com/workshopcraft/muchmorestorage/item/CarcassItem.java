package com.workshopcraft.muchmorestorage.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class CarcassItem extends BlockItem {
    public final String body;

    public CarcassItem(Block block, String body, Properties props) {
        super(block, props);
        this.body = body;
    }
    //? if <1.21.2 {

    /*// BlockItem would take the cupboard's name
    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }
    *///?}
    //? if forge {

    /*@Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        com.workshopcraft.muchmorestorage.forge.MuchMoreStorageForgeClient.itemRenderer(consumer);
    }
    *///?}
}
