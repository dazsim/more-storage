#!/usr/bin/env python3
# draws the assembly manual for Simple Books and writes its pages, run from repo root (gen_resources.py calls it)
import json, math, os, random
from PIL import Image, ImageDraw, ImageFont

MOD = "muchmorestorage"
ROOT = "common/src/main/resources"
A = f"{ROOT}/assets/{MOD}"
TEX = f"textures/simplebooks/manual"
PW, PH, SS = 644, 876, 3
STAND = (160, 218)
FONTS = ["/usr/share/fonts/truetype/sand-box/google/Noto Sans/NotoSans-VariableFont_wdth,wght.ttf",
         "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", "DejaVuSans-Bold.ttf", "Arial Bold.ttf"]
_fonts = {}

def font(size):
    if size not in _fonts:
        f = None
        for p in FONTS:
            try:
                f = ImageFont.truetype(p, size)
                try: f.set_variation_by_name("Bold")
                except Exception: pass
                break
            except OSError:
                continue
        _fonts[size] = f or ImageFont.load_default(size)
    return _fonts[size]

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

class Page:
    def __init__(self, weight=1.0):
        self.weight = weight
        self.im = Image.new("RGBA", (PW * SS, PH * SS), (255, 255, 255, 0))
        self.d = ImageDraw.Draw(self.im)

    def _wobble(self, a, b, amp=0.7):
        (x0, y0), (x1, y1) = a, b
        L = math.hypot(x1 - x0, y1 - y0)
        if L < 0.01: return [a, b]
        key = tuple(sorted([(round(x0), round(y0)), (round(x1), round(y1))]))
        r = random.Random(hash(key) & 0xFFFFFFFF)
        ph = [r.uniform(0, 6.3) for _ in range(3)]
        amp = min(amp, L * 0.02) * r.uniform(0.6, 1.2)
        nx, ny = -(y1 - y0) / L, (x1 - x0) / L
        n = max(2, int(L / 5))
        out = []
        for i in range(n + 1):
            s = i / n
            o = amp * math.sin(math.pi * s) * (math.sin(ph[0] + s * 4.1) + 0.5 * math.sin(ph[1] + s * 9.3) + 0.25 * math.sin(ph[2] + s * 17))
            out.append((x0 + (x1 - x0) * s + nx * o, y0 + (y1 - y0) * s + ny * o))
        return out

    def _line(self, pts, width):
        wpx = max(1, round(width * self.weight * SS))
        sp = [(x * SS, y * SS) for x, y in pts]
        self.d.line(sp, fill=(0, 0, 0, 255), width=wpx, joint="curve")
        r = wpx / 2
        for x, y in (sp[0], sp[-1]):
            self.d.ellipse([x - r, y - r, x + r, y + r], fill=(0, 0, 0, 255))

    def line(self, a, b, width=2.4, amp=0.7):
        self._line(self._wobble(a, b, amp), width)

    def path(self, pts, width=2.4, closed=False, amp=0.7):
        pts = list(pts)
        if closed: pts = pts + [pts[0]]
        out = []
        for a, b in zip(pts, pts[1:]):
            seg = self._wobble(a, b, amp)
            out += seg if not out else seg[1:]
        self._line(out, width)

    def fill(self, pts, colour=(255, 255, 255, 255)):
        self.d.polygon([(x * SS, y * SS) for x, y in pts], fill=colour)

    def dot(self, p, r=1.6):
        x, y = p[0] * SS, p[1] * SS
        r *= SS * self.weight ** 0.5
        self.d.ellipse([x - r, y - r, x + r, y + r], fill=(0, 0, 0, 255))

    def curve(self, f, n=40, width=2.4):
        self._line([f(i / n) for i in range(n + 1)], width)

    def circle(self, c, r, width=2.4, filled=False, seed=0):
        rnd = random.Random(seed + int(c[0] * 7 + c[1] * 13))
        ph, a0 = rnd.uniform(0, 6.3), rnd.uniform(0, 6.3)
        pts = [(c[0] + math.cos(a0 + t * 6.2832 * 1.04) * r * (1 + 0.025 * math.sin(ph + t * 12)),
                c[1] + math.sin(a0 + t * 6.2832 * 1.04) * r * (1 + 0.025 * math.sin(ph + t * 12))) for t in [i / 60 for i in range(61)]]
        if filled: self.fill(pts)
        self._line(pts, width)

    def text(self, xy, s, size, anchor="la"):
        self.d.text((xy[0] * SS, xy[1] * SS), s, font=font(size * SS), fill=(0, 0, 0, 255), anchor=anchor)

    def arrow(self, pts, width=3.0, head=16):
        self.path(pts, width)
        (x0, y0), (x1, y1) = pts[-2], pts[-1]
        L = math.hypot(x1 - x0, y1 - y0) or 1
        ux, uy = (x1 - x0) / L, (y1 - y0) / L
        tip = (x1 + ux * head * 0.6, y1 + uy * head * 0.6)
        l = (x1 - uy * head * 0.55 - ux * head * 0.4, y1 + ux * head * 0.55 - uy * head * 0.4)
        r = (x1 + uy * head * 0.55 - ux * head * 0.4, y1 - ux * head * 0.55 - uy * head * 0.4)
        self.fill([tip, l, r], (0, 0, 0, 255))
        self.path([tip, l, r], 1.6, closed=True, amp=0.3)

    def curved_arrow(self, a, b, bend, width=3.0, head=16):
        mx, my = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
        dx, dy = b[0] - a[0], b[1] - a[1]
        c = (mx - dy * bend, my + dx * bend)
        pts = [((1 - t) ** 2 * a[0] + 2 * (1 - t) * t * c[0] + t * t * b[0],
                (1 - t) ** 2 * a[1] + 2 * (1 - t) * t * c[1] + t * t * b[1]) for t in [i / 24 for i in range(25)]]
        self.arrow(pts, width, head)

    def save(self, name, stand=False):
        os.makedirs(f"{A}/{TEX}", exist_ok=True)
        im = self.im.resize((PW, PH), Image.LANCZOS)
        if stand:
            bg = Image.new("RGBA", im.size, (255, 255, 255, 255))
            bg.alpha_composite(im)
            im = bg.convert("RGB").resize(STAND, Image.LANCZOS)
        im.save(f"{A}/{TEX}/{name}.png", optimize=True)

