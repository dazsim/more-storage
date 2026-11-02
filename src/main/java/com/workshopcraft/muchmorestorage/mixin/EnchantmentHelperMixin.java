package com.workshopcraft.muchmorestorage.mixin;

//? if fabric {
import com.workshopcraft.muchmorestorage.block.BokwurmBlock;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// BOKWURM power found by EnchantmentMenuMixin is added before vanilla caps the total at 15
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @ModifyVariable(method = "getEnchantmentCost", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private static int muchmorestorage$bokwurm(int bookcases) {
        Integer extra = BokwurmBlock.TABLE_EXTRA.get();
        return extra == null ? bookcases : bookcases + extra;
    }
}
//?}
