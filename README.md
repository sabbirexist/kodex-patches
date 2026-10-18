<div align="center">

<img src="https://socialify.git.ci/sabbirexist/kodex-patches/image?forks=1&language=1&name=1&owner=1&pattern=Circuit%20Board&stargazers=1&theme=Auto" alt="Kodex Patches" width="640" height="320" />

<h1>
    Morphe Patches by Kodex
</h1>

</div>

## 🕹️ Usage

### Morphe Manager

Tap the link to add Kodex Patches as a patch source in Morphe Manager:

[**➕ Add Kodex Patches to Morphe**](https://morphe.software/add-source?github=sabbirexist/kodex-patches)

Then pick an app, download the matching APK version listed in Patch Details below, and patch.

Enable **pre-release** on the source to pull `dev` builds (`v…-dev.N`).

### Morphe CLI

```sh
java -jar morphe-desktop-*-all.jar patch -p https://github.com/sabbirexist/kodex-patches -o patched.apk app.apk
```

## ⚙️ Patch Details

<!-- PATCHES_START -->
> **[v1.4.0](https://github.com/sabbirexist/kodex-patches/releases/tag/v1.4.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;15 patches total
<details>
<summary>📦 Eyecon&nbsp;&nbsp;•&nbsp;&nbsp;5 patches</summary>
<br>

**🎯 Supported versions:**

| 4.2026.09.16.1233 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [AMOLED black theme](#amoled-black-theme) | Repaints the dark theme's background, cards, bars and dialogs to true black for AMOLED screens. |  |
| [Block contact upload](#block-contact-upload) | Stops Eyecon from uploading your contacts to its server for caller ID. |  |
| [Block tracking & analytics](#block-tracking-analytics) | Disables Amplitude (including Session Replay screen recording), Singular attribution and Crashlytics crash reporting. |  |
| [Remove premium tab](#remove-premium-tab) | Hides the premium tab from the home bottom bar. |  |
| [Unlock premium](#unlock-premium) | Unlocks every premium feature client-side: removes ads, unlocks themes, premium caller ID and after-call screen, call recording, SMS extras, reverse lookup and ID Plus. |  |

</details>

<details>
<summary>📦 VLC&nbsp;&nbsp;•&nbsp;&nbsp;9 patches</summary>
<br>

**🎯 Supported versions:**

| 3.7.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [AMOLED black theme](#amoled-black-theme) | Repaints every dark surface — background, cards, dialogs, popups, header and the navigation bar — to true black. |  |
| [Disable audio time-stretch](#disable-audio-time-stretch) | Forces time-stretching off for artifact-free playback at 1x speed. |  |
| [Force AAudio output](#force-aaudio-output) | Locks the audio output to AAudio (low latency), overriding the device preference. |  |
| [Force ReplayGain](#force-replaygain) | Normalizes loudness across tracks using track mode with peak protection. |  |
| [Force audio passthrough](#force-audio-passthrough) | Enables bitstream audio passthrough (digital output) to an external DAC or receiver. |  |
| [Raise network cache](#raise-network-cache) | Buffers 3 seconds of stream ahead to reduce stalls on flaky networks. |  |
| [Skip "What's New" dialog](#skip-what-s-new-dialog) | Suppresses the What's New changelog dialog shown after app updates. |  |
| [Unlock experimental features](#unlock-experimental-features) | Enables every experimental feature flag regardless of its stored value. |  |
| [Verbose libVLC logging](#verbose-libvlc-logging) | Sets libVLC to maximum verbosity (-vvv) for troubleshooting playback. |  |

</details>

<details>
<summary>📦 InShare&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 2.1.3 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Remove ads](#remove-ads) | Unlocks the paid Remove Ads feature: disables every ad and hides the Remove Ads purchase icon. |  |

</details>

<!-- PATCHES_END -->

## 🛠️ Building

```sh
./gradlew buildAndroid   # -> patches/build/libs/patches-*.mpp
```

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for the full setup.

## ✨ Stargazers over time

<p align="center">
    <img src="https://api.star-history.com/svg?repos=sabbirexist/kodex-patches&type=Date" alt="Kodex Patches star history" width="620" />
</p>

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)

These patches are FOSS, redistributable and modifiable under the [GNU General Public License v3](https://www.gnu.org/licenses/gpl.html) or later.

Not affiliated with the Morphe project. See [NOTICE](NOTICE).
