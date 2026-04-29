# CLAUDE.md — Reusable Android + Claude Workflow

This file captures the conventions, CI pipeline, and coding patterns
established in this project so they can be reused in other Android +
Compose codebases driven by Claude. Copy this file (and the templates
referenced below) into a new repo, adjust the marked placeholders,
and the same iterate-by-image / push-and-verify loop will work.

---

## 1. TL;DR

- **One dev branch per session.** Claude commits + pushes to a single
  pre-agreed feature branch and does not touch others.
- **GitHub Actions builds a debug APK on every push** to the branches
  in the workflow's `branches` list. Successful builds publish the
  APK to a `latest-debug` GitHub Release.
- **All build output is persisted to a `build-logs` orphan branch.**
  Claude reads `runs/<sha>.log` (or `runs/latest.log`) to diagnose
  failures without needing the GitHub Actions UI.
- **The app self-updates** by fetching `version.json` from the
  `latest-debug` release on launch.
- **UI edits live in bottom-sheet drawers, not full screens.** No
  separate edit page; the same `TaskDetailSheet`-style drawer covers
  both create (id == 0) and update flows. Debounced auto-save means
  no Save button.

---

## 2. Required repo layout

```
.
├── CLAUDE.md                              ← this file
├── .github/workflows/build-apk.yml        ← see §4 template
├── gradle/wrapper/{gradle-wrapper.jar,    ← committed wrapper
│                   gradle-wrapper.properties}
├── gradlew                                ← committed, executable
├── gradlew.bat
├── gradle.properties                      ← see §5 template
├── app/build.gradle.kts                   ← compose + ksp + room
├── app/debug.keystore                     ← stable, committed (debug only)
└── ...
```

The `debug.keystore` is committed deliberately so APKs published to
`latest-debug` always have the same signature — required for the
in-app updater to install over the running app.

---

## 3. Branch & CI strategy

### Development branch

The user assigns Claude a single branch like
`claude/<feature-tag>-<random>`. Claude:

- Develops, commits, and pushes only to that branch.
- Uses `git push -u origin <branch>` — retry on network errors with
  exponential backoff (2s, 4s, 8s, 16s) up to 4 times. Never
  `--force` to `main`.
- Does **not** open PRs unless the user explicitly asks.

### CI trigger branches

The workflow `branches:` list must include the dev branch and any
branches that should publish APKs (typically `main` plus the active
dev branch). Add new branches as needed; **never** trigger on
`build-logs` (paths-ignore covers that).

### Verifying a build

After pushing, Claude can verify the build via the `build-logs`
branch (no need to poll Actions):

```
GET runs/latest.sha       # the commit just built
GET runs/latest.status    # 'success' or 'failure'
GET runs/<sha>.log        # full Gradle log, ~5KB on success,
                          # ~30KB on a compile failure
```

`runs/latest.*` is rewritten on every build so old commits' issues
(`Build failure for <sha>`) accumulate in Issues — close them when
they reference commits that were already fixed.

---

## 4. Workflow template (`.github/workflows/build-apk.yml`)

Key choices:

- Wrapper-based Gradle (no `gradle-version:` on `setup-gradle@v3`)
  — eliminates the 150 MB Gradle distribution download on cache-warm
  runs. Cuts ~30–60 s.
- Explicit `actions/cache@v4` for `~/.gradle/wrapper/dists`,
  `~/.android/build-cache`, `~/.gradle/caches/transforms-4`, and
  `app/build/intermediates`. The project-output cache key includes
  branch + sha so a new commit warm-starts from its predecessor.
- `assembleDebug -x lint -x test` — lint alone is often 30–50% of
  wall time on Compose projects.
- `--build-cache --parallel --daemon`. No `--no-daemon`; with the
  wrapper cache, daemon startup is amortized.
- `--info` and `--stacktrace` are intentionally **off** — they bloat
  build.log without helping diagnosis. Re-enable temporarily if a
  build is failing for non-obvious reasons.
