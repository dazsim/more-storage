package com.workshopcraft.muchmorestorage.fabric;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.fabricmc.api.ModInitializer;

public class MuchMoreStorageFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModContent.registerAllDirect();
    }
}