# ---------- 3d line drawings ----------

def rot(axis, deg, pivot):
    c, s = math.cos(math.radians(deg)), math.sin(math.radians(deg))
    def f(p):
        x, y, z = p[0] - pivot[0], p[1] - pivot[1], p[2] - pivot[2]
        if axis == "y": x, z = x * c + z * s, -x * s + z * c
        elif axis == "x": y, z = y * c - z * s, y * s + z * c
        else: x, y = x * c - y * s, x * s + y * c
        return (x + pivot[0], y + pivot[1], z + pivot[2])
    return f

def move(dx, dy, dz):
    return lambda p: (p[0] + dx, p[1] + dy, p[2] + dz)

class Scene:
    def __init__(self, ox, oy, scale, yaw=34, pitch=28):
        self.ox, self.oy, self.sc = ox, oy, scale
        self.cy, self.sy = math.cos(math.radians(yaw)), math.sin(math.radians(yaw))
        self.cp, self.sp = math.cos(math.radians(pitch)), math.sin(math.radians(pitch))
        self.items = []

    def at(self, cx, cy, lo=(0, 0, 0), hi=(16, 16, 16)):
        self.ox = self.oy = 0
        ps = [self.proj((hi[0] if i & 1 else lo[0], hi[1] if i & 2 else lo[1], hi[2] if i & 4 else lo[2])) for i in range(8)]
        self.ox = cx - (min(p[0] for p in ps) + max(p[0] for p in ps)) / 2
        self.oy = cy - (min(p[1] for p in ps) + max(p[1] for p in ps)) / 2
        return self

    def cam(self, p):
        x, y, z = p
        xr, zr = x * self.cy - z * self.sy, x * self.sy + z * self.cy
        return xr, y * self.cp - zr * self.sp, y * self.sp + zr * self.cp

    def proj(self, p):
        xr, yc, _ = self.cam(p)
        return (self.ox + xr * self.sc, self.oy - yc * self.sc)

    def box(self, a, b, xf=(), width=2.4, marks=None, bias=0):
        cs = []
        for i in range(8):
            p = (b[0] if i & 1 else a[0], b[1] if i & 2 else a[1], b[2] if i & 4 else a[2])
            for f in xf: p = f(p)
            cs.append(p)
        marks = [[tuple(_apply(q, xf) for q in m[0])] + list(m[1:]) for m in (marks or [])]
        cen = tuple(sum(c[k] for c in cs) / 8 for k in range(3))
        self.items.append((self.cam(cen)[2] + bias, "box", cs, width, marks))

    def extra(self, depth_point, fn, bias=0):
        self.items.append((self.cam(depth_point)[2] + bias, "fn", fn))

    def draw(self, pg):
        faces = [(0, 2, 6, 4), (1, 5, 7, 3), (0, 4, 5, 1), (2, 3, 7, 6), (0, 1, 3, 2), (4, 6, 7, 5)]
        for it in sorted(self.items, key=lambda t: t[0]):
            if it[1] == "fn":
                it[2](pg, self)
                continue
            _, _, cs, width, marks = it
            bc = [sum(c[k] for c in cs) / 8 for k in range(3)]
            vis, edges = [], set()
            for fc in faces:
                p = [cs[i] for i in fc]
                u = [p[1][k] - p[0][k] for k in range(3)]
                v = [p[3][k] - p[0][k] for k in range(3)]
                n = [u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0]]
                m = [sum(q[k] for q in p) / 4 - bc[k] for k in range(3)]
                if sum(n[k] * m[k] for k in range(3)) < 0: n = [-c for c in n]
                nd = n[1] * self.sp + (n[0] * self.sy + n[2] * self.cy) * self.cp
                if nd > 1e-6:
                    vis.append(fc)
                    for i in range(4): edges.add(tuple(sorted((fc[i], fc[(i + 1) % 4]))))
            for fc in vis: pg.fill([self.proj(cs[i]) for i in fc])
            for e in sorted(edges): pg.line(self.proj(cs[e[0]]), self.proj(cs[e[1]]), width)
            for m in marks:
                pts = [self.proj(q) for q in m[0]]
                kind = m[1] if len(m) > 1 else "line"
                if kind == "dot": pg.dot(pts[0], m[2] if len(m) > 2 else 1.5)
                else: pg.path(pts, m[2] if len(m) > 2 else 1.4, amp=0.5)

