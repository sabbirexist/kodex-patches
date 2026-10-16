# Contributing to Kodex Patches

Thanks for taking the time to help. This project follows the Morphe patch conventions —
please read this before opening an issue or PR.

## Before you open an issue

- Search [existing issues](https://github.com/sabbirexist/kodex-patches/issues) first.
- A bug report must include the **exact app version**, the **patches bundle version**,
  and the captured logcat if the app crashed.
- Feature requests must name the **target app and version** they apply to.

## Development workflow

1. Fork the repo and work on the `dev` branch.
2. Build locally:
   ```bash
   ./gradlew buildAndroid
   ```
   The bundle is written to `patches/build/libs/patches-*.mpp`.
3. Apply it with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) to test.
4. Commit with a **semantic message**:
   - `feat: <new behavior>` — cuts a pre-release
   - `fix: <correction>` — cuts a pre-release
   - `chore: <non-user-facing change>` — no release
5. Open a PR against `dev`.

Do **not** hand-edit generated files: `patches-list.json`, `patches-bundle.json`,
`CHANGELOG.md`. `release.yml` owns them.

## Patch conventions

- One package per target app under `patches/src/main/kotlin/app/kodex/patches/<app>/`.
- Split `Fingerprints.kt` from the patch file when a patch uses more than one fingerprint.
- Name patches for what they do; write descriptions in third-person present, ending in a period.
- Keep patches minimal. Put multi-line logic in an extension
  (`extensions/extension/.../app/kodex/extension/`) and call it from smali in one hook.
- Fingerprint on stable traits — return type, parameters, access flags, instruction filters —
  never on obfuscated names.
- Document the *why* of any non-obvious modification.

## License

By contributing you agree your work is licensed under the
[GNU General Public License v3.0](LICENSE).
