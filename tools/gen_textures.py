#!/usr/bin/env python3
# draws the cupboard textures, run from repo root
import os, random
from PIL import Image

OUT = "common/src/main/resources/assets/morestorage/textures/block"

def hexc(s):
    return tuple(int(s[i:i + 2], 16) for i in (0, 2, 4)) + (255,)

# light to dark, sampled from the vanilla planks
WOODS = {
    "oak": ["c29d62", "b8945f", "af8f55", "9f844d", "7e6237", "67502c"],
    "spruce": ["886539", "82613a", "7a5a34", "70522e", "5a4424", "553a1f"],
    "dark_oak": ["5e3f1e", "53381a", "4f3218", "492f17", "3e2912", "301e0e"],
}
WHITE = ["f4f2ec", "eae7e0", "e0ddd5", "d2cfc7", "b9b5ad", "9c9890"]
GRANITE = ["b6b2aa", "8e8c8a", "7a7979", "6c6b6d", "58585b", "414144"]
STEEL = ["eeeeee", "d4d4d4", "bcbcbc", "a2a2a2", "7c7c7c"]

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

def main():
    os.makedirs(OUT, exist_ok=True)
    finishes = [("white", WHITE, False, "granite")] + [(w, p, True, "wood") for w, p in WOODS.items()]
    for name, pal, wood, top in finishes:
        rng = random.Random(name)
        door(rng, pal, wood).save(f"{OUT}/{name}_cupboard_door.png")
        side(rng, pal, wood).save(f"{OUT}/{name}_cupboard_side.png")
        inside(rng, pal if wood else WHITE, wood).save(f"{OUT}/{name}_cupboard_inside.png")
        (granite(rng) if top == "granite" else butcher_block(rng, pal)).save(f"{OUT}/{name}_cupboard_top.png")
    handle().save(f"{OUT}/cupboard_handle.png")

main()
