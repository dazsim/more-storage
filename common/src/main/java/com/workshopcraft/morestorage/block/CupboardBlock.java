package com.workshopcraft.morestorage.block;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.morestorage.item.CarcassItem;
import com.workshopcraft.morestorage.item.ScrewdriverItem;
import com.workshopcraft.morestorage.item.WorktopItem;
import com.workshopcraft.morestorage.registry.Finishes;
import com.workshopcraft.morestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// one block for every finish, the body and top live in the block entity
public class CupboardBlock extends BaseEntityBlock {
    public static final MapCodec<CupboardBlock> CODEC = simpleCodec(CupboardBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");
    public static final BooleanProperty CONCRETE = BooleanProperty.create("concrete");

    public CupboardBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HINGE, DoorHingeSide.LEFT)
                .setValue(ASSEMBLED, true).setValue(CONCRETE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, HINGE, ASSEMBLED, CONCRETE); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        boolean carcass = stack.getItem() instanceof CarcassItem;
        String body = carcass ? ((CarcassItem) stack.getItem()).body : stack.getOrDefault(ModContent.BODY, "oak");
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(ASSEMBLED, !carcass).setValue(CONCRETE, !Finishes.isWood(body));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        if (stack.getItem() instanceof CarcassItem carcass && level.getBlockEntity(pos) instanceof CupboardBlockEntity be) {
            be.setFinishes(carcass.body, "");
        }
    }

    @Override
    protected BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }

    @Override
    protected BlockState mirror(BlockState s, Mirror m) {
        BlockState r = s.rotate(m.getRotation(s.getValue(FACING)));
        return m == Mirror.NONE ? r : r.cycle(HINGE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected SoundType getSoundType(BlockState state) { return state.getValue(CONCRETE) ? SoundType.STONE : SoundType.WOOD; }

    // axe for wood bodies, pickaxe for concrete, both still work by hand
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState like = state.getValue(CONCRETE) ? Blocks.STONE.defaultBlockState() : Blocks.OAK_PLANKS.defaultBlockState();
        return player.getDestroySpeed(like) / 2.5F / 30F;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CupboardBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, ModContent.CUPBOARD_BE, CupboardBlockEntity::clientTick) : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (held.getItem() instanceof ScrewdriverItem) return InteractionResult.PASS;
        if (held.getItem() instanceof WorktopItem worktop && !state.getValue(ASSEMBLED)
                && level.getBlockEntity(pos) instanceof CupboardBlockEntity be && !be.hasTop()) {
            if (!level.isClientSide()) {
                be.setFinishes(be.body(), worktop.top);
                held.consume(1, player);
                SoundType sound = Finishes.isWood(worktop.top) ? SoundType.WOOD : SoundType.STONE;
                level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!state.getValue(ASSEMBLED)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof CupboardBlockEntity cupboard) {
            player.openMenu(cupboard);
            player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
            PiglinAi.angerNearbyPiglins(server, player, true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof CupboardBlockEntity be)) return;
        be.recheckOpen();
        if (!state.getValue(ASSEMBLED) && be.settleEnd > 0) {
            long left = be.settleEnd - level.getGameTime();
            if (left > 0) {
                level.scheduleTick(pos, this, (int) left);
                return;
            }
            be.settleEnd = 0;
            if (!be.hasTop()) return;
            level.setBlock(pos, state.setValue(ASSEMBLED, true), 3);
            level.playSound(null, pos, getSoundType(state).getPlaceSound(), SoundSource.BLOCKS, 0.8F, 1.1F);
        }
    }

    public static void unscrew(Level level, BlockPos pos, BlockState state, CupboardBlockEntity be) {
        Containers.dropContents(level, pos, be);
        be.clearContent();
        if (be.hasTop()) popResource(level, pos.above(), new ItemStack(ModContent.WORKTOPS.get(be.top())));
        be.setFinishes(be.body(), "");
        level.setBlock(pos, state.setValue(ASSEMBLED, false), 3);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> out = new ArrayList<>();
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CupboardBlockEntity be) {
            if (state.getValue(ASSEMBLED)) {
                out.add(ModContent.cupboard(be.body(), be.hasTop() ? be.top() : "oak"));
            } else {
                out.add(new ItemStack(ModContent.CARCASSES.get(be.body())));
                if (be.hasTop()) out.add(new ItemStack(ModContent.WORKTOPS.get(be.top())));
            }
        }
        return out;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        if (!(level.getBlockEntity(pos) instanceof CupboardBlockEntity be)) return new ItemStack(ModContent.CUPBOARD_ITEM);
        return state.getValue(ASSEMBLED) ? ModContent.cupboard(be.body(), be.top()) : new ItemStack(ModContent.CARCASSES.get(be.body()));
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
