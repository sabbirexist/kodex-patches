<div align="center">

<img src="assets/banner.svg" alt="Kodex Patches" width="840">

# Kodex Patches

**A curated set of [Morphe](https://github.com/MorpheApp) patch bundles — built clean, kept alive across app updates.**

[![Release](https://img.shields.io/github/v/release/sabbirexist/kodex-patches?include_prereleases&sort=semver&style=for-the-badge&label=release&color=5eead4)](https://github.com/sabbirexist/kodex-patches/releases)
[![Build](https://img.shields.io/github/actions/workflow/status/sabbirexist/kodex-patches/release.yml?style=for-the-badge&label=build)](https://github.com/sabbirexist/kodex-patches/actions/workflows/release.yml)
[![License](https://img.shields.io/github/license/sabbirexist/kodex-patches?style=for-the-badge&color=60a5fa)](LICENSE)
[![Last commit](https://img.shields.io/github/last-commit/sabbirexist/kodex-patches/dev?style=for-the-badge&color=a78bfa)](https://github.com/sabbirexist/kodex-patches/commits/dev)
[![Stars](https://img.shields.io/github/stars/sabbirexist/kodex-patches?style=for-the-badge&color=f59e0b)](https://github.com/sabbirexist/kodex-patches/stargazers)

<a href="https://morphe.software/add-source?github=sabbirexist/kodex-patches">
  <img src="https://img.shields.io/badge/Add%20to%20Morphe%20%E2%86%92-5eead4?style=for-the-badge&logo=android&logoColor=white" alt="Add to Morphe">
</a>

</div>

---

## 📖 Table of contents

- [Overview](#-overview)
- [Why Kodex Patches](#-why-kodex-patches)
- [Installation](#-installation)
- [Patches](#-patches)
- [Development](#-development)
- [Anatomy of a patch](#-anatomy-of-a-patch)
- [Contributing](#-contributing)
- [Credits](#-credits)
- [License](#-license)

---

## 🚀 Overview

**Kodex Patches** is a personal patch project for [Morphe](https://github.com/MorpheApp) — the
Android patching framework that modifies apps by applying patches to their bytecode and resources.

Every patch here follows the conventions of the Morphe open-source ecosystem: **minimal smali
changes**, **fingerprints that survive app updates**, and **complex logic kept in extensions** so
the patched app stays stable. The project targets a small, deliberately chosen set of apps rather
than chasing breadth.

Built with [Morphe Patcher](https://github.com/MorpheApp/morphe-patcher) · built on top of
[smali](https://github.com/google/smali) and [Apktool](https://github.com/iBotPeaches/Apktool).

## ✨ Why Kodex Patches

| | |
|---|---|
| 🎯 **Update-resilient** | Fingerprints match on stable traits — return types, parameters, access flags, instruction filters — never on obfuscated names that rotate every release. |
| 🧩 **Minimal footprint** | Each patch injects the fewest instructions possible and delegates the heavy lifting to a precompiled extension. |
| 🔒 **Isolated logic** | Multi-line Java lives in the extension (`app.kodex.extension`), reached from smali by a single hook — easy to read, easy to test. |
| ⚙️ **Configurable** | Patches expose typed options where a choice makes sense, editable in Morphe Manager or the CLI. |
| 🚢 **Released properly** | Semantic-release on `dev`/`main`: `feat:`/`fix:` commits auto-cut pre-releases, verified builds ship stable. |

---

## 📲 Installation

### Morphe Manager (Android)

1. Open **Morphe Manager** and go to **Settings → Sources → Add source**.
2. Add this URL:

   ```
   https://morphe.software/add-source?github=sabbirexist/kodex-patches
   ```

3. Enable **pre-release** to pull `dev` builds, or leave it off for stable releases.

Or tap the **Add to Morphe** button above.

### Morphe Desktop (CLI)

Point `-p` straight at this repo — Morphe downloads the bundle from the latest release:

```bash
java -jar morphe-desktop-*-all.jar patch \
  -p https://github.com/sabbirexist/kodex-patches \
  --prerelease \          # omit for stable
  target.apk
```

### Requirements

- **Morphe Manager** (Android) or **Morphe Desktop** (CLI/desktop).
- The **exact app version** listed for the patch you want — matching versions means more patches apply.

---

## 🩹 Patches

<!-- PATCHES_START EXPANDED -->

<!-- Do not modify this section by hand. The patch list is generated when release.yml creates a new release.

     If you wish for the patches list to be collapsed, then remove the word 'EXPANDED' from the comment tag above.

     If you wish to manually keep this list updated then remove the PATCHES_START and PATCHES_END
     comment blocks entirely. -->

#### A list of patches will automatically be shown here after the first release is created.

&nbsp;

<!-- PATCHES_END -->

---

## 🧑‍💻 Development

All work happens on the **`dev`** branch.

```bash
# get the toolchain: JDK 21 + Git + (gh) authenticated to GitHub Packages
git clone https://github.com/sabbirexist/kodex-patches
cd kodex-patches

# build the bundle
./gradlew buildAndroid
# -> patches/build/libs/patches-*.mpp
```

Apply the bundle locally with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop):

```bash
java -jar morphe-desktop-*-all.jar patch \
  -p patches/build/libs/patches-*.mpp \
  -i target.apk        # -i installs over ADB
```

### Project layout

```
patches/src/main/kotlin/app/kodex/patches/
├── shared/            # Compatibility declarations (app name, package, targets)
└── <app>/             # One package per target app
    ├── Fingerprints.kt
    └── <Feature>Patch.kt
extensions/extension/  # Precompiled DEX merged into the patched app at runtime
```

### Branch model

| Branch | Purpose | On push |
|--------|---------|---------|
| `dev`  | All development | `feat:`/`fix:` cut a pre-release |
| `main` | Stable only | merge `dev` → `main` to ship |

### Commit convention

Semantic messages only — they drive the release:

- `feat: …` — new behavior → **pre-release**
- `fix: …` — correction → **pre-release**
- `chore: …` — internal change → **no release**

Generated files (`patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`) are owned by
`release.yml` — **never edit them by hand.**

---

## 🔬 Anatomy of a patch

A fingerprint describes a target method by its stable traits; a patch hooks it.

```kotlin
object AdsGateFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;", "I"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        string("showBannerAds"),
        methodCall(definingClass = "Ljava/lang/String;", name = "equals"),
        opcode(Opcode.MOVE_RESULT, InstructionLocation.MatchAfterImmediately()),
    )
)

val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Disables banner ads in the app.",
    default = true,
) {
    compatibleWith(COMPAT_APP)
    extendWith("extensions/extension.mpe")

    execute {
        AdsGateFingerprint.method.addInstructions(0, """
            invoke-static {}, Lapp/kodex/extension/AdsPatch;->shouldShowAds()Z
            move-result v0
            return v0
        """)
    }
}
```

Read the [patcher documentation](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/README.md)
for the full API.

---

## 🤝 Contributing

Contributions, bug reports, and feature requests are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md)
and the [issue templates](../../issues/new/choose). Please include the **exact app version** and the
**patches bundle version** in any report.

## 🙏 Credits

- [Morphe](https://github.com/MorpheApp) and the [Morphe Patcher](https://github.com/MorpheApp/morphe-patcher) team — the framework this is built on.
- [smali](https://github.com/google/smali) and [Apktool](https://github.com/iBotPeaches/Apktool) — the underlying bytecode and resource tooling.
- The ReVanced/Morphe patch community — conventions and reference implementations.

## ⚖️ License

Kodex Patches are licensed under the [**GNU General Public License v3.0**](LICENSE).

> This project is **not affiliated with, endorsed by, or sponsored by** the Morphe project. References
> to "Morphe" are for descriptive compatibility only. See [NOTICE](NOTICE) for details.
