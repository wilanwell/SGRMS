"""Generates every SGRMS logo asset (Android vectors + SVGs) from one geometry.

Run from anywhere:  python tools/generate_logo.py

Geometry is drawn in "design units" (the 1254 px reference image), then mapped
into each target viewport with an affine transform.
"""
import math
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, 'app', 'src', 'main', 'res')

INDIGO = '#4F46E5'
DEEP = '#3730A3'
WHITE = '#FFFFFF'
PALE = '#C7D2FE'

# ---------------------------------------------------------------- geometry
BOX = (178, 215, 1075, 1005)  # x0, y0, x1, y1 of the whole mark
R = 40          # outer page corner radius
SPINE_L, SPINE_R = 604, 644
GAP = 34        # transparent gap between page edge and bars
BAR_R = 16      # bar top corner radius
BARS = [(656, 776, 400), (806, 926, 308), (956, 1075, 215)]  # x0, x1, top

LEFT_TOP = [(178, 445), (400, 455), (545, 525), (SPINE_L, 612)]
LEFT_BOTTOM = [(SPINE_L, 1005), (530, 925), (390, 882), (178 + R, 886)]
RIGHT_TOP = [(SPINE_R, 612), (703, 525), (850, 470), (1075, 418)]
RIGHT_BOTTOM = [(1075 - R, 886), (863, 882), (723, 925), (SPINE_R, 1005)]


def bez(p, t):
    u = 1 - t
    return tuple(u**3 * p[0][i] + 3 * u * u * t * p[1][i] + 3 * u * t * t * p[2][i] + t**3 * p[3][i] for i in (0, 1))


def bez_d(p, t):
    u = 1 - t
    return tuple(3 * u * u * (p[1][i] - p[0][i]) + 6 * u * t * (p[2][i] - p[1][i]) + 3 * t * t * (p[3][i] - p[2][i]) for i in (0, 1))


def offset_curve(p, g, n=400):
    pts = []
    for k in range(n + 1):
        t = k / n
        x, y = bez(p, t)
        dx, dy = bez_d(p, t)
        L = math.hypot(dx, dy)
        nx, ny = dy / L, -dx / L  # points up-left, away from the page
        pts.append((x + g * nx, y + g * ny))
    return pts


def y_on(poly, x):
    for (x0, y0), (x1, y1) in zip(poly, poly[1:]):
        if x0 <= x <= x1:
            return y0 + (y1 - y0) * (x - x0) / (x1 - x0)
    # Past either end: continue the nearest segment's slope.
    (x0, y0), (x1, y1) = (poly[0], poly[1]) if x < poly[0][0] else (poly[-2], poly[-1])
    return y0 + (y1 - y0) * (x - x0) / (x1 - x0)


OFFSET = offset_curve(RIGHT_TOP, GAP)


class T:
    """Affine map design units -> viewport units."""

    def __init__(self, s, dx, dy):
        self.s, self.dx, self.dy = s, dx, dy

    def p(self, x, y):
        return f'{self.s * x + self.dx:.2f},{self.s * y + self.dy:.2f}'


def page_paths(t):
    lt, lb, rt, rb = LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM
    left = (
        f'M{t.p(178, lt[0][1] + R)} Q{t.p(178, lt[0][1])} {t.p(178 + R, lt[0][1] + 2)} '
        f'C{t.p(*lt[1])} {t.p(*lt[2])} {t.p(*lt[3])} '
        f'L{t.p(*lb[0])} C{t.p(*lb[1])} {t.p(*lb[2])} {t.p(*lb[3])} '
        f'Q{t.p(178, 886)} {t.p(178, 886 - R)} Z'
    )
    right = (
        f'M{t.p(*rt[0])} C{t.p(*rt[1])} {t.p(*rt[2])} {t.p(*rt[3])} '
        f'L{t.p(1075, 886 - R)} Q{t.p(1075, 886)} {t.p(*rb[0])} '
        f'C{t.p(*rb[1])} {t.p(*rb[2])} {t.p(*rb[3])} Z'
    )
    return left + ' ' + right


