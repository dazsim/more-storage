package com.workshopcraft.muchmorestorage.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class CarcassItem extends BlockItem {
    public final String body;

    public CarcassItem(Block block, String body, Properties props) {
        super(block, props);
        this.body = body;
    }
}
