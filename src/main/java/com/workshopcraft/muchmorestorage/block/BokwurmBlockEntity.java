package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

// two shelves of six books, one book a slot like a chiseled bookshelf
public class BokwurmBlockEntity extends FurnitureBlockEntity {
    public static final int PER_SHELF = 6;
    public static final int SLOTS = 12;

    public BokwurmBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.BOKWURM_BE, pos, state, SLOTS);
    }

    @Override
    public int getMaxStackSize() { return 1; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return assembled() && stack.is(ModContent.BOKWURM_BOOKS) && getItem(slot).isEmpty();
    }

    public int books() {
        int n = 0;
        for (int i = 0; i < SLOTS; i++) if (!getItem(i).isEmpty()) n++;
        return n;
    }

    @Override
    protected Component getDefaultName() { return Component.translatable("container.muchmorestorage.bokwurm"); }
}
