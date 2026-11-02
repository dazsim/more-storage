#!/usr/bin/env python3
# draws the assembly manual for Simple Books and writes its pages, run from repo root (gen_resources.py calls it)
import json, math, os, random
from PIL import Image, ImageDraw, ImageFont

MOD = "muchmorestorage"
ROOT = "src/main/resources"
A = f"{ROOT}/assets/{MOD}"
TEX = f"textures/simplebooks/manual"
PW, PH, SS = 644, 876, 3
STAND = (160, 218)
WOODS = ["oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped"]
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

def kubhala(sc, at=(0, 0, 0), insert=None, open=0.0, loose=0.0, items=0, body="wood", joins=""):
    """One cube shelf, open to +z. insert is None, "drawer", "door" or a SAKKSÄKK.
    joins holds "e" when a cube sits to the east, it then draws the one shared wall, "w" for the cube west of a join."""
    base = [move(*at)]
    xside = lambda z, y, x: (x, y, z)
    mk = grain(xside, 1, 15, 2, 14, 16, 2, 5) if body == "wood" else specks(xside, 0, 16, 2, 14, 16, 4, 6)
    x0 = 1 if "w" in joins else 2
    x1 = 15 if "e" in joins else 14
    sc.box((0, 0, 0), (16, 16, 1), base, width=2.0, bias=-4)
    if "w" not in joins:
        sc.box((0, 0, 0), (2, 16, 16), base)
    if "e" in joins:
        sc.box((15, 0, 0), (17, 16, 16), base, bias=0.5)
    else:
        sc.box((14, 0, 0), (16, 16, 16), base, marks=mk)
    sc.box((x0, 0, 1), (x1, 2, 16), base, width=2.0, bias=-2)
    sc.box((x0, 14, 1), (x1, 16, 16), base, width=2.0)
    for i, x in enumerate([4, 9, 6][:items]):
        item_cube(None, sc, (x + at[0], 2 + at[1], 6 + 2 * i + at[2]), 3.5)
    if not insert: return
    lxf = [rot("y", 6 * loose, (8, 8, 16)), move(0, 0, 2 * loose)]
    if insert == "box":
        bxf = [move(0, 0, 3 * open)] + base
        sc.box((2.4, 2, 1.5), (13.6, 12.8, 15.4), bxf, bias=3)
        sc.box((2.4, 11.6, 15.4), (13.6, 12.8, 15.7), bxf, width=1.6, bias=3.2)
        sc.box((6, 9.2, 15.4), (10, 10.4, 15.6), bxf, width=1.6, bias=3.4)
        return
    if insert == "drawer":
        dxf = lxf + [move(0, 0, 9 * open)] + base
        sc.box((2.3, 2.3, 15), (13.7, 13.7, 16), dxf, bias=3)
        sc.box((5.5, 10, 16), (10.5, 11, 17), dxf, width=1.8, bias=3.5)
        if open > 0:
            sc.box((3, 2.3, 3), (13, 10, 15), dxf, width=1.8, bias=-1)
    else:
        dxf = lxf + [rot("y", 100 * open, (13.7, 0, 16))] + base
        sc.box((2.3, 2.3, 15), (13.7, 13.7, 16), dxf, bias=3)
        sc.box((3.5, 5.5, 16), (4.5, 10.5, 17), dxf, width=1.8, bias=3.5)

def flatpack(sc, at=(0, 0, 0)):
    base = [move(*at)]
    for i, (dx, dz, a) in enumerate([(0, 0, 0), (0.6, -0.4, 4), (-0.5, 0.5, -3), (0.3, 0.2, 2)]):
        sc.box((1, i * 1.5, 1), (15, i * 1.5 + 1.5, 15), [rot("y", a, (8, 0, 8)), move(dx, 0, dz)] + base, width=2.0, bias=i)

def worktop(sc, at=(0, 0, 0), finish="wood", xf=()):
    top_face = lambda x, z, y: (x, y, z)
    tm = (grain(top_face, 1, 15, 1, 15, 2, 3, 9, vertical=False) if finish == "wood"
          else specks(top_face, 0, 16, 0, 16, 2, 9, 10) if finish == "stone"
          else specks(top_face, 0, 16, 0, 16, 2, 18, 12) if finish == "granite" else specks(top_face, 0, 16, 0, 16, 2, 3, 11))
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
    elif kind in ("slab", "slab_d", "slab_a", "slab_s"):
        ym = (y0 + y1) / 2
        pg.path([(x0, ym), (x1, ym), (x1, y1), (x0, y1)], 1.8, closed=True)
        if kind == "slab":
            yy = ym + (y1 - ym) / 2
            pg.line((x0, yy), (x1, yy), 1.2)
            pg.line((x0 + (x1 - x0) * 0.35, ym), (x0 + (x1 - x0) * 0.35, yy), 1.1)
            pg.line((x0 + (x1 - x0) * 0.7, yy), (x0 + (x1 - x0) * 0.7, y1), 1.1)
        if kind == "slab_s":
            pg.path([(x0 + 3, ym + 3), (x1 - 3, ym + 3), (x1 - 3, y1 - 3), (x0 + 3, y1 - 3)], 1.0, closed=True)
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
    elif kind == "string":
        pg.curve(lambda t: (x0 + 3 + (x1 - x0 - 6) * t, (y0 + y1) / 2 + math.sin(t * 9) * 5), 30, 1.8)
    elif kind == "wool":
        pg.path([(x0, y0), (x1, y0), (x1, y1), (x0, y1)], 1.8, closed=True)
        for k in range(3):
            cy = y0 + (y1 - y0) * (k + 0.5) / 3
            pg.curve(lambda t, cy=cy: (x0 + 4 + (x1 - x0 - 8) * t, cy + math.sin(t * 12) * 2), 24, 1.1)
    elif kind == "cane":
        for dx in (0.3, 0.55, 0.8):
            xx = x0 + (x1 - x0) * dx
            pg.line((xx, y1 - 1), (xx, y0 + 1), 2.2)
            pg.line((xx - 2, y0 + (y1 - y0) * 0.45), (xx + 2, y0 + (y1 - y0) * 0.45), 1.2)
    elif kind == "pane":
        cx = (x0 + x1) / 2
        pg.path([(cx - 3, y0), (cx + 3, y0), (cx + 3, y1), (cx - 3, y1)], 1.6, closed=True)
        pg.line((cx - 1, y0 + 5), (cx + 1, y0 + 10), 1.0)
    elif kind == "slime":
        pg.path([(x0 + 4, y0 + 4), (x1 - 4, y0 + 4), (x1 - 4, y1 - 4), (x0 + 4, y1 - 4)], 1.8, closed=True)
        pg.dot((x0 + (x1 - x0) * 0.38, y0 + (y1 - y0) * 0.42), 1.6)
        pg.dot((x0 + (x1 - x0) * 0.62, y0 + (y1 - y0) * 0.42), 1.6)
    elif kind == "dye":
        cx = (x0 + x1) / 2
        pg.path([(cx - 6, y1 - 2), (cx + 6, y1 - 2), (cx + 4, y0 + 8), (cx - 4, y0 + 8)], 1.8, closed=True)
        pg.line((cx - 2, y0 + 8), (cx - 2, y0 + 3), 1.4)
        pg.line((cx + 2, y0 + 8), (cx + 2, y0 + 3), 1.4)
    elif kind == "nugget":
        cx, cy = (x0 + x1) / 2, (y0 + y1) / 2 + 2
        pg.path([(cx - 8, cy + 2), (cx - 4, cy - 5), (cx + 5, cy - 6), (cx + 9, cy + 1), (cx + 3, cy + 6), (cx - 5, cy + 6)], 1.8, closed=True)
        pg.line((cx - 2, cy - 2), (cx + 3, cy - 3), 1.1)
    elif kind in ("book", "ebook", "wbook", "guide"):
        # a closed book seen from the front, its spine on the left
        bx0, bx1 = x0 + (x1 - x0) * 0.12, x1 - (x1 - x0) * 0.12
        pg.path([(bx0, y0), (bx1, y0), (bx1, y1), (bx0, y1)], 1.8, closed=True)
        pg.line((bx0 + 5, y0), (bx0 + 5, y1), 1.4)
        if kind == "ebook":
            for a in range(4):
                c = (bx0 + 6 + (bx1 - bx0 - 6) * (0.3 + 0.4 * (a % 2)), y0 + (y1 - y0) * (0.25 + 0.17 * a))
                pg.line((c[0] - 3, c[1]), (c[0] + 3, c[1]), 1.2)
                pg.line((c[0], c[1] - 3), (c[0], c[1] + 3), 1.2)
        elif kind == "wbook":
            pg.curve(lambda t: (bx0 + 10 + (bx1 - bx0 - 16) * t, y0 + (y1 - y0) * 0.75 - math.sin(t * 3) * 3 - t * 10), 20, 1.6)
            pg.line((bx1 - 6, y0 + (y1 - y0) * 0.35), (bx1 - 2, y0 + (y1 - y0) * 0.22), 2.2)
        elif kind == "guide":
            for k in range(3):
                yy = y0 + (y1 - y0) * (0.3 + 0.15 * k)
                pg.line((bx0 + 10, yy), (bx1 - 5, yy), 1.2)
        else:
            pg.line((bx0 + 10, y0 + (y1 - y0) * 0.3), (bx1 - 5, y0 + (y1 - y0) * 0.3), 1.6)
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

