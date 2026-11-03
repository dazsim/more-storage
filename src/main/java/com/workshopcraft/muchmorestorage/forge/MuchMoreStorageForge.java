package com.workshopcraft.muchmorestorage.forge;

//? if forge {
/*import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.item.ScrewdriverItem;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

@Mod(MuchMoreStorage.MOD_ID)
public class MuchMoreStorageForge {
    public MuchMoreStorageForge(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(MuchMoreStorageForge::onRegister);
        MinecraftForge.EVENT_BUS.addListener(MuchMoreStorageForge::onUseBlock);
        if (FMLEnvironment.dist == Dist.CLIENT) MuchMoreStorageForgeClient.init(modBus);
    }

    private static void onUseBlock(PlayerInteractEvent.RightClickBlock e) {
        if (ScrewdriverItem.use(e.getLevel(), e.getPos(), e.getEntity(), e.getItemStack())) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    // Forge already exposes an item handler for every BaseContainerBlockEntity
    private static void onRegister(RegisterEvent e) {
        e.register(Registries.BLOCK, h -> ModContent.registerBlocks(h::register));
        e.register(Registries.ITEM, h -> ModContent.registerItems(h::register));
        e.register(Registries.BLOCK_ENTITY_TYPE, h -> ModContent.registerBlockEntities(h::register));
        e.register(Registries.ENTITY_TYPE, h -> ModContent.registerEntities(h::register));
        e.register(Registries.MENU, h -> ModContent.registerMenus(h::register));
        e.register(Registries.RECIPE_SERIALIZER, h -> ModContent.registerRecipeSerializers(h::register));
        e.register(Registries.CREATIVE_MODE_TAB, h -> ModContent.registerCreativeTabs(h::register));
    }
}
*///?}
