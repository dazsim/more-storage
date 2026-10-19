package com.workshopcraft.muchmorestorage.item;

import com.workshopcraft.muchmorestorage.block.CupboardBlock;
import com.workshopcraft.muchmorestorage.block.CupboardBlockEntity;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ScrewdriverItem extends Item {
    public ScrewdriverItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CupboardBlock) || !(level.getBlockEntity(pos) instanceof CupboardBlockEntity be)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        boolean sneaking = player != null && player.isSecondaryUseActive();
        boolean assembled = state.getValue(CupboardBlock.ASSEMBLED);
        if (sneaking && assembled) {
            CupboardBlock.unscrew(level, pos, state, be);
            ratchet(level, pos, 1.3F);
        } else if (sneaking && be.hasTop() && be.settleEnd == 0) {
            Block.popResource(level, pos.above(), new ItemStack(ModContent.WORKTOPS.get(be.top())));
            be.setFinishes(be.body(), "");
            click(level, pos, 1.2F);
        } else if (!assembled && be.hasTop()) {
            if (be.startSettle()) {
                click(level, pos, 1.2F);
                ratchet(level, pos, 1.6F);
            }
        } else {
            level.setBlock(pos, state.cycle(CupboardBlock.HINGE), 3);
            click(level, pos, 1.6F);
        }
        return InteractionResult.SUCCESS;
    }

    private static void click(Level level, BlockPos pos, float pitch) {
        level.playSound(null, pos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.6F, pitch);
    }

    private static void ratchet(Level level, BlockPos pos, float pitch) {
        level.playSound(null, pos, SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.BLOCKS, 0.5F, pitch);
    }
}
