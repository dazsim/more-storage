package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

// nine slots in one row, the contents go to the client so they show through the sides
public class GenomskadaBlockEntity extends FurnitureBlockEntity {
    public GenomskadaBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.GENOMSKADA_BE, pos, state, 9);
    }

    @Override
    protected Component getDefaultName() { return Component.translatable("container.muchmorestorage.genomskada"); }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x1, id, inventory, this, 1);
    }

    public static String lid(String body) { return body == null || body.isEmpty() ? "white_concrete" : body; }
}
