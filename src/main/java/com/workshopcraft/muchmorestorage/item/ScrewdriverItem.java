package com.workshopcraft.muchmorestorage.item;

import com.workshopcraft.muchmorestorage.block.CupboardBlock;
import com.workshopcraft.muchmorestorage.block.CupboardBlockEntity;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import com.workshopcraft.muchmorestorage.block.Insert;
import com.workshopcraft.muchmorestorage.block.KubhalaBlock;
import com.workshopcraft.muchmorestorage.block.KubhalaBlockEntity;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ScrewdriverItem extends Item {
    public ScrewdriverItem(Properties props) {
        super(props);
    }

    // called from the loaders' use-block events so any c:tools/screwdriver works, sneaking included
    public static boolean use(Level level, BlockPos pos, Player player, ItemStack held) {
        if (!held.is(ModContent.SCREWDRIVERS) || player.isSpectator()) return false;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof KubhalaBlock) return kubhala(level, pos, player, state);
        if (state.getBlock() instanceof com.workshopcraft.muchmorestorage.block.KladkaosBlock) return false;
        if (state.getBlock() instanceof FurnitureBlock) return furniture(level, pos, player, state);
        if (!(state.getBlock() instanceof CupboardBlock) || !(level.getBlockEntity(pos) instanceof CupboardBlockEntity be)) return false;
        if (!player.mayBuild()) return false;
        if (level.isClientSide()) return true;
        boolean sneaking = player.isSecondaryUseActive();
        boolean assembled = state.getValue(CupboardBlock.ASSEMBLED);
        if (sneaking && assembled) {
            CupboardBlock.unscrew(level, pos, state, be);
            ratchet(level, pos, 1.3F);
        } else if (sneaking && be.hasTop() && be.settleEnd == 0) {
            Block.popResource(level, pos.above(), ModContent.worktop(be.top()));
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
        return true;
    }

    private static boolean kubhala(Level level, BlockPos pos, Player player, BlockState state) {
        boolean sneaking = player.isSecondaryUseActive();
        if (!KubhalaBlock.screwdriverActs(state, sneaking) || !player.mayBuild()
                || !(level.getBlockEntity(pos) instanceof KubhalaBlockEntity be)) return false;
        if (level.isClientSide()) return true;
        boolean assembled = state.getValue(KubhalaBlock.ASSEMBLED);
        boolean popping = assembled && state.getValue(KubhalaBlock.INSERT) != Insert.NONE && (sneaking || state.getValue(KubhalaBlock.FITTED));
        if (!assembled && be.settleEnd > 0) return true;
        KubhalaBlock.screwdriver(level, pos, state, be, sneaking);
        if (popping) {
            click(level, pos, 1.2F);
        } else {
            click(level, pos, 1.4F);
            ratchet(level, pos, assembled ? 1.3F : 1.6F);
        }
        return true;
    }

    private static boolean furniture(Level level, BlockPos pos, Player player, BlockState state) {
        if (!FurnitureBlock.screwdriverActs(state, player.isSecondaryUseActive()) || !player.mayBuild()
                || !(level.getBlockEntity(pos) instanceof CupboardBlockEntity be)) return false;
        if (level.isClientSide()) return true;
        boolean assembled = state.getValue(FurnitureBlock.ASSEMBLED);
        if (!assembled && be.settleEnd > 0) return true;
        FurnitureBlock.screwdriver(level, pos, state, be);
        click(level, pos, 1.4F);
        ratchet(level, pos, assembled ? 1.3F : 1.6F);
        return true;
    }

    private static void click(Level level, BlockPos pos, float pitch) {
        level.playSound(null, pos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.6F, pitch);
    }

    private static void ratchet(Level level, BlockPos pos, float pitch) {
        //? if >=1.20.5 {
        level.playSound(null, pos, SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.BLOCKS, 0.5F, pitch);
        //?} else
        /*level.playSound(null, pos, SoundEvents.CROSSBOW_LOADING_MIDDLE, SoundSource.BLOCKS, 0.5F, pitch);*/
    }
}
