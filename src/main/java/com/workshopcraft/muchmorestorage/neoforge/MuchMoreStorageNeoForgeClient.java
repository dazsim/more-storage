package com.workshopcraft.muchmorestorage.neoforge;

//? if neoforge {
/*import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.registry.ModContent;
//? if >=26.1 {
import com.workshopcraft.muchmorestorage.client.CupboardItemRenderer;
import com.workshopcraft.muchmorestorage.client.CupboardRenderer;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
//?} else {
/^import com.workshopcraft.muchmorestorage.client.v1_21.CupboardItemRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.CupboardRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
^///?}
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = MuchMoreStorage.MOD_ID, dist = Dist.CLIENT)
public class MuchMoreStorageNeoForgeClient {
    public MuchMoreStorageNeoForgeClient(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerBlockEntityRenderer(ModContent.CUPBOARD_BE, CupboardRenderer::new));
        //? if >=26.1 {
        modBus.addListener((RegisterSpecialModelRendererEvent e) -> e.register(MuchMoreStorage.id("cupboard"), CupboardItemRenderer.Unbaked.MAP_CODEC));
        //?} else {
        /^modBus.addListener((RegisterClientExtensionsEvent e) -> e.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new CupboardItemRenderer();
                return renderer;
            }
        }, CupboardItemRenderer.items().toArray(Item[]::new)));
        ^///?}
    }
}
*///?}
