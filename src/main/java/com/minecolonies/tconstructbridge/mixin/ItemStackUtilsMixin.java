package com.minecolonies.tconstructbridge.mixin;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mixin(value = ItemStackUtils.class, remap = false)
public class ItemStackUtilsMixin {
    @Inject(method = "getDurability(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetDurability(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof IModifiable) {
            ToolStack toolStack = ToolStack.from(stack);
            if (toolStack.isBroken()) {
                cir.setReturnValue(0);
            } else {
                cir.setReturnValue(toolStack.getCurrentDurability());
            }
        }
    }
}
