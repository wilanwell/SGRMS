# SGRMS UI Redesign — "Indigo Clean"

Date: 2026-11-21 · Status: approved by the owner (2026-11-21), with the change below

## Intent

SGRMS is going into the owner's portfolio for **recruiters**. They will see it
in screenshots, a short demo video, or on a phone in an interview. The current
UI (Material blue `#1976D2`, flat grey cards, default font, uneven corner
radii) looks dated and inconsistent. The goal is a modern, consistent look
that reads as "professional product" at first glance. The app must still work
the same way.

The owner picked direction **A · Indigo Clean** from the mockups on the
[SGRMS Redesign canvas](https://claude.ai/artifact/1vqxkwPdL1NVPpwvb3U2d8)
(artboards "A · Indigo Clean — Admin" and "— Student"). Those two artboards
are the visual reference.

## Goals

- Every screen (all 37, for Admin, Teacher and Student) uses one visual
  system: the same colours, type, radii, spacing and components.
- The four "first impression" screens match the mockups closely:
  **Login, Admin dashboard, Teacher dashboard, Student dashboard**.
- **Every screen's layout is redesigned too** (owner's change on approval):
  list, detail, form and profile screens get new layouts, and old elements
  may be replaced with more modern ones (for example a filter dialog with
  filter chips, a dropdown with a bottom sheet or segmented control, a
  plain list with avatar rows, an info table with a grouped card).
- The design is built as a real **design system in code** (theme tokens plus
  shared components), so a recruiter reading the code sees good structure,
  not hard-coded hex values.

## Non-goals

- No changes to features, navigation routes, data, rules or the backend.
- No dark mode in this round. The colours are defined as `ColorScheme`
  tokens, so dark mode can be added later.
- No new screens; no translation work (copy stays as it is today).
- No app icon or splash-art redesign (the existing logo is kept).

## Anti-goals

These would count as failure even if the app technically works:

- Anything that used to work no longer works: a broken flow, lost data,
  crashes, or touch targets under 44 dp.
- A half-redesigned app, where some screens still have the old blue look.
- Text that fails contrast (body text below 4.5:1).
- Generic "AI-template" look: gradient washes, emoji, everything in purple.

## Constraints