# ---------- recipe legends, read from the recipe files gen_resources.py just wrote ----------

def recipe_files():
    d = f"src/eras/26.1/data/{MOD}/recipe"
    out = {}
    for f in sorted(os.listdir(d)):
        with open(f"{d}/{f}") as fh: out[f[:-5]] = json.load(fh)
    return out

def drawn(pg, family, text, result_box, title=None, names=None, at=None, cell=None):
    """A drawn_recipes entry for every recipe in family, which must share one pattern."""
    pats = {tuple(r["pattern"]) for r in family}
    counts = {r["result"].get("count", 1) for r in family}
    assert len(pats) == 1 and len(counts) == 1, family
    pattern = list(pats.pop())
    keys = {}
    for r in family:
        for k, v in r["key"].items():
            for i in (v if isinstance(v, list) else [v]):
                keys.setdefault(k, [])
                if i not in keys[k]: keys[k].append(i)
    def ref(k):
        ids, n = keys[k], (names or {}).get(k)
        if len(ids) == 1 and not n: return ids[0]
        o = {"items": ids} if len(ids) > 1 else {"item": ids[0]}
        if n: o["name"] = n
        return o
    res = sorted(r["result"]["id"] for r in family)
    result = {"items": res} if len(res) > 1 else {"item": res[0]}
    result.update({"count": counts.pop(), "box": result_box})
    e = {"side": pg.side, "text": text, "result": result}
    if title: e["title"] = title
    if at is not None:
        e["grid"] = {"at": at, "cell": cell, "pattern": pattern}
        e["key"] = {k: ref(k) for k in keys}
    else:
        order = [c for c in "".join(pattern) if c != " "]
        e["ingredients"] = [dict(r if isinstance(r := ref(k), dict) else {"item": r}, count=order.count(k))
                            for k in dict.fromkeys(order)]
    pg.recipes.append(e)

def legend(name):
    return f"book.{MOD}.manual.{name}"

LEGENDS = {"iron_ingot": "Iron Ingot", "iron_nugget": "Iron Nugget", "chest": "Wooden Chest", "planks": "Planks (all one kind)",
           "concrete": "Concrete (all one colour)", "wood_slab": "Wooden Slab (all one kind)",
           "stone_slab": "Smooth Stone or Polished Stone Slab (all one kind)",
           "wood_carcass": "Wooden Carcass", "concrete_carcass": "Concrete Carcass",
           "wood_worktop": "Wooden Worktop", "stone_worktop": "Stone Worktop", "concrete_worktop": "Concrete Worktop",
           "wool_sakksakk": "SAKKSAKK (any wool colour)", "string": "String", "wool": "Wool (all one colour)",
           "sugar_cane": "Sugar Cane",
           "wood_flatpack": "Wooden KUBHALA Flat Pack", "concrete_flatpack": "Concrete KUBHALA Flat Pack",
           "wood_bokwurm": "Wooden BOKWURM Flat Pack", "concrete_bokwurm": "Concrete BOKWURM Flat Pack",
           "wood_ladstapel": "Wooden LADSTAPEL Flat Pack", "concrete_ladstapel": "Concrete LADSTAPEL Flat Pack",
           "wood_kladkaos": "Wooden KLADKAOS Panel", "concrete_kladkaos": "Concrete KLADKAOS Panel"}

def fam(pred):
    return [r for n, r in RECIPES.items() if pred(n, r)]

def is_wood(item):
    return any(item == f"minecraft:{w}_{s}" for w in WOODS for s in ("planks", "slab"))

def page_tools(pg):
    count(pg, (70, 150), 1, 44)
    screwdriver(pg, (330, 250), -25, 2.2, 3.0)
    grid(pg, 90, 520, 80, [[None, "ingot", None], [None, "stick", None]])
    pg.arrow([(355, 600), (435, 600)], 3.0)
    screwdriver(pg, (530, 620), -50, 0.9)
    count(pg, (500, 710), 1)
    drawn(pg, fam(lambda n, r: n == "screwdriver"), [90, 750, 520], [470, 560, 120, 120],
          names={"I": legend("iron_ingot")}, at=[170, 520], cell=80)

def page_parts_carcass(pg):
    count(pg, (60, 120), 1, 44)
    sc = Scene(0, 0, 11).at(330, 240)
    cupboard(sc, (0, 0, 0), top=False)
    sc.draw(pg)
    grid(pg, 50, 410, 70, [["plank"] * 3, ["plank", "chest", "plank"], ["plank"] * 3])
    pg.arrow([(285, 515), (350, 515)], 3.0)
    s2 = Scene(0, 0, 7).at(480, 510)
    cupboard(s2, (0, 0, 0), top=False)
    s2.draw(pg)
    count(pg, (560, 610), 1, 30)
    cell_icon(pg, "plank", 60, 630, 60)
    pg.text((140, 678), "/", 40, "ms")
    cell_icon(pg, "concrete", 160, 630, 60)
    carcass = lambda wood: fam(lambda n, r: n.endswith("_carcass") and is_wood(r["key"]["P"]) == wood)
    names = {"C": legend("chest")}
    drawn(pg, carcass(True), [50, 712, 560], [410, 440, 140, 140], legend("wood_carcass"),
          dict(names, P=legend("planks")), at=[50, 410], cell=70)
    drawn(pg, carcass(False), [50, 772, 560], [410, 440, 140, 140], legend("concrete_carcass"),
          dict(names, P=legend("concrete")))
    pg.recipes[-2]["hover"] = [{"box": [66, 636, 48, 48], "item": pg.recipes[-2]["key"]["P"]},
                               {"box": [166, 636, 48, 48], "item": {k: v for k, v in pg.recipes[-1]["ingredients"][0].items() if k != "count"}}]

