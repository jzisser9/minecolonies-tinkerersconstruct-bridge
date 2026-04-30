package com.minecolonies.tconstructbridge.mixin;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.core.entity.ai.workers.crafting.EntityAIWorkBlacksmith;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBlacksmith;
import com.minecolonies.core.colony.jobs.JobBlacksmith;
import com.minecolonies.tconstructbridge.TiCRepairHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = EntityAIWorkBlacksmith.class, remap = false)
public abstract class EntityAIWorkBlacksmithMixin extends com.minecolonies.core.entity.ai.workers.crafting.AbstractEntityAICrafting<JobBlacksmith, BuildingBlacksmith> {

    public EntityAIWorkBlacksmithMixin(JobBlacksmith job) {
        super(job);
    }

    @Inject(method = "decide", at = @At("HEAD"), cancellable = true, remap = false)
    private void onDecide(CallbackInfoReturnable<IAIState> cir) {
        for (IItemHandler handler : this.building.getHandlers()) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains("TiCRepairMaterial") && stack.getTag().contains("TiCOwnerUUID")) {
                    String materialTagStr = stack.getTag().getString("TiCRepairMaterial");
                    UUID ownerUUID = stack.getTag().getUUID("TiCOwnerUUID");
                    ResourceLocation repairTag = new ResourceLocation(materialTagStr);

                    // Search for the repair material in the building's inventory
                    for (IItemHandler matHandler : this.building.getHandlers()) {
                        for (int j = 0; j < matHandler.getSlots(); j++) {
                            ItemStack matStack = matHandler.getStackInSlot(j);
                            if (!matStack.isEmpty() && matStack.is(net.minecraft.tags.ItemTags.create(repairTag))) {
                                // Found broken tool and material!

                                // 1. Extract the tool
                                ItemStack toolToRepair = handler.extractItem(i, 1, false);
                                if (toolToRepair.isEmpty()) continue;

                                // 2. Consume the material
                                matHandler.extractItem(j, 1, false);

                                // 3. Repair the tool
                                TiCRepairHelper.repairTool(toolToRepair);

                                // 4. Clean up our custom tags
                                toolToRepair.getOrCreateTag().remove("TiCRepairMaterial");
                                toolToRepair.getOrCreateTag().remove("TiCOwnerUUID");

                                // 5. Play Blacksmith repair sound
                                if (this.worker instanceof com.minecolonies.core.entity.citizen.EntityCitizen) {
                                    this.job.playSound(this.building.getPosition(), (com.minecolonies.core.entity.citizen.EntityCitizen) this.worker);
                                }

                                // 6. Return repaired tool to worker via courier
                                // We create the delivery request using a copy of the repaired tool.
                                com.minecolonies.api.colony.requestsystem.location.ILocation start = new com.minecolonies.core.colony.requestsystem.locations.StaticLocation(this.building.getPosition(), this.building.getColony().getDimension());
                                com.minecolonies.api.colony.requestsystem.location.ILocation target = new com.minecolonies.core.colony.requestsystem.locations.EntityLocation(ownerUUID);
                                com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery delivery = new com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery(start, target, toolToRepair.copy(), 1);
                                this.building.createRequest(delivery, true);

                                // 7. Place repaired tool back in the building's output rack (rack/handler)
                                // Use forceTransferStack to ensure it's placed correctly in the building's inventory
                                this.building.forceTransferStack(toolToRepair, this.world);

                                // Stop further decision making for this tick
                                cir.setReturnValue(com.minecolonies.core.entity.ai.workers.crafting.AbstractEntityAICrafting.NO_CHANGE);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