- Jetpack Compose + Material 3, compileSdk 36 (Hilt and compose versions are
  pinned; see the project's build notes).
- The owner tests on a real phone (Xiaomi, Android 16), never an emulator.
- The free Spark Firebase plan is unaffected; the redesign is UI only.
- Fonts must not be hand-downloaded into the repo: use Compose
  **downloadable Google Fonts** (`ui-text-google-fonts`), falling back to the
  system font if the provider is unavailable.

## Approaches considered

1. **Token swap only.** Point the existing colour names at the new palette.
   Fast, but the old layouts stay, the dashboards would not match the
   mockups, and the code keeps misleading names (`PrimaryBlue` = indigo).
2. **Design system + restyled shared components + rebuilt key screens**
   *(chosen)*. Replace the colour names with semantic tokens, restyle the
   ~20 shared `Sgrms*` components (which all screens already use), then
   rebuild the 4 first-impression screens to match the mockups and tidy the
   rest. This gives the most visible change per hour and leaves clean code.
3. **Rewrite every screen from scratch.** Most control, but by far the most
   work and the highest risk of breaking behaviour. Not worth it, because
   the shared components already carry most of the look.

## The design

### Colour tokens (light)

| Token | Value | Use |
|---|---|---|
| `primary` | `#4F46E5` indigo | buttons, active nav, hero card, links |
| `onPrimary` | `#FFFFFF` | |
| `primaryContainer` | `#EEF2FF` | icon tiles, selected nav pill, grade chips |
| `onPrimaryContainer` | `#3730A3` | |
| `background` | `#F5F6FA` | screen ground |
| `surface` | `#FFFFFF` | cards, nav bar, fields |
| `onSurface` | `#111827` | primary text |
| `onSurfaceVariant` | `#6B7280` (≥4.5:1 on both grounds) | secondary text |
| `outlineVariant` | `#F1F2F6` | dividers inside cards |
| `outline` | `#D1D5DB` | field borders |
| success | `#047857` on `#ECFDF5` | Active, Complete, passing grades |
| warning | `#92400E` on `#FFFBEB` (bar `#F59E0B`) | Pending, low grade (C/D) |
| error | `#B91C1C` on `#FEF2F2` | failing grade, delete, errors |
| info accents | orange `#C2410C`/`#FFF7ED`, pink `#BE185D`/`#FDF2F8` | dashboard icon tiles only |

The old names (`PrimaryBlue`, `BackgroundGrey`, `CardWhite`, `TextGrey`,
`TextDark`, `SuccessGreen`, `WarningOrange`, `ErrorRed`, `LightBlue`,
`DarkBlue`; about 900 uses in 76 files) are replaced by
`MaterialTheme.colorScheme.*` or a small `SgrmsColors` object for the status
colours. `markColor()` (grade colours) moves to the new success, primary,
warning and error colours.

### Type

**Plus Jakarta Sans** (400/500/600/700/800) as the app font, through the
Material 3 `Typography`: display/headline 800 with tight letter-spacing,
titles 700, body 400/500, labels 600.

### Shape and spacing

- Radii: cards **20 dp**, hero card **24 dp**, fields and buttons **14 dp**,
  chips and pills fully round; icon tiles 12 dp. This replaces today's mix of
  12, 14, 16 and 18.
- Screen padding 20 dp; 12 to 14 dp between cards. Cards have no visible
  border and no heavy shadow; only the floating nav bar has a soft shadow.

### Components (restyled; same names and parameters wherever possible)

- **TopBar** becomes a large header with no blue bar: a greeting or screen
  title in large type on the background, optional subtitle, and a trailing
  action (avatar or icon button). Screens with a back arrow use a compact
  variant.
- **BottomNavigationBar** becomes a floating white pill with a soft shadow.
  The active item gets a `primaryContainer` pill; icon above label.
- Cards (`SgrmsFormCard`, `SgrmsCardStyle`, `DashboardCard`,
  `SgrmsDashboardSummaryCard`, `SgrmsSmallSummaryBox`) become white,
  borderless, 20 dp, with a coloured icon tile.
- `SgrmsTextField`, `SgrmsDropdownField` and `SgrmsSearchBar` become filled
  white fields with a 14 dp radius and an indigo focus ring.
- Buttons (`SgrmsButtons`): primary is filled indigo; secondary is a
  tonal `primaryContainer` button; destructive uses the error colour;
  52 dp tall.
- Info rows, empty states, progress bars and chips get the new type and
  colours. Empty states get an icon tile and one line of guidance.
- Status and grade chips: rounded pills using the semantic colour pairs.

### First-impression screens (match the mockups)

- **Login:** logo tile, large headline, white fields, full-width indigo
  button; light status bar icons are already handled.
- **Admin dashboard:** greeting header; indigo hero card with total
  accounts, a split bar and the role breakdown; a 2×2 grid of stat cards
  (Students, Teachers, Subjects, Classes); quick-action buttons; recent
  accounts list.
- **Student dashboard:** greeting header with a notification bell; overall
  grade ring card; subject list with progress bars and grade chips; latest
  notification banner.
- **Teacher dashboard:** the same pattern as Admin, using its existing
  data (student / subject / grade / report counts and subject averages).

All other screens are re-laid-out with the same patterns, by screen type:

- **List screens** (students, teachers, subjects, classes, grades, users,
  reports): header with a count; a search field; horizontally scrolling
  **filter chips** instead of dropdown dialogs; avatar rows inside one
  grouped card, with status pills and a trailing chevron; row actions in an
  overflow menu instead of three outlined buttons per card; an
  extended FAB for the main add action.
- **Detail screens** (student, teacher, subject, class, grade, report):
  a profile hero (avatar, name, key chips), then grouped info cards with
  icon rows, then primary and secondary actions at the bottom.
- **Form screens** (add/edit student, teacher, subject, grade, profile,
  password): one field group per card with section titles; dropdowns
  become exposed menus or **segmented buttons** where there are 2–3 options
  (gender, status); a sticky full-width save button.
- **Profile screens:** hero with photo, grouped settings-style rows,
  logout as a quiet destructive row.
- **Dialogs:** confirmation dialogs restyled; filter dialogs replaced by
  chips or a modal bottom sheet.

## Decided by the owner

- Direction A · Indigo Clean, for a recruiter-facing portfolio.

## My calls (please check)

- Light theme only in this round; dark mode later.
- Plus Jakarta Sans through downloadable Google Fonts.
- Colour names become semantic tokens (a code-quality signal for
  recruiters), rather than reusing the old names.
- Admin "Recent accounts" uses the existing users list (no new query).

## Left to the builder

- The exact order of the work, as long as the app builds after each step.
- Icon choices from Material Icons (rounded style where it exists).
- The exact layout of each secondary screen, within the patterns above.

## Done when

- No old colour names are left in `ui/` and no blue `#1976D2` look remains.
- The 4 key screens match the mockup layout on the real phone.
- All existing unit tests and rules tests still pass, and the app builds.
- Every screen has been opened on the phone for each role that can reach
  it, with no crash.
- Screenshots of the 4 key screens are taken for the portfolio.
