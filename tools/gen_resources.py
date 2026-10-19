#!/usr/bin/env python3
# writes the cupboard json files, keep FINISHES in sync with Finish.java, run from repo root
import json, os

MOD = "morestorage"
ROOT = "common/src/main/resources"
A, D = f"{ROOT}/assets/{MOD}", f"{ROOT}/data/{MOD}"

STONE = "minecraft:smooth_stone_slab"
# id, display name, panel textures, stone top, top slab, panel material
FINISHES = [
    ("white", "White", "white", True, STONE, "minecraft:white_concrete"),
    ("oak", "Oak", "oak", False, "minecraft:oak_slab", "minecraft:oak_planks"),
    ("spruce", "Spruce", "spruce", False, "minecraft:spruce_slab", "minecraft:spruce_planks"),
    ("dark_oak", "Dark Oak", "dark_oak", False, "minecraft:dark_oak_slab", "minecraft:dark_oak_planks"),
    ("stone_top_oak", "Stone Top Oak", "oak", True, STONE, "minecraft:oak_planks"),
    ("stone_top_spruce", "Stone Top Spruce", "spruce", True, STONE, "minecraft:spruce_planks"),
    ("stone_top_dark_oak", "Stone Top Dark Oak", "dark_oak", True, STONE, "minecraft:dark_oak_planks"),
]

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

def el(frm, to, faces):
    return {"from": frm, "to": to, "faces": {d: {"uv": uv, "texture": t} for d, (t, uv) in faces.items()}}

CARCASS = [
    el([0, 14, 0], [16, 16, 16], {"up": ("#top", [0, 0, 16, 16]), "down": ("#inside", [0, 0, 16, 16]),
        "north": ("#top", [0, 0, 16, 2]), "south": ("#top", [0, 14, 16, 16]),
        "east": ("#top", [0, 0, 16, 2]), "west": ("#top", [0, 0, 16, 2])}),
    el([0, 2, 2], [1, 14, 16], {"west": ("#side", [2, 2, 16, 14]), "east": ("#inside", [2, 2, 16, 14]),
        "north": ("#side", [0, 2, 1, 14]), "south": ("#side", [15, 2, 16, 14]), "down": ("#side", [0, 0, 1, 14])}),
    el([15, 2, 2], [16, 14, 16], {"east": ("#side", [0, 2, 14, 14]), "west": ("#inside", [0, 2, 14, 14]),
        "north": ("#side", [15, 2, 16, 14]), "south": ("#side", [0, 2, 1, 14]), "down": ("#side", [15, 0, 16, 14])}),
    el([1, 2, 15], [15, 14, 16], {"north": ("#inside", [1, 2, 15, 14]), "south": ("#side", [1, 2, 15, 14])}),
    el([1, 2, 2], [15, 3, 15], {"up": ("#inside", [1, 2, 15, 15]), "north": ("#side", [1, 13, 15, 14]),
        "down": ("#side", [1, 1, 15, 14])}),
    el([1, 8, 3], [15, 9, 15], {"up": ("#inside", [1, 3, 15, 15]), "down": ("#inside", [1, 1, 15, 13]),
        "north": ("#inside", [1, 7, 15, 8])}),
    el([0, 0, 3], [16, 2, 16], {"north": ("#plinth", [0, 14, 16, 16]), "south": ("#plinth", [0, 14, 16, 16]),
        "east": ("#plinth", [0, 14, 13, 16]), "west": ("#plinth", [3, 14, 16, 16]), "down": ("#plinth", [0, 0, 16, 13])}),
]

# the renderer draws the same door in code, this copy is only for the item
DOOR = [
    el([0, 2, 1], [16, 14, 2], {"north": ("#door", [0, 2, 16, 14]), "south": ("#side", [0, 2, 16, 14]),
        "up": ("#side", [0, 0, 16, 1]), "down": ("#side", [0, 15, 16, 16]),
        "west": ("#side", [0, 2, 1, 14]), "east": ("#side", [15, 2, 16, 14])}),
    el([1, 6, 0], [2, 11, 1], {d: ("#handle", [7, 5, 8, 10]) for d in ["north", "up", "down", "east", "west"]}),
]