def bar_paths(t):
    out = []
    for x0, x1, top in BARS:
        bottom = [(x, y) for x, y in OFFSET if x0 < x < x1]
        bottom = [(x1, y_on(OFFSET, x1))] + bottom[::-1] + [(x0, y_on(OFFSET, x0))]
        d = (
            f'M{t.p(x0, top + BAR_R)} Q{t.p(x0, top)} {t.p(x0 + BAR_R, top)} '
            f'L{t.p(x1 - BAR_R, top)} Q{t.p(x1, top)} {t.p(x1, top + BAR_R)} '
            + ' '.join('L' + t.p(x, y) for x, y in bottom[::8] + [bottom[-1]])
            + ' Z'
        )
        out.append(d)
    return ' '.join(out)


def fit(viewport, content):
    """Transform that centres the mark's box in a square viewport, `content` wide/tall at most."""
    x0, y0, x1, y1 = BOX
    w, h = x1 - x0, y1 - y0
    s = content / max(w, h)
    return T(s, viewport / 2 - s * (x0 + w / 2), viewport / 2 - s * (y0 + h / 2))


# ---------------------------------------------------------------- writers
def vector(name, size_dp, viewport, page_color, bar_color, content):
    t = fit(viewport, content)
    xml = f'''<?xml version="1.0" encoding="utf-8"?>
<!-- Generated SGRMS mark: an open record book with three rising grade bars. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="{size_dp}dp"
    android:height="{size_dp}dp"
    android:viewportWidth="{viewport}"
    android:viewportHeight="{viewport}">
    <path
        android:fillColor="{page_color}"
        android:pathData="{page_paths(t)}" />
    <path
        android:fillColor="{bar_color}"
        android:pathData="{bar_paths(t)}" />
</vector>
'''
    open(os.path.join(RES, 'drawable', name + '.xml'), 'w', encoding='utf-8', newline='\n').write(xml)


def svg(path, viewport, page_color, bar_color, content, tile=None):
    t = fit(viewport, content)
    bg = f'<rect width="{viewport}" height="{viewport}" rx="{viewport * 0.22:.1f}" fill="{tile}"/>' if tile else ''
    s = (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {viewport} {viewport}" width="512" height="512">'
         f'{bg}<path fill="{page_color}" d="{page_paths(t)}"/><path fill="{bar_color}" d="{bar_paths(t)}"/></svg>\n')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, 'w', encoding='utf-8', newline='\n').write(s)


# Adaptive icon layers: 108 dp canvas, mark kept inside the 66 dp safe circle.
vector('ic_launcher_foreground', 108, 108, WHITE, PALE, 46)
vector('ic_launcher_monochrome', 108, 108, WHITE, WHITE, 46)
# Notification small icon: one colour, the system tints it.
vector('ic_stat_sgrms', 24, 24, WHITE, WHITE, 20)
# Full-colour mark for the login tile and the PDF report header.
vector('sgrms_logo', 48, 48, WHITE, PALE, 44)

def legacy_icon(name, round_):
    """Pre-Android 8 launcher icon: indigo tile (or circle) with the mark, one vector."""
    t = fit(48, 28)
    tile = ('M24,2 A22,22 0 1,1 24,46 A22,22 0 1,1 24,2 Z' if round_ else
            'M12,2 L36,2 Q46,2 46,12 L46,36 Q46,46 36,46 L12,46 Q2,46 2,36 L2,12 Q2,2 12,2 Z')
    xml = f'''<?xml version="1.0" encoding="utf-8"?>
<!-- Generated SGRMS launcher icon for Android 7.x (Android 8+ uses the adaptive icon). -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp"
    android:height="48dp"
    android:viewportWidth="48"
    android:viewportHeight="48">
    <path android:fillColor="{INDIGO}" android:pathData="{tile}" />
    <path android:fillColor="{WHITE}" android:pathData="{page_paths(t)}" />
    <path android:fillColor="{PALE}" android:pathData="{bar_paths(t)}" />
</vector>
'''
    d = os.path.join(RES, 'mipmap-anydpi')
    os.makedirs(d, exist_ok=True)
    open(os.path.join(d, name + '.xml'), 'w', encoding='utf-8', newline='\n').write(xml)


legacy_icon('ic_launcher', False)
legacy_icon('ic_launcher_round', True)

# SVG copies for the README and documents.
svg(os.path.join(ROOT, 'docs', 'logo', 'sgrms-logo.svg'), 108, INDIGO, DEEP, 100)
svg(os.path.join(ROOT, 'docs', 'logo', 'sgrms-app-icon.svg'), 108, WHITE, PALE, 60, tile=INDIGO)
print('generated')
