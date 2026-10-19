package com.workshopcraft.morestorage.fabric;

import com.workshopcraft.morestorage.MoreStorage;
import com.workshopcraft.morestorage.client.CupboardItemRenderer;
import com.workshopcraft.morestorage.client.CupboardRenderer;
import com.workshopcraft.morestorage.registry.ModContent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.special.SpecialModelRenderers;

public class MoreStorageFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ModContent.CUPBOARD_BE, CupboardRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(MoreStorage.id("cupboard"), CupboardItemRenderer.Unbaked.MAP_CODEC);
    }
}
