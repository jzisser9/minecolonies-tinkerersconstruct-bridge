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
                                toolToRepair.getTag().remove("TiCRepairMaterial");
                                toolToRepair.getTag().remove("TiCOwnerUUID");

                                // 5. Return repaired tool to worker via courier
                                com.minecolonies.api.colony.requestsystem.location.ILocation start = new com.minecolonies.core.colony.requestsystem.locations.StaticLocation(this.building.getPosition(), this.building.getColony().getDimension());
                                com.minecolonies.api.colony.requestsystem.location.ILocation target = new com.minecolonies.core.colony.requestsystem.locations.EntityLocation(ownerUUID);
                                com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery delivery = new com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery(start, target, toolToRepair, 1);
                                this.building.createRequest(delivery, true);

                                // 6. If courier can't pick it up immediately, put it back in the building's output rack (handler)
                                // In MineColonies, usually the tool is placed back and then the request is created.
                                // We'll put it back into the inventory so it can be picked up.
                                ItemHandlerHelper.insertItemStacked(handler, toolToRepair, false);

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
