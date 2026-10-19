package com.workshopcraft.morestorage.fabric;

import com.workshopcraft.morestorage.client.CupboardRenderer;
import com.workshopcraft.morestorage.registry.ModContent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class MoreStorageFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ModContent.CUPBOARD_BE, CupboardRenderer::new);
    }
}
