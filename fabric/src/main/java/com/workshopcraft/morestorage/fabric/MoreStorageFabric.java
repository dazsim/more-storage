package com.workshopcraft.morestorage.fabric;

import com.workshopcraft.morestorage.registry.ModContent;
import net.fabricmc.api.ModInitializer;

public class MoreStorageFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModContent.registerAllDirect();
    }
}
