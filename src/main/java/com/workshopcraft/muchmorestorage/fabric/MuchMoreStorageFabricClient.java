package com.workshopcraft.muchmorestorage.fabric;

//? if fabric {
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
//? if >=26.1 {
import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.client.CupboardItemRenderer;
import com.workshopcraft.muchmorestorage.client.CupboardRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
//?} else {
/*import com.workshopcraft.muchmorestorage.client.v1_21.CupboardItemRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.CupboardRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.world.item.Item;
*///?}

public class MuchMoreStorageFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ModContent.CUPBOARD_BE, CupboardRenderer::new);
        //? if >=26.1 {
        SpecialModelRenderers.ID_MAPPER.put(MuchMoreStorage.id("cupboard"), CupboardItemRenderer.Unbaked.MAP_CODEC);
        //?} else {
        /*for (Item item : CupboardItemRenderer.items())
            BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, pose, buffers, light, overlay) -> CupboardItemRenderer.draw(stack, pose, buffers, light, overlay));
        *///?}
    }
}
//?}
