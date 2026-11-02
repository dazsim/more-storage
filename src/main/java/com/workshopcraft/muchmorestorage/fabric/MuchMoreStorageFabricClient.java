package com.workshopcraft.muchmorestorage.fabric;

//? if fabric {
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
//? if >=26.1 {
import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.client.CupboardItemRenderer;
import com.workshopcraft.muchmorestorage.client.CupboardRenderer;
import com.workshopcraft.muchmorestorage.client.KubhalaRenderer;
import com.workshopcraft.muchmorestorage.client.BokwurmRenderer;
import com.workshopcraft.muchmorestorage.client.LadstapelRenderer;
import com.workshopcraft.muchmorestorage.client.KladkaosRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
//?} else {
/*import com.workshopcraft.muchmorestorage.client.v1_21.CupboardItemRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.CupboardRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.KubhalaRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.BokwurmRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.LadstapelRenderer;
import com.workshopcraft.muchmorestorage.client.v1_21.KladkaosRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.world.item.Item;
*///?}

public class MuchMoreStorageFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ModContent.CUPBOARD_BE, CupboardRenderer::new);
        BlockEntityRenderers.register(ModContent.KUBHALA_BE, KubhalaRenderer::new);
        BlockEntityRenderers.register(ModContent.BOKWURM_BE, BokwurmRenderer::new);
        BlockEntityRenderers.register(ModContent.LADSTAPEL_BE, LadstapelRenderer::new);
        BlockEntityRenderers.register(ModContent.KLADKAOS_BE, KladkaosRenderer::new);
        //? if >=26.1 {
        SpecialModelRenderers.ID_MAPPER.put(MuchMoreStorage.id("cupboard"), CupboardItemRenderer.Unbaked.MAP_CODEC);
        //?} else {
        /*for (Item item : CupboardItemRenderer.items())
            BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, pose, buffers, light, overlay) -> CupboardItemRenderer.draw(stack, pose, buffers, light, overlay));
        *///?}
    }
}
//?}
