#!/usr/bin/env python3
# writes the json files, keep the finish lists in sync with Finishes.java, run from repo root
import json, os, shutil

MOD = "muchmorestorage"
ROOT = "common/src/main/resources"
A, D = f"{ROOT}/assets/{MOD}", f"{ROOT}/data/{MOD}"

WOODS = ["oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped"]
COLOURS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan",
           "purple", "blue", "brown", "green", "red", "black"]
STONES = ["smooth_stone", "granite", "polished_granite", "polished_diorite", "polished_andesite"]
BODIES = WOODS + [c + "_concrete" for c in COLOURS]
TOPS = WOODS + STONES + [c + "_concrete" for c in COLOURS]

def label(name):
    return " ".join(p.capitalize() for p in name.split("_"))

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

for sub in ["assets/" + MOD + "/blockstates", "assets/" + MOD + "/items", "assets/" + MOD + "/models",
            "data/" + MOD + "/recipe", "data/" + MOD + "/advancement", "data/" + MOD + "/loot_table",
            "data/" + MOD + "/tags", "data/minecraft"]:
    shutil.rmtree(f"{ROOT}/{sub}", ignore_errors=True)

def advancement(path, recipe, unlock):
    crit = {"has_the_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": recipe}},
            "has_item": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": unlock}]}}}
    w(path, {"parent": "minecraft:recipes/root", "criteria": crit, "requirements": [list(crit)], "rewards": {"recipes": [recipe]}})

def recipe(name, pattern, key, count=1, category="misc", unlock=None):
    w(f"{D}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": category, "pattern": pattern,
        "key": key, "result": {"id": f"{MOD}:{name}", "count": count}})
    advancement(f"{D}/advancement/recipes/{category}/{name}.json", f"{MOD}:{name}", unlock)

# the renderer draws everything, the block models only carry the break particles
w(f"{A}/blockstates/cupboard.json", {"variants": {
    "concrete=false": {"model": f"{MOD}:block/cupboard_wood"},
    "concrete=true": {"model": f"{MOD}:block/cupboard_concrete"}}})
w(f"{A}/models/block/cupboard_wood.json", {"textures": {"particle": "minecraft:block/oak_planks"}})
w(f"{A}/models/block/cupboard_concrete.json", {"textures": {"particle": "minecraft:block/light_gray_concrete"}})
w(f"{A}/models/item/cupboard.json", {"parent": "minecraft:block/block", "textures": {"particle": "minecraft:block/oak_planks"}})

def special(name):
    w(f"{A}/items/{name}.json", {"model": {"type": "minecraft:special", "base": f"{MOD}:item/cupboard",
        "model": {"type": f"{MOD}:cupboard"}}})

lang = {f"itemGroup.{MOD}": "Much More Storage", f"container.{MOD}.cupboard": "Cupboard",
        f"block.{MOD}.cupboard": "Cupboard", f"item.{MOD}.cupboard": "Cupboard",
        f"tooltip.{MOD}.body": "Body: %s", f"tooltip.{MOD}.top": "Top: %s"}
special("cupboard")

for body in BODIES:
    name = f"{body}_carcass"
    special(name)
    panel = f"minecraft:{body}_planks" if body in WOODS else f"minecraft:{body}"
    recipe(name, ["PPP", "PCP", "PPP"], {"P": panel, "C": "#c:chests/wooden"}, unlock="minecraft:chest")
    lang[f"item.{MOD}.{name}"] = f"{label(body)} Carcass"

for top in TOPS:
    name = f"{top}_worktop"
    special(name)
    if top in COLOURS or top.endswith("_concrete"):
        recipe(name, ["CCC"], {"C": f"minecraft:{top}"}, 2, unlock=f"minecraft:{top}")
    elif top == "granite":
        recipe(name, ["ADA"], {"A": "minecraft:polished_andesite_slab", "D": "minecraft:polished_diorite_slab"},
               unlock="minecraft:polished_diorite_slab")
    else:
        recipe(name, ["SSS"], {"S": f"minecraft:{top}_slab"}, unlock=f"minecraft:{top}_slab")
    lang[f"item.{MOD}.{name}"] = f"{label(top)} Worktop"

for f in dict.fromkeys(BODIES + TOPS):
    lang[f"finish.{MOD}.{f}"] = label(f)

w(f"{D}/tags/item/carcasses.json", {"values": [f"{MOD}:{b}_carcass" for b in BODIES]})
w(f"{A}/models/item/screwdriver.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{MOD}:item/screwdriver"}})
w(f"{A}/items/screwdriver.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/screwdriver"}})
recipe("screwdriver", ["I", "S"], {"I": "#c:ingots/iron", "S": "minecraft:stick"}, category="tools", unlock=f"#{MOD}:carcasses")
lang[f"item.{MOD}.screwdriver"] = "Screwdriver"
w(f"{A}/lang/en_us.json", lang)
