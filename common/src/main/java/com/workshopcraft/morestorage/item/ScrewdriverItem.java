package com.workshopcraft.morestorage.item;

import com.workshopcraft.morestorage.block.CupboardBlock;
import com.workshopcraft.morestorage.block.CupboardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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
        if (!(state.getBlock() instanceof CupboardBlock)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (state.getValue(CupboardBlock.LOOSE)) {
                if (level.getBlockEntity(pos) instanceof CupboardBlockEntity be && be.startSettle()) {
                    level.playSound(null, pos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.6F, 1.2F);
                    level.playSound(null, pos, SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.BLOCKS, 0.5F, 1.6F);
                }
            } else {
                level.setBlock(pos, state.cycle(CupboardBlock.HINGE), 3);
                level.playSound(null, pos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.6F, 1.6F);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