def page_parts_worktop(pg):
    count(pg, (60, 110), 1, 44)
    sc = Scene(0, 0, 11).at(330, 170, (0, 0, 0), (16, 2, 16))
    worktop(sc, (0, 0, 0))
    sc.draw(pg)
    slabs = lambda r: r["key"].get("S", "")
    rows = [(["slab"] * 3, "wood", fam(lambda n, r: n.endswith("_worktop") and is_wood(slabs(r))), "wood_worktop", {"S": "wood_slab"}),
            (["slab_s"] * 3, "stone", fam(lambda n, r: n.endswith("_worktop") and "S" in r["key"] and not is_wood(slabs(r))),
             "stone_worktop", {"S": "stone_slab"}),
            (["slab_a", "slab_d", "slab_a"], "granite", fam(lambda n, r: n == "granite_worktop"), None, {}),
            (["concrete"] * 3, "plain", fam(lambda n, r: n.endswith("_worktop") and "C" in r["key"]), "concrete_worktop", {"C": "concrete"})]
    for i, (cells, fin, family, title, names) in enumerate(rows):
        y = 245 + i * 152
        n = family[0]["result"].get("count", 1)
        grid(pg, 40, y, 60, [cells])
        pg.arrow([(240, y + 30), (300, y + 30)], 3.0)
        s2 = Scene(0, 0, 5).at(400, y + 30, (0, -5, 0), (16, 2, 22) if n == 2 else (16, 2, 16))
        worktop(s2, (0, 0, 0), fin)
        if n == 2: worktop(s2, (0, -5, 6), fin)
        s2.draw(pg)
        count(pg, (500, y + 44), n, 30)
        drawn(pg, family, [40, y + 70, 580], [330, y - 6, 140, 72], title and legend(title),
              {k: legend(v) for k, v in names.items()}, at=[40, y], cell=60)

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

def page_kubhala(pg):
    sc = Scene(0, 0, 8).at(130, 200, (0, 0, 0), (16, 6, 16))
    flatpack(sc)
    sc.draw(pg)
    pg.arrow([(230, 200), (300, 200)], 3.0)
    screwdriver(pg, (360, 110), -60, 0.7)
    twist(pg, (356, 70), 26, 8)
    s1 = Scene(0, 0, 8).at(480, 210)
    kubhala(s1, items=2)
    s1.draw(pg)
    count(pg, (560, 330), 9, 30)
    grid(pg, 50, 410, 70, [["plank"] * 3, ["plank", "nugget", "plank"], ["plank"] * 3])
    pg.arrow([(285, 515), (350, 515)], 3.0)
    s2 = Scene(0, 0, 7).at(480, 515, (0, 0, 0), (16, 6, 16))
    flatpack(s2)
    s2.draw(pg)
    count(pg, (560, 610), 2, 30)
    cell_icon(pg, "plank", 60, 630, 60)
    pg.text((140, 678), "/", 40, "ms")
    cell_icon(pg, "concrete", 160, 630, 60)
    packs = lambda wood: fam(lambda n, r: n.endswith("_kubhala_flatpack") and is_wood(r["key"]["P"]) == wood)
    names = {"N": legend("iron_nugget")}
    drawn(pg, packs(True), [50, 712, 560], [410, 440, 140, 140], legend("wood_flatpack"),
          dict(names, P=legend("planks")), at=[50, 410], cell=70)
    drawn(pg, packs(False), [50, 772, 560], [410, 440, 140, 140], legend("concrete_flatpack"),
          dict(names, P=legend("concrete")))
    pg.recipes[-2]["hover"] = [{"box": [66, 636, 48, 48], "item": pg.recipes[-2]["key"]["P"]},
                               {"box": [166, 636, 48, 48], "item": {k: v for k, v in pg.recipes[-1]["ingredients"][0].items() if k != "count"}}]

def page_inserts(pg):
    rows = [("kubhala_drawer", [["slab", None, "slab"], ["slab", "nugget", "slab"], ["slab"] * 3], "drawer"),
            ("kubhala_door", [["slab", "slab"], ["slab", "nugget"], ["slab", "slab"]], "door")]
    for i, (name, cells, kind) in enumerate(rows):
        y = 50 + i * 250
        grid(pg, 50, y, 56, cells)
        pg.arrow([(250, y + 84), (310, y + 84)], 3.0)
        sc = Scene(0, 0, 6).at(430, y + 84)
        kubhala(sc, insert=kind, open=0.6)
        sc.draw(pg)
        drawn(pg, fam(lambda n, r: n == name), [50, y + 190, 560], [330, y, 200, 168], None,
              {"S": legend("wood_slab"), "N": legend("iron_nugget")}, at=[50, y], cell=56)
    pg.line((60, 545), (584, 545), 1.6)
    sc = Scene(0, 0, 7).at(250, 720, (0, 0, 0), (32, 16, 20))
    kubhala(sc, insert="drawer", loose=1.0, joins="e")
    kubhala(sc, (16, 0, 0), joins="w")
    sc.draw(pg)
    tip = sc.proj((8, 16, 16))
    screwdriver(pg, (tip[0] + 4, tip[1] - 50), -95, 1.0, 2.4)
    twist(pg, (tip[0] + 2, tip[1] - 110), 26, 8)
    screwdriver(pg, (520, 640), -60, 0.7)
    pg.arrow([(470, 700), (540, 780)], 2.6, 12)
    s3 = Scene(0, 0, 4).at(560, 820, (2, 2, 15), (14, 14, 17))
    s3.box((2.3, 2.3, 15), (13.7, 13.7, 16), [], bias=3)
    s3.box((5.5, 10, 16), (10.5, 11, 17), [], width=1.8, bias=3.5)
    s3.draw(pg)

def sakksakk(sc, at=(0, 0, 0), xf=()):
    """A loose SAKKSÄKK, a soft box with a rim and a hand hole on the front."""
    base = list(xf) + [move(*at)]
    sc.box((0, 0, 0), (12, 10, 12), base)
    sc.box((0, 9, 12), (12, 10, 12.4), base, width=1.6, bias=0.5)
    sc.box((4, 6.5, 12), (8, 7.5, 12.3), base, width=1.6, bias=0.7)

def slots(pg, x, y, s, n, filled=()):
    for i in range(n):
        pg.path([(x + i * s, y), (x + i * s + s, y), (x + i * s + s, y + s), (x + i * s, y + s)], 1.6, closed=True)
        if i in filled: cell_icon(pg, "plank" if i % 2 else "nugget", x + i * s, y, s)

