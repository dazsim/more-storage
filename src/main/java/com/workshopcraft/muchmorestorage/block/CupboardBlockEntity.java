package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
//? if >=1.21.6
import net.minecraft.core.component.DataComponentGetter;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
//?}
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
//? if >=1.21.9
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.state.BlockState;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}

import java.util.List;

public class CupboardBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
    private static final int EVENT_OPEN_COUNT = 1;
    private static final int EVENT_SETTLE = 2;
    public static final int SETTLE_TICKS = 6;
    public long settleEnd;
    private long settleStart = -1;
    private String body = "oak";
    private String top = "oak";
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final ChestLidController door = new ChestLidController();
    private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) { sound(SoundEvents.BARREL_OPEN); }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) { sound(SoundEvents.BARREL_CLOSE); }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
            level.blockEvent(pos, state.getBlock(), EVENT_OPEN_COUNT, current);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof ChestMenu menu && menu.getContainer() == CupboardBlockEntity.this;
        }
    };

    public CupboardBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.CUPBOARD_BE, pos, state);
    }

    protected CupboardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CupboardBlockEntity be) {
        be.door.tickLid();
    }

    protected void sound(SoundEvent event) {
        if (level == null) return;
        BlockPos p = getBlockPos();
        level.playSound(null, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, event, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 1.0F);
    }

    public String body() { return body; }

    public String top() { return top; }

    public boolean hasTop() { return !top.isEmpty(); }

    public void setFinishes(String body, String top) {
        this.body = body;
        this.top = top;
        if (top.isEmpty()) settleEnd = 0;
        setChanged();
        if (level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    protected boolean assembled() {
        return getBlockState().getBlock() instanceof CupboardBlock && getBlockState().getValue(CupboardBlock.ASSEMBLED);
    }

    @Override
    public int getContainerSize() { return 27; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) { return assembled(); }

    @Override
    public boolean stillValid(Player player) { return assembled() && super.stillValid(player); }

    @Override
    protected Component getDefaultName() { return Component.translatable("container.muchmorestorage.cupboard"); }

    @Override
    protected NonNullList<ItemStack> getItems() { return items; }

    @Override
    protected void setItems(NonNullList<ItemStack> items) { this.items = items; }

    //? if >=1.20.5 {
    // items saved by 1.20.4 or older are not run through the data fixer for modded block entities
    private static Tag upgradeItem(CompoundTag tag) {
        //? if >=1.21.6 {
        int now = SharedConstants.getCurrentVersion().dataVersion().version();
        //?} else {
        /*int now = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        *///?}
        return DataFixers.getDataFixer().update(References.ITEM_STACK, new Dynamic<Tag>(NbtOps.INSTANCE, tag), 3465, now).getValue();
    }
    //?}

    //? if >=1.21.6 {
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        body = input.getStringOr("Body", "oak");
        top = input.getStringOr("Top", "");
        if (top.isEmpty()) settleStart = -1;
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (tryLoadLootTable(input)) return;
        List<CompoundTag> raw = input.listOrEmpty("Items", CompoundTag.CODEC).stream().toList();
        if (raw.stream().noneMatch(t -> t.contains("Count"))) {
            ContainerHelper.loadAllItems(input, items);
            return;
        }
        var ops = input.lookup().createSerializationContext(NbtOps.INSTANCE);
        for (CompoundTag t : raw) {
            int slot = t.getByteOr("Slot", (byte) 0) & 255;
            if (slot < items.size()) ItemStack.CODEC.parse(ops, upgradeItem(t)).result().ifPresent(st -> items.set(slot, st));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Body", body);
        output.putString("Top", top);
        if (!trySaveLootTable(output)) ContainerHelper.saveAllItems(output, items);
    }
    //?} else if >=1.20.5 {
    /*@Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        body = tag.contains("Body") ? tag.getString("Body") : "oak";
        top = tag.getString("Top");
        if (top.isEmpty()) settleStart = -1;
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (tryLoadLootTable(tag)) return;
        ListTag raw = tag.getList("Items", Tag.TAG_COMPOUND);
        if (raw.isEmpty() || !raw.getCompound(0).contains("Count")) {
            ContainerHelper.loadAllItems(tag, items, registries);
            return;
        }
        for (int i = 0; i < raw.size(); i++) {
            CompoundTag t = raw.getCompound(i);
            int slot = t.getByte("Slot") & 255;
            if (slot < items.size()) ItemStack.parse(registries, upgradeItem(t)).ifPresent(st -> items.set(slot, st));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Body", body);
        tag.putString("Top", top);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
    }
    *///?} else {
    /*@Override
    public void load(CompoundTag tag) {
        super.load(tag);
        body = tag.contains("Body") ? tag.getString("Body") : "oak";
        top = tag.getString("Top");
        if (top.isEmpty()) settleStart = -1;
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Body", body);
        tag.putString("Top", top);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items);
    }
    *///?}

    //? if >=1.20.5 {
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder b) {
        super.collectImplicitComponents(b);
        b.set(ModContent.BODY, body);
        if (hasTop()) b.set(ModContent.TOP, top);
    }

    @Override
    //? if >=1.21.6 {
    protected void applyImplicitComponents(DataComponentGetter in) {
    //?} else
    /*protected void applyImplicitComponents(DataComponentInput in) {*/
        super.applyImplicitComponents(in);
        String b = ModContent.legacy(in.get(DataComponents.CUSTOM_DATA), "Body");
        String t = ModContent.legacy(in.get(DataComponents.CUSTOM_DATA), "Top");
        if (b == null) b = in.get(ModContent.BODY);
        if (t == null) t = in.get(ModContent.TOP);
        if (b != null) body = b;
        if (t != null) top = t;
    }

    //? if >=1.21.6 {
    @Override
    public void removeComponentsFromTag(ValueOutput out) {
        super.removeComponentsFromTag(out);
        out.discard("Body");
        out.discard("Top");
    }
    //?} else {
    /*@Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("Body");
        tag.remove("Top");
    }
    *///?}
    //?}

    @Override
    //? if >=1.20.5 {
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    //?} else
    /*public CompoundTag getUpdateTag() {*/
        CompoundTag tag = new CompoundTag();
        tag.putString("Body", body);
        tag.putString("Top", top);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public boolean triggerEvent(int id, int value) {
        if (id == EVENT_OPEN_COUNT) {
            door.shouldBeOpen(value > 0);
            return true;
        }
        if (id == EVENT_SETTLE) {
            if (level != null) settleStart = level.getGameTime();
            return true;
        }
        return super.triggerEvent(id, value);
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

    @Override
    public List<ContainerUser> getEntitiesWithContainerOpen() {
        return openers.getEntitiesWithContainerOpen(getLevel(), getBlockPos());
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

    public boolean startSettle() {
        if (level == null || settleEnd > 0) return false;
        settleEnd = level.getGameTime() + SETTLE_TICKS;
        level.blockEvent(getBlockPos(), getBlockState().getBlock(), EVENT_SETTLE, 0);
        level.scheduleTick(getBlockPos(), getBlockState().getBlock(), SETTLE_TICKS);
        return true;
    }

    // 0 while loose, rising to 1 as the worktop drops into place
    public float settleProgress(float partialTick) {
        if (settleStart < 0 || level == null) return 0F;
        return Math.min(1F, (level.getGameTime() - settleStart + partialTick) / SETTLE_TICKS);
    }

    public void recheckOpen() {
        if (!remove) openers.recheckOpeners(getLevel(), getBlockPos(), getBlockState());
    }

    @Override
    public float getOpenNess(float partialTick) { return door.getOpenness(partialTick); }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory, this);
    }
}
