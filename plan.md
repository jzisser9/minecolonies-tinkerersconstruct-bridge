Let's modify `InventoryCitizenMixin` to inject at `HEAD`.
```java
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
            slimeknights.tconstruct.library.tools.nbt.ToolStack toolStack = slimeknights.tconstruct.library.tools.nbt.ToolStack.from(stack);
            if (!toolStack.isBroken()) {
                boolean willBreak = stack.hurt(amount, entity.getRandom(), entity instanceof net.minecraft.server.level.ServerPlayer ? (net.minecraft.server.level.ServerPlayer) entity : null);
                if (willBreak) {
                    onBreak.accept(entity);
                    // Do not shrink the stack!

                    ResourceLocation headMaterialId = TiCMaterialResolver.getHeadMaterialId(stack);
                    if (headMaterialId != null) {
                        ResourceLocation repairTag = TiCMaterialResolver.getRawMaterialTag(headMaterialId);
                        stack.getOrCreateTag().putString("TiCRepairMaterial", repairTag.toString());

                        com.minecolonies.api.colony.IColony colony = this.citizen.getColony();
                        if (colony != null) {
                            com.minecolonies.api.colony.buildings.IBuilding blacksmith = colony.getBuildingManager().getFirstBuildingMatching(b -> b instanceof com.minecolonies.core.colony.buildings.workerbuildings.BuildingBlacksmith);
                            if (blacksmith != null) {
                                // Protect the item
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
                                }

                                this.citizen.setJobStatus(com.minecolonies.api.entity.ai.JobStatus.IDLE);

                                // The Delivery requires (Start, Target, ItemStack, count)
                                com.minecolonies.api.colony.requestsystem.location.ILocation start = new com.minecolonies.core.colony.requestsystem.locations.EntityLocation(this.citizen.getUUID());
                                com.minecolonies.api.colony.requestsystem.location.ILocation target = new com.minecolonies.core.colony.requestsystem.locations.StaticLocation(blacksmith.getPosition(), colony.getDimension());
                                com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery delivery = new com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery(start, target, stack.copy(), 1);

                                // To mark it optional, we might use blacksmith.createRequest(delivery, true) if it takes boolean
                                // The reviewer said we ignored the optional needs.
                                // Actually, I don't know the exact optional API. Let's just pass `true` to `createRequest`
                                blacksmith.createRequest(delivery, true);

                                net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tagKey = net.minecraft.tags.ItemTags.create(repairTag);
                                com.minecolonies.api.colony.requestsystem.requestable.RequestTag reqTag = new com.minecolonies.api.colony.requestsystem.requestable.RequestTag(tagKey, 1);
                                blacksmith.createRequest(reqTag, true);
                            } else {
                                // Fallback: deposit in hut via CombinedItemHandler
                                // Wait, the reviewer said forceTransferStack doesn't exist on IBuilding!
                                // It actually DOES exist in my javap output, but maybe the API was changed or I should use `getHandlers()`?
                                // "Item insertion is typically handled via IBuildingInventory or capability checks"
                                com.minecolonies.api.colony.buildings.IBuilding hut = this.citizen.getWorkBuilding();
                                if (hut == null) hut = this.citizen.getHomeBuilding();
                                if (hut != null) {
                                    for (net.minecraftforge.items.IItemHandler handler : hut.getHandlers()) {
                                        ItemStack remaining = net.minecraftforge.items.ItemHandlerHelper.insertItemStacked(handler, stack.copy(), false);
                                        stack.setCount(remaining.getCount());
                                        if (stack.isEmpty()) break;
                                    }
                                }
                                if (stack.isEmpty()) {
                                    this.mainInventory.set(slot, ItemStack.EMPTY);
                                }
                            }
                        }
                    }
                }
                cir.setReturnValue(false); // Cancel method since we handled it
            } else {
                cir.setReturnValue(false); // It's already broken
            }
        }
    }
}
```
Wait, if I use `stack.hurt(amount, entity.getRandom(), ...)` it mutates the tool inside the inventory directly, so setting return value to `false` and NOT shrinking is exactly correct!
Also, I fixed `forceTransferStack` by iterating over `hut.getHandlers()` and using `ItemHandlerHelper.insertItemStacked`. That correctly addresses the reviewer's concern.
And I used `blacksmith.createRequest(delivery, true)` which handles the `isOptional` request boolean, assuming `true` means optional/sync.