def page_sakksakk(pg):
    grid(pg, 50, 50, 64, [["string", None, "string"], ["wool", None, "wool"], ["wool"] * 3])
    pg.arrow([(270, 146), (330, 146)], 3.0)
    sc = Scene(0, 0, 9).at(460, 146, (0, 0, 0), (12, 10, 12.4))
    sakksakk(sc)
    sc.draw(pg)
    wools = fam(lambda n, r: n.endswith("_sakksakk") and n != "wicker_sakksakk")
    drawn(pg, wools, [50, 262, 560], [350, 60, 220, 172], legend("wool_sakksakk"),
          {"S": legend("string"), "W": legend("wool")}, at=[50, 50], cell=64)
    grid(pg, 50, 330, 64, [["string", None, "string"], ["cane", None, "cane"], ["cane"] * 3])
    pg.arrow([(270, 426), (330, 426)], 3.0)
    sc = Scene(0, 0, 9).at(460, 426, (0, 0, 0), (12, 10, 12.4))
    sakksakk(sc)
    sc.draw(pg)
    drawn(pg, fam(lambda n, r: n == "wicker_sakksakk"), [50, 542, 560], [350, 340, 220, 172], None,
          {"S": legend("string"), "W": legend("sugar_cane")}, at=[50, 330], cell=64)
    pg.line((60, 610), (584, 610), 1.6)
    sc = Scene(0, 0, 6).at(150, 760, (0, 0, 0), (12, 10, 12.4))
    sakksakk(sc)
    sc.draw(pg)
    pg.arrow([(230, 760), (290, 760)], 3.0)
    slots(pg, 310, 735, 30, 9, filled=(0, 1, 3))
    count(pg, (445, 820), 9, 30)

def heading(pg, plain, accented, y=34):
    """The plain name the game uses, the accented spelling under it as a joke."""
    pg.text((34, y), plain, 46)
    pg.text((36, y + 58), accented, 22)

def bokwurm(sc, at=(0, 0, 0), books=0, body="wood"):
    """One bookcase, open to +z, books fill the top shelf from the left then the bottom one."""
    base = [move(*at)]
    xside = lambda z, y, x: (x, y, z)
    mk = grain(xside, 1, 15, 2, 14, 16, 2, 5) if body == "wood" else specks(xside, 0, 16, 2, 14, 16, 4, 6)
    sc.box((0, 0, 0), (16, 16, 1), base, width=2.0, bias=-4)
    sc.box((0, 0, 0), (1, 16, 16), base)
    sc.box((15, 0, 0), (16, 16, 16), base, marks=mk)
    sc.box((1, 0, 1), (15, 1, 16), base, width=2.0, bias=-2)
    sc.box((1, 15, 1), (15, 16, 16), base, width=2.0)
    sc.box((1, 7.5, 1), (15, 8.5, 15.5), base, width=2.0, bias=-1)
    bw = 14 / 6
    for i in range(books):
        row, col = divmod(i, 6)
        y0 = 8.5 if row == 0 else 1
        h = 5 + (i * 7 % 4) * 0.4
        x0 = 1 + col * bw + 0.15
        sc.box((x0, y0, 2), (x0 + bw - 0.3, y0 + h, 14.5), base, width=1.6, bias=1)

def enchanting_table(sc, at=(0, 0, 0)):
    base = [move(*at)]
    sc.box((0, 0, 0), (16, 12, 16), base)
    sc.box((4, 12, 6), (12, 13.5, 10), [rot("x", -15, (8, 12, 8))] + base, width=1.8, bias=2)

def ladstapel(sc, at=(0, 0, 0), open=None, amount=1.0):
    """The chest of drawers, open to +z, open is the drawer pulled out, 0 the top one."""
    base = [move(*at)]
    xside = lambda z, y, x: (x, y, z)
    sc.box((0, 0, 0), (16, 16, 1), base, width=2.0, bias=-4)
    sc.box((0, 0, 0), (1, 16, 16), base)
    sc.box((15, 0, 0), (16, 16, 16), base, marks=grain(xside, 1, 15, 2, 14, 16, 2, 5))
    sc.box((1, 0, 1), (15, 1, 16), base, width=2.0, bias=-2)
    sc.box((1, 15, 1), (15, 16, 16), base, width=2.0)
    for d in range(3):
        y0 = 1 + (2 - d) * 14 / 3 + 0.15
        y1 = y0 + 14 / 3 - 0.3
        dxf = ([move(0, 0, 9 * amount)] if d == open else []) + base
        sc.box((1.15, y0, 15), (14.85, y1, 16), dxf, bias=3)
        cy = (y0 + y1) / 2
        sc.box((5.5, cy - 0.5, 16), (10.5, cy + 0.5, 17), dxf, width=1.8, bias=3.5)
        if d == open:
            sc.box((2, y0, 3), (14, y1 - 1, 15), dxf, width=1.8, bias=-1)

def kladkaos(sc, at=(0, 0, 0), open=0):
    """The wardrobe, two blocks tall, doors to +z swung open by open degrees."""
    base = [move(*at)]
    xside = lambda z, y, x: (x, y, z)
    sc.box((0, 0, 0), (16, 32, 1), base, width=2.0, bias=-4)
    sc.box((0, 0, 0), (1, 32, 16), base)
    sc.box((15, 0, 0), (16, 32, 16), base, marks=grain(xside, 1, 15, 2, 30, 16, 2, 5))
    sc.box((1, 0, 1), (15, 1, 16), base, width=2.0, bias=-2)
    sc.box((1, 31, 1), (15, 32, 16), base, width=2.0)
    if open:
        sc.box((1, 6, 2), (15, 7, 15), base, width=1.6, bias=-1)
        sc.box((1, 27, 7.5), (15, 28, 8.5), base, width=1.6, bias=-1)
    for x0, x1, hinge, sign, hx in ((1, 7.9, 1, 1, 6.2), (8.1, 15, 15, -1, 8.8)):
        door = [rot("y", sign * open, (hinge, 0, 16))] + base
        sc.box((x0, 1, 15), (x1, 31, 16), door, bias=3)
        sc.box((hx, 13, 16), (hx + 1, 19, 17), door, width=1.8, bias=3.5)

def panel(sc, at=(0, 0, 0)):
    sc.box((1, 0, 2), (15, 1.5, 14), [move(*at)], width=2.0)

def page_kladkaos(pg):
    heading(pg, "KLADKAOS", "KLÄDKAOS")
    sc = Scene(0, 0, 5).at(90, 260, (0, 0, 0), (16, 6, 16))
    for i in range(3): panel(sc, (i * 0.6, i * 2, -i * 0.5))
    sc.draw(pg)
    pg.text((150, 330), "x 3", 26, "ls")
    screwdriver(pg, (250, 185), -60, 0.6)
    twist(pg, (246, 150), 22, 7)
    pg.arrow([(310, 250), (360, 250)], 3.0)
    s1 = Scene(0, 0, 6).at(470, 270, (0, 0, 0), (16, 32, 16))
    kladkaos(s1)
    s1.draw(pg)
    grid(pg, 50, 410, 70, [["plank", "plank"], ["plank", "nugget"], ["plank", "plank"]])
    pg.arrow([(215, 515), (300, 515)], 3.0)
    s2 = Scene(0, 0, 9).at(420, 515, (0, 0, 0), (16, 2, 16))
    panel(s2)
    s2.draw(pg)
    count(pg, (500, 580), 1, 30)
    cell_icon(pg, "plank", 60, 630, 60)
    pg.text((140, 678), "/", 40, "ms")
    cell_icon(pg, "concrete", 160, 630, 60)
    panels = lambda wood: fam(lambda n, r: n.endswith("_kladkaos_panel") and is_wood(r["key"]["P"]) == wood)
    names = {"N": legend("iron_nugget")}
    drawn(pg, panels(True), [50, 712, 560], [350, 440, 140, 140], legend("wood_kladkaos"),
          dict(names, P=legend("planks")), at=[50, 410], cell=70)
    drawn(pg, panels(False), [50, 772, 560], [350, 440, 140, 140], legend("concrete_kladkaos"),
          dict(names, P=legend("concrete")))

