# Indigo Clean UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give all 37 SGRMS screens the "Indigo Clean" look and new layouts, built as a design system in code, with no change in behaviour.

**Architecture:** First a theme layer (Material 3 `ColorScheme`, `Typography`, `Shapes`, plus `SgrmsColors` for status colours) replaces every old hard-coded colour name. Then the shared `ui/components` are restyled and a few new pattern components are added. Finally the screens are re-laid-out group by group using those components. ViewModels, repositories and navigation routes stay as they are, except for small read-only additions to the dashboards.

**Tech Stack:** Kotlin, Jetpack Compose + Material 3 (BOM 2024.09.00), `androidx.compose.ui:ui-text-google-fonts` (version from the BOM), Hilt.

**Spec:** `docs/superpowers/specs/2026-11-21-ui-redesign-design.md`. Visual reference: artboards "A · Indigo Clean — Admin / Student" on https://claude.ai/artifact/1vqxkwPdL1NVPpwvb3U2d8.

## Global Constraints

- Colours exactly as the spec's token table: primary `#4F46E5`, primaryContainer `#EEF2FF`, onPrimaryContainer `#3730A3`, background `#F5F6FA`, surface `#FFFFFF`, onSurface `#111827`, onSurfaceVariant `#6B7280`, outlineVariant `#F1F2F6`, outline `#D1D5DB`; success `#047857`/`#ECFDF5`; warning `#92400E`/`#FFFBEB` (bar `#F59E0B`); error `#B91C1C`/`#FEF2F2`; accent orange `#C2410C`/`#FFF7ED`, pink `#BE185D`/`#FDF2F8`.
- Font: Plus Jakarta Sans 400/500/600/700/800 via downloadable Google Fonts, falling back to the system font.
- Radii: card 20 dp, hero 24 dp, field/button 14 dp, icon tile 12 dp, chips/pills fully round. Screen padding 20 dp, 12–14 dp between cards.
- Touch targets ≥ 44 dp; buttons 52 dp tall; body text contrast ≥ 4.5:1.
- Light theme only. No emoji, no gradient washes.
- No changes to features, routes, Firestore schema, rules or backend. compileSdk 36; do not bump pinned versions.
- After every task: `./gradlew :app:assembleDebug :app:testDebugUnitTest` passes and the change is committed on `main` (no push).
- Phone testing goes over adb to the owner's Xiaomi (Android 16), never an emulator. Before any `adb shell input`, check `dumpsys activity activities | grep topResumedActivity` and do not take over the phone if another app is in front.

## Review Focus

1. **Long names and labels** (e.g. "Sarah Hadizah binti Abdullah", "Bahasa Inggeris"): must ellipsize on one line in rows, chips and headers, never push trailing chips or buttons off-screen.
2. **Large system font size** (Settings → font size ~1.3×): headers, stat cards and the bottom nav must still fit without overlap at 360 dp width.
3. **Font provider unavailable** (no Google Play services / offline first launch): text must render with the system font, with no crash or blank text.
4. **Empty and loading states** on every list and dashboard (0 students, 0 grades, loading): show the new empty-state card or a spinner, never a blank screen.
5. **Landscape and narrow screens:** the floating nav bar and FABs must stay inside the horizontal safe area (the earlier landscape fix must not regress), and lists stay scrollable.

Owning tests: (1) Task 2 `initialsOf` tests plus `maxLines = 1` + `TextOverflow.Ellipsis` in `SgrmsListRow`/`SgrmsPageHeader`; (2) and (5) Task 11 phone checks; (3) Task 1 `SgrmsFontFamily` fallback; (4) Task 2 `SgrmsEmptyState` used by every list task.

---

### Task 1: Theme foundation and colour migration