w(f"{A}/models/block/cupboard.json", {"parent": "minecraft:block/block",
    "textures": {"particle": "#side", "plinth": "#side", "handle": f"{MOD}:block/cupboard_handle"}, "elements": CARCASS})
w(f"{A}/models/block/cupboard_closed.json", {"parent": f"{MOD}:block/cupboard", "elements": CARCASS + DOOR})

lang = {f"itemGroup.{MOD}": "More Storage", f"container.{MOD}.cupboard": "Cupboard"}
axe, pickaxe = [], []
for name, label, panels, stone, slab, panel in FINISHES:
    bid = f"{name}_cupboard"
    (pickaxe if stone else axe).append(f"{MOD}:{bid}")
    tex = {k: f"{MOD}:block/{panels}_cupboard_{k}" for k in ["side", "inside", "door"]}
    tex["top"] = f"{MOD}:block/{'stone' if stone else panels}_cupboard_top"
    rot = {"north": 0, "east": 90, "south": 180, "west": 270}
    w(f"{A}/blockstates/{bid}.json", {"variants": {f"facing={f}": ({"model": f"{MOD}:block/{bid}", "y": y} if y else {"model": f"{MOD}:block/{bid}"}) for f, y in rot.items()}})
    w(f"{A}/models/block/{bid}.json", {"parent": f"{MOD}:block/cupboard", "textures": tex})
    w(f"{A}/models/block/{bid}_closed.json", {"parent": f"{MOD}:block/cupboard_closed", "textures": tex})
    w(f"{A}/items/{bid}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{bid}_closed"}})
    w(f"{D}/loot_table/blocks/{bid}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": f"{MOD}:{bid}",
        "functions": [{"function": "minecraft:copy_components", "source": "block_entity", "include": ["minecraft:custom_name"]}]}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}], "random_sequence": f"{MOD}:blocks/{bid}"})
    w(f"{D}/recipe/{bid}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "group": f"{MOD}:cupboard",
        "pattern": ["TTT", "PCP", "PPP"], "key": {"T": slab, "P": panel, "C": "#c:chests/wooden"},
        "result": {"id": f"{MOD}:{bid}", "count": 1}})
    crit = {"has_the_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": f"{MOD}:{bid}"}},
            "has_chest": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": "minecraft:chest"}]}}}
    w(f"{D}/advancement/recipes/misc/{bid}.json", {"parent": "minecraft:recipes/root", "criteria": crit,
        "requirements": [list(crit)], "rewards": {"recipes": [f"{MOD}:{bid}"]}})
    lang[f"block.{MOD}.{bid}"] = f"{label} Cupboard"


w(f"{ROOT}/data/minecraft/tags/block/mineable/axe.json", {"values": axe})
w(f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json", {"values": pickaxe})

w(f"{A}/models/item/screwdriver.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{MOD}:item/screwdriver"}})
w(f"{A}/items/screwdriver.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/screwdriver"}})
w(f"{D}/recipe/screwdriver.json", {"type": "minecraft:crafting_shaped", "category": "tools", "pattern": ["I", "S"],
    "key": {"I": "#c:ingots/iron", "S": "minecraft:stick"}, "result": {"id": f"{MOD}:screwdriver", "count": 1}})
crit = {"has_the_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": f"{MOD}:screwdriver"}},
        "has_cupboard": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": "#" + MOD + ":cupboards"}]}}}
w(f"{D}/advancement/recipes/tools/screwdriver.json", {"parent": "minecraft:recipes/root", "criteria": crit,
    "requirements": [list(crit)], "rewards": {"recipes": [f"{MOD}:screwdriver"]}})
w(f"{D}/tags/item/cupboards.json", {"values": axe + pickaxe})
lang[f"item.{MOD}.screwdriver"] = "Screwdriver"
w(f"{A}/lang/en_us.json", lang)
