#!/usr/bin/env python3
# writes the json files, keep the finish lists in sync with Finishes.java, run from repo root
import json, os, shutil, sys

MOD = "muchmorestorage"
ROOT = "src/main/resources"
A = f"{ROOT}/assets/{MOD}"
# files whose format moved between versions go to src/eras/<era>, the build picks one per node
# 1.20.1 has no c:chests/wooden or c:ingots/iron on both loaders, so it uses its own tags
OLD_TAGS = {"c:chests/wooden": f"{MOD}:wooden_chests", "c:ingots/iron": f"{MOD}:iron_ingots"}
ERAS = {
    "26.1": {"items": True},
    "26.3": {"items": True, "unlock": "recipes", "modifier": True, "tool_category": "equipment"},
    "1.21.1": {"objects": True, "tool_category": "equipment", "no_pale": True},
    "1.20.1": {"objects": True, "tool_category": "equipment", "no_pale": True, "plural": True, "nbt": True, "tags": OLD_TAGS},
}
PLURAL = {"recipe": "recipes", "advancement": "advancements", "loot_table": "loot_tables", "tags/item": "tags/items"}

def era_root(era):
    return f"src/eras/{era}"

def folder(era, kind):
    return PLURAL[kind] if ERAS[era].get("plural") else kind

def has(era, item):
    # pale oak arrived in 1.21.4
    return not (ERAS[era].get("no_pale") and "pale_oak" in item)

def tagname(era, t):
    return ERAS[era].get("tags", {}).get(t, t)

def ing(era, x):
    # 1.21.1 and older want ingredient objects instead of plain ids and #tags
    if not ERAS[era].get("objects"):
        return x
    return {"tag": tagname(era, x[1:])} if x.startswith("#") else {"item": x}

def item_pred(era, x):
    if not ERAS[era].get("plural"):
        return {"items": x}
    return {"tag": tagname(era, x[1:])} if x.startswith("#") else {"items": [x]}

def result(era, item, count=None, **extra):
    r = {"item" if ERAS[era].get("plural") else "id": item}
    if count is not None: r["count"] = count
    r.update(extra)
    return r

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

for sub in ["assets/" + MOD + "/blockstates", "assets/" + MOD + "/items", "assets/" + MOD + "/models", "data"]:
    shutil.rmtree(f"{ROOT}/{sub}", ignore_errors=True)
if os.path.exists(f"{ROOT}/pack.mcmeta"): os.remove(f"{ROOT}/pack.mcmeta")
shutil.rmtree("src/eras", ignore_errors=True)

def unlocked(era, recipe):
    return {"trigger": "minecraft:recipe_unlocked", "conditions": {ERAS[era].get("unlock", "recipe"): recipe}}

def advancement(era, path, recipe, unlock):
    # unlock is one item or tag, or a list of them where any one will do
    crit = {"has_the_recipe": unlocked(era, recipe)}
    items = unlock if isinstance(unlock, list) else [unlock]
    for n, item in enumerate(items):
        crit["has_item" if n == 0 else f"has_item_{n + 1}"] = {"trigger": "minecraft:inventory_changed",
                                                               "conditions": {"items": [item_pred(era, item)]}}
    w(path, {"parent": "minecraft:recipes/root", "criteria": crit, "requirements": [list(crit)], "rewards": {"recipes": [recipe]}})

def recipe(name, pattern, key, count=1, category="misc", unlock=None):
    for era, e in ERAS.items():
        if not has(era, name): continue
        D = f"{era_root(era)}/data/{MOD}"
        cat = e.get("tool_category", category) if category == "tools" else category
        w(f"{D}/{folder(era, 'recipe')}/{name}.json", {"type": "minecraft:crafting_shaped", "category": cat, "pattern": pattern,
            "key": {k: ing(era, v) for k, v in key.items()}, "result": result(era, f"{MOD}:{name}", count)})
        advancement(era, f"{D}/{folder(era, 'advancement')}/recipes/{category}/{name}.json", f"{MOD}:{name}", unlock)

def item_model(name, model, old=None):
    # 1.21.4+ item model definitions, older versions only have models/item
    for era, e in ERAS.items():
        if not has(era, name): continue
        if e.get("items"):
            w(f"{era_root(era)}/assets/{MOD}/items/{name}.json", {"model": model})
        elif old:
            w(f"{era_root(era)}/assets/{MOD}/models/item/{name}.json", old)

def tag(path, values):
    for era in ERAS:
        w(f"{era_root(era)}/data/{path.replace('tags/item', folder(era, 'tags/item'))}.json",
          {"values": [v for v in values if has(era, v if isinstance(v, str) else v["id"])]})

def pack():
    for era, e in ERAS.items():
        if e.get("items"):
            body = '"min_format": ${pack_min},\n        "max_format": ${pack_max}'
        else:
            body = '"pack_format": ${pack_format}'
        with open(f"{era_root(era)}/pack.mcmeta", "w") as f:
            f.write('{\n    "pack": {\n        "description": "${name}",\n        ' + body + '\n    }\n}\n')

