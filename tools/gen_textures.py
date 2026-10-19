#!/usr/bin/env python3
# draws the cupboard textures, run from repo root
import os, random
from PIL import Image

OUT = "common/src/main/resources/assets/morestorage/textures/block"
ITEMS = "common/src/main/resources/assets/morestorage/textures/item"

def hexc(s):
    return tuple(int(s[i:i + 2], 16) for i in (0, 2, 4)) + (255,)

# light to dark, sampled from the vanilla planks
WOODS = {
    "oak": ["c29d62", "b8945f", "af8f55", "9f844d", "7e6237", "67502c"],
    "spruce": ["886539", "82613a", "7a5a34", "70522e", "5a4424", "553a1f"],
    "birch": ["d7cb8d", "d7c185", "c8b77a", "b8a875", "a59467", "9e8b61"],
    "jungle": ["bf8e6b", "b88764", "aa7954", "9f714a", "785437", "68462f"],
    "acacia": ["c26d3f", "ba6337", "ad5d32", "a05630", "8f4c2a", "884728"],
    "dark_oak": ["5e3f1e", "53381a", "4f3218", "492f17", "3e2912", "301e0e"],
    "mangrove": ["8b4d3a", "7f4234", "773934", "753136", "642423", "5d1c1e"],
    "cherry": ["e7cac5", "e7c2bb", "e7bab4", "e6b3ad", "dd9d97", "cd8580"],
    "pale_oak": ["fffbf8", "faefee", "e7e3e1", "ddcecd", "c7b8b8", "c0b2b2"],
    "bamboo": ["e3cc6a", "d9c25e", "c8b14d", "bba84c", "998741", "907e3a"],
    "crimson": ["863e5a", "7e3a56", "6a344b", "5c3042", "442131", "3f1e2d"],
    "warped": ["3a8e8c", "398382", "287067", "2e5f51", "1e4340", "113835"],
}
# the main colour of each vanilla concrete
CONCRETE = {
    "orange": "e16100", "magenta": "a9309f", "light_blue": "2389c7", "yellow": "f1af15",
    "lime": "5ea918", "pink": "d6658f", "gray": "373a3e", "light_gray": "7d7d73", "cyan": "157788",
    "purple": "65209d", "blue": "2c2e8f", "brown": "613c20", "green": "495b24", "red": "8e2020", "black": "090b10",
}
WHITE = ["f4f2ec", "eae7e0", "e0ddd5", "d2cfc7", "b9b5ad", "9c9890"]
GRANITE = ["b6b2aa", "8e8c8a", "7a7979", "6c6b6d", "58585b", "414144"]
STEEL = ["eeeeee", "d4d4d4", "bcbcbc", "a2a2a2", "7c7c7c"]

def mix(c, to, f):
    return "".join("%02x" % int(int(c[i:i + 2], 16) * (1 - f) + to * f) for i in (0, 2, 4))

def concrete(base):
    return [mix(base, 255, 0.14), base, mix(base, 0, 0.07), mix(base, 0, 0.16), mix(base, 0, 0.32), mix(base, 0, 0.45)]

def img():
    return Image.new("RGBA", (16, 16))

def grain(rng, pal, vertical, streak=0.22):
    # long streaks along the grain with a little speckle
    im = img()
    px = im.load()
    lines = [rng.choice([1, 1, 2, 2, 2, 3]) for _ in range(16)]
    for a in range(16):
        base = lines[a]
        for b in range(16):
            k = base
            r = rng.random()
            if r < streak * 0.5:
                k = min(base + 1, 4)
            elif r < streak:
                k = max(base - 1, 0)
            x, y = (a, b) if vertical else (b, a)
            px[x, y] = hexc(pal[k])
    return im

def flat(rng, pal):
    im = img()
    px = im.load()
    for x in range(16):
        for y in range(16):
            r = rng.random()
            px[x, y] = hexc(pal[1] if r < 0.75 else pal[0] if r < 0.88 else pal[2])
    return im

def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (255,)

def door(rng, pal, wood):
    im = grain(rng, pal, True) if wood else flat(rng, pal)
    px = im.load()
    rails = grain(rng, pal, False) if wood else flat(rng, pal)
    rp = rails.load()
    for x in range(16):
        for y in list(range(0, 4)) + list(range(12, 16)):
            if 2 <= x <= 13:
                px[x, y] = rp[x, y]
    # recessed centre panel, shadow on the top and left, light on the bottom and right
    for x in range(2, 14):
        for y in range(4, 12):
            if wood:
                c = px[x, y]
                px[x, y] = shade(c, 0.94)
            if y == 4 or x == 2:
                px[x, y] = hexc(pal[4])
            elif y == 11 or x == 13:
                px[x, y] = hexc(pal[0])
            elif y == 5 or x == 3:
                px[x, y] = hexc(pal[3])
    # outline so neighbouring doors read as separate
    for i in range(16):
        for x, y in [(0, i), (15, i), (i, 2), (i, 13)]:
            px[x, y] = hexc(pal[4] if wood else pal[3])
    for i in range(1, 15):
        px[1, i] = shade(px[1, i], 1.06) if 2 < i < 13 else px[1, i]
    return im