- After the build, the log is committed to a `build-logs` orphan
  branch under `runs/<sha>.log` plus `runs/latest.*` pointers. On
  failure the script also opens an Issue with the last 50 KB of the
  log inline.
- On success, an `actions/upload-release-asset` style step republishes
  `TestApp.apk` and `version.json` to the `latest-debug` Release tag
  (prerelease, never marked latest).

The exact YAML lives in this repo at `.github/workflows/build-apk.yml`.
Copy it verbatim, then change:

| Placeholder            | Replace with                                  |
|------------------------|-----------------------------------------------|
| `claude/...` branches  | Your dev branch list                          |
| `TestApp.apk` filename | Your app's APK name                           |
| Release tag            | Keep `latest-debug` to match the in-app updater
| Repo references in     | Inline `version.json` `apkUrl` —              |
| version.json           | `https://github.com/<owner>/<repo>/releases/download/latest-debug/<apk>` |

---

## 5. `gradle.properties` template

```properties
# Heap & GC for the Gradle and Kotlin daemons. The Compose compiler
# is heavy; 4 GB / 2 GB are comfortable on GitHub-hosted runners.
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8 -XX:+UseParallelGC
kotlin.daemon.jvmargs=-Xmx2048m -XX:+UseParallelGC

# Build graph parallelism.
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
org.gradle.workers.max=4

# Skip configuration cache: a parameter that changes every commit
# (e.g. -PappVersionCode=<code>) invalidates it on every build, so
# the write-cost is pure overhead.
# (Do NOT add `org.gradle.configuration-cache=true`.)

# AGP / AndroidX
android.useAndroidX=true
android.nonTransitiveRClass=true
android.enableJetifier=false        # AndroidX-only — avoid the per-dep transform pass

# Kotlin incremental
kotlin.code.style=official
kotlin.incremental=true
kotlin.incremental.useClasspathSnapshot=true
kotlin.parallel.tasks.in.project=true
ksp.incremental=true
```

---

## 6. Iterative development loop

A typical Claude session looks like:

1. **User sends an image** of a target UI screen, or describes a feature.
2. Claude inspects existing code via `Read` / `grep` / `find` — never
   guesses the file structure.
3. Claude edits files locally (no separate scratch files), commits,
   and pushes. Commit messages follow this style: short imperative
   subject (≤ 70 chars), bullet body that describes the WHY (what
   bug it fixes, what user-visible behavior changes), no Markdown
   headers in the body.
4. Claude immediately verifies via `runs/latest.sha` + `.status`. If
   `failure`, reads the log, finds the compile error, and pushes a
   focused fix.
5. Claude does **not** wait for builds via polling — it relies on the
   `build-logs` branch being current.

### Common compile-failure patterns and fixes

| Symptom in log                                              | Fix |
|-------------------------------------------------------------|-----|
| `Unresolved reference 'HexagonOutlined'`                    | `Icons.Filled.HexagonOutlined` doesn't exist; use `Icons.Outlined.Hexagon` (separate import). |
| `Unresolved reference 'isSp'`                               | `TextUnit.isSp` is not a real import; use `type == TextUnitType.Sp`. |
| `Icons.Outlined.Settings` "receiver type mismatch" with alias | Aliasing extension properties is fragile; either don't import both filled+outlined of the same name, or use a different filled icon to avoid the clash. |
| `'Icons.Filled.ArrowBack' is deprecated`                    | Migrate to `Icons.AutoMirrored.Filled.ArrowBack` (also `FormatListBulleted`, `Help`, `EventNote`). |
| `'kotlinx.coroutines.flow.debounce' is in a preview state`  | `@OptIn(FlowPreview::class)` on the composable using it. |

---

## 7. Coding conventions

### UI: drawer-first

- **No nested edit screens.** A single `TaskDetailSheet`-style
  `ModalBottomSheet` covers create + edit. The id of the entity
  passed in distinguishes modes: id = 0 ⇒ insert on first save;
  id ≠ 0 ⇒ update.
- `skipPartiallyExpanded = false` so the sheet sits at half-height
  by default. `skipPartiallyExpanded = true` forces full-screen and
  is almost always wrong for forms.
