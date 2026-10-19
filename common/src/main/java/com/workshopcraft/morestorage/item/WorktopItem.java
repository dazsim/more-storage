package com.workshopcraft.morestorage.item;

import net.minecraft.world.item.Item;

public class WorktopItem extends Item {
    public final String top;

    public WorktopItem(String top, Properties props) {
        super(props);
        this.top = top;
    }
}