# the renderer draws everything, the block models only carry the break particles
w(f"{A}/blockstates/cupboard.json", {"variants": {
    "concrete=false": {"model": f"{MOD}:block/cupboard_wood"},
    "concrete=true": {"model": f"{MOD}:block/cupboard_concrete"}}})
w(f"{A}/models/block/cupboard_wood.json", {"textures": {"particle": "minecraft:block/oak_planks"}})
w(f"{A}/models/block/cupboard_concrete.json", {"textures": {"particle": "minecraft:block/light_gray_concrete"}})
BLOCK_DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
    "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]}}
for era, e in ERAS.items():
    # 1.21.4+ draws items through the special model, older versions through builtin/entity
    w(f"{era_root(era)}/assets/{MOD}/models/item/cupboard.json",
      {"parent": "minecraft:block/block", "textures": {"particle": "minecraft:block/oak_planks"}} if e.get("items") else
      {"parent": "minecraft:builtin/entity", "gui_light": "side", "display": BLOCK_DISPLAY,
       "textures": {"particle": "minecraft:block/oak_planks"}})

def special(name):
    item_model(name, {"type": "minecraft:special", "base": f"{MOD}:item/cupboard", "model": {"type": f"{MOD}:cupboard"}},
               None if name == "cupboard" else {"parent": f"{MOD}:item/cupboard"})

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

tag(f"{MOD}/tags/item/carcasses", [f"{MOD}:{b}_carcass" for b in BODIES])
tag("c/tags/item/worktops", [f"{MOD}:{t}_worktop" for t in TOPS])
tag("c/tags/item/tools/screwdriver", [{"id": f"{m}:screwdriver", "required": False} for m in [MOD, "simplemachines", "morelighting"]])
w(f"{A}/models/item/screwdriver.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{MOD}:item/screwdriver"}})
item_model("screwdriver", {"type": "minecraft:model", "model": f"{MOD}:item/screwdriver"})
recipe("screwdriver", ["I", "S"], {"I": "#c:ingots/iron", "S": "minecraft:stick"}, category="tools",
       unlock=["#c:ingots/iron", "minecraft:stick"])
lang[f"item.{MOD}.screwdriver"] = "Screwdriver"

# KUBHÅLA cube shelf, the renderer draws it like the cupboard
w(f"{A}/blockstates/kubhala.json", {"variants": {
    "concrete=false": {"model": f"{MOD}:block/cupboard_wood"},
    "concrete=true": {"model": f"{MOD}:block/cupboard_concrete"}}})
special("kubhala")
for body in BODIES:
    name = f"{body}_kubhala_flatpack"
    special(name)
    panel = f"minecraft:{body}_planks" if body in WOODS else f"minecraft:{body}"
    recipe(name, ["PPP", "PNP", "PPP"], {"P": panel, "N": "minecraft:iron_nugget"}, 2, unlock=panel)
    lang[f"item.{MOD}.{name}"] = f"{label(body)} KUBHÅLA Flat Pack"
for name, pattern in [("kubhala_drawer", ["S S", "SNS", "SSS"]), ("kubhala_door", ["SS", "SN", "SS"])]:
    w(f"{A}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{name}"}})
    item_model(name, {"type": "minecraft:model", "model": f"{MOD}:item/{name}"})
    recipe(name, pattern, {"S": "#minecraft:wooden_slabs", "N": "minecraft:iron_nugget"}, unlock="#minecraft:wooden_slabs")
lang.update({f"block.{MOD}.kubhala": "KUBHÅLA", f"item.{MOD}.kubhala": "KUBHÅLA", f"container.{MOD}.kubhala": "KUBHÅLA",
             f"item.{MOD}.kubhala_drawer": "KUBHÅLA Drawer Insert", f"item.{MOD}.kubhala_door": "KUBHÅLA Door Insert",
             f"tooltip.{MOD}.kubhala": "Cube shelf, holds 9 stacks",
             f"tooltip.{MOD}.kubhala.join": "Joins up with cubes beside, above and below",
             f"tooltip.{MOD}.flatpack": "Place it, then use a screwdriver",
             f"tooltip.{MOD}.insert": "Use on an empty cube, then fix with a screwdriver",
             f"tooltip.{MOD}.insert.out": "A screwdriver pops it back out"})
lang[f"book.{MOD}.manual.title"] = "Much More Storage Manual"
lang[f"tag.item.{MOD}.carcasses"] = "Carcasses"
lang["tag.item.c.worktops"] = "Worktops"
lang["tag.item.c.tools.screwdriver"] = "Screwdrivers"
w(f"{A}/lang/en_us.json", lang)
for era, e in ERAS.items():
    if e.get("tags"):
        D = f"{era_root(era)}/data/{MOD}/{folder(era, 'tags/item')}"
        w(f"{D}/wooden_chests.json", {"values": ["minecraft:chest", "minecraft:trapped_chest",
            {"id": "#forge:chests/wooden", "required": False}, {"id": "#c:chests/wooden", "required": False}]})
        w(f"{D}/iron_ingots.json", {"values": ["minecraft:iron_ingot",
            {"id": "#forge:ingots/iron", "required": False}, {"id": "#c:iron_ingots", "required": False}]})
pack()

import gen_manual
gen_manual.main(sys.modules[__name__])
