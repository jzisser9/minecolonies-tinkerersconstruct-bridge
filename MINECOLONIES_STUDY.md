# MineColonies Codebase Study

## Project Overview
MineColonies is a complex Minecraft mod that implements a colony simulation. It features NPC workers, building systems, and a progression-based research tree.

## Architecture
- **API (`com.minecolonies.api`)**: Defines the public interfaces for colony management, citizen data, buildings, and jobs. Key interface: `IMinecoloniesAPI`.
- **Core (`com.minecolonies.core`)**: Contains the primary logic of the mod.
    - `MineColonies.java`: The mod's main entry point and event subscriber.
    - `colony`: Manages colony life, citizen data (`CitizenDataManager`), and colony-wide systems.
    - `entity`: Handles NPC AI, citizen behaviors, and raider entities.
    - `network`: Custom networking for syncing colony state between server and client.
- **API Implementation (`com.minecolonies.apiimp`)**: Concrete implementations of the API interfaces, usually delegating to the core logic.
- **Data Generation (`com.minecolonies.core.generation`)**: Extensive use of Forge's data generation system to produce recipes, loot tables, and research configurations.

## Build System
- **Gradle**: Uses Gradle 8.1.1 (upgraded to 8.5 for Java 21 compatibility).
- **Forge**: Built on Minecraft Forge 1.20.1-47.1.3.
- **Dependencies**: Depends on `structurize`, `blockui`, `domum_ornamentum`, and `multi-piston`.

## Observations
- The project does not use standard JUnit tests in `src/test/java`. Most verification seems to happen through data generation and potentially integration testing.
- The build process requires a specific Java version (17 is recommended, 21 is possible with Gradle upgrades).
- Networking is modularized into `client` and `server` message packages.