def page_kladkaos_use(pg):
    s1 = Scene(0, 0, 7).at(200, 250, (0, 0, 0), (16, 32, 26))
    kladkaos(s1, open=100)
    s1.draw(pg)
    pg.text((390, 200), "6 x 9", 30, "ls")
    for r in range(6): slots(pg, 330, 230 + r * 24, 24, 9)
    pg.line((60, 510), (584, 510), 1.6)
    sneak(pg, (130, 640))
    pg.arrow([(260, 700), (330, 700)], 3.0)
    s2 = Scene(0, 0, 4).at(450, 720, (0, 0, 0), (16, 32, 26))
    kladkaos(s2, open=100)
    s2.draw(pg)

def hjulborg(sc, at=(0, 0, 0)):
    """The chest on wheels, handle to +z."""
    base = [move(*at)]
    for x, z in ((2, 3), (2, 12), (13, 3), (13, 12)):
        sc.box((x, 0, z), (x + 1, 3, z + 1), base, width=1.8, bias=-1)
    sc.box((1.5, 3, 1.5), (14.5, 4, 14.5), base, width=1.8)
    sc.box((2, 4, 2), (14, 10, 14), base)
    sc.box((1.5, 10, 1.5), (14.5, 12, 14.5), base, width=2.0, bias=1)
    for x in (3, 12):
        sc.box((x, 5, 14), (x + 1, 16, 15), base, width=1.8, bias=2)
    sc.box((4, 15, 14), (12, 16, 15), base, width=1.8, bias=2.5)

def page_hjulborg(pg):
    heading(pg, "HJULBORG", "HJULBÖRG")
    sc = Scene(0, 0, 5).at(90, 250, (0, 0, 0), (16, 6, 16))
    for i in range(2): panel(sc, (i * 0.6, i * 2, -i * 0.5))
    sc.draw(pg)
    pg.text((150, 320), "x 2", 26, "ls")
    cell_icon(pg, "nugget", 60, 360, 60)
    pg.text((140, 405), "x 4", 26, "ls")
    screwdriver(pg, (250, 205), -60, 0.6)
    twist(pg, (246, 170), 22, 7)
    pg.arrow([(310, 270), (360, 270)], 3.0)
    s1 = Scene(0, 0, 10).at(470, 300, (0, 0, 0), (16, 16, 16))
    hjulborg(s1)
    s1.draw(pg)
    pg.line((60, 470), (584, 470), 1.6)
    s2 = Scene(0, 0, 8).at(320, 640, (0, 0, 0), (16, 16, 16))
    hjulborg(s2)
    s2.draw(pg)
    pg.text((320, 800), "2 x 9", 30, "ms")
    for r in range(2): slots(pg, 200, 820 + r * 24, 24, 9)

def page_hjulborg_use(pg):
    s1 = Scene(0, 0, 5).at(90, 220, (-4, 0, -2), (12, 30, 8))
    steve(s1, (0, 0, 0), yaw=90)
    s1.draw(pg)
    pg.arrow([(200, 230), (280, 230)], 3.0)
    s2 = Scene(0, 0, 7).at(420, 250, (0, 0, 0), (16, 16, 16))
    hjulborg(s2)
    s2.draw(pg)
    pg.arrow([(520, 260), (590, 260)], 3.0)
    pg.line((60, 400), (584, 400), 1.6)
    sneak(pg, (110, 540))
    s3 = Scene(0, 0, 6).at(420, 560, (0, 0, 0), (16, 16, 16))
    hjulborg(s3)
    s3.draw(pg)
    pg.arrow([(380, 640), (250, 640)], 2.6, 12)
    pg.text((400, 700), "< 7", 28, "ms")
    s4 = Scene(0, 0, 4).at(140, 800, (0, 0, 0), (16, 16, 16))
    hjulborg(s4)
    s4.draw(pg)
    check(pg, (250, 810), 20, True)
    s5 = Scene(0, 0, 4).at(400, 800, (0, 0, 0), (16, 16, 16))
    hjulborg(s5)
    s5.draw(pg)
    cell_icon(pg, "nugget", 440, 760, 30)
    check(pg, (520, 810), 20, False)

