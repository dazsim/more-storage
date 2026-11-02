package com.workshopcraft.muchmorestorage.mixin;

//? if fabric {
import com.workshopcraft.muchmorestorage.block.BokwurmBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.20.5 {
@Mixin(net.minecraft.world.level.block.EnchantingTableBlock.class)
//?} else {
/*@Mixin(net.minecraft.world.level.block.EnchantmentTableBlock.class)
*///?}
public abstract class EnchantingTableBlockMixin {
    @Inject(method = "animateTick", at = @At("TAIL"))
    private void muchmorestorage$bokwurmParticles(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        for (BlockPos offset : BokwurmBlock.SHELF_OFFSETS) {
            if (random.nextInt(16) == 0 && BokwurmBlock.reaches(level, pos, offset)) {
                level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5,
                        offset.getX() + random.nextFloat() - 0.5, offset.getY() - random.nextFloat() - 1.0F, offset.getZ() + random.nextFloat() - 0.5);
            }
        }
    }
}
//?}
