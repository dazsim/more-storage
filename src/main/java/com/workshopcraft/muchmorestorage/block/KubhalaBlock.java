package com.workshopcraft.muchmorestorage.block;

//? if >=1.20.5 && <26.3
import com.mojang.serialization.MapCodec;
import com.workshopcraft.muchmorestorage.item.FlatpackItem;
import com.workshopcraft.muchmorestorage.item.InsertItem;
import com.workshopcraft.muchmorestorage.registry.Finishes;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if >=1.20.5 && <1.21.2
/*import net.minecraft.world.ItemInteractionResult;*/
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// the KUBHÅLA cube shelf, the finish lives in the block entity and the insert in the state
public class KubhalaBlock extends BaseEntityBlock {
    //? if >=1.20.5 && <26.3
    public static final MapCodec<KubhalaBlock> CODEC = simpleCodec(KubhalaBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ASSEMBLED = CupboardBlock.ASSEMBLED;
    public static final BooleanProperty CONCRETE = CupboardBlock.CONCRETE;
    public static final EnumProperty<Insert> INSERT = EnumProperty.create("insert", Insert.class);
    public static final BooleanProperty FITTED = BooleanProperty.create("fitted");
    private static final VoxelShape FLAT = Block.box(0, 0, 0, 16, 5, 16);

    public KubhalaBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ASSEMBLED, true)
                .setValue(CONCRETE, false).setValue(INSERT, Insert.NONE).setValue(FITTED, false));
    }

    //? if >=1.20.5 && <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, ASSEMBLED, CONCRETE, INSERT, FITTED); }

    public static boolean openable(BlockState state) {
        return state.getBlock() instanceof KubhalaBlock && state.getValue(ASSEMBLED)
                && (state.getValue(INSERT) == Insert.NONE || state.getValue(FITTED));
    }

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
        if (!(level.getBlockEntity(pos) instanceof KubhalaBlockEntity be)) return;
        if (stack.getItem() instanceof FlatpackItem flat) be.setFinishes(flat.body, be.top());
        //? if <1.20.5
        /*else be.setFinishes(ModContent.bodyOf(stack), be.top());*/
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

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new KubhalaBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, ModContent.KUBHALA_BE, CupboardBlockEntity::clientTick) : null;
    }

    //? if >=1.21.2 {
    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (held.is(ModContent.SCREWDRIVERS)) return InteractionResult.PASS;
        return fitInsert(held, state, level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    //?} else if >=1.20.5 {
    /*@Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (held.is(ModContent.SCREWDRIVERS)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        return fitInsert(held, state, level, pos, player) ? ItemInteractionResult.sidedSuccess(level.isClientSide())
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    *///?} else {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ModContent.SCREWDRIVERS)) return InteractionResult.PASS;
        if (fitInsert(held, state, level, pos, player)) return InteractionResult.sidedSuccess(level.isClientSide());
        return open(state, level, pos, player);
    }
    *///?}

    // an insert pushed into an empty cube sits loose until it is screwed in
    private static boolean fitInsert(ItemStack held, BlockState state, Level level, BlockPos pos, Player player) {
        if (!(held.getItem() instanceof InsertItem insert) || !state.getValue(ASSEMBLED) || state.getValue(INSERT) != Insert.NONE) return false;
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(INSERT, insert.kind).setValue(FITTED, false), 3);
            //? if >=1.21 {
            held.consume(1, player);
            //?} else
            /*if (!player.getAbilities().instabuild) held.shrink(1);*/
            level.playSound(null, pos, SoundType.WOOD.getPlaceSound(), SoundSource.BLOCKS, 0.8F, 1.2F);
        }
        return true;
    }

    //? if >=1.20.5 {
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return open(state, level, pos, player);
    }
    //?}

    private static InteractionResult open(BlockState state, Level level, BlockPos pos, Player player) {
        if (!openable(state)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof KubhalaBlockEntity be) {
            player.openMenu(be);
            player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
            //? if >=1.21.2 {
            PiglinAi.angerNearbyPiglins(server, player, true);
            //?} else
            /*PiglinAi.angerNearbyPiglins(player, true);*/
        }
        return InteractionResult.SUCCESS;
    }

    // what a screwdriver would do here, the same answer on both sides
    public static boolean screwdriverActs(BlockState state, boolean sneaking) {
        return !state.getValue(ASSEMBLED) || state.getValue(INSERT) != Insert.NONE || sneaking;
    }

    // flat pack to cube, loose insert to fitted, fitted insert back out, sneaking takes a bare cube apart
    public static void screwdriver(Level level, BlockPos pos, BlockState state, KubhalaBlockEntity be, boolean sneaking) {
        Insert insert = state.getValue(INSERT);
        if (!state.getValue(ASSEMBLED)) {
            be.startSettle();
        } else if (insert != Insert.NONE && !state.getValue(FITTED) && !sneaking) {
            level.setBlock(pos, state.setValue(FITTED, true), 3);
        } else if (insert != Insert.NONE) {
            popResource(level, pos.relative(state.getValue(FACING)), ModContent.insert(insert));
            level.setBlock(pos, state.setValue(INSERT, Insert.NONE).setValue(FITTED, false), 3);
        } else {
            Containers.dropContents(level, pos, be);
            be.clearContent();
            level.setBlock(pos, state.setValue(ASSEMBLED, false), 3);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof KubhalaBlockEntity be)) return;
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
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof KubhalaBlockEntity be) {
            if (state.getValue(ASSEMBLED)) {
                out.add(ModContent.kubhala(be.body()));
                ItemStack insert = ModContent.insert(state.getValue(INSERT));
                if (!insert.isEmpty()) out.add(insert);
            } else {
                out.add(new ItemStack(ModContent.FLATPACKS.get(be.body())));
            }
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
        if (!(level.getBlockEntity(pos) instanceof KubhalaBlockEntity be)) return new ItemStack(ModContent.KUBHALA_ITEM);
        return state.getValue(ASSEMBLED) ? ModContent.kubhala(be.body()) : new ItemStack(ModContent.FLATPACKS.get(be.body()));
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
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
