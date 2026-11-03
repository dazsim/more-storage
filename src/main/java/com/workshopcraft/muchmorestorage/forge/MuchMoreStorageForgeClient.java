package com.workshopcraft.muchmorestorage.forge;

//? if forge {
/*import com.workshopcraft.muchmorestorage.client.v1_21.CupboardItemRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.CupboardRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.KubhalaRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.BokwurmRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.LadstapelRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.GenomskadaRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.CrateRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.KladkaosRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.HjulborgRenderer;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.Consumer;

public final class MuchMoreStorageForgeClient {
    private MuchMoreStorageForgeClient() {}

    static void init(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerBlockEntityRenderer(ModContent.CUPBOARD_BE, CupboardRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerBlockEntityRenderer(ModContent.KUBHALA_BE, KubhalaRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerBlockEntityRenderer(ModContent.BOKWURM_BE, BokwurmRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerBlockEntityRenderer(ModContent.LADSTAPEL_BE, LadstapelRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerBlockEntityRenderer(ModContent.GENOMSKADA_BE, GenomskadaRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerBlockEntityRenderer(ModContent.CRATE_BE, CrateRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerBlockEntityRenderer(ModContent.KLADKAOS_BE, KladkaosRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerEntityRenderer(ModContent.HJULBORG, HjulborgRenderer::new));
        modBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e) -> e.enqueueWork(() ->
                net.minecraft.client.gui.screens.MenuScreens.register(com.workshopcraft.muchmorestorage.client.KladkaosScreen.type(), com.workshopcraft.muchmorestorage.client.KladkaosScreen::new)));
    }

    // called from the items' initializeClient
    public static void itemRenderer(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new CupboardItemRenderer();
                return renderer;
            }
        });
    }
}
*///?}
