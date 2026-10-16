# 👋🧩 Kodex Patches

Patch bundles for apps I use daily, built for [Morphe](https://github.com/MorpheApp).

## ❓ About

Kodex Patches is a personal patch project maintained by **Kodex**. It targets a small,
deliberately chosen set of apps and follows the same conventions as the Morphe
open-source patches: minimal patches, update-resilient fingerprints, and complex logic
kept in extensions.

### How to use these patches

Add this source to Morphe Manager:

```
https://morphe.software/add-source?github=sabbirexist/kodex-patches
```

Enable **pre-release** in Manager to pull `dev` builds.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->

<!-- Do not modify this section by hand. The patch list is generated when release.yml creates a new release.

     If you wish for the patches list to be collapsed, then remove the word 'EXPANDED' from the comment tag above.

     If you wish to manually keep this list updated then remove the PATCHES_START and PATCHES_END
     comment blocks entirely. -->

#### A list of patches will automatically be shown here after the first release is created.

&nbsp;

## 🧑‍💻 Development

- **All work goes on the `dev` branch.**
- Build locally with `./gradlew buildAndroid` — the bundle lands in `patches/build/libs/patches-*.mpp`.
- Apply it with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) like any other bundle.
- Use semantic commits only: `feat:`, `fix:`, `chore:`.
- `feat:`/`fix:` cut a pre-release automatically; `chore:` does not.
- Ship stable by merging `dev` into `main` (merge, never squash).

### Project layout

```
patches/src/main/kotlin/app/kodex/patches/
├── shared/            # Compatibility declarations (app name, package, targets)
└── <app>/             # One package per target app
    ├── Fingerprints.kt
    └── <Feature>Patch.kt
extensions/extension/  # Precompiled DEX merged into the patched app at runtime
```

Fingerprints describe a target method by stable traits (return type, parameters,
access flags, instruction filters) — never by obfuscated names. Complex behavior lives
in the extension (`app.kodex.extension.*`) and is invoked from smali by a single hook.

## 📜 License

Kodex Patches are licensed under the [GNU General Public License v3.0](LICENSE).
This project is not affiliated with or endorsed by the Morphe project. See [NOTICE](NOTICE).
