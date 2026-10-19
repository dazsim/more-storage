package com.workshopcraft.muchmorestorage.neoforge;

import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.client.CupboardItemRenderer;
import com.workshopcraft.muchmorestorage.client.CupboardRenderer;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

@Mod(value = MuchMoreStorage.MOD_ID, dist = Dist.CLIENT)
public class MuchMoreStorageNeoForgeClient {
    public MuchMoreStorageNeoForgeClient(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerBlockEntityRenderer(ModContent.CUPBOARD_BE, CupboardRenderer::new));
        modBus.addListener((RegisterSpecialModelRendererEvent e) -> e.register(MuchMoreStorage.id("cupboard"), CupboardItemRenderer.Unbaked.MAP_CODEC));
    }
}