def genomskada(sc, at=(0, 0, 0), items=0):
    """The clear box, a lid on top and a few stacks seen through the walls."""
    base = [move(*at)]
    for i in range(items):
        x, z = 4 + (i % 3) * 4, 4 + (i // 3) * 4
        sc.box((x - 1.5, 0.5, z - 1.5), (x + 1.5, 2.5, z + 1.5), base, width=1.2, bias=-2)
    sc.box((1, 0, 1), (15, 11, 15), base, width=1.4)
    sc.box((0.5, 11, 0.5), (15.5, 12.5, 15.5), base, width=2.2, bias=1)
    sc.box((6, 12.5, 0.5), (10, 13, 2), base, width=1.6, bias=2)

def page_genomskada(pg):
    heading(pg, "GENOMSKADA", "GENÖMSKÅDA")
    grid(pg, 50, 200, 70, [["pane", None, "pane"], ["pane", "slime", "pane"], ["pane"] * 3])
    pg.arrow([(280, 305), (340, 305)], 3.0)
    s1 = Scene(0, 0, 10).at(470, 330, (0, 0, 0), (16, 13, 16))
    genomskada(s1, items=5)
    s1.draw(pg)
    pg.line((60, 470), (584, 470), 1.6)
    s2 = Scene(0, 0, 8).at(320, 620, (0, 0, 0), (16, 13, 16))
    genomskada(s2, items=9)
    s2.draw(pg)
    pg.text((320, 780), "1 x 9", 30, "ms")
    slots(pg, 212, 800, 24, 9, filled=range(9))

def page_genomskada_use(pg):
    s1 = Scene(0, 0, 4).at(150, 330, (0, 0, 0), (16, 38, 16))
    for k in range(3): genomskada(s1, (0, k * 12.5, 0), items=3)
    s1.draw(pg)
    check(pg, (260, 300), 20, True)
    s2 = Scene(0, 0, 4).at(420, 330, (0, 0, 0), (16, 50, 16))
    for k in range(4): genomskada(s2, (0, k * 12.5, 0))
    s2.draw(pg)
    check(pg, (530, 260), 20, False)
    pg.line((60, 470), (584, 470), 1.6)
    cell_icon(pg, "dye", 90, 580, 70)
    pg.arrow([(180, 615), (250, 615)], 3.0)
    s3 = Scene(0, 0, 7).at(400, 640, (0, 0, 0), (16, 13, 16))
    genomskada(s3, items=2)
    s3.draw(pg)

def hexkey(pg, c, size=1.0, ang=-30):
    """The L shaped hex key, long arm along ang, short arm bent down at the end."""
    a = math.radians(ang)
    ux, uy = math.cos(a), math.sin(a)
    nx, ny = -uy, ux
    P = lambda t, k: (c[0] + (ux * t + nx * k) * size, c[1] + (uy * t + ny * k) * size)
    outline = [P(0, -5), P(110, -5), P(116, 0), P(116, 40), P(106, 40), P(106, 5), P(0, 5)]
    pg.fill(outline)
    pg.path(outline + [P(-3, 0)], 2.4, closed=True)
    pg.line(P(8, 0), P(104, 0), 1.0)
    pg.line(P(111, 8), P(111, 36), 1.0)

def crate(sc, at=(0, 0, 0), step=4):
    """The flat-pack crate, boards at step 0, a wall a turn, the lid on the last."""
    base = [move(*at)]
    if step == 0:
        for i in range(4):
            d = 0.4 if i % 2 == 0 else -0.4
            sc.box((1 + d, i * 1.5, 1 - d), (15 + d, i * 1.5 + 1.5, 15 - d), base, width=1.8, bias=i)
        return
    sc.box((1, 0, 1), (15, 1.5, 15), base, width=1.8)
    sc.box((1, 1.5, 13.5), (15, 14, 15), base, width=2.0)
    if step >= 2:
        sc.box((1, 1.5, 1), (2.5, 14, 13.5), base, width=2.0)
        sc.box((13.5, 1.5, 1), (15, 14, 13.5), base, width=2.0)
    if step >= 3: sc.box((2.5, 1.5, 1), (13.5, 14, 2.5), base, width=2.0, bias=1)
    if step >= 4:
        sc.box((1, 14, 1), (15, 15.5, 15), base, width=2.2, bias=2)
        sc.box((2.5, 7, 0.5), (13.5, 8.5, 1), base, width=1.4, bias=3)

def wobble_marks(pg, c, r=60):
    for k in (-1, 1):
        pg.curve(lambda t, k=k: (c[0] + k * (r + 6 * math.sin(t * 9)), c[1] - 30 + t * 60), 20, 1.6)

def phone_guy(pg, c, s=1.0):
    """A stick figure on the phone, a question mark over its head."""
    x, y = c
    pg.circle((x, y - 90 * s), 18 * s, 2.4)
    pg.line((x, y - 72 * s), (x, y), 2.6)
    pg.line((x, y), (x - 22 * s, y + 55 * s), 2.6)
    pg.line((x, y), (x + 22 * s, y + 55 * s), 2.6)
    pg.line((x, y - 55 * s), (x - 34 * s, y - 25 * s), 2.6)
    pg.path([(x, y - 55 * s), (x + 26 * s, y - 70 * s), (x + 22 * s, y - 92 * s)], 2.6)
    phone = [(x + 14 * s, y - 112 * s), (x + 26 * s, y - 112 * s), (x + 28 * s, y - 84 * s), (x + 16 * s, y - 84 * s)]
    pg.fill(phone)
    pg.path(phone, 2.2, closed=True)
    pg.text((x - 6 * s, y - 120 * s), "?", int(54 * s), "ms")

def page_hexhojk(pg):
    hexkey(pg, (70, 160), 1.3, -20)
    s0 = Scene(0, 0, 9).at(430, 190, (0, 0, 0), (16, 6, 16))
    crate(s0, step=0)
    s0.draw(pg)
    pg.line((60, 330), (584, 330), 1.6)
    for i in range(4):
        cx = 100 + i * 140
        s = Scene(0, 0, 4.2).at(cx, 470, (0, 0, 0), (16, 16, 16))
        crate(s, step=i + 1)
        s.draw(pg)
        if i < 3: wobble_marks(pg, (cx, 470), 44)
        twist(pg, (cx, 390), 24, 8)
        for k in range(i + 1): pg.dot((cx - 15 + k * 10, 560), 3.0)
    pg.line((60, 600), (584, 600), 1.6)
    s4 = Scene(0, 0, 6.5).at(300, 720, (0, 0, 0), (16, 16, 16))
    crate(s4, step=4)
    s4.draw(pg)
    check(pg, (470, 720), 22, True)
    slots(pg, 214, 820, 24, 9)
    slots(pg, 214, 844, 24, 9)

def page_hexhojk_help(pg):
    s0 = Scene(0, 0, 7).at(160, 280, (0, 0, 0), (16, 16, 16))
    crate(s0, step=2)
    s0.draw(pg)
    wobble_marks(pg, (160, 280), 70)
    hexkey(pg, (60, 420), 0.8, -10)
    phone_guy(pg, (430, 300), 1.3)
    pg.line((60, 520), (584, 520), 1.6)
    s1 = Scene(0, 0, 7).at(220, 720, (0, 0, 0), (16, 16, 16))
    crate(s1, step=4)
    s1.draw(pg)
    pg.arrow([(330, 700), (400, 650)], 2.6, 12)
    screw = [(440, 600), (456, 600), (452, 612), (452, 660), (448, 668), (444, 660), (444, 612)]
    pg.path(screw, 2.2, closed=True)
    for k in range(5): pg.line((444, 620 + k * 9), (452, 616 + k * 9), 1.2)
    check(pg, (500, 720), 20, True)

def comparator(sc, at=(0, 0, 0)):
    base = [move(*at)]
    sc.box((0, 0, 0), (16, 2, 16), base)
    for x, z in ((4, 3), (12, 3), (8, 13)):
        sc.box((x - 1, 2, z - 1), (x + 1, 6, z + 1), base, width=1.6, bias=1)

def page_bokwurm(pg):
    heading(pg, "BOKWURM", "BÖKWÜRM")
    sc = Scene(0, 0, 7).at(110, 250, (0, 0, 0), (16, 6, 16))
    flatpack(sc)
    sc.draw(pg)
    pg.arrow([(195, 250), (255, 250)], 3.0)
    screwdriver(pg, (310, 175), -60, 0.6)
    twist(pg, (306, 140), 22, 7)
    s1 = Scene(0, 0, 8).at(480, 250)
    bokwurm(s1, books=7)
    s1.draw(pg)
    count(pg, (560, 370), 12, 30)
    grid(pg, 50, 410, 70, [["plank"] * 3, ["nugget", None, "nugget"], ["plank"] * 3])
    pg.arrow([(285, 515), (350, 515)], 3.0)
    s2 = Scene(0, 0, 7).at(480, 515, (0, 0, 0), (16, 6, 16))
    flatpack(s2)
    s2.draw(pg)
    count(pg, (560, 610), 1, 30)
    cell_icon(pg, "plank", 60, 630, 60)
    pg.text((140, 678), "/", 40, "ms")
    cell_icon(pg, "concrete", 160, 630, 60)
    packs = lambda wood: fam(lambda n, r: n.endswith("_bokwurm_flatpack") and is_wood(r["key"]["P"]) == wood)
    names = {"N": legend("iron_nugget")}
    drawn(pg, packs(True), [50, 712, 560], [410, 440, 140, 140], legend("wood_bokwurm"),
          dict(names, P=legend("planks")), at=[50, 410], cell=70)
    drawn(pg, packs(False), [50, 772, 560], [410, 440, 140, 140], legend("concrete_bokwurm"),
          dict(names, P=legend("concrete")))
    pg.recipes[-2]["hover"] = [{"box": [66, 636, 48, 48], "item": pg.recipes[-2]["key"]["P"]},
                               {"box": [166, 636, 48, 48], "item": {k: v for k, v in pg.recipes[-1]["ingredients"][0].items() if k != "count"}}]

def page_bokwurm_use(pg):
    for i, kind in enumerate(["book", "ebook", "wbook", "guide"]):
        cell_icon(pg, kind, 40 + i * 62, 90, 60)
    pg.arrow([(170, 170), (300, 230)], 3.0)
    s1 = Scene(0, 0, 9).at(440, 230)
    bokwurm(s1, books=9)
    s1.draw(pg)
    slots(pg, 60, 330, 34, 6, filled=())
    slots(pg, 60, 364, 34, 6, filled=())
    for i in range(9):
        cell_icon(pg, "book", 60 + (i % 6) * 34, 330 + (i // 6) * 34, 34)
    count(pg, (290, 395), 12, 30)
    pg.line((60, 450), (584, 450), 1.6)
    s2 = Scene(0, 0, 6).at(320, 660, (0, 0, 0), (48, 16, 40))
    bokwurm(s2, books=12)
    bokwurm(s2, (16, 0, 0), books=12)
    bokwurm(s2, (32, 0, 0), books=12)
    enchanting_table(s2, (16, 0, 32))
    s2.draw(pg)
    check(pg, (560, 800), 22)

def page_ladstapel(pg):
    heading(pg, "LADSTAPEL", "LÅDSTÄPEL")
    sc = Scene(0, 0, 7).at(110, 250, (0, 0, 0), (16, 6, 16))
    flatpack(sc)
    sc.draw(pg)
    pg.arrow([(195, 250), (255, 250)], 3.0)
    screwdriver(pg, (310, 175), -60, 0.6)
    twist(pg, (306, 140), 22, 7)
    s1 = Scene(0, 0, 8).at(480, 250)
    ladstapel(s1)
    s1.draw(pg)
    pg.text((520, 380), "3 x 9", 30, "ls")
    grid(pg, 50, 410, 70, [["plank", "nugget", "plank"], ["plank", "chest", "plank"], ["plank", "nugget", "plank"]])
    pg.arrow([(285, 515), (350, 515)], 3.0)
    s2 = Scene(0, 0, 7).at(480, 515, (0, 0, 0), (16, 6, 16))
    flatpack(s2)
    s2.draw(pg)
    count(pg, (560, 610), 1, 30)
    cell_icon(pg, "plank", 60, 630, 60)
    pg.text((140, 678), "/", 40, "ms")
    cell_icon(pg, "concrete", 160, 630, 60)
    packs = lambda wood: fam(lambda n, r: n.endswith("_ladstapel_flatpack") and is_wood(r["key"]["P"]) == wood)
    names = {"N": legend("iron_nugget"), "C": legend("chest")}
    drawn(pg, packs(True), [50, 712, 560], [410, 440, 140, 140], legend("wood_ladstapel"),
          dict(names, P=legend("planks")), at=[50, 410], cell=70)
    drawn(pg, packs(False), [50, 772, 560], [410, 440, 140, 140], legend("concrete_ladstapel"),
          dict(names, P=legend("concrete")))
    pg.recipes[-2]["hover"] = [{"box": [66, 636, 48, 48], "item": pg.recipes[-2]["key"]["P"]},
                               {"box": [166, 636, 48, 48], "item": {k: v for k, v in pg.recipes[-1]["ingredients"][0].items() if k != "count"}}]

def page_ladstapel_use(pg):
    s1 = Scene(0, 0, 10).at(250, 230, (0, 0, 0), (16, 16, 26))
    ladstapel(s1, open=1)
    s1.draw(pg)
    tip = s1.proj((8, 8, 26))
    pg.arrow([(tip[0] + 150, tip[1] + 40), (tip[0] + 20, tip[1] + 8)], 3.0)
    slots(pg, 170, 420, 34, 9, filled=(0, 1, 3))
    count(pg, (490, 452), 9, 30)
    pg.line((60, 510), (584, 510), 1.6)
    s2 = Scene(0, 0, 7).at(320, 690, (0, 0, 0), (40, 16, 16))
    ladstapel(s2, (0, 0, 0))
    comparator(s2, (24, 0, 0))
    s2.draw(pg)
    a, b = s2.proj((16, 1, 8)), s2.proj((24, 1, 8))
    pg.arrow([a, b], 2.6, 12)

def page_sakksakk_use(pg):
    sc = Scene(0, 0, 6).at(120, 160, (0, 0, 0), (12, 10, 12.4))
    sakksakk(sc)
    sc.draw(pg)
    pg.arrow([(200, 160), (270, 160)], 3.0)
    s1 = Scene(0, 0, 7).at(430, 160)
    kubhala(s1, insert="box", open=0.0)
    s1.draw(pg)
    check(pg, (560, 260), 20)
    s2 = Scene(0, 0, 7).at(320, 460, (0, 0, 0), (48, 16, 20))
    kubhala(s2, insert="box", joins="e")
    kubhala(s2, (16, 0, 0), insert="box", joins="we")
    kubhala(s2, (32, 0, 0), insert="box", open=1.0, joins="w")
    s2.draw(pg)
    pg.line((60, 590), (584, 590), 1.6)
    s3 = Scene(0, 0, 7).at(170, 740)
    kubhala(s3, insert="box", open=0.4)
    s3.draw(pg)
    tip = s3.proj((8, 14, 16))
    screwdriver(pg, (tip[0] + 4, tip[1] - 40), -95, 0.9, 2.4)
    pg.arrow([(290, 740), (350, 740)], 3.0)
    sc = Scene(0, 0, 5).at(420, 740, (0, 0, 0), (12, 10, 12.4))
    sakksakk(sc)
    sc.draw(pg)
    slots(pg, 470, 800, 26, 3, filled=(0, 1))
    # no box inside a box
    sc = Scene(0, 0, 3).at(470, 640, (0, 0, 0), (12, 10, 12.4))
    sakksakk(sc)
    sc.draw(pg)
    slots(pg, 510, 625, 30, 1)
    sc = Scene(0, 0, 1.6).at(525, 640, (0, 0, 0), (12, 10, 12.4))
    sakksakk(sc)
    sc.draw(pg)
    check(pg, (575, 640), 14, ok=False)

SPREADS = [
    ("cover", page_cover, page_tools),
    ("parts", page_parts_carcass, page_parts_worktop),
    ("steps_a", page_step1, page_step2),
    ("steps_b", page_step3, page_step4),
    ("finishes", page_bodies, page_mix),
    ("hinge", page_hinge, page_break),
    ("apart", page_apart, page_loose),
    ("kubhala", page_kubhala, page_inserts),
    ("sakksakk", page_sakksakk, page_sakksakk_use),
    ("bokwurm", page_bokwurm, page_bokwurm_use),
    ("ladstapel", page_ladstapel, page_ladstapel_use),
    ("kladkaos", page_kladkaos, page_kladkaos_use),
    ("hjulborg", page_hjulborg, page_hjulborg_use),
    ("genomskada", page_genomskada, page_genomskada_use),
    ("hexhojk", page_hexhojk, page_hexhojk_help),
]
SECTIONS = {"cover": "cover", "parts": "parts", "steps_a": "assembly", "steps_b": "assembly", "finishes": "assembly",
            "hinge": "use", "apart": "apart", "kubhala": "kubhala", "sakksakk": "kubhala",
            "bokwurm": "bokwurm", "ladstapel": "ladstapel", "kladkaos": "kladkaos", "hjulborg": "hjulborg", "genomskada": "genomskada", "hexhojk": "hexhojk"}

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
# forge 1.20.1 reads plain "conditions" on recipes and advancements, its loot tables have none
OLD_GUARD = {"fabric:load_conditions": GUARD["fabric:load_conditions"],
             "conditions": [{"type": "forge:mod_loaded", "modid": "simplebooks"}]}
# no custom_name, Simple Books names it from book.<mod>.manual.title
BOOK = {"simplebooks:book": f"{MOD}:manual", "minecraft:item_model": f"{MOD}:manual"}
SCREWDRIVERS = "#c:tools/screwdriver"

def data(era):
    g = G
    e = g.ERAS[era]
    D = f"src/eras/{era}/data/{MOD}"
    f = lambda kind: g.folder(era, kind)
    guard = OLD_GUARD if e.get("nbt") else GUARD
    book = BOOK if e.get("items") else {"simplebooks:book": BOOK["simplebooks:book"]}
    # any mod's screwdriver works and comes back, see KeepToolShapelessRecipe
    out = g.result(era, "simplebooks:guide", nbt=book) if e.get("nbt") else g.result(era, "simplebooks:guide", components=book)
    w(f"{D}/{f('recipe')}/manual.json", {**guard, "type": f"{MOD}:crafting_shapeless_keep_tool", "category": "misc",
        "ingredients": [g.ing(era, i) for i in ["minecraft:book", SCREWDRIVERS, "#c:chests/wooden"]], "keep": g.ing(era, SCREWDRIVERS),
        "result": out})
    unlock = {"has_the_recipe": g.unlocked(era, f"{MOD}:manual"),
              "has_screwdriver": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [g.item_pred(era, SCREWDRIVERS)]}}}
    w(f"{D}/{f('advancement')}/recipes/misc/manual.json", {**guard, "parent": "minecraft:recipes/root", "criteria": unlock,
        "requirements": [list(unlock)], "rewards": {"recipes": [f"{MOD}:manual"]}})
    w(f"{D}/{f('advancement')}/got_manual.json", {**guard, "criteria": {"screwdriver": {"trigger": "minecraft:inventory_changed",
        "conditions": {"items": [g.item_pred(era, SCREWDRIVERS)]}}}, "requirements": [["screwdriver"]],
        "rewards": {"loot": [f"{MOD}:manual"]}})
    if e.get("modifier"):
        entry = {"type": "minecraft:item", "name": "simplebooks:guide", "modifier": {"type": "minecraft:set_components", "components": book}}
    elif e.get("nbt"):
        # an optional tag instead of the item, so the table still parses on forge without Simple Books
        # not expanded, 1.20.1 skips the functions on expanded tag entries
        entry = {"type": "minecraft:tag", "name": f"{MOD}:manual_book", "expand": False,
                 "functions": [{"function": "minecraft:set_nbt", "tag": json.dumps(book, separators=(",", ":"))}]}
        w(f"{D}/{f('tags/item')}/manual_book.json", {"values": [{"id": "simplebooks:guide", "required": False}]})
    else:
        entry = {"type": "minecraft:item", "name": "simplebooks:guide", "functions": [{"function": "minecraft:set_components", "components": book}]}
    w(f"{D}/{f('loot_table')}/manual.json", {**guard, "type": "minecraft:advancement_reward", "pools": [{"rolls": 1, "entries": [entry]}]})
    w(f"{A}/models/item/manual.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/manual"}})
    if e.get("items"):
        w(f"src/eras/{era}/assets/{MOD}/items/manual.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/manual"}})

