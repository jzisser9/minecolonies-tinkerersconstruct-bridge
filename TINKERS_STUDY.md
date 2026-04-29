# Tinkers' Construct Codebase Study

## Repository Info
- URL: https://github.com/slimeknights/tinkersconstruct
- Mod ID: `tconstruct`
- Version Studied: 1.20.1 (specifically tag v3.10.2.92)
- Forge Version: 47.2.6
- Base Minecraft Version: 1.20.1
- License: MIT License

## Dependencies
- **Mantle**: Key library used by SlimeKnights mods (version 1.11.69+)
- **JEI (Just Enough Items)**: Used for optional compatibility (version 15.20.+)
- **Json Things**: Used for optional compatibility (version 0.9.9+)
- **Parchment**: Used for mappings (version 2023.09.03)

## Architecture & Code Structure
The primary source code is located in `src/main/java/slimeknights/tconstruct/`.
The mod's base package is `slimeknights.tconstruct`.

Key directories/packages:
- `TConstruct.java`: The main mod file containing the `@Mod("tconstruct")` annotation and setup logic.
- **`library`**: Contains the core APIs and systems used to build tools and materials.
  - `library.materials`: Handles material definitions (`IMaterial`), stats, and traits. Materials are modular and data-driven.
  - `library.tools`: Defines the tools, parts, standard items (`IModifiable`), and tool layouts.
  - `library.modifiers`: The modifier system. Modifiers are logic applied to tools (like sharpness, luck, etc.).
  - `library.recipe`: Custom recipe types and serializers, e.g., for the Tinker Station (`ITinkerStationRecipe`), melter, and casting.
- **`common`**: General purpose code, tags (`TinkerTags`), network packets, and registration (`TinkerModule`).
- **`smeltery`**: Code relating to the Smeltery multiblock, casting, and fluid handling.
- **`tools`**: Implementations of the specific tools (pickaxes, swords) and modifier implementations.
- **`world`**: Worldgen stuff (slime islands, geodes).
- **`tables`**: The crafting tables, part builder, tinker station blocks/entities.
- **`fluids`**: Fluid definitions (molten metals).

## Key Systems
1. **Materials**: Defined using the `IMaterial` interface. Materials dictate base stats when used in specific tool parts (head, handle, binding).
2. **Tools**: Tool logic uses an NBT-based system (`ToolStack`) rather than standard ItemStack properties. `IModifiable` is the base interface for items that can receive modifiers. Tools are built from parts (or standard materials) at the Tinker Station.
3. **Modifiers**: The "enchantments" of Tinkers' Construct. They are applied to tools, consume modifier slots (or specific slot types like Upgrades/Abilities), and add functional behaviors.
4. **Data-Driven Design**: In 1.20.1, heavily relies on JSONs for defining materials, modifiers, and stats. The Java side provides the framework and serializers.
