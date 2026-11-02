package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

public class CrateBlockEntity extends FurnitureBlockEntity {
    public CrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.CRATE_BE, pos, state, 18);
    }

    @Override
    protected Component getDefaultName() { return Component.translatable("container.muchmorestorage.flatpack_crate"); }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x2, id, inventory, this, 2);
    }
}
