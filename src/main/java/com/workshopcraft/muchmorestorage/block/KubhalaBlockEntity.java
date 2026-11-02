package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
//? if >=1.20.5
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

// a cupboard block entity cut down to one row, the contents go to the client so open cubes can show them
public class KubhalaBlockEntity extends CupboardBlockEntity {
    public KubhalaBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.KUBHALA_BE, pos, state, 9);
    }

    @Override
    protected boolean assembled() {
        return getBlockState().getBlock() instanceof KubhalaBlock && getBlockState().getValue(KubhalaBlock.ASSEMBLED);
    }

    public Insert insert() {
        return getBlockState().getBlock() instanceof KubhalaBlock ? getBlockState().getValue(KubhalaBlock.INSERT) : Insert.NONE;
    }

    @Override
    public int getContainerSize() { return 9; }

    @Override
    public boolean stillValid(Player player) { return KubhalaBlock.openable(getBlockState()) && super.stillValid(player); }

    @Override
    protected void sound(SoundEvent event) {
        if (insert().closes()) super.sound(event);
    }

    @Override
    protected Component getDefaultName() { return Component.translatable("container.muchmorestorage.kubhala"); }

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

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ChestMenu(MenuType.GENERIC_9x1, id, inventory, this, 1);
    }
}