**Files:**
- Modify: `app/src/main/java/com/example/sgrms/ui/theme/Color.kt`, `Theme.kt`, `Type.kt`, `GradeColors.kt`
- Create: `app/src/main/java/com/example/sgrms/ui/theme/Shape.kt`, `app/src/main/res/values/font_certs.xml`
- Modify: every `ui/**/*.kt` that uses the old colour names; `app/build.gradle.kts`, `gradle/libs.versions.toml`
- Test: `app/src/test/java/com/example/sgrms/ui/theme/ColorContrastTest.kt`

**Interfaces:**
- Produces: `SgrmsTheme` colour scheme as in Global Constraints; `object SgrmsColors { Success, SuccessContainer, Warning, WarningContainer, WarningBar, Error, ErrorContainer, Orange, OrangeContainer, Pink, PinkContainer }` (all `Color`); `val SgrmsFontFamily: FontFamily`; `SgrmsShapes` with `small = 12.dp`, `medium = 14.dp`, `large = 20.dp`, `extraLarge = 24.dp`; `fun markColor(marks: Double): Color` (unchanged signature); `fun contrastRatio(a: Color, b: Color): Double` in `Color.kt`.
- Removes: `PrimaryBlue`, `DarkBlue`, `LightBlue`, `BackgroundGrey`, `CardWhite`, `TextDark`, `TextGrey`, `SuccessGreen`, `WarningOrange`, `ErrorRed`.

- [ ] **Step 1: Write `ColorContrastTest`** with tests `secondaryTextPassesOnBackgroundAndSurface` (`contrastRatio(OnSurfaceVariant, Background) >= 4.5` and on `Surface`), `statusPairsPass` (each of Success/Warning/Error on its container ≥ 4.5), and `primaryButtonTextPasses` (`contrastRatio(Color.White, Primary) >= 4.5`).
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests '*ColorContrastTest*'`. Expected: FAIL (no `contrastRatio`).
- [ ] **Step 3: Implement the tokens:** `contrastRatio` with the WCAG relative-luminance formula; a `lightColorScheme` from the constants; `SgrmsColors`; `SgrmsShapes` (`RoundedCornerShape`); `Typography` built on `SgrmsFontFamily` (`GoogleFont("Plus Jakarta Sans")` with `GoogleFont.Provider("com.google.android.gms.fonts", "com.google.android.gms", R.array.com_google_android_gms_fonts_certs)`; display/headline weight 800 and letter spacing −0.5 sp, titles 700, body 400/500, labels 600). `markColor`: EXCELLENT → `SgrmsColors.Success`, GOOD/SATISFACTORY → `colorScheme.primary` value `#4F46E5`, NEEDS_IMPROVEMENT → `SgrmsColors.WarningBar`, AT_RISK → `SgrmsColors.Error`. Add `ui-text-google-fonts` to the catalog without a version (BOM-managed).
- [ ] **Step 4: Migrate the colour names** across `ui/`: in composables use `MaterialTheme.colorScheme.primary` / `.background` / `.surface` / `.onSurface` / `.onSurfaceVariant` / `.primaryContainer`; outside composition (e.g. default parameters) use the `Color.kt` constants `Primary`, `Background`, `Surface`, `OnSurface`, `OnSurfaceVariant`. Map old to new: PrimaryBlue→primary, DarkBlue→onPrimaryContainer, LightBlue→primaryContainer, BackgroundGrey→background, CardWhite→surface, TextDark→onSurface, TextGrey→onSurfaceVariant, SuccessGreen→SgrmsColors.Success, WarningOrange→SgrmsColors.WarningBar, ErrorRed and `Color.Red`→SgrmsColors.Error. Delete the old names.
- [ ] **Step 5: Verify** `grep -rnE 'PrimaryBlue|BackgroundGrey|CardWhite|TextGrey|TextDark|SuccessGreen|WarningOrange|ErrorRed|LightBlue|DarkBlue|0xFF1976D2' app/src/main` returns nothing, and `./gradlew :app:assembleDebug :app:testDebugUnitTest` passes.
- [ ] **Step 6: Commit** `feat(ui): Indigo Clean theme tokens, Plus Jakarta Sans, semantic colours`.

### Task 2: Shared components — restyle and new patterns