def side(rng, pal, wood):
    return grain(rng, pal, True) if wood else flat(rng, pal)

def inside(rng, pal, wood):
    im = grain(rng, pal, False, 0.15) if wood else flat(rng, ["e2ddd0", "d8d3c6", "cbc6b8"])
    px = im.load()
    for x in range(16):
        for y in range(16):
            px[x, y] = shade(px[x, y], 0.78 if wood else 0.9)
    return im

def butcher_block(rng, pal):
    im = img()
    px = im.load()
    for stave in range(8):
        tone = rng.choice([0, 1, 1, 2, 2, 3])
        for y in (stave * 2, stave * 2 + 1):
            for x in range(16):
                k = tone
                if rng.random() < 0.18:
                    k = min(tone + 1, 4)
                px[x, y] = hexc(pal[k])
        seam = stave * 2 + 1
        for x in range(16):
            if rng.random() < 0.6:
                px[x, seam] = shade(px[x, seam], 0.9)
    return im

def granite(rng):
    im = img()
    px = im.load()
    for x in range(16):
        for y in range(16):
            r = rng.random()
            k = 3 if r < 0.45 else 2 if r < 0.7 else 4 if r < 0.85 else 1 if r < 0.93 else 5 if r < 0.98 else 0
            px[x, y] = hexc(GRANITE[k])
    return im

def handle():
    im = img()
    px = im.load()
    for x in range(16):
        for y in range(16):
            px[x, y] = hexc(STEEL[[3, 2, 1, 0, 1, 2, 3][x % 7]])
    for x in range(16):
        px[x, 5] = hexc(STEEL[3])
        px[x, 10] = hexc(STEEL[4])
    px[7, 6] = hexc(STEEL[0])
    return im

def screwdriver():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = im.load()
    OUTLINE = hexc("2b1d14")
    RED, RED_LIT, RED_DARK = hexc("b8322a"), hexc("e0574a"), hexc("7a1d17")
    METAL, METAL_LIT, METAL_DARK = hexc("a8a8a8"), hexc("e6e6e6"), hexc("5a5a5a")
    # handle, a fat diagonal from the bottom left
    for i in range(2, 8):
        x, y = i, 15 - i
        for dx, dy, c in [(0, 0, RED), (-1, 0, RED_LIT), (0, 1, RED_DARK), (1, 1, RED_DARK), (-1, -1, RED_LIT)]:
            px[x + dx, y + dy] = c
    for i in range(2, 8):
        x, y = i, 15 - i
        for dx, dy in [(-2, 0), (-2, -1), (-1, -2), (0, 2), (1, 2), (2, 1), (2, 2)]:
            a, b = x + dx, y + dy
            if 0 <= a < 16 and 0 <= b < 16 and px[a, b][3] == 0:
                px[a, b] = OUTLINE
    for a, b in [(1, 15), (0, 14), (1, 14), (0, 13)]:
        px[a, b] = OUTLINE
    px[1, 13] = RED_DARK
    px[2, 14] = RED_DARK
    # ferrule and shaft
    for i in range(8, 14):
        x, y = i, 15 - i
        px[x, y] = METAL if i < 13 else METAL_DARK
        px[x - 1, y] = METAL_LIT if i < 13 else METAL
        for a, b in [(x - 1, y - 1), (x + 1, y), (x, y + 1)]:
            if 0 <= a < 16 and 0 <= b < 16 and px[a, b][3] == 0:
                px[a, b] = OUTLINE
    px[8, 7] = METAL_DARK
    px[9, 7] = METAL_DARK
    px[14, 1] = OUTLINE
    return im

def main():
    os.makedirs(OUT, exist_ok=True)
    for name in os.listdir(OUT):
        os.remove(f"{OUT}/{name}")
    bodies = [(w, w, p, True) for w, p in WOODS.items()] + [("white_concrete", "white", WHITE, False)]
    bodies += [(c + "_concrete", c, concrete(b), False) for c, b in CONCRETE.items()]
    for name, seed, pal, wood in bodies:
        rng = random.Random(seed)
        door(rng, pal, wood).save(f"{OUT}/{name}_body_door.png")
        side(rng, pal, wood).save(f"{OUT}/{name}_body_side.png")
        inside(rng, pal if wood else WHITE, wood).save(f"{OUT}/{name}_body_inside.png")
        if wood:
            butcher_block(rng, pal).save(f"{OUT}/{name}_worktop.png")
        elif seed == "white":
            granite(rng).save(f"{OUT}/granite_worktop.png")
    handle().save(f"{OUT}/cupboard_handle.png")
    os.makedirs(ITEMS, exist_ok=True)
    screwdriver().save(f"{ITEMS}/screwdriver.png")

main()
