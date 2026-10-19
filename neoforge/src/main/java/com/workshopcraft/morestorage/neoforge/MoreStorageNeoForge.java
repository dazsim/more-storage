package com.workshopcraft.morestorage.neoforge;

import com.workshopcraft.morestorage.MoreStorage;
import com.workshopcraft.morestorage.registry.ModContent;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

@Mod(MoreStorage.MOD_ID)
public class MoreStorageNeoForge {
    public MoreStorageNeoForge(IEventBus modBus) {
        modBus.addListener(MoreStorageNeoForge::onRegister);
        modBus.addListener(MoreStorageNeoForge::onCapabilities);
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