**Files:**
- Modify: all of `ui/components/*.kt` except `ProfileImagePicker.kt` (photo logic) and `StatusBarIcons.kt`
- Create: `ui/components/SgrmsPatterns.kt`
- Test: `app/src/test/java/com/example/sgrms/ui/components/InitialsTest.kt`

**Interfaces:**
- Consumes: Task 1 tokens.
- Keeps existing signatures of `TopBar`, `BottomNavigationBar`, `SgrmsTextField`, `SgrmsDropdownField`, `SgrmsSearchBar`, `SgrmsPrimaryButton`, `SgrmsSecondaryButton`, `SgrmsButtonRow`, `SgrmsCard`, `SgrmsFormCard`, `SgrmsInfoRow`, `SgrmsEmptyStateCard`, `SgrmsDeleteDialog`, `SgrmsDashboardSummaryCard`, `SgrmsSmallSummaryBox`, `SgrmsProgressBar`, `LoadMoreButton` (callers keep compiling).
- Produces in `SgrmsPatterns.kt`:
  - `fun initialsOf(name: String): String`
  - `@Composable fun SgrmsPageHeader(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null, trailing: @Composable RowScope.() -> Unit = {})`
  - `@Composable fun SgrmsAvatar(name: String, size: Dp = 40.dp, imageUrl: String = "", legacyImageBase64: String = "")`
  - `@Composable fun SgrmsIconTile(icon: ImageVector, tint: Color, container: Color, size: Dp = 36.dp)`
  - `enum class ChipTone { Neutral, Primary, Success, Warning, Error }` and `@Composable fun SgrmsStatusChip(text: String, tone: ChipTone)`
  - `@Composable fun SgrmsFilterChips(options: List<String>, selected: String, onSelect: (String) -> Unit)` (one horizontally scrolling row)
  - `@Composable fun SgrmsGroupedCard(content: @Composable ColumnScope.() -> Unit)` (white 20 dp card, rows divided by outlineVariant)
  - `@Composable fun SgrmsListRow(title: String, subtitle: String?, leading: @Composable () -> Unit, trailing: @Composable RowScope.() -> Unit = {}, onClick: (() -> Unit)? = null)`
  - `data class RowAction(val label: String, val icon: ImageVector, val destructive: Boolean = false, val onClick: () -> Unit)` and `@Composable fun SgrmsOverflowMenu(actions: List<RowAction>)`
  - `@Composable fun SgrmsSegmentedField(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit)`
  - `@Composable fun SgrmsStatCard(value: String, label: String, icon: ImageVector, tint: Color, container: Color, modifier: Modifier = Modifier)`
  - `@Composable fun SgrmsEmptyState(title: String, message: String, icon: ImageVector)`

- [ ] **Step 1: Write `InitialsTest`:** `initialsOf("Adam bin Anuar") == "AA"`, `initialsOf("Sarah Hadizah binti Abdullah") == "SH"` (the words "bin"/"binti"/"a/l"/"a/p" are skipped), `initialsOf("System Administrator") == "SA"`, `initialsOf("  ") == "?"`, `initialsOf("arman") == "A"`.
- [ ] **Step 2: Run** `./gradlew :app:testDebugUnitTest --tests '*InitialsTest*'`. Expected: FAIL.
- [ ] **Step 3: Implement `SgrmsPatterns.kt`** per the Interfaces block. Titles in `SgrmsListRow` and `SgrmsPageHeader` use `maxLines = 1` and `TextOverflow.Ellipsis`; chips never wrap.
- [ ] **Step 4: Restyle the existing components to the spec.**
  - `TopBar` renders `SgrmsPageHeader` on the background, with no blue surface. `showBack` maps to `onBack`; `actions` maps to `trailing`; `showIcon`/`icon` are ignored.
  - `BottomNavigationBar` becomes a floating white pill: radius 22 dp, 16 dp side and bottom margins, shadow `0 8 24 #11182714`; the active item gets a primaryContainer pill and primary colour, with icon above label.
  - Fields: white fill, 14 dp, outline `#D1D5DB`, primary focus.
  - Buttons: 52 dp, 14 dp radius. Primary is filled primary; secondary is a tonal primaryContainer button.
  - Cards: 20 dp, no border, no elevation.
  - `SgrmsEmptyStateCard` delegates to `SgrmsEmptyState`.
