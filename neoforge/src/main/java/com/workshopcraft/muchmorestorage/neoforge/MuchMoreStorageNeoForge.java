package com.workshopcraft.muchmorestorage.neoforge;

import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.item.ScrewdriverItem;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

@Mod(MuchMoreStorage.MOD_ID)
public class MuchMoreStorageNeoForge {
    public MuchMoreStorageNeoForge(IEventBus modBus) {
        modBus.addListener(MuchMoreStorageNeoForge::onRegister);
        modBus.addListener(MuchMoreStorageNeoForge::onCapabilities);
        NeoForge.EVENT_BUS.addListener(MuchMoreStorageNeoForge::onUseBlock);
    }

    private static void onUseBlock(PlayerInteractEvent.RightClickBlock e) {
        if (ScrewdriverItem.use(e.getLevel(), e.getPos(), e.getEntity(), e.getItemStack())) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static void onRegister(RegisterEvent e) {
        e.register(Registries.DATA_COMPONENT_TYPE, h -> ModContent.registerDataComponents(h::register));
        e.register(Registries.BLOCK, h -> ModContent.registerBlocks(h::register));
        e.register(Registries.ITEM, h -> ModContent.registerItems(h::register));
        e.register(Registries.BLOCK_ENTITY_TYPE, h -> ModContent.registerBlockEntities(h::register));
        e.register(Registries.CREATIVE_MODE_TAB, h -> ModContent.registerCreativeTabs(h::register));
    }

    private static void onCapabilities(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(Capabilities.Item.BLOCK, ModContent.CUPBOARD_BE, (be, side) -> VanillaContainerWrapper.of(be));
    }
}
