package com.minecolonies.tconstructbridge;

import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class TiCDurabilityHelper {

    /**
     * Checks if the given ItemStack is a Tinkers' Construct tool.
     *
     * @param stack The ItemStack to check.
     * @return true if it is a Tinkers' Construct tool, false otherwise.
     */
    public static boolean isTiCTool(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof IModifiable;
    }

    /**
     * Gets the current durability of the Tinkers' Construct tool.
     *
     * @param stack The ItemStack representing the tool.
     * @return The current durability, or -1 if the item is not a Tinkers' tool.
     */
    public static int getCurrentDurability(ItemStack stack) {
        if (!isTiCTool(stack)) {
            return -1;
        }
        ToolStack toolStack = ToolStack.from(stack);
        return toolStack.getCurrentDurability();
    }

    /**
     * Gets the maximum durability of the Tinkers' Construct tool.
     *
     * @param stack The ItemStack representing the tool.
     * @return The maximum durability, or -1 if the item is not a Tinkers' tool.
     */
    public static int getMaxDurability(ItemStack stack) {
        if (!isTiCTool(stack)) {
            return -1;
        }
        ToolStack toolStack = ToolStack.from(stack);
        return toolStack.getStats().getInt(ToolStats.DURABILITY);
    }

    /**
     * Checks if the Tinkers' Construct tool is broken.
     *
     * @param stack The ItemStack representing the tool.
     * @return true if the tool is broken, false otherwise or if it is not a Tinkers' tool.
     */
    public static boolean isBroken(ItemStack stack) {
        if (!isTiCTool(stack)) {
            return false;
        }
        ToolStack toolStack = ToolStack.from(stack);
        return toolStack.isBroken();
    }
}