- [ ] **Step 5: Verify** tests pass, the app builds, and an install on the phone shows the new header and floating nav with no crash on the Admin dashboard.
- [ ] **Step 6: Commit** `feat(ui): restyle shared components and add pattern components`.

### Task 3: Login and Forgot Password

**Files:** Modify `ui/login/LoginScreen.kt`, `ui/login/ForgotPasswordDialog.kt`.

**Interfaces:** Consumes Task 1–2 components; `AuthViewModel` and `ForgotPasswordViewModel` are unchanged.

- [ ] **Step 1: Re-lay-out Login:** a 64 dp primaryContainer logo tile with the graduation icon, the headline "Welcome back" (headlineMedium 800), subtitle "Sign in to SGRMS", email and password fields, a full-width 52 dp primary "Sign in" button, and a "Forgot password?" text button. Keep the validation messages and the loading state.
- [ ] **Step 2: Restyle the dialog** with the new fields and buttons.
- [ ] **Step 3: Verify** the build, then on the phone after logout: empty-submit errors show and the dialog opens.
- [ ] **Step 4: Commit** `feat(ui): redesign login`.

### Task 4: Admin dashboard

**Files:** Modify `ui/admin/AdminDashboardScreen.kt`, `viewmodel/DashboardViewModels.kt`.

**Interfaces:**
- Consumes: `AdminSummary`, `UserFirebaseRepository.getAllUsers()`.
- Produces: `AdminDashboardUiState.recentAccounts: List<UserModel>` (the 3 newest by `createdAt`, Admins excluded).

- [ ] **Step 1: Add `recentAccounts`** to `AdminDashboardViewModel.load()`, loaded with `getAllUsers()` in parallel with the summary. A failure there leaves the list empty without failing the dashboard.
- [ ] **Step 2: Build the layout to match "A · Indigo Clean — Admin":**
  - A greeting header ("Good morning/afternoon/evening" from the device time; `adminName`; an avatar button → `onProfileClick`).
  - A primary hero card with total accounts, a split bar (Admin amber, Teacher `#A5B4FC`, Student white, weighted by count) and the role breakdown text.
  - A 2×2 `SgrmsStatCard` grid: Students, Teachers, Subjects, Classes, each clickable to its callback.
  - "Quick actions" with Add teacher and Add student.
  - "Recent accounts" as a `SgrmsGroupedCard` of `SgrmsListRow` with `SgrmsAvatar` and a role chip.
  - Links to User Management and Reports kept as rows.
- [ ] **Step 3: Verify** on the phone: the counts match Firestore (9 / 1 / 2 / 6 at the time of writing) and every card navigates.
- [ ] **Step 4: Commit** `feat(ui): redesign admin dashboard`.

### Task 5: Student dashboard

**Files:** Modify `ui/dashboard/StudentDashboardScreen.kt`.

**Interfaces:** Consumes the existing `StudentFirebaseViewModel`, `GradeFirebaseViewModel`, `SubjectFirebaseViewModel`, plus `NotificationFirebaseViewModel.loadNotificationsByStudentId(studentId)`. Adds `onNotificationsClick: () -> Unit = {}` to `StudentDashboardScreen`, wired in `AppNavigation` to `StudentNotifications`.

- [ ] **Step 1: Build the layout to match "A · Indigo Clean — Student":**
  - A header with "5 Bestari · <studentCode>" and "Hi, <first name>", and a bell button with an unread dot.
  - An overall card with a ring showing the average percentage, the overall grade letter in the middle, the performance label from `GradeCalculator.performanceStatus`, and an "N of M subjects graded" chip.
  - "My subjects" as a grouped card of rows with marks, a `markColor` progress bar and a grade chip.
  - The newest unread notification as an orange banner (hidden when there is none).
  - A "Full report" link → `onReportClick`.
