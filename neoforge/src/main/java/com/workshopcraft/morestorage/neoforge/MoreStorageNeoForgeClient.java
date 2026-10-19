package com.workshopcraft.morestorage.neoforge;

import com.workshopcraft.morestorage.MoreStorage;
import com.workshopcraft.morestorage.client.CupboardItemRenderer;
import com.workshopcraft.morestorage.client.CupboardRenderer;
import com.workshopcraft.morestorage.registry.ModContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

@Mod(value = MoreStorage.MOD_ID, dist = Dist.CLIENT)
public class MoreStorageNeoForgeClient {
    public MoreStorageNeoForgeClient(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerBlockEntityRenderer(ModContent.CUPBOARD_BE, CupboardRenderer::new));
        modBus.addListener((RegisterSpecialModelRendererEvent e) -> e.register(MoreStorage.id("cupboard"), CupboardItemRenderer.Unbaked.MAP_CODEC));
    }
}
