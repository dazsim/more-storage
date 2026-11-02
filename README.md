# Much More Storage

Kitchen cupboards that hold 27 stacks, same as a chest. The door swings open on its hinge when you use one.

## Building a cupboard

Cupboards come flat packed in two parts, so any body can go with any top.

1. Craft a carcass: eight planks or concrete around a chest. Place it and you get an open frame with no top. It can't be opened yet.
2. Craft a worktop and use it on the carcass. The worktop sits on top loose.
3. Use a screwdriver (iron ingot over a stick) on the carcass. The worktop drops into place and the cupboard is ready to use.

Worktops are made from three slabs in a row (any wood, smooth stone, polished granite, polished diorite or polished andesite), from three concrete in a row (makes two), or the speckled granite one from polished andesite, polished diorite and polished andesite slabs.

Bodies: the twelve woods and the sixteen concrete colours. Wood bodies are quickest with an axe, concrete with a pickaxe.

## Screwdriver

- On a loose worktop: screws it down.
- On a cupboard: swaps the door hinge between left and right.
- Sneaking on a cupboard: unscrews it. The worktop pops off, the contents drop and it goes back to a carcass.

Any worktop in the `c:worktops` tag fits a carcass, so Simple Machines worktops work too, and any screwdriver in `c:tools/screwdriver` works on cupboards. Neither mod needs the other.

Breaking a cupboard drops one cupboard item that keeps its body and top, and it places back already assembled. A carcass drops itself, plus the worktop if one was sitting on it.

With Simple Books installed there is a picture manual: craft a book, a screwdriver and a chest (you keep the screwdriver), or get one the first time you pick up any screwdriver. Its pages are drawn by `python3 tools/gen_manual.py`, which `gen_resources.py` runs for you.

Minecraft 1.20.1 (Forge and Fabric), 1.21.1, 26.1 to 26.1.2 and 26.3 (NeoForge and Fabric). Cupboards from a 1.20.1 world keep their body and top after an upgrade to 1.21.1 or later.

1.0.2: worktops and screwdrivers from other mods work through the `c:worktops` and `c:tools/screwdriver` tags.

1.0.3: the manual is book + any screwdriver + chest and the screwdriver comes back, so it no longer clashes with the More Foods knife. It is named by Simple Books, and the screwdriver recipe shows once you have iron or a stick.

1.1.0: the KUBHÅLA cube shelf with drawer and door inserts.

## KUBHÅLA cube shelf

A square open shelf that holds 9 stacks. Cubes next to each other, above or below, facing the same way join into one grid with thin dividers. An open cube shows what is inside.

1. Craft a flat pack: eight planks or concrete around an iron nugget (makes two). Every wood and concrete colour works.
2. Place it and use a screwdriver on it. The boards stand up and it becomes a cube.
3. Craft a drawer (slabs around an iron nugget, open at the top) or a door (two columns of slabs with a nugget). Use it on a cube, then screw it in with a screwdriver. A drawer slides out and a door swings open when you use the cube; the contents are hidden while it is closed.

Screwdriver on a cube with an insert pops the insert out and the contents stay. Sneaking with a screwdriver on a bare cube takes it back to a flat pack and drops the contents. Breaking a cube drops a cube item that keeps its finish, plus the insert and contents.

## Building

The build uses [Stonecutter](https://stonecutter.kikugie.dev/) so every Minecraft version and loader comes from the one source tree in `src`. Version specific values are in `stonecutter.properties.toml` and the versions built are listed in `settings.gradle.kts`.

Build with JDK 25, one node at a time, for example `./gradlew :26.1.2-fabric:build` or `./gradlew :1.20.1-forge:build`. Jars end up in `versions/<version>-<loader>/build/libs/`. Add `-Pversion=1.0.4-alpha.1` for a test jar.

Textures are drawn by `python3 tools/gen_textures.py` and the json files are made by `python3 tools/gen_resources.py`, both run from the repo root.