- [ ] **Step 2: Verify** the build. Phone check happens in Task 11 (needs a student login).
- [ ] **Step 3: Commit** `feat(ui): redesign student dashboard`.

### Task 6: Teacher dashboard

**Files:** Modify `ui/dashboard/TeacherDashboardScreen.kt`.

- [ ] **Step 1: Use the Admin pattern:**
  - Greeting header with `teacherName` and an avatar → profile.
  - A hero card with the student count and "across N subjects".
  - `SgrmsStatCard` grid: Students, Subjects, Grades, Reports.
  - Quick actions: Add grade (→ `onGradeClick`) and Reports.
  - "Subject averages" as a grouped card of rows with `markColor` bars from `summary.subjectAverages`, and an empty state when the list is empty.
- [ ] **Step 2: Verify** the build. Commit `feat(ui): redesign teacher dashboard`.

### Task 7: List screens

**Files:** Modify `ui/student/StudentManagementScreen.kt` + `StudentManagementComponents.kt`, `ui/admin/TeacherManagementScreen.kt`, `ui/subject/SubjectManagementScreen.kt` + `…Components.kt`, `ui/admin/ClassManagementScreen.kt` + `…Components.kt`, `ui/grade/GradeManagementScreen.kt` + `…Components.kt`, `ui/admin/UserManagementScreen.kt`, `ui/report/ReportScreen.kt` + `ReportComponents.kt`, `ui/notification/StudentNotificationScreen.kt`.

**Interfaces:** Consumes the Task 2 patterns. Filtering logic, paging (`hasMore`, `loadMore*`), export and delete flows are unchanged; only their controls move.

- [ ] **Step 1: Apply the list pattern to each screen:**
  - `SgrmsPageHeader` with the title and "N loaded" (or a count) subtitle.
  - `SgrmsSearchBar`.
  - Each filter that used a dialog or dropdown becomes a `SgrmsFilterChips` row: class, status, role, department, category, subject. Sort moves into the header's trailing `SgrmsOverflowMenu`.
  - Items become `SgrmsListRow` inside `SgrmsGroupedCard`, with `SgrmsAvatar` (people) or `SgrmsIconTile` (subjects, classes), a status/grade chip, and a `SgrmsOverflowMenu` holding View / Edit / Delete (Delete marked destructive) in place of the three outlined buttons. A row tap equals View.
  - The main add action becomes an `ExtendedFloatingActionButton` (icon + "Add …"), shown under the same conditions as today (e.g. `canAddStudent`).
  - `LoadMoreButton` stays as the last item.
  - `SgrmsEmptyState` when the filtered list is empty.
- [ ] **Step 2: Verify** the build. On the phone as Admin: open Students, Teachers, Subjects, Classes and Users; a filter chip filters, the overflow menu opens and its delete dialog appears and can be cancelled.
- [ ] **Step 3: Commit** `feat(ui): redesign list screens`.

### Task 8: Detail screens

**Files:** Modify `ui/student/StudentDetailScreen.kt` + `…Components.kt`, `ui/admin/TeacherDetailScreen.kt`, `ui/subject/SubjectDetailScreen.kt` + `…Components.kt`, `ui/admin/ClassDetailScreen.kt` + `…Components.kt`, `ui/grade/StudentGradeDetailScreen.kt`, `ui/report/ReportDetailScreen.kt` + `…Components.kt`, `ui/report/StudentReportSummaryScreen.kt` + `…Components.kt`.

- [ ] **Step 1: Apply the detail pattern:**
  - A compact `SgrmsPageHeader` with back.
  - A hero card: a 72 dp `SgrmsAvatar` or icon tile, the name, and chips (class, status, code).
  - Grouped info cards built from `SgrmsInfoRow` with icons, under section titles.
  - Grade/subject lists as grouped rows with `markColor` bars and grade chips.
  - Actions at the bottom: a primary button (Edit / Add grade / Download PDF) and a secondary button.
  - Delete as a destructive text button where one exists today.