def _apply(p, xf):
    for f in xf: p = f(p)
    return p

def grain(face, x0, x1, y0, y1, z, n=3, seed=1, vertical=True):
    r = random.Random(seed)
    out = []
    for i in range(n):
        if vertical:
            x = x0 + (x1 - x0) * (i + 0.5 + r.uniform(-0.25, 0.25)) / n
            ya, yb = y0 + r.uniform(0.5, 3), y1 - r.uniform(0.5, 4)
            out.append([[face(x, ya, z), face(x + r.uniform(-0.4, 0.4), (ya + yb) / 2, z), face(x, yb, z)], "line", 1.2])
        else:
            y = y0 + (y1 - y0) * (i + 0.5 + r.uniform(-0.25, 0.25)) / n
            xa, xb = x0 + r.uniform(0.5, 3), x1 - r.uniform(0.5, 4)
            out.append([[face(xa, y, z), face((xa + xb) / 2, y + r.uniform(-0.4, 0.4), z), face(xb, y, z)], "line", 1.2])
    return out

def specks(face, x0, x1, y0, y1, z, n=6, seed=2):
    r = random.Random(seed)
    return [[[face(r.uniform(x0 + 1, x1 - 1), r.uniform(y0 + 1, y1 - 1), z)], "dot", r.uniform(0.9, 1.4)] for _ in range(n)]

def cupboard(sc, at=(0, 0, 0), top=True, loose=0.0, door=0.0, hinge="left", body="wood", finish="wood",
             frame=True, lift=0.0, door_only=False):
    base = [move(*at)]
    zfront = lambda x, y, z: (x, y, z)
    xside = lambda z, y, x: (x, y, z)
    if frame:
        mk = grain(xside, 1, 13, 3, 13, 16, 2, 5, vertical=True) if body == "wood" else specks(xside, 0, 14, 2, 14, 16, 5, 6)
        sc.box((0, 2, 0), (1, 14, 14), base)
        sc.box((1, 2, 0), (15, 14, 1), base)
        sc.box((1, 2, 1), (15, 3, 14), base, width=2.0)
        sc.box((1, 8, 1), (15, 9, 13), base, width=2.0)
        sc.box((15, 2, 0), (16, 14, 14), base, marks=mk)
        sc.box((0, 0, 0), (16, 2, 13), base, bias=-3)
    hx = 0 if hinge == "left" else 16
    ang = -door if hinge == "left" else door
    dxf = [rot("y", ang, (hx, 0, 15))] + base
    hand = (14, 15) if hinge == "left" else (1, 2)
    dm = grain(zfront, 1.5, 14.5, 3, 13, 15, 3, 7) if body == "wood" else specks(zfront, 0, 16, 2, 14, 15, 7, 8)
    sc.box((0, 2, 14), (16, 14, 15), dxf, marks=dm, bias=1)
    sc.box((hand[0], 6, 15), (hand[1], 11, 16), dxf, width=1.8, bias=1.5)
    if top:
        txf = [rot("y", 7 * loose, (8, 15, 8)), rot("z", -4 * loose, (8, 15, 8)), move(0.8 * loose, 3 * loose + lift, 0.6 * loose)] + base
        top_face = lambda x, z, y: (x, y, z)
        tm = (grain(top_face, 1, 15, 1, 15, 16, 3, 9, vertical=False) if finish == "wood"
              else specks(top_face, 0, 16, 0, 16, 16, 9, 10) if finish == "stone" else specks(top_face, 0, 16, 0, 16, 16, 3, 11))
        sc.box((0, 14, 0), (16, 16, 16), txf, marks=tm, bias=2)

def worktop(sc, at=(0, 0, 0), finish="wood", xf=()):
    top_face = lambda x, z, y: (x, y, z)
    tm = (grain(top_face, 1, 15, 1, 15, 2, 3, 9, vertical=False) if finish == "wood"
          else specks(top_face, 0, 16, 0, 16, 2, 9, 10) if finish == "stone" else specks(top_face, 0, 16, 0, 16, 2, 3, 11))
    sc.box((0, 0, 0), (16, 2, 16), list(xf) + [move(*at)], marks=tm)