- The sheet owns local state and emits a single
  `onSave(entity, children)` callback. The VM's `save()` no-ops on
  blank title and otherwise calls `repo.upsert(...)`.
- Auto-save via `snapshotFlow { ... }.debounce(400).collect { ... }`
  inside a `LaunchedEffect(initial.id)`.

### Material 3 icons

- Always prefer `Icons.AutoMirrored.Filled.*` for direction-aware
  glyphs (`ArrowBack`, `FormatListBulleted`, `Help`, `EventNote`,
  `Send`).
- Don't import both `Icons.Filled.X` and `Icons.Outlined.X` in the
  same file unless you alias one — the simple-name conflicts. If
  the alias path is awkward, switch one usage to a different glyph
  (e.g. `Icons.Filled.Tune` instead of an outlined `Settings`).

### State & DI

- StateFlow + `combine` over LiveData.
- Each feature has `<Feature>ViewModel` + `<Feature>UiState` (data
  class).
- Koin module `appModule`; ViewModels declared with
  `viewModel { ... }`.
- `Repository` per domain table; `upsert(entity, ...)` is the canonical
  write API and handles both insert and update.

### Persistence

- DataStore `preferencesDataStore` for settings.
- Settings exposed as `Flow<AppSettings>` from a `SettingsRepository`,
  consumed via `vm.settings.stateIn(...)` in feature VMs.

### DnD between Compose surfaces

- Long-press to start drag (`detectDragGesturesAfterLongPress`).
- Track pointer in **window coordinates**: rowOriginInWindow + local
  drag offset.
- For overlay rendering with `Modifier.offset { ... }` (which is
  parent-local), subtract the parent Box's `boundsInWindow().topLeft`
  before computing the IntOffset.
- Hit-testing uses each target's `boundsInWindow()` — same coordinate
  space as the pointer.

---

## 8. In-app self-update

The pattern that ships in this repo:

1. CI publishes `TestApp.apk` + `version.json` to the `latest-debug`
   GitHub Release.
2. App reads `BuildConfig.VERSION_MANIFEST_URL` (set in
   `app/build.gradle.kts`) on launch via `UpdateChecker.fetchManifest()`.
3. If the remote `versionCode` is higher, `UpdateDialog` prompts the
   user; on confirm, the APK is downloaded to `cacheDir/updates/` and
   installed via `PackageInstaller`.
4. The committed `debug.keystore` keeps the signature stable so the
   install replaces the existing app rather than failing.

When porting, the only thing that needs changing is the
`VERSION_MANIFEST_URL` `buildConfigField` in
`app/build.gradle.kts` (point it at the new repo's release).

---

## 9. Risk-and-confirmation rules

For Claude operating on this repo:

- **Local edits, builds, file reads** → freely.
- **`git push`** to the assigned dev branch → freely; never to other
  branches.
- **`gh pr create` / merging / closing PRs** → only on explicit user
  request.
- **Deleting files / branches**, force-push, `git reset --hard`,
  `--no-verify` → only on explicit request, and only after a
  one-line "I'm about to do X — confirm?" check.
- **Closing GitHub Issues** auto-created by the workflow (titles like
  `Build failure for <sha>`) → fine without confirmation when the
  referenced commit has already been superseded by a green build.

---

## 10. Where to start in a new project

1. Copy `CLAUDE.md`, `.github/workflows/build-apk.yml`,
   `gradle.properties`, and `gradle/wrapper/*` from this repo.
2. `chmod +x gradlew && git add gradlew gradle/wrapper/gradle-wrapper.jar`.
3. Adjust the workflow's `branches:` list and APK filename.
4. In `app/build.gradle.kts`, set `VERSION_MANIFEST_URL` to the new
   repo's `latest-debug` release URL.
5. First push to the dev branch: the build will be a cold cache hit
   (~3 m). The next push should be in the 1–2 m range.
6. If a build fails, read `build-logs` branch's `runs/latest.log`
   and apply one of the known patterns from §6.
