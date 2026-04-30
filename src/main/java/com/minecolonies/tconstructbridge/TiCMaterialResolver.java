package com.minecolonies.tconstructbridge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TiCMaterialResolver {

    private static final Map<String, String> MATERIAL_TO_TAG_MAP = new HashMap<>();

    static {
        // Special mappings for non-ingot or oddly named materials
        MATERIAL_TO_TAG_MAP.put("wood", "minecraft:logs");
        MATERIAL_TO_TAG_MAP.put("rock", "forge:stone");
        MATERIAL_TO_TAG_MAP.put("stone", "forge:stone");
        MATERIAL_TO_TAG_MAP.put("iron", "forge:ingots/iron");
        MATERIAL_TO_TAG_MAP.put("copper", "forge:ingots/copper");
        MATERIAL_TO_TAG_MAP.put("flint", "forge:flint");
        MATERIAL_TO_TAG_MAP.put("bone", "forge:bones");
        MATERIAL_TO_TAG_MAP.put("necrotic_bone", "forge:bones/wither");
        MATERIAL_TO_TAG_MAP.put("string", "forge:string");
        MATERIAL_TO_TAG_MAP.put("leather", "forge:leather");
        MATERIAL_TO_TAG_MAP.put("vine", "forge:vines");
        MATERIAL_TO_TAG_MAP.put("obsidian", "forge:obsidian");
        MATERIAL_TO_TAG_MAP.put("glass", "forge:glass");
        MATERIAL_TO_TAG_MAP.put("emerald", "forge:gems/emerald");
        MATERIAL_TO_TAG_MAP.put("diamond", "forge:gems/diamond");
        MATERIAL_TO_TAG_MAP.put("amethyst", "forge:gems/amethyst");
        MATERIAL_TO_TAG_MAP.put("quartz", "forge:gems/quartz");
        MATERIAL_TO_TAG_MAP.put("gold", "forge:ingots/gold");
        MATERIAL_TO_TAG_MAP.put("rose_gold", "forge:ingots/rose_gold");
        MATERIAL_TO_TAG_MAP.put("pig_iron", "forge:ingots/pig_iron");
        MATERIAL_TO_TAG_MAP.put("cobalt", "forge:ingots/cobalt");
        MATERIAL_TO_TAG_MAP.put("manyullyn", "forge:ingots/manyullyn");
        MATERIAL_TO_TAG_MAP.put("hepatizon", "forge:ingots/hepatizon");
        MATERIAL_TO_TAG_MAP.put("queens_slime", "forge:ingots/queens_slime");
        MATERIAL_TO_TAG_MAP.put("slimesteel", "forge:ingots/slimesteel");
        MATERIAL_TO_TAG_MAP.put("amethyst_bronze", "forge:ingots/amethyst_bronze");
        MATERIAL_TO_TAG_MAP.put("bloodbone", "forge:bloodbone");
        MATERIAL_TO_TAG_MAP.put("nahuatl", "forge:nahuatl");
        MATERIAL_TO_TAG_MAP.put("tinkers_bronze", "forge:ingots/tinkers_bronze");
    }

    /**
     * Identifies the material used for the 'Head' part of a Tinkers' Construct tool.
     *
     * @param stack The ItemStack representing the tool.
     * @return The ResourceLocation representing the material ID, or null if it cannot be found.
     */
    public static ResourceLocation getHeadMaterialId(ItemStack stack) {
        if (!TiCDurabilityHelper.isTiCTool(stack)) {
            return null;
        }

        ToolStack toolStack = ToolStack.from(stack);
        ToolDefinition definition = toolStack.getDefinition();

        List<MaterialStatsId> statTypes = definition.getData().getHook(ToolHooks.TOOL_MATERIALS).getStatTypes(definition);

        int headIndex = -1;
        for (int i = 0; i < statTypes.size(); i++) {
            if (statTypes.get(i).getPath().equals("head")) {
                headIndex = i;
                break;
            }
        }

        if (headIndex != -1) {
            MaterialVariantId variantId = toolStack.getMaterials().get(headIndex).getVariant();
            if (variantId != null && variantId.getId() != null) {
                return variantId.getId();
            }
        }

        return null;
    }

    /**
     * Maps a Tinkers' material ID to the standard Forge item tag that represents the raw repair material.
     *
     * @param materialId The ResourceLocation of the Tinkers' material.
     * @return The ResourceLocation of the corresponding Forge tag, or null if the input is null.
     */
    public static ResourceLocation getRawMaterialTag(ResourceLocation materialId) {
        if (materialId == null) {
            return null;
        }

        String path = materialId.getPath();

        if (MATERIAL_TO_TAG_MAP.containsKey(path)) {
            return new ResourceLocation(MATERIAL_TO_TAG_MAP.get(path));
        }

        // Default to forge:ingots/<material>
        return new ResourceLocation("forge", "ingots/" + path);
    }
}
