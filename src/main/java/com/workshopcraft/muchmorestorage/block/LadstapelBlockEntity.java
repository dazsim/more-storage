package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
//? if >=1.21.9
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;

// three drawers of nine, hoppers see all 27 slots, a player opens one drawer at a time
public class LadstapelBlockEntity extends FurnitureBlockEntity {
    public static final int DRAWERS = 3;
    public static final int PER_DRAWER = 9;
    private static final int EVENT_DRAWER = 10;
    private final Drawer[] drawers = {new Drawer(0), new Drawer(1), new Drawer(2)};

    public LadstapelBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.LADSTAPEL_BE, pos, state, DRAWERS * PER_DRAWER);
    }

    public Drawer drawer(int d) { return drawers[d]; }

    public float drawerOpenness(int d, float partialTick) { return drawers[d].lid.getOpenness(partialTick); }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LadstapelBlockEntity be) {
        for (Drawer d : be.drawers) d.lid.tickLid();
    }

    @Override
    public boolean triggerEvent(int id, int value) {
        if (id >= EVENT_DRAWER && id < EVENT_DRAWER + DRAWERS) {
            drawers[id - EVENT_DRAWER].lid.shouldBeOpen(value > 0);
            return true;
        }
        return super.triggerEvent(id, value);
    }

    @Override
    public void recheckOpen() {
        super.recheckOpen();
        if (!remove) for (Drawer d : drawers) d.openers.recheckOpeners(getLevel(), getBlockPos(), getBlockState());
    }

    @Override
    protected Component getDefaultName() { return Component.translatable("container.muchmorestorage.ladstapel"); }

    // one drawer's nine slots, a view onto the block entity's own items
    public final class Drawer implements Container {
        private final int index;
        private final ChestLidController lid = new ChestLidController();
        private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
            @Override
            protected void onOpen(Level level, BlockPos pos, BlockState state) { sound(SoundEvents.BARREL_OPEN); }

            @Override
            protected void onClose(Level level, BlockPos pos, BlockState state) { sound(SoundEvents.BARREL_CLOSE); }

            @Override
            protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
                level.blockEvent(pos, state.getBlock(), EVENT_DRAWER + index, current);
            }

            @Override
            public boolean isOwnContainer(Player player) {
                return player.containerMenu instanceof ChestMenu chest && chest.getContainer() == Drawer.this;
            }
        };

        private Drawer(int index) { this.index = index; }

        private LadstapelBlockEntity owner() { return LadstapelBlockEntity.this; }

        @Override
        public int getContainerSize() { return PER_DRAWER; }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < PER_DRAWER; i++) if (!getItem(i).isEmpty()) return false;
            return true;
        }

        @Override
        public ItemStack getItem(int slot) { return owner().getItem(index * PER_DRAWER + slot); }

        @Override
        public ItemStack removeItem(int slot, int count) { return owner().removeItem(index * PER_DRAWER + slot, count); }

        @Override
        public ItemStack removeItemNoUpdate(int slot) { return owner().removeItemNoUpdate(index * PER_DRAWER + slot); }

        @Override
        public void setItem(int slot, ItemStack stack) { owner().setItem(index * PER_DRAWER + slot, stack); }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) { return owner().canPlaceItem(index * PER_DRAWER + slot, stack); }

        @Override
        public void setChanged() { owner().setChanged(); }

        @Override
        public boolean stillValid(Player player) { return owner().stillValid(player); }

        @Override
        public void clearContent() {
            for (int i = 0; i < PER_DRAWER; i++) setItem(i, ItemStack.EMPTY);
        }

        //? if >=1.21.9 {
        @Override
        public void startOpen(ContainerUser user) {
            if (!remove && !user.getLivingEntity().isSpectator()) {
                openers.incrementOpeners(user.getLivingEntity(), getLevel(), getBlockPos(), getBlockState(), user.getContainerInteractionRange());
            }
        }

        @Override
        public void stopOpen(ContainerUser user) {
            if (!remove && !user.getLivingEntity().isSpectator()) {
                openers.decrementOpeners(user.getLivingEntity(), getLevel(), getBlockPos(), getBlockState());
            }
        }
        //?} else {
        /*@Override
        public void startOpen(Player player) {
            if (!remove && !player.isSpectator()) openers.incrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
        }

        @Override
        public void stopOpen(Player player) {
            if (!remove && !player.isSpectator()) openers.decrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
        }
        *///?}
    }
}
