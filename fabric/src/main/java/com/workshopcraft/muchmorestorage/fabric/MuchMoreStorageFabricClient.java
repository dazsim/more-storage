package com.workshopcraft.muchmorestorage.fabric;

import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.client.CupboardItemRenderer;
import com.workshopcraft.muchmorestorage.client.CupboardRenderer;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.special.SpecialModelRenderers;

public class MuchMoreStorageFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ModContent.CUPBOARD_BE, CupboardRenderer::new);
        SpecialModelRenderers.ID_MAPPER.put(MuchMoreStorage.id("cupboard"), CupboardItemRenderer.Unbaked.MAP_CODEC);
    }
}
