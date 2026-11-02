package com.workshopcraft.muchmorestorage.forge;

//? if forge {
/*import com.workshopcraft.muchmorestorage.client.v1_21.CupboardItemRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.CupboardRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.KubhalaRenderer;
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