def steve(sc, at=(0, 0, 0), yaw=0, arm=0, arm2=0, crouch=False, eyes=True):
    base = [rot("y", yaw, (4, 0, 2)), move(*at)]
    lean = [rot("x", 26, (4, 12, 2)), move(0, -3, 0)] if crouch else []
    leg = [move(0, 0, -2)] if crouch else []
    sc.box((0, 0, 0), (4, 12, 4), leg + base, width=2.2)
    sc.box((4, 0, 0), (8, 12, 4), leg + base, width=2.2)
    sc.box((0, 12, 0), (8, 24, 4), lean + base, width=2.2, bias=0.5)
    sc.box((-4, 12, 0), (0, 24, 4), [rot("x", -arm2, (-2, 22, 2))] + lean + base, width=2.2, bias=0.6)
    sc.box((8, 12, 0), (12, 24, 4), [rot("x", -arm, (10, 22, 2))] + lean + base, width=2.2, bias=0.7)
    hd = [move(0, -3, 3)] if crouch else []
    marks = [[[(2.4, 28.5, 6)], "dot", 2.2], [[(5.6, 28.5, 6)], "dot", 2.2]] if eyes else []
    sc.box((0, 24, -2), (8, 32, 6), hd + base, width=2.2, marks=marks, bias=1)
    hand = _apply((10, 13, 2), [rot("x", -arm, (10, 22, 2))] + lean + base)
    return sc.proj(hand)

# ---------- flat drawings ----------

def screwdriver(pg, c, ang=-40, size=1.0, width=2.4):
    a = math.radians(ang)
    ux, uy = math.cos(a), math.sin(a)
    nx, ny = -uy, ux
    P = lambda t, s: (c[0] + (ux * t + nx * s) * size, c[1] + (uy * t + ny * s) * size)
    handle = [P(-60, -9), P(-6, -11), P(0, -6), P(0, 6), P(-6, 11), P(-60, 9), P(-66, 5), P(-66, -5)]
    pg.fill(handle)
    pg.path(handle, width, closed=True)
    for t in (-48, -36, -24, -12):
        pg.line(P(t, -9), P(t, 9), 1.3)
    pg.fill([P(0, -3), P(46, -3), P(46, 3), P(0, 3)])
    pg.path([P(0, -3), P(46, -3), P(54, -1.5), P(54, 1.5), P(46, 3), P(0, 3)], width * 0.9, closed=True)

def twist(pg, c, rx=34, ry=11, width=2.6):
    pts = [(c[0] + math.cos(math.radians(a)) * rx, c[1] + math.sin(math.radians(a)) * ry) for a in range(200, 420, 12)]
    pg.arrow(pts, width, 13)

def check(pg, c, r=20, ok=True):
    pg.circle(c, r, 2.4)
    if ok: pg.path([(c[0] - r * 0.45, c[1]), (c[0] - r * 0.1, c[1] + r * 0.4), (c[0] + r * 0.5, c[1] - r * 0.4)], 3.2, amp=0.4)
    else:
        pg.line((c[0] - r * 0.4, c[1] - r * 0.4), (c[0] + r * 0.4, c[1] + r * 0.4), 3.2)
        pg.line((c[0] + r * 0.4, c[1] - r * 0.4), (c[0] - r * 0.4, c[1] + r * 0.4), 3.2)

