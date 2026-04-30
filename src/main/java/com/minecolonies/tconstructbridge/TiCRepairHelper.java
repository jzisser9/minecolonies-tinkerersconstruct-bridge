package com.minecolonies.tconstructbridge;

import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;

public class TiCRepairHelper {

    /**
     * Repairs a Tinkers' Construct tool using the internal API.
     *
     * @param stack The tool ItemStack to repair.
     */
    public static void repairTool(ItemStack stack) {
        if (!TiCDurabilityHelper.isTiCTool(stack)) {
            return;
        }

        ToolStack tool = ToolStack.from(stack);

        // Use ToolDamageUtil to repair the tool.
        // 1000000 is a safe large number to fully repair any tool.
        ToolDamageUtil.repair(tool, 1000000);
    }
}
