package com.workshopcraft.muchmorestorage.block;

import net.minecraft.core.BlockPos;
//? if >=1.20.5
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// shared by BOKWURM and LADSTAPEL, the contents go to the client so they can be drawn
public abstract class FurnitureBlockEntity extends CupboardBlockEntity {
    private final int size;

    protected FurnitureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state, size);
        this.size = size;
    }

    @Override
    protected boolean assembled() {
        return getBlockState().getBlock() instanceof FurnitureBlock && getBlockState().getValue(FurnitureBlock.ASSEMBLED);
    }

    @Override
    public int getContainerSize() { return size; }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override
    //? if >=1.20.5 {
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
    //?} else {
    /*public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
    *///?}
}