def cell_icon(pg, kind, x, y, s):
    m = s * 0.16
    x0, y0, x1, y1 = x + m, y + m, x + s - m, y + s - m
    if kind == "plank":
        pg.path([(x0, y0), (x1, y0), (x1, y1), (x0, y1)], 1.8, closed=True)
        for k in (1, 2):
            yy = y0 + (y1 - y0) * k / 3
            pg.line((x0, yy), (x1, yy), 1.3)
        pg.line((x0 + (x1 - x0) * 0.6, y0), (x0 + (x1 - x0) * 0.6, y0 + (y1 - y0) / 3), 1.1)
        pg.line((x0 + (x1 - x0) * 0.3, y0 + (y1 - y0) / 3), (x0 + (x1 - x0) * 0.3, y0 + (y1 - y0) * 2 / 3), 1.1)
        pg.line((x0 + (x1 - x0) * 0.75, y0 + (y1 - y0) * 2 / 3), (x0 + (x1 - x0) * 0.75, y1), 1.1)
    elif kind == "concrete":
        pg.path([(x0, y0), (x1, y0), (x1, y1), (x0, y1)], 1.8, closed=True)
        r = random.Random(int(x * 3 + y))
        for _ in range(5): pg.dot((r.uniform(x0 + 4, x1 - 4), r.uniform(y0 + 4, y1 - 4)), 1.1)
    elif kind == "chest":
        pg.path([(x0, y0 + 3), (x1, y0 + 3), (x1, y1), (x0, y1)], 1.8, closed=True)
        ly = y0 + (y1 - y0) * 0.42
        pg.line((x0, ly), (x1, ly), 1.5)
        cx = (x0 + x1) / 2
        pg.path([(cx - 3, ly - 4), (cx + 3, ly - 4), (cx + 3, ly + 5), (cx - 3, ly + 5)], 1.4, closed=True)
    elif kind in ("slab", "slab_d", "slab_a"):
        ym = (y0 + y1) / 2
        pg.path([(x0, ym), (x1, ym), (x1, y1), (x0, y1)], 1.8, closed=True)
        if kind == "slab_d":
            r = random.Random(int(x))
            for _ in range(6): pg.dot((r.uniform(x0 + 3, x1 - 3), r.uniform(ym + 3, y1 - 3)), 1.0)
        if kind == "slab_a":
            pg.line((x0 + 4, ym + (y1 - ym) / 2), (x1 - 4, ym + (y1 - ym) / 2), 1.1)
    elif kind == "ingot":
        cy = (y0 + y1) / 2
        pts = [(x0 + 4, cy + 6), (x0 + 10, cy - 6), (x1 - 2, cy - 6), (x1 - 8, cy + 6)]
        pg.path(pts, 1.8, closed=True)
        pg.line((x0 + 4, cy + 6), (x0 + 4, cy + 10), 1.6)
        pg.line((x1 - 8, cy + 6), (x1 - 8, cy + 10), 1.6)
        pg.line((x0 + 4, cy + 10), (x1 - 8, cy + 10), 1.6)
        pg.line((x1 - 2, cy - 6), (x1 - 2, cy - 2), 1.6)
        pg.line((x1 - 2, cy - 2), (x1 - 8, cy + 10), 1.6)
    elif kind == "stick":
        pg.line((x0 + 2, y1 - 2), (x1 - 2, y0 + 2), 3.2)
        pg.line((x0 + (x1 - x0) * 0.45, y0 + (y1 - y0) * 0.55), (x0 + (x1 - x0) * 0.4, y0 + (y1 - y0) * 0.4), 1.6)

def grid(pg, x, y, s, rows):
    n = len(rows[0])
    for r in range(len(rows) + 1): pg.line((x, y + r * s), (x + n * s, y + r * s), 1.8)
    for c in range(n + 1): pg.line((x + c * s, y), (x + c * s, y + len(rows) * s), 1.8)
    for r, row in enumerate(rows):
        for c, k in enumerate(row):
            if k: cell_icon(pg, k, x + c * s, y + r * s, s)

def count(pg, xy, n, size=34):
    pg.text(xy, f"{n}x", size, "ls")

def step(pg, n):
    pg.text((24, 104), str(n), 92, "ls")

def ground(pg, sc, at=(0, 0, 0), r=26):
    pts = [sc.proj((at[0] + 8 + math.cos(a / 20 * 6.283) * r, at[1], at[2] + 8 + math.sin(a / 20 * 6.283) * r)) for a in range(21)]
    pg.path([pts[i] for i in range(4, 15)], 1.4, amp=0.4)

def item_cube(pg, sc, at, s=3.5):
    sc.box(at, (at[0] + s, at[1] + s, at[2] + s), (), width=1.8)

# ---------- the pages ----------

def page_cover(pg):
    pg.text((34, 70), "MUCH MORE", 64)
    pg.text((34, 142), "STORAGE", 64)
    pg.line((36, 238), (300, 238), 3.2)
    sc = Scene(0, 0, 13).at(322, 560, (0, 0, 0), (34, 32, 16))
    cupboard(sc, (0, 0, 0))
    hand = steve(sc, (24, 0, 6), yaw=10, arm=40)
    sc.draw(pg)
    screwdriver(pg, (hand[0] + 4, hand[1] + 46), 105, 0.8)

def page_tools(pg):
    count(pg, (70, 150), 1, 44)
    screwdriver(pg, (330, 250), -25, 2.2, 3.0)
    grid(pg, 90, 540, 80, [[None, "ingot", None], [None, "stick", None]])
    pg.arrow([(355, 620), (435, 620)], 3.0)
    screwdriver(pg, (530, 640), -50, 0.9)
    count(pg, (500, 730), 1)

def page_parts_carcass(pg):
    count(pg, (60, 150), 1, 44)
    sc = Scene(0, 0, 14).at(330, 330)
    cupboard(sc, (0, 0, 0), top=False)
    sc.draw(pg)
    grid(pg, 50, 570, 70, [["plank"] * 3, ["plank", "chest", "plank"], ["plank"] * 3])
    pg.arrow([(285, 675), (350, 675)], 3.0)
    s2 = Scene(0, 0, 7).at(480, 670)
    cupboard(s2, (0, 0, 0), top=False)
    s2.draw(pg)
    count(pg, (560, 770), 1, 30)
    cell_icon(pg, "plank", 60, 800, 60)
    pg.text((140, 848), "/", 40, "ms")
    cell_icon(pg, "concrete", 160, 800, 60)

