package com.workshopcraft.muchmorestorage.entity;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}

import java.util.UUID;

// a chest on wheels, pushed by walking into it or pulled along behind a player, flat ground only
public class HjulborgEntity extends Entity {
    public static final int SIZE = 18;
    private static final EntityDataAccessor<String> BODY = SynchedEntityData.defineId(HjulborgEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> WOBBLE = SynchedEntityData.defineId(HjulborgEntity.class, EntityDataSerializers.INT);
    private static final double PUSH = 0.3, PULL = 0.35, FOLLOW = 1.8, LET_GO = 7.0;

    private final SimpleContainer items = new SimpleContainer(SIZE) {
        @Override
        public boolean stillValid(Player player) {
            return isAlive() && player.distanceToSqr(HjulborgEntity.this) < 64;
        }
    };
    private UUID puller;
    private double fall;

    public HjulborgEntity(EntityType<? extends HjulborgEntity> type, Level level) {
        super(type, level);
        //? if <1.20.5
        /*setMaxUpStep(0F);*/
    }

    //? if >=1.20.5 {
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(BODY, "oak");
        b.define(WOBBLE, 0);
    }
    //?} else {
    /*@Override
    protected void defineSynchedData() {
        entityData.define(BODY, "oak");
        entityData.define(WOBBLE, 0);
    }
    *///?}

    public String body() { return entityData.get(BODY); }

    public void setBody(String body) { entityData.set(BODY, body); }

    public int wobble() { return entityData.get(WOBBLE); }

    @Override
    public float maxUpStep() { return 0F; }

    @Override
    public boolean isPickable() { return !isRemoved(); }

    @Override
    public boolean isPushable() { return false; }

    public boolean isEmpty() { return items.isEmpty(); }

    public ItemStack asItem() { return ModContent.furniture(ModContent.HJULBORG_ITEM, body()); }

    @Override
    public ItemStack getPickResult() { return asItem(); }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (wobble() > 0) entityData.set(WOBBLE, wobble() - 1);
        if (!supported(getBoundingBox())) {
            fall = Math.max(fall - 0.04, -2.0);
            move(MoverType.SELF, new Vec3(0, fall, 0));
            if (onGround()) fall = 0;
            return;
        }
        fall = 0;
        Vec3 v = getDeltaMovement().multiply(0.5, 0, 0.5);
        Player p = puller == null ? null : level().getPlayerByUUID(puller);
        if (p != null && (!p.isAlive() || p.isSpectator() || distanceTo(p) > LET_GO)) {
            tell(p, "message.muchmorestorage.hjulborg.let_go");
            puller = null;
            p = null;
        }
        if (p != null) {
            double dx = p.getX() - getX(), dz = p.getZ() - getZ(), d = Math.sqrt(dx * dx + dz * dz);
            if (d > 1.0E-3) setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
            if (d > FOLLOW) {
                double s = Math.min(PULL, (d - FOLLOW) * 0.5);
                v = new Vec3(dx / d * s, 0, dz / d * s);
            }
        }
        for (Player o : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.15, 0, 0.15))) {
            if (o.isSpectator() || o == p) continue;
            double dx = getX() - o.getX(), dz = getZ() - o.getZ(), d = Math.sqrt(dx * dx + dz * dz);
            if (d > 1.0E-3) v = new Vec3(dx / d * PUSH, 0, dz / d * PUSH);
        }
        if (v.horizontalDistanceSqr() < 1.0E-5) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        AABB to = getBoundingBox().move(v.x, 0, v.z);
        if (!level().noCollision(this, to.deflate(1.0E-3)) || !supported(to)) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        setDeltaMovement(v);
        setPos(getX() + v.x, getY(), getZ() + v.z);
    }

    // room to stand and a floor under it, checked before placing
    public boolean fits() {
        return level().noCollision(this, getBoundingBox().deflate(1.0E-3)) && supported(getBoundingBox());
    }

    // solid floor right under the middle and nothing lower, so a drop or a step stops it
    private boolean supported(AABB box) {
        double cx = (box.minX + box.maxX) / 2, cz = (box.minZ + box.maxZ) / 2;
        return !level().noCollision(this, new AABB(cx - 0.2, box.minY - 0.05, cz - 0.2, cx + 0.2, box.minY - 1.0E-3, cz + 0.2));
    }

    private void tell(Player p, String key) {
        //? if >=26.1 {
        p.sendOverlayMessage(Component.translatable(key));
        //?} else
        /*p.displayClientMessage(Component.translatable(key), true);*/
    }

    //? if >=26.1 {
    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 at) {
        return use(player, hand);
    }
    //?} else {
    /*@Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return use(player, hand);
    }
    *///?}

    private InteractionResult use(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            if (player.getUUID().equals(puller)) {
                puller = null;
                tell(player, "message.muchmorestorage.hjulborg.let_go");
            } else {
                puller = player.getUUID();
                tell(player, "message.muchmorestorage.hjulborg.pull");
            }
            return InteractionResult.SUCCESS;
        }
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x2, id, inv, items, 2), getDisplayName()));
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.5F, 1.1F);
        return InteractionResult.SUCCESS;
    }

    //? if >=1.21.2 {
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return hit(source);
    }
    //?} else {
    /*@Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide()) return source.getDirectEntity() instanceof Player;
        return hit(source);
    }
    *///?}

    // only a player's own hit counts, and only an empty one comes back as an item
    private boolean hit(DamageSource source) {
        if (!(source.getDirectEntity() instanceof Player p) || isRemoved()) return false;
        if (!isEmpty()) {
            entityData.set(WOBBLE, 10);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.6F, 1.0F);
            tell(p, "message.muchmorestorage.hjulborg.full");
            return true;
        }
        if (!p.getAbilities().instabuild) {
            ItemStack out = asItem();
            if (!p.getInventory().add(out)) Block.popResource(level(), blockPosition(), out);
        }
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.6F, 1.0F);
        discard();
        return true;
    }

    // /kill and the void are the only ways it goes with items inside, so they fall out
    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide() && reason.shouldDestroy()) Containers.dropContents(level(), this, items);
        super.remove(reason);
    }

    private NonNullList<ItemStack> list() {
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        for (int i = 0; i < SIZE; i++) list.set(i, items.getItem(i));
        return list;
    }

    private void fill(NonNullList<ItemStack> list) {
        for (int i = 0; i < SIZE; i++) items.setItem(i, list.get(i));
    }

    //? if >=1.21.6 {
    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        setBody(in.getStringOr("Body", "oak"));
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, list);
        fill(list);
        puller = in.read("Puller", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        out.putString("Body", body());
        out.storeNullable("Puller", UUIDUtil.CODEC, puller);
        ContainerHelper.saveAllItems(out, list());
    }
    //?} else if >=1.20.5 {
    /*@Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Body")) setBody(tag.getString("Body"));
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, list, registryAccess());
        fill(list);
        puller = tag.hasUUID("Puller") ? tag.getUUID("Puller") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("Body", body());
        if (puller != null) tag.putUUID("Puller", puller);
        ContainerHelper.saveAllItems(tag, list(), registryAccess());
    }
    *///?} else {
    /*@Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Body")) setBody(tag.getString("Body"));
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, list);
        fill(list);
        puller = tag.hasUUID("Puller") ? tag.getUUID("Puller") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("Body", body());
        if (puller != null) tag.putUUID("Puller", puller);
        ContainerHelper.saveAllItems(tag, list());
    }
    *///?}
}
