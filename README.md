# Acorn

A [Fabric](https://fabricmc.net/) mod addon for **Telos Realms** (Minecraft 1.21.10) that extends [Melinoe](https://modrinth.com/mod/melinoe) with quality-of-life features.

## Features

### Auto React
Automatically reacts in chat to notable in-game events.

- **Auto F** — Sends a configurable message (default: `f`) when a player death is announced. Only fires for deaths on your current server.
- **Auto GG** — Sends a configurable message (default: `gg`) when:
  - A **Bloodshot**, **Unholy**, or **Companion** item drop is broadcast
  - A player **fully transcends** a class (e.g. `Has just fully transcended Samurai! (3/6)`)

  Each rarity and transcend can be individually toggled. Cross-server broadcasts are filtered out — reactions only fire for events on your current hub.

  Cooldown: 1.5 seconds between reactions to prevent spam.

### Chat Shortcuts
Client-side commands for quickly sending messages to different chat channels.

| Command | Channel |
|---------|---------|
| `/ac <message>` | Default chat |
| `/gc <message>` | Guild chat |
| `/cc <message>` | Group chat |

A **Default Chat** setting lets you choose which chat mode to return to after a shortcut is used (Default, Guild, or Group).

## Requirements

- Minecraft **1.21.10**
- [Fabric Loader](https://fabricmc.net/use/installer/)
- [Melinoe](https://modrinth.com/mod/melinoe) (required dependency)

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.10.
2. Download and install [Melinoe](https://modrinth.com/mod/melinoe).
3. Drop the Acorn `.jar` into your `mods` folder.

## Building from Source

```bash
./gradlew build
```

The output jar will be in `acorn/build/libs/`.

> **Note:** Melinoe must be installed to your local Maven repository (`~/.m2`) before building.