def page_parts_worktop(pg):
    count(pg, (60, 150), 1, 44)
    sc = Scene(0, 0, 14).at(330, 240, (0, 0, 0), (16, 2, 16))
    worktop(sc, (0, 0, 0))
    sc.draw(pg)
    rows = [(["slab", "slab", "slab"], 1, "wood"), (["concrete"] * 3, 2, "plain"), (["slab_a", "slab_d", "slab_a"], 1, "stone")]
    for i, (cells, n, fin) in enumerate(rows):
        y = 420 + i * 150
        grid(pg, 40, y, 70, [cells])
        pg.arrow([(272, y + 35), (330, y + 35)], 3.0)
        s2 = Scene(0, 0, 6).at(440, y + 35, (0, -5, 0), (16, 2, 22) if n == 2 else (16, 2, 16))
        worktop(s2, (0, 0, 0), fin)
        if n == 2: worktop(s2, (0, -5, 6), fin)
        s2.draw(pg)
        count(pg, (560, y + 48), n, 30)

def page_step1(pg):
    step(pg, 1)
    count(pg, (500, 96), 1, 40)
    sc = Scene(0, 0, 19).at(330, 420)
    cupboard(sc, (0, 0, 0), top=False)
    sc.draw(pg)
    pg.arrow([sc.proj((8, 34, 8)), sc.proj((8, 18, 8))], 3.0)
    s2 = Scene(0, 0, 8).at(230, 760)
    cupboard(s2, (0, 0, 0), top=False)
    s2.draw(pg)
    h = s2.proj((14.5, 9, 16))
    pg.curved_arrow((h[0] + 6, h[1] - 6), (h[0] + 70, h[1] + 30), -0.45, 2.6, 12)
    check(pg, (420, 730), 26, False)

def page_step2(pg):
    step(pg, 2)
    count(pg, (500, 96), 1, 40)
    sc = Scene(0, 0, 19).at(330, 470, (0, 0, 0), (16, 30, 16))
    cupboard(sc, (0, 0, 0), top=False)
    worktop(sc, (0, 24, 0), xf=[rot("y", 8, (8, 0, 8)), rot("z", -4, (8, 0, 8))])
    sc.draw(pg)
    for x in (1.5, 14.5):
        pg.arrow([sc.proj((x, 23, 17.5)), sc.proj((x, 16.5, 17.5))], 3.0, 13)

def page_step3(pg):
    step(pg, 3)
    count(pg, (470, 96), 1, 40)
    screwdriver(pg, (585, 70), -60, 0.55)
    sc = Scene(0, 0, 19).at(320, 560)
    cupboard(sc, (0, 0, 0), loose=1.0)
    sc.draw(pg)
    tip = sc.proj((8, 19, 8))
    screwdriver(pg, (tip[0] + 4, tip[1] - 64), -95, 1.3, 2.6)
    twist(pg, (tip[0] + 2, tip[1] - 140))

def page_step4(pg):
    step(pg, 4)
    sc = Scene(0, 0, 17).at(330, 420, (-8, 0, 0), (16, 16, 24))
    cupboard(sc, (0, 0, 0), door=100)
    item_cube(pg, sc, (4, 9, 4))
    item_cube(pg, sc, (9, 9, 5), 3)
    item_cube(pg, sc, (6, 3, 5), 4)
    sc.draw(pg)
    gx, gy, cs = 70, 720, 28
    for r in range(3):
        for c in range(9):
            pg.path([(gx + c * cs, gy + r * cs), (gx + c * cs + cs, gy + r * cs), (gx + c * cs + cs, gy + r * cs + cs),
                     (gx + c * cs, gy + r * cs + cs)], 1.5, closed=True, amp=0.3)
    count(pg, (450, 790), 27, 44)

def tool_icon(pg, c, kind, size=1.0):
    x, y = c
    ux, uy = 0.707, -0.707
    P = lambda t, n: (x + (ux * t - uy * n) * size, y + (uy * t + ux * n) * size)
    pg.path([P(-55, -3.5), P(40, -3.5), P(40, 3.5), P(-55, 3.5)], 2.2, closed=True)
    if kind == "axe":
        blade = [P(14, -3.5), P(10, -22), P(4, -38), P(26, -40), P(46, -34), P(40, -16), P(38, -3.5)]
        pg.fill(blade)
        pg.path(blade, 2.4, closed=True)
        pg.path([P(8, -30), P(28, -33), P(44, -29)], 1.2)
    else:
        head = [P(40 - 0.012 * n * n, n) for n in range(-46, 47, 4)]
        head2 = [P(32 - 0.009 * n * n, n) for n in range(46, -47, -4)]
        pg.fill(head + head2)
        pg.path(head + head2, 2.4, closed=True)

def page_bodies(pg):
    for i, (body, tool, n) in enumerate([("wood", "axe", 12), ("concrete", "pickaxe", 16)]):
        y = i * 438
        sc = Scene(0, 0, 13).at(220, y + 230)
        cupboard(sc, (0, 0, 0), top=False, body=body)
        sc.draw(pg)
        count(pg, (420, y + 150), n, 40)
        tool_icon(pg, (500, y + 290), tool, 1.5)
    pg.line((60, 448), (584, 448), 1.6)

