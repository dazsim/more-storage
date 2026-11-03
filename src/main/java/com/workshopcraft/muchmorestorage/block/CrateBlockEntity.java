package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
//? if forge {
/*import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
*///?}

// hoppers and pipes only see the crate once it is put together
public class CrateBlockEntity extends FurnitureBlockEntity implements WorldlyContainer {
    private static final int[] NONE = new int[0];
    private final int[] all;

    public CrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.CRATE_BE, pos, state, 18);
        all = new int[18];
        for (int i = 0; i < all.length; i++) all[i] = i;
    }

    public boolean open() { return assembled(); }

    @Override
    public int[] getSlotsForFace(Direction side) { return assembled() ? all : NONE; }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return assembled(); }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return assembled(); }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) { return assembled(); }

    //? if forge {
    /*@Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && !assembled()) return LazyOptional.empty();
        return super.getCapability(cap, side);
    }
    *///?}

    @Override
    protected Component getDefaultName() { return Component.translatable("container.muchmorestorage.flatpack_crate"); }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x2, id, inventory, this, 2);
    }
}