def era_page(era, obj):
    """The page json for an era, without items the era lacks and with its tag names."""
    g = G
    if isinstance(obj, dict):
        return {k: era_page(era, v) for k, v in obj.items()}
    if isinstance(obj, list):
        return [era_page(era, v) for v in obj if not isinstance(v, str) or g.has(era, v)]
    if isinstance(obj, str) and obj.startswith("#"):
        return "#" + g.tagname(era, obj[1:])
    return obj

def main(gen):
    """gen is the running gen_resources module, for its era helpers."""
    global RECIPES, G
    G = gen
    eras = gen.ERAS
    for era in eras:
        data(era)
    RECIPES = recipe_files()
    with open(f"{A}/lang/en_us.json") as f: lang = json.load(f)
    lang.update({legend(k): v for k, v in LEGENDS.items()})
    w(f"{A}/lang/en_us.json", lang)
    books = f"{A}/simplebooks/books"
    import shutil
    shutil.rmtree(f"{books}/manual", ignore_errors=True)
    pages = lambda era: f"src/eras/{era}/assets/{MOD}/simplebooks/books/manual/pages"
    shutil.rmtree(f"{A}/{TEX}", ignore_errors=True)
    os.makedirs(f"{A}/{TEX}", exist_ok=True)
    w(f"{books}/manual.json", {"title": f"book.{MOD}.manual.title", "default_template": f"{MOD}:leaflet",
        "style": {"page": "#ffffff", "ink": "#111111", "soft": "#555555", "cover": "#3a3a3a", "cover_edge": "#1e1e1e", "edge": "#c8c8c8"},
        "stand_cover": f"{MOD}:{TEX}/stand_cover.png", "sections": ["cover", "parts", "assembly", "use", "apart", "kubhala", "bokwurm", "ladstapel", "kladkaos", "hjulborg", "genomskada", "hexhojk"]})
    w(f"{A}/simplebooks/templates/leaflet.json", {"vars": {}, "slots": {
        "left.art": {"type": "image_box", "box": [0, 0, PW, PH], "bind": "left_image", "frame": False},
        "right.art": {"type": "image_box", "box": [0, 0, PW, PH], "bind": "right_image", "frame": False},
        "left.page": {"type": "page_number", "box": [0, 892, PW, 20]},
        "right.page": {"type": "page_number", "box": [0, 892, PW, 20]}}})
    for i, (name, left, right) in enumerate(SPREADS):
        recipes = []
        for side, fn in (("l", left), ("r", right)):
            for stand in (False, True):
                pg = Page(2.0 if stand else 1.0)
                pg.side, pg.recipes = "left" if side == "l" else "right", []
                fn(pg)
                pg.save(f"{name}_{side}" + ("_stand" if stand else ""), stand)
            recipes += pg.recipes
        page = {"section": SECTIONS[name], "sort": i,
            "left_image": f"{MOD}:{TEX}/{name}_l.png", "right_image": f"{MOD}:{TEX}/{name}_r.png",
            "stand_left": f"{MOD}:{TEX}/{name}_l_stand.png", "stand_right": f"{MOD}:{TEX}/{name}_r_stand.png"}
        if recipes: page["drawn_recipes"] = recipes
        for era in eras:
            w(f"{pages(era)}/{name}.json", era_page(era, page))
    manual_icon()

if __name__ == "__main__":
    import gen_resources