def page_mix(pg):
    for r, body in enumerate(["wood", "concrete"]):
        for c, fin in enumerate(["wood", "stone", "plain"]):
            sc = Scene(0, 0, 8.5).at(110 + c * 212, 230 + r * 250)
            cupboard(sc, (0, 0, 0), body=body, finish=fin)
            sc.draw(pg)
    check(pg, (322, 700), 30, True)

def page_hinge(pg):
    sc = Scene(0, 0, 11).at(220, 240, (-16, 0, 0), (16, 16, 32))
    cupboard(sc, (0, 0, 0), door=70, hinge="left")
    sc.draw(pg)
    screwdriver(pg, (480, 170), -120, 0.9)
    twist(pg, (446, 112), 26, 8)
    pg.curved_arrow((470, 330), (470, 540), -0.35, 3.0)
    sc2 = Scene(0, 0, 11).at(300, 660, (0, 0, -16), (32, 16, 32))
    cupboard(sc2, (0, 0, 0), door=70, hinge="right")
    sc2.draw(pg)

def page_break(pg):
    sc = Scene(0, 0, 12).at(220, 260)
    cupboard(sc, (0, 0, 0))
    sc.draw(pg)
    tool_icon(pg, (460, 220), "axe", 1.3)
    pg.arrow([(300, 420), (380, 520)], 3.0)
    s2 = Scene(0, 0, 8).at(420, 640)
    cupboard(s2, (0, 0, 0))
    s2.draw(pg)
    count(pg, (530, 790), 1, 40)
    check(pg, (200, 650), 28, True)

def sneak(pg, c, size=6):
    s = Scene(0, 0, size).at(c[0], c[1], (-4, 0, -2), (12, 30, 8))
    steve(s, (0, 0, 0), yaw=10, crouch=True)
    s.draw(pg)
    pg.arrow([(c[0] + 80, c[1] - 60), (c[0] + 80, c[1] + 10)], 2.6, 12)

def page_apart(pg):
    sneak(pg, (110, 150))
    screwdriver(pg, (300, 140), -60, 0.9)
    sc = Scene(0, 0, 15).at(330, 560, (0, 0, 0), (28, 28, 28))
    cupboard(sc, (0, 0, 0), lift=10)
    item_cube(pg, sc, (20, 0.5, 20), 3.5)
    item_cube(pg, sc, (13, 0.5, 26), 3)
    item_cube(pg, sc, (24, 0.5, 13), 3)
    sc.draw(pg)
    pg.arrow([sc.proj((8, 28, 17)), sc.proj((8, 38, 17))], 3.0)
    for p in ((20, 0.5, 20), (13, 0.5, 26), (24, 0.5, 13)):
        q = sc.proj((p[0] + 1.5, p[1] + 8, p[2] + 1.5))
        pg.line((q[0], q[1] - 24), (q[0], q[1] - 6), 1.4)

def page_loose(pg):
    sneak(pg, (90, 120), 4.5)
    screwdriver(pg, (230, 110), -60, 0.7)
    sc = Scene(0, 0, 9).at(430, 190, (0, 0, 0), (16, 26, 16))
    cupboard(sc, (0, 0, 0), loose=1.0, lift=8)
    sc.draw(pg)
    pg.arrow([sc.proj((8, 26, 17)), sc.proj((8, 34, 17))], 2.6, 12)
    pg.line((60, 420), (584, 420), 1.6)
    s1 = Scene(0, 0, 8).at(150, 560)
    cupboard(s1, (0, 0, 0), loose=1.0)
    s1.draw(pg)
    tool_icon(pg, (330, 520), "axe", 0.8)
    pg.arrow([(250, 640), (300, 700)], 2.6, 12)
    s2 = Scene(0, 0, 7).at(330, 770)
    cupboard(s2, (0, 0, 0), top=False)
    s2.draw(pg)
    s3 = Scene(0, 0, 7).at(510, 790, (0, 0, 0), (16, 2, 16))
    worktop(s3)
    s3.draw(pg)
    count(pg, (260, 860), 1, 30)
    count(pg, (560, 860), 1, 30)

SPREADS = [
    ("cover", page_cover, page_tools),
    ("parts", page_parts_carcass, page_parts_worktop),
    ("steps_a", page_step1, page_step2),
    ("steps_b", page_step3, page_step4),
    ("finishes", page_bodies, page_mix),
    ("hinge", page_hinge, page_break),
    ("apart", page_apart, page_loose),
]
SECTIONS = {"cover": "cover", "parts": "parts", "steps_a": "assembly", "steps_b": "assembly", "finishes": "assembly",
            "hinge": "use", "apart": "apart"}

