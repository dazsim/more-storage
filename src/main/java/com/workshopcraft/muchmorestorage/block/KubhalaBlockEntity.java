package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.item.SakksakkItem;
import com.workshopcraft.muchmorestorage.menu.SakksakkMenu;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
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

    // the colour of a fitted SAKKSÄKK, its items are this cube's items
    public String box() { return box.isEmpty() ? "white" : box; }

    public void fillBox(String colour, NonNullList<ItemStack> contents) {
        box = colour;
        for (int i = 0; i < getContainerSize(); i++) setItem(i, i < contents.size() ? contents.get(i) : ItemStack.EMPTY);
        setChanged();
    }

    // the fitted box as an item, holding what the cube held
    public ItemStack boxItem() {
        NonNullList<ItemStack> list = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < getContainerSize(); i++) list.set(i, getItem(i).copy());
        return SakksakkItem.of(box(), list);
    }

    public ItemStack takeBox() {
        ItemStack out = boxItem();
        clearContent();
        box = "";
        setChanged();
        return out;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return super.canPlaceItem(slot, stack) && (insert() != Insert.BOX || SakksakkItem.fits(stack));
    }

    @Override
    protected boolean ownsMenu(AbstractContainerMenu menu) {
        return super.ownsMenu(menu) || menu instanceof SakksakkMenu m && m.box == this;
    }

    //? if >=1.21.5 {
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // a fitted box drops with its contents inside
        if (insert() != Insert.BOX) super.preRemoveSideEffects(pos, state);
    }
    //?}

    @Override
    public boolean stillValid(Player player) { return KubhalaBlock.openable(getBlockState()) && super.stillValid(player); }

    @Override
    protected void sound(SoundEvent event) {
        if (insert().closes()) super.sound(event);
        else if (insert() == Insert.BOX && event == SoundEvents.BARREL_OPEN) super.sound(SoundEvents.WOOL_PLACE);
    }

    @Override
    protected Component getDefaultName() {
        if (insert() == Insert.BOX) return Component.translatable("item.muchmorestorage." + box() + "_sakksakk");
        return Component.translatable("container.muchmorestorage.kubhala");
    }

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
        if (insert() == Insert.BOX) return new SakksakkMenu(id, inventory, this);
        return new ChestMenu(MenuType.GENERIC_9x1, id, inventory, this, 1);
    }
}
