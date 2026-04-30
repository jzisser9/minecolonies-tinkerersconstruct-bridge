1. **Create `TiCDurabilityHelper` Utility Class**
   - Location: `src/main/java/com/minecolonies/tconstructbridge/utils/TiCDurabilityHelper.java` (or somewhere appropriate)
   - Add a method to check if an `ItemStack` is a Tinkers' Construct tool (e.g., `isTiCTool(ItemStack stack)`). From Tinkers' codebase, we can check `stack.getItem() instanceof slimeknights.tconstruct.library.tools.item.IModifiable`.
   - Add methods to retrieve:
     - Current durability (`getCurrentDurability(ItemStack stack)` -> `ToolStack.from(stack).getCurrentDurability()`)
     - Max durability (`getMaxDurability(ItemStack stack)` -> `ToolStack.from(stack).getStats().getInt(ToolStats.DURABILITY)`)
     - Broken state (`isBroken(ItemStack stack)` -> `ToolStack.from(stack).isBroken()`)

2. **Implement the logic using TiC API**
   - The Tinkers' API is in `slimeknights.tconstruct.library`.
   - `ToolStack.from(ItemStack)` loads the tool stack.
   - `toolStack.getCurrentDurability()` gives current durability.
   - `toolStack.getStats().getInt(ToolStats.DURABILITY)` gives max durability.
   - `toolStack.isBroken()` gives the broken state.

3. **Pre-commit Checks**
   - Ensure the new file has valid imports and compiles correctly.
   - Follow standard `AGENTS.md` testing and validation steps by obtaining pre-commit instructions.
   - Add tests if required by instructions.

4. **Submit Code**