def manual_icon():
    rows = ["................", "...##########...", "...#WWWWWWWWW#..", "...#WWWWWWWWW#..", "...#WKKKKKKWW#..",
            "...#WKWWWWKWW#..", "...#WKKKKKKWW#..", "...#WKWWWWKWW#..", "...#WKWWWWKWW#..", "...#WKKKKKKWW#..",
            "...#WWWWWWWWW#..", "...#WWWWWKKWW#..", "...#WWWWWWWWW#..", "...###########..", "....##########..", "................"]
    col = {"#": (40, 40, 40, 255), "W": (250, 250, 250, 255), "K": (70, 70, 70, 255), ".": (0, 0, 0, 0)}
    im = Image.new("RGBA", (16, 16))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row): im.putpixel((x, y), col[ch])
    os.makedirs(f"{A}/textures/item", exist_ok=True)
    im.save(f"{A}/textures/item/manual.png")
    cover = Image.new("RGBA", (16, 16), (52, 52, 52, 255))
    for i in range(16):
        for p in ((i, 0), (i, 15), (0, i), (15, i)): cover.putpixel(p, (30, 30, 30, 255))
    cover.save(f"{A}/{TEX}/stand_cover.png")

GUARD = {"fabric:load_conditions": [{"condition": "fabric:all_mods_loaded", "values": ["simplebooks"]}],
         "neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "simplebooks"}]}
# no custom_name, Simple Books names it from book.<mod>.manual.title
BOOK = {"simplebooks:book": f"{MOD}:manual", "minecraft:item_model": f"{MOD}:manual"}
SCREWDRIVERS = "#c:tools/screwdriver"

def data():
    D = f"{ROOT}/data/{MOD}"
    # any mod's screwdriver works and comes back, see KeepToolShapelessRecipe
    w(f"{D}/recipe/manual.json", {**GUARD, "type": f"{MOD}:crafting_shapeless_keep_tool", "category": "misc",
        "ingredients": ["minecraft:book", SCREWDRIVERS, "#c:chests/wooden"], "keep": SCREWDRIVERS,
        "result": {"id": "simplebooks:guide", "components": BOOK}})
    unlock = {"has_the_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": f"{MOD}:manual"}},
              "has_screwdriver": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": SCREWDRIVERS}]}}}
    w(f"{D}/advancement/recipes/misc/manual.json", {**GUARD, "parent": "minecraft:recipes/root", "criteria": unlock,
        "requirements": [list(unlock)], "rewards": {"recipes": [f"{MOD}:manual"]}})
    w(f"{D}/advancement/got_manual.json", {**GUARD, "criteria": {"screwdriver": {"trigger": "minecraft:inventory_changed",
        "conditions": {"items": [{"items": SCREWDRIVERS}]}}}, "requirements": [["screwdriver"]],
        "rewards": {"loot": [f"{MOD}:manual"]}})
    w(f"{D}/loot_table/manual.json", {**GUARD, "type": "minecraft:advancement_reward", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": "simplebooks:guide", "functions": [{"function": "minecraft:set_components", "components": BOOK}]}]}]})
    w(f"{A}/items/manual.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/manual"}})
    w(f"{A}/models/item/manual.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/manual"}})

def main(only=None):
    data()
    books = f"{A}/simplebooks/books"
    import shutil
    shutil.rmtree(f"{books}/manual", ignore_errors=True)
    shutil.rmtree(f"{A}/{TEX}", ignore_errors=True)
    os.makedirs(f"{A}/{TEX}", exist_ok=True)
    w(f"{books}/manual.json", {"title": f"book.{MOD}.manual.title", "default_template": f"{MOD}:leaflet",
        "style": {"page": "#ffffff", "ink": "#111111", "soft": "#8a8a8a", "cover": "#3a3a3a", "cover_edge": "#1e1e1e", "edge": "#c8c8c8"},
        "stand_cover": f"{MOD}:{TEX}/stand_cover.png", "sections": ["cover", "parts", "assembly", "use", "apart"]})
    w(f"{A}/simplebooks/templates/leaflet.json", {"vars": {}, "slots": {
        "left.art": {"type": "image_box", "box": [0, 0, PW, PH], "bind": "left_image", "frame": False},
        "right.art": {"type": "image_box", "box": [0, 0, PW, PH], "bind": "right_image", "frame": False},
        "left.page": {"type": "page_number", "box": [0, 892, PW, 20]},
        "right.page": {"type": "page_number", "box": [0, 892, PW, 20]}}})
    for i, (name, left, right) in enumerate(SPREADS):
        for side, fn in (("l", left), ("r", right)):
            for stand in (False, True):
                pg = Page(2.0 if stand else 1.0)
                fn(pg)
                pg.save(f"{name}_{side}" + ("_stand" if stand else ""), stand)
        w(f"{books}/manual/pages/{name}.json", {"section": SECTIONS[name], "sort": i,
            "left_image": f"{MOD}:{TEX}/{name}_l.png", "right_image": f"{MOD}:{TEX}/{name}_r.png",
            "stand_left": f"{MOD}:{TEX}/{name}_l_stand.png", "stand_right": f"{MOD}:{TEX}/{name}_r_stand.png"})
    manual_icon()

if __name__ == "__main__":
    main()