- [ ] **Step 2: Verify** the build. On the phone, open Student, Teacher, Subject and Class detail as Admin.
- [ ] **Step 3: Commit** `feat(ui): redesign detail screens`.

### Task 9: Form screens

**Files:** Modify `ui/student/AddStudentScreen.kt`, `EditStudentScreen.kt`, `ui/admin/AddTeacherScreen.kt`, `EditTeacherScreen.kt`, `ui/subject/AddSubjectScreen.kt`, `EditSubjectScreen.kt`, `ui/grade/AddGradeScreen.kt`, `EditGradeScreen.kt`, `QuickAddGradeScreen.kt` + `…Components.kt`, `AddStudentGradeScreen.kt`, `ui/report/UpdateReportScreen.kt`, `ui/profile/ChangePasswordScreen.kt`, `EditAdminProfileScreen.kt`, `EditTeacherProfileScreen.kt`, `EditStudentProfileScreen.kt` + `…Components.kt`.

- [ ] **Step 1: Apply the form pattern:**
  - A compact header with back.
  - Fields grouped into `SgrmsFormCard`s with section titles (e.g. "Account", "Personal", "Class").
  - Gender and Status become `SgrmsSegmentedField`; other dropdowns stay `SgrmsDropdownField`.
  - Marks inputs show the live grade chip.
  - The save button sits in a bottom bar, pinned above the keyboard (`imePadding`), full width, showing its loading state.
  - Validation messages and save/cancel behaviour are unchanged.
- [ ] **Step 2: Verify** the build. On the phone as Admin: open Add Teacher, Add Student and Edit Admin Profile; segmented controls switch, and the save button stays visible with the keyboard open. Do not submit.
- [ ] **Step 3: Commit** `feat(ui): redesign form screens`.

### Task 10: Profile screens

**Files:** Modify `ui/profile/AdminProfileScreen.kt` + `…Components.kt`, `TeacherProfileScreen.kt` + `…Components.kt`, `StudentProfileScreen.kt`.

- [ ] **Step 1: Apply the profile pattern:**
  - A hero with the 88 dp photo (`ProfileImageView`), the name, and a role chip.
  - Grouped info rows.
  - Admin keeps its system summary as `SgrmsStatCard`s.
  - A settings group with Edit profile and Change password rows (chevrons), then Logout as a destructive row in its own group.
- [ ] **Step 2: Verify** the build, and the Admin profile on the phone. Commit `feat(ui): redesign profile screens`.

### Task 11: Whole-app check on the phone and portfolio screenshots

**Files:** none (fixes only if a check fails, committed as `fix(ui): …`).

- [ ] **Step 1: Run the checks:** `grep -rn 'Color(0x' app/src/main/java/com/example/sgrms/ui | grep -v theme/` returns no screen-level hex colours (or only justified decorative ones), and `./gradlew :app:assembleDebug :app:testDebugUnitTest` and `cd firestore-tests && npm test` pass.
- [ ] **Step 2: Test on the phone (Admin):** open every Admin-reachable screen in portrait. Then repeat Students and the Dashboard in landscape and with the system font at its largest setting (Review Focus 2 and 5). There must be no crash (`adb logcat -b crash` empty) and no clipped or overlapping text.
- [ ] **Step 3: Teacher and Student roles:** ask the owner to log in as a teacher and then as a student (or to create test accounts through Add Teacher / Add Student with their own passwords). Open each role's screens the same way.
- [ ] **Step 4: Portfolio screenshots:** `adb exec-out screencap -p` of Login, Admin dashboard, Teacher dashboard and Student dashboard, saved to `docs/screenshots/` (`login.png`, `admin-dashboard.png`, `teacher-dashboard.png`, `student-dashboard.png`).
- [ ] **Step 5: Commit** `docs: portfolio screenshots of the redesign`.
