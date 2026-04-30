1. Update `build.gradle` to add Mixin compilation dependencies and configure the mixin plugin.
2. Update `src/main/resources/META-INF/mods.toml` to load the mixins config.
3. Create `src/main/resources/mixins.minecolonies_tconstruct_bridge.json` to register the Mixin classes for the mod.
4. Create `src/main/java/com/minecolonies/tconstructbridge/mixin/ItemStackUtilsMixin.java` and inject into `getDurability` (`@Inject(method = "getDurability", at = @At("HEAD"), cancellable = true)`). Use `ToolStack.from(stack).isBroken()` to check if it's broken; if true return `0`. Else if it's a TiC tool, return the current durability using `ToolStack.from(stack).getCurrentDurability()`.
5. Create `src/main/java/com/minecolonies/tconstructbridge/mixin/InventoryCitizenMixin.java` and inject into `damageInventoryItem` (`@Inject(method = "damageInventoryItem", at = @At("RETURN"), cancellable = true)`). Override the return value to `false` if the item is a Tinkers' Construct tool.
6. Verify file modifications using `cat` to ensure `build.gradle`, `META-INF/mods.toml` and the Mixin Java classes were correctly created/updated.
7. Compile and test using `./gradlew build` to verify the mod compiles successfully with the new Mixins.
8. Complete pre-commit steps to ensure proper testing, verification, review, and reflection are done.
9. Submit the changes.
