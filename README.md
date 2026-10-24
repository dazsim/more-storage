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

With Simple Books installed there is a picture manual: craft a book with a screwdriver, or get one the first time you pick a screwdriver up. Its pages are drawn by `python3 tools/gen_manual.py`, which `gen_resources.py` runs for you.

Minecraft 26.1 to 26.1.2, NeoForge and Fabric.

1.0.2: worktops and screwdrivers from other mods work through the `c:worktops` and `c:tools/screwdriver` tags.

Build with `./gradlew build`. Textures are drawn by `python3 tools/gen_textures.py` and the json files are made by `python3 tools/gen_resources.py`.
