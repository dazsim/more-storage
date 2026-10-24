package com.workshopcraft.muchmorestorage.fabric;

import com.workshopcraft.muchmorestorage.item.ScrewdriverItem;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;

public class MuchMoreStorageFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModContent.registerAllDirect();
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
                ScrewdriverItem.use(level, hit.getBlockPos(), player, player.getItemInHand(hand)) ? InteractionResult.SUCCESS : InteractionResult.PASS);
    }
}
