package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
//? if >=1.20.5
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import com.workshopcraft.muchmorestorage.menu.KladkaosMenu;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
//? if >=1.21
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.level.block.state.BlockState;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}

import java.util.stream.IntStream;

// 54 slots that hoppers and comparators see, plus a rail of four armour pieces kept apart
public class KladkaosBlockEntity extends FurnitureBlockEntity implements WorldlyContainer {
    public static final int SIZE = 54;
    public static final EquipmentSlot[] RAIL = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final int[] SLOTS = IntStream.range(0, SIZE).toArray();
    private final NonNullList<ItemStack> armour = NonNullList.withSize(RAIL.length, ItemStack.EMPTY);

    public KladkaosBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.KLADKAOS_BE, pos, state, SIZE);
    }

    public ItemStack armour(int i) { return armour.get(i); }

    // which rail hook a stack goes on, -1 when it is not worn armour
    public static int railSlot(Player player, ItemStack stack) {
        if (stack.isEmpty()) return -1;
        //? if >=1.20.5 {
        EquipmentSlot slot = player.getEquipmentSlotForItem(stack);
        //?} else
        /*EquipmentSlot slot = net.minecraft.world.entity.Mob.getEquipmentSlotForItem(stack);*/
        for (int i = 0; i < RAIL.length; i++) if (RAIL[i] == slot) return i;
        return -1;
    }

    private static boolean stuck(ItemStack stack) {
        //? if >=1.21 {
        return EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
        //?} else
        /*return EnchantmentHelper.hasBindingCurse(stack);*/
    }

    // hangs the held piece, whatever hung there comes back
    public void hang(Player player, InteractionHand hand, int slot) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack old = armour.set(slot, held.split(1));
        if (!old.isEmpty()) {
            if (held.isEmpty()) player.setItemInHand(hand, old);
            else if (!player.getInventory().add(old)) net.minecraft.world.level.block.Block.popResource(player.level(), player.blockPosition(), old);
        }
        setChanged();
    }

    public void swapOutfit(Player player) {
        for (int i = 0; i < RAIL.length; i++) {
            ItemStack worn = player.getItemBySlot(RAIL[i]);
            if (worn.isEmpty() && armour.get(i).isEmpty()) continue;
            if (!player.isCreative() && stuck(worn)) continue;
            player.setItemSlot(RAIL[i], armour.get(i));
            armour.set(i, worn);
        }
        setChanged();
    }

    public void dropArmour() {
        if (level != null) Containers.dropContents(level, getBlockPos(), armour);
        armour.clear();
    }

    //? if >=1.21.5 {
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        dropArmour();
    }
    //?}

    @Override
    protected Component getDefaultName() { return Component.translatable("container.muchmorestorage.kladkaos"); }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new KladkaosMenu(id, inventory, this, rail); }

    // the rail as a container for the menu, hoppers never see it
    public final Container rail = new Container() {
        @Override
        public int getContainerSize() { return RAIL.length; }

        @Override
        public boolean isEmpty() { return armour.stream().allMatch(ItemStack::isEmpty); }

        @Override
        public ItemStack getItem(int slot) { return armour.get(slot); }

        @Override
        public ItemStack removeItem(int slot, int count) {
            ItemStack out = ContainerHelper.removeItem(armour, slot, count);
            if (!out.isEmpty()) setChanged();
            return out;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(armour, slot); }

        @Override
        public void setItem(int slot, ItemStack stack) {
            armour.set(slot, stack);
            setChanged();
        }

        @Override
        public int getMaxStackSize() { return 1; }

        @Override
        public void setChanged() { KladkaosBlockEntity.this.setChanged(); }

        @Override
        public boolean stillValid(Player player) { return KladkaosBlockEntity.this.stillValid(player); }

        @Override
        public void clearContent() { armour.clear(); }
    };

    @Override
    public int[] getSlotsForFace(Direction side) { return SLOTS; }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return canPlaceItem(slot, stack); }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return true; }

    //? if >=1.21.6 {
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        armour.clear();
        input.child("Armour").ifPresent(a -> ContainerHelper.loadAllItems(a, armour));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.child("Armour"), armour);
    }
    //?} else if >=1.20.5 {
    /*@Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        armour.clear();
        ContainerHelper.loadAllItems(tag.getCompound("Armour"), armour, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag a = new CompoundTag();
        ContainerHelper.saveAllItems(a, armour, registries);
        tag.put("Armour", a);
    }
    *///?} else {
    /*@Override
    public void load(CompoundTag tag) {
        super.load(tag);
        armour.clear();
        ContainerHelper.loadAllItems(tag.getCompound("Armour"), armour);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag a = new CompoundTag();
        ContainerHelper.saveAllItems(a, armour);
        tag.put("Armour", a);
    }
    *///?}
}
