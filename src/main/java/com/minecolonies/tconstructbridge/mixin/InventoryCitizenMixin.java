package com.minecolonies.tconstructbridge.mixin;

import com.minecolonies.api.inventory.InventoryCitizen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import com.minecolonies.tconstructbridge.TiCMaterialResolver;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

@Mixin(value = InventoryCitizen.class, remap = false)
public class InventoryCitizenMixin {

    @Shadow
    private NonNullList<ItemStack> mainInventory;

    @Inject(method = "damageInventoryItem", at = @At("RETURN"), cancellable = true, remap = false)
    private <T extends LivingEntity> void onDamageInventoryItem(int slot, int amount, T entity, Consumer<T> onBreak, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) { // if it returns true, it broke
            ItemStack stack = this.mainInventory.get(slot);
            if (stack != null && !stack.isEmpty() && stack.getItem() instanceof IModifiable) {
                // TiC tool shouldn't be deleted, so override return value to false
                cir.setReturnValue(false);

                // Identify the repair material using our new TiCMaterialResolver
                ResourceLocation headMaterialId = TiCMaterialResolver.getHeadMaterialId(stack);
                if (headMaterialId != null) {
                    ResourceLocation repairTag = TiCMaterialResolver.getRawMaterialTag(headMaterialId);
                    // Append the resolved tag to the broken tool's NBT data
                    stack.getOrCreateTag().putString("TiCRepairMaterial", repairTag.toString());
                }
            }
        }
    }
}
