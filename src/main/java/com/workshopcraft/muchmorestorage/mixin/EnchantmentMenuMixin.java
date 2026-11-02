package com.workshopcraft.muchmorestorage.mixin;

//? if fabric {
import com.workshopcraft.muchmorestorage.block.BokwurmBlock;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    @Shadow @Final private ContainerLevelAccess access;

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    private void muchmorestorage$bokwurmStart(Container container, CallbackInfo ci) {
        BokwurmBlock.TABLE_EXTRA.set(access.evaluate(BokwurmBlock::tablePower, 0));
    }

    @Inject(method = "slotsChanged", at = @At("RETURN"))
    private void muchmorestorage$bokwurmEnd(Container container, CallbackInfo ci) {
        BokwurmBlock.TABLE_EXTRA.remove();
    }
}
//?}
