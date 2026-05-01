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
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
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

    @Inject(method = "damageInventoryItem", at = @At("HEAD"), cancellable = true, remap = false)
    private <T extends LivingEntity> void onDamageInventoryItem(int slot, int amount, T entity, Consumer<T> onBreak, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.mainInventory.get(slot);
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof IModifiable) {
            ToolStack toolStack = ToolStack.from(stack);
            if (!toolStack.isBroken()) {
                boolean willBreak = stack.hurt(amount, entity.getRandom(), entity instanceof net.minecraft.server.level.ServerPlayer ? (net.minecraft.server.level.ServerPlayer) entity : null);
                if (willBreak) {
                    onBreak.accept(entity);

                    ResourceLocation headMaterialId = TiCMaterialResolver.getHeadMaterialId(stack);
                    if (headMaterialId != null) {
                        ResourceLocation repairTag = TiCMaterialResolver.getRawMaterialTag(headMaterialId);
                        stack.getOrCreateTag().putString("TiCRepairMaterial", repairTag.toString());
                        stack.getOrCreateTag().putUUID("TiCOwnerUUID", this.citizen.getUUID());

                        com.minecolonies.api.colony.IColony colony = this.citizen.getColony();
                        if (colony != null) {
                            com.minecolonies.api.colony.buildings.IBuilding blacksmith = colony.getBuildingManager().getFirstBuildingMatching(b -> b instanceof com.minecolonies.core.colony.buildings.workerbuildings.BuildingBlacksmith);
                            if (blacksmith != null) {
                                // Protect the item by moving it to an empty slot
                                int emptySlot = -1;
                                for (int i = 0; i < this.mainInventory.size(); i++) {
                                    if (this.mainInventory.get(i).isEmpty() && i != slot) {
                                        emptySlot = i;
                                        break;
                                    }
                                }
                                if (emptySlot != -1) {
                                    this.mainInventory.set(emptySlot, stack);
                                    this.mainInventory.set(slot, ItemStack.EMPTY);
                                } else {
                                    // Fallback if inventory is full: swap with a different slot to get it out of the active hand
                                    int swapSlot = (slot == 0) ? 1 : 0;
                                    ItemStack swapItem = this.mainInventory.get(swapSlot);
                                    this.mainInventory.set(swapSlot, stack);
                                    this.mainInventory.set(slot, swapItem);
                                }

                                // Set citizen state to stop working
                                this.citizen.setJobStatus(com.minecolonies.api.entity.ai.JobStatus.IDLE);

                                // Part 1: Courier picks up the broken tool and delivers it to the Blacksmith hut.
                                com.minecolonies.api.colony.requestsystem.location.ILocation start = new com.minecolonies.core.colony.requestsystem.locations.EntityLocation(this.citizen.getUUID());
                                com.minecolonies.api.colony.requestsystem.location.ILocation target = new com.minecolonies.core.colony.requestsystem.locations.StaticLocation(blacksmith.getPosition(), colony.getDimension());
                                com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery delivery = new com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery(start, target, stack, 1);
                                blacksmith.createRequest(delivery, true); // true = optional/sync

                                // Part 2: Courier fetches the required repair items from the Warehouse and delivers them to the Blacksmith.
                                net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tagKey = net.minecraft.tags.ItemTags.create(repairTag);
                                com.minecolonies.api.colony.requestsystem.requestable.RequestTag reqTag = new com.minecolonies.api.colony.requestsystem.requestable.RequestTag(tagKey, 1);
                                blacksmith.createRequest(reqTag, true); // true = optional/sync
                            } else {
                                // Fallback: deposit in hut using capability
                                com.minecolonies.api.colony.buildings.IBuilding hut = this.citizen.getWorkBuilding();
                                if (hut == null) hut = this.citizen.getHomeBuilding();
                                if (hut != null) {
                                    for (net.minecraftforge.items.IItemHandler handler : hut.getHandlers()) {
                                        ItemStack remaining = net.minecraftforge.items.ItemHandlerHelper.insertItemStacked(handler, stack.copy(), false);
                                        stack.setCount(remaining.getCount());
                                        if (stack.isEmpty()) {
                                            this.mainInventory.set(slot, ItemStack.EMPTY);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Always return false to cancel further vanilla processing so TiC handles its own state
                cir.setReturnValue(false);
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}
