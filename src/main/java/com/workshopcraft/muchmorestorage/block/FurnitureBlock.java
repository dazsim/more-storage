package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.item.FlatpackItem;
import com.workshopcraft.muchmorestorage.registry.Finishes;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if >=1.20.5 && <1.21.2
/*import net.minecraft.world.ItemInteractionResult;*/
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// flat-packed furniture that a screwdriver puts together, BOKWURM and LADSTAPEL
public abstract class FurnitureBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ASSEMBLED = CupboardBlock.ASSEMBLED;
    public static final BooleanProperty CONCRETE = CupboardBlock.CONCRETE;
    private static final VoxelShape FLAT = Block.box(0, 0, 0, 16, 5, 16);

    protected FurnitureBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ASSEMBLED, true).setValue(CONCRETE, false));
    }

    protected abstract Map<String, ? extends Item> flatpacks();

    protected abstract ItemStack assembledItem(String body);

    // a click on the assembled block with an item that is not a screwdriver, true when it did something
    protected boolean useHeld(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) { return false; }

    protected abstract boolean useEmpty(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, ASSEMBLED, CONCRETE); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        boolean flat = stack.getItem() instanceof FlatpackItem;
        String body = flat ? ((FlatpackItem) stack.getItem()).body : ModContent.bodyOf(stack);
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(ASSEMBLED, !flat).setValue(CONCRETE, !Finishes.isWood(body));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof CupboardBlockEntity be)) return;
        if (stack.getItem() instanceof FlatpackItem flat) be.setFinishes(flat.body, "");
        //? if <1.20.5
        /*else be.setFinishes(ModContent.bodyOf(stack), "");*/
    }

    // where on the front a hit landed, u from the viewer's left and v from the bottom, null off the front
    public static @Nullable Vec3 frontHit(BlockState state, BlockHitResult hit) {
        Direction f = state.getValue(FACING);
        if (hit.getDirection() != f) return null;
        BlockPos pos = hit.getBlockPos();
        Vec3 p = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        double u = switch (f) {
            case NORTH -> 1 - p.x;
            case SOUTH -> p.x;
            case WEST -> p.z;
            default -> 1 - p.z;
        };
        return new Vec3(Math.max(0, Math.min(0.999, u)), Math.max(0, Math.min(0.999, p.y)), 0);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(ASSEMBLED) ? Shapes.block() : FLAT;
    }

    @Override
    public BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }

    @Override
    public BlockState mirror(BlockState s, Mirror m) { return s.rotate(m.getRotation(s.getValue(FACING))); }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public SoundType getSoundType(BlockState state) { return state.getValue(CONCRETE) ? SoundType.STONE : SoundType.WOOD; }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState like = state.getValue(CONCRETE) ? Blocks.STONE.defaultBlockState() : Blocks.OAK_PLANKS.defaultBlockState();
        return player.getDestroySpeed(like) / 2.5F / 30F;
    }

    //? if >=1.21.2 {
    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (held.is(ModContent.SCREWDRIVERS)) return InteractionResult.PASS;
        if (!state.getValue(ASSEMBLED)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        return useHeld(held, state, level, pos, player, hit) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    //?} else if >=1.20.5 {
    /*@Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (held.is(ModContent.SCREWDRIVERS)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (!state.getValue(ASSEMBLED)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return useHeld(held, state, level, pos, player, hit) ? ItemInteractionResult.sidedSuccess(level.isClientSide())
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    *///?} else {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ModContent.SCREWDRIVERS) || !state.getValue(ASSEMBLED)) return InteractionResult.PASS;
        if (useHeld(held, state, level, pos, player, hit)) return InteractionResult.sidedSuccess(level.isClientSide());
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        return useEmpty(state, level, pos, player, hit) ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
    }
    *///?}

    //? if >=1.20.5 {
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!state.getValue(ASSEMBLED)) return InteractionResult.PASS;
        return useEmpty(state, level, pos, player, hit) ? done(level) : InteractionResult.PASS;
    }
    //?}

    protected static InteractionResult done(Level level) {
        //? if >=1.21.2 {
        return InteractionResult.SUCCESS;
        //?} else
        /*return InteractionResult.sidedSuccess(level.isClientSide());*/
    }

    // a flat pack settles into shape, sneaking takes an assembled one apart, the same answer on both sides
    public static boolean screwdriverActs(BlockState state, boolean sneaking) {
        return !state.getValue(ASSEMBLED) || sneaking;
    }

    public static void screwdriver(Level level, BlockPos pos, BlockState state, CupboardBlockEntity be) {
        if (!state.getValue(ASSEMBLED)) {
            be.startSettle();
        } else {
            Containers.dropContents(level, pos, be);
            be.clearContent();
            level.setBlock(pos, state.setValue(ASSEMBLED, false), 3);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof CupboardBlockEntity be)) return;
        be.recheckOpen();
        if (!state.getValue(ASSEMBLED) && be.settleEnd > 0) {
            long left = be.settleEnd - level.getGameTime();
            if (left > 0) {
                level.scheduleTick(pos, this, (int) left);
                return;
            }
            be.settleEnd = 0;
            level.setBlock(pos, state.setValue(ASSEMBLED, true), 3);
            level.playSound(null, pos, getSoundType(state).getPlaceSound(), SoundSource.BLOCKS, 0.8F, 1.1F);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> out = new ArrayList<>();
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CupboardBlockEntity be) {
            out.add(state.getValue(ASSEMBLED) ? assembledItem(be.body()) : new ItemStack(flatpacks().get(be.body())));
        }
        return out;
    }

    //? if >=1.21.4 {
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
    //?} else if >=1.20.5 {
    /*@Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
    *///?} else {
    /*@Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
    *///?}
        String body = level.getBlockEntity(pos) instanceof CupboardBlockEntity be ? be.body() : "white_concrete";
        return state.getValue(ASSEMBLED) ? assembledItem(body) : new ItemStack(flatpacks().get(body));
    }

    //? if >=1.21.5 {
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
    //?} else {
    /*@Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        //? if >=1.20.5 {
        /^Containers.dropContentsOnDestroy(state, newState, level, pos);
        ^///?} else {
        /^if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof net.minecraft.world.Container c) {
            Containers.dropContents(level, pos, c);
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        ^///?}
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    *///?}

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    //? if >=1.21.9 {
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
    //?} else
    /*public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {*/
        return signal(level, pos);
    }

    protected int signal(Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
