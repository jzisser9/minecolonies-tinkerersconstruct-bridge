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

    @Shadow
    @org.spongepowered.asm.mixin.Final
    private com.minecolonies.api.colony.ICitizenData citizen;

    @Inject(method = "damageInventoryItem", at = @At("RETURN"), cancellable = true, remap = false)
    private <T extends LivingEntity> void onDamageInventoryItem(int slot, int amount, T entity, Consumer<T> onBreak, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) { // if it returns true, it broke
            ItemStack stack = this.mainInventory.get(slot);
            if (stack != null && !stack.isEmpty() && stack.getItem() instanceof IModifiable) {
                // Identify the repair material using our new TiCMaterialResolver
                ResourceLocation headMaterialId = TiCMaterialResolver.getHeadMaterialId(stack);
                if (headMaterialId != null) {
                    ResourceLocation repairTag = TiCMaterialResolver.getRawMaterialTag(headMaterialId);
                    // Append the resolved tag to the broken tool's NBT data
                    stack.getOrCreateTag().putString("TiCRepairMaterial", repairTag.toString());

                    com.minecolonies.api.colony.IColony colony = this.citizen.getColony();
                    if (colony != null) {
                        com.minecolonies.api.colony.buildings.IBuilding blacksmith = colony.getBuildingManager().getFirstBuildingMatching(b -> b instanceof com.minecolonies.core.colony.buildings.workerbuildings.BuildingBlacksmith);
                        if (blacksmith != null) {
                            // TiC tool shouldn't be deleted, so override return value to false
                            cir.setReturnValue(false);

                            // Part 1: Courier picks up the broken tool and delivers it to the Blacksmith hut.
                            com.minecolonies.api.colony.requestsystem.location.ILocation start = new com.minecolonies.core.colony.requestsystem.locations.EntityLocation(this.citizen.getUUID());
                            com.minecolonies.api.colony.requestsystem.location.ILocation target = new com.minecolonies.core.colony.requestsystem.locations.StaticLocation(blacksmith.getPosition(), colony.getDimension());
                            com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery delivery = new com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery(start, target, stack, 1);
                            colony.getRequestManager().createAndAssignRequest(blacksmith.getRequester(), delivery);

                            // Part 2: Courier fetches the required repair items from the Warehouse and delivers them to the Blacksmith.
                            net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tagKey = net.minecraft.tags.ItemTags.create(repairTag);
                            com.minecolonies.api.colony.requestsystem.requestable.RequestTag reqTag = new com.minecolonies.api.colony.requestsystem.requestable.RequestTag(tagKey, 1);
                            colony.getRequestManager().createAndAssignRequest(blacksmith.getRequester(), reqTag);
                        } else {
                            // Fallback: If no Blacksmith exists, deposit the tool directly into the NPC's hut inventory so the player can retrieve it.
                            com.minecolonies.api.colony.buildings.IBuilding hut = this.citizen.getWorkBuilding();
                            if (hut == null) {
                                hut = this.citizen.getHomeBuilding();
                            }
                            if (hut != null) {
                                ItemStack remaining = hut.forceTransferStack(stack.copy(), colony.getWorld());
                                stack.setCount(remaining.getCount());
                                if (stack.isEmpty()) {
                                    cir.setReturnValue(true); // Effectively delete from citizen inventory as it is fully transferred
                                } else {
                                    cir.setReturnValue(false); // Keep what wasn't transferred
                                }
                            } else {
                                cir.setReturnValue(false); // Nowhere to deposit, keep it in inventory
                            }
                        }
                    } else {
                        cir.setReturnValue(false);
                    }
                } else {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
