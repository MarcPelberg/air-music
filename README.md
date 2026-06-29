# Android2Apple4Music

[![Latest release](https://img.shields.io/github/v/release/MarcPelberg/android2apple4music?label=download&logo=github)](https://github.com/MarcPelberg/android2apple4music/releases/latest)
[![Public repo](https://img.shields.io/badge/repo-public-brightgreen?logo=github)](https://github.com/MarcPelberg/android2apple4music)
[![License: GPLv3](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE)

**Free Android music playback for the Apple speaker world.**

Android2Apple4Music lets a Pixel or other Android phone play allowed app audio on a HomePod, Apple speaker, Apple TV, or AirPlay-compatible receiver on the same Wi-Fi network. It is built for one simple use case: open the app, connect to the HomePod, accept Android's capture prompt, then play Spotify or another music app from the phone.

## Download

Latest public APK: [android2apple4music.apk](https://github.com/MarcPelberg/android2apple4music/releases/latest/download/android2apple4music.apk)

The public APK shows the sponsor banner at the top of the app.

The repository is public at [github.com/MarcPelberg/android2apple4music](https://github.com/MarcPelberg/android2apple4music).

## Made Free By

Android2Apple4Music is sponsored and made free by:

- <a href="https://pelpush.com" target="_blank" rel="noopener noreferrer">PelPush.com Accounting</a>
- <a href="https://markcompass.com" target="_blank" rel="noopener noreferrer">MarkCompass.com</a> - fully functional accounting tools
- <a href="https://realarb.com" target="_blank" rel="noopener noreferrer">RealArb.com</a>
- <a href="https://marcpelberg.com" target="_blank" rel="noopener noreferrer">MarcPelberg.com</a>

## What It Does

- Plays allowed Android audio from a Pixel or Android phone to HomePod and AirPlay-compatible Apple devices.
- Discovers HomePod on the local network, with manual IP connection as a fallback.
- Keeps the Pixel speaker muted while the HomePod plays, so the phone and speaker do not play over each other.
- Includes Phone Volume Sync and a HomePod volume slider.
- Runs as a visible foreground service while connected, with Stop available in the app or notification.
- Ships as a reduced AirPlay-only build with the larger mirror-app attack surface removed.

## Spotify / Music Flow

1. Install the latest `android2apple4music.apk`.
2. Open **Android2Apple4Music**.
3. Tap **Scan for HomePod** or enter the HomePod IP manually.
4. Tap **Connect**.
5. Accept Android's **Share screen** prompt. Android uses this wording because playback-audio capture is backed by `MediaProjection`.
6. Open Spotify or another app that allows Android playback capture and press play.
7. Use the HomePod Volume slider or leave Phone Volume Sync on.
8. Tap **Stop** in Android2Apple4Music or the notification when finished.

Android2Apple4Music is not Spotify Connect, Bluetooth, or a system-wide Android audio route. It captures allowed playback audio locally and forwards it to an AirPlay-compatible receiver.

## Privacy And Safety

Android2Apple4Music does not bypass Apple FairPlay, app DRM, paid subscriptions, or app capture restrictions. Audio is available only from Android apps that allow playback capture.

Privacy policy: [marcpelberg.github.io/android2apple4music/privacy.html](https://marcpelberg.github.io/android2apple4music/privacy.html)

The public APK is intentionally slimmed down for this music use case: no Shizuku provider, no overlay permission, no secure-settings permission, no USB/touchscreen bridge, and no Sunshine foreground service. The manifest check can be run with:

```bash
python scripts/check_android2apple4music_apk.py --aapt2 <path-to-aapt2> --apk app/build/outputs/apk/release/app-release.apk
```

## Build

For personal installs, prefer the signed release APK over a debug APK. On Marc's Windows workspace, this command creates an ignored local PKCS12 signing key under `%USERPROFILE%\.android`, builds `app/build/outputs/apk/release/app-release.apk`, and runs the manifest security check:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\build_android2apple4music_release.ps1
```

The default build skips the Sunshine/Moonlight native server so the AirPlay-only APK can build without the OpenSSL native toolchain. To build the original native server too, pass `-PbuildSunshineNative=true`.

## Technical Notes

Android2Apple4Music uses Android `MediaProjection` plus `AudioPlaybackCaptureConfiguration` to capture allowed playback audio, then forwards 44.1 kHz stereo audio through the AirPlay path. The HomePod audio setup path has been tested against a HomePod 2nd gen on a local network.

This fork is based on the open-source **Screen Mirroring Manager for Android** project and keeps upstream technical attribution below.

---

## Upstream Project: Screen Mirroring Manager for Android

[![Stars](https://img.shields.io/github/stars/jqssun/android-display-mirror?label=stars&logo=GitHub)](https://github.com/jqssun/android-display-mirror/stargazers)
[![GitHub](https://img.shields.io/github/downloads/jqssun/android-display-mirror/total?label=GitHub&logo=GitHub)](https://github.com/jqssun/android-display-mirror/releases)
[![license](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://github.com/jqssun/android-display-mirror/blob/main/LICENSE)
[![build](https://img.shields.io/github/actions/workflow/status/jqssun/android-display-mirror/apk.yml?label=build)](https://github.com/jqssun/android-display-mirror/actions/workflows/apk.yml)
[![release](https://img.shields.io/github/v/release/jqssun/android-display-mirror)](https://github.com/jqssun/android-display-mirror/releases)

**Mirror** is an all-in-one application that forwards your screen content to external displays and expands Android's built-in screen mirroring capabilities. It creates virtual displays and supports screen sharing to:
- AirPlay receivers (Apple tvOS and macOS built-in receivers, or third-party receivers using [AirPlay Server for Android](https://github.com/jqssun/android-airplay-server) or [UxPlay](https://github.com/FDH2/UxPlay))
- [DisplayLink](https://www.synaptics.com/products/displaylink-graphics) adapters connected to the device via USB host mode
- [Moonlight](https://github.com/moonlight-stream) ([Nvidia GameStream](https://www.nvidia.com/en-us/support/gamestream/)) clients with remote control support via the built-in [Sunshine](https://github.com/lizardbyte/sunshine) server

It can be used in conjunction with [**Extend**](https://github.com/jqssun/android-display-extend) to turn any connected display or sink device into a secondary display where you can cast and control any application. You can also use this with [**AirPlay Server for Android**](https://github.com/jqssun/android-airplay-server) or [**Moonlight for Android**](https://github.com/moonlight-stream/moonlight-android) to create a dummy display that stays in Picture-in-Picture.

## Usage

Mirror supports creating virtual displays and streaming to them without Shizuku. Remote input requires Shizuku: enable it in any supported mode (wireless debugging, USB debugging, or root) and grant access to this application. See [Shizuku documentation](https://shizuku.rikka.app/guide/setup/) for details. 

### Using with Extend
Mirror gets pixels onto a display. What application runs on it, where it renders, and how input is routed are handled by [Extend](https://github.com/jqssun/android-display-extend). The two applications connect through Android's display system via `DisplayManager` API.

## Compatibility

- Any device on Android 8.0+ (no privileged access required)
- (Optional) Receiver on the same subnet for AirPlay and Moonlight sinks
- (Optional) USB 2 or higher for DisplayLink sinks
- (Optional) [Extend](https://github.com/jqssun/android-display-extend) for managing the secondary display
- (Optional) [Shizuku](https://github.com/RikkaApps/Shizuku) for hidden API access

## Features

- Outbound AirPlay 2 (modern) and AirPlay 1 (legacy) screen mirroring to Apple devices or third-party AirPlay receivers
- Outbound Moonlight streaming and remote control, with Sunshine server built in
- DisplayLink USB output via USB host mode (including USB 2)
- Each display sink is registered as a virtual display via `DisplayManager` API allowing other applications to launch activities on it
- Touchscreen relay for remote pointer and keyboard input directed at the mirrored display

| Feature | Shizuku | Minimum API |
|---|:---:|:---:|
| AirPlay mirroring | N | 26 (`MediaProjection.createVirtualDisplay`) <br> 34 (`MediaProjectionConfig.createConfigForDefaultDisplay`) |
| DisplayLink USB output | N | 26 (`android.hardware.usb.host`) |
| Moonlight video streaming | N | 26 (`MediaCodec` H.264) <br> 28 (BT.709/BT.2020 color metadata) <br> 30 (`KEY_LOW_LATENCY`) |
| Moonlight H.265/HEVC encoding | N | 29 (`MediaCodecInfo.isHardwareAccelerated`) |
| Audio capture (playback submix) | N | 29 (`AudioPlaybackCaptureConfiguration`) |
| Audio capture (remote submix) | O | 31 (`REMOTE_SUBMIX`) |
| Cursor overlay | O | 26 (`TYPE_APPLICATION_OVERLAY`) |
| Remote device input | R | 26 (`IInputManager.injectInputEvent`) |
| Touchscreen relay with live preview | R | 31 (`setFocusedRootTask`) <br> 34 (`focusTopTask`) |
| Auto-match aspect ratio | R | 26 (`IWindowManager.setForcedDisplaySize`) |
| Trusted virtual display | R | 33 (`VIRTUAL_DISPLAY_FLAG_TRUSTED`, `OWN_DISPLAY_GROUP`, `ALWAYS_UNLOCKED`, `TOUCH_FEEDBACK_DISABLED`) <br> 34 (`DEVICE_DISPLAY_GROUP`) |
| Untrusted virtual display | F | 26 (`MediaProjection.createVirtualDisplay`) |
| Application mirroring | N | 26 (`ActivityOptions.setLaunchDisplayId`) <br> 29 (`ActivityManager.isActivityStartAllowedOnDisplay`) |
| Prevent auto-lock | R | 26 (`WRITE_SECURE_SETTINGS`) |
| Disable USB audio output | R | 26 (`IAudioService.setWiredDeviceConnectionState`) <br> 33 (`IAudioService.getDevicesForAttributes`) |
| Display hotplug monitor | I | 26 (`DisplayManager.DisplayListener`) |

| Legend | Description |
|---|---|
| R | Required |
| O | Optional |
| F | Fallback |
| I | Inherited |
| N | Unused |

## Implementation

Native streaming servers are bridged to Android via JNI. The Moonlight path compiles [Sunshine](https://github.com/LizardByte/Sunshine) for Android and exposes a [`MediaProjection`](https://developer.android.com/reference/android/media/projection/MediaProjection) backed video source. Sunshine handles RTP packetization itself; [moonlight-common-c](https://github.com/moonlight-stream/moonlight-common-c) supplies the Limelight protocol headers, RTSP parser, and ENet UDP control transport. The AirPlay path is built on [doubletake](https://github.com/omarroth/doubletake), ported to Android via [Go mobile](https://github.com/golang/mobile) and patched for Apple devices and legacy AirPlay receivers. The DisplayLink path uses Android USB host mode with a user-imported vendor driver.

```mermaid
flowchart LR
    Android["Mirror"]
    Sunshine["Sunshine (C/JNI)<br/><small>Moonlight (RTSP + RTP)</small>"]
    Doubletake["doubletake (Go/JNI)<br/><small>RAOP + FairPlay + H.264</small>"]
    DL["DisplayLink (USB)<br/><small>USB host mode</small>"]
    Moonlight["Moonlight clients"]
    AppleSink["Apple devices or third-party AirPlay receivers"]
    DLSink["USB Display"]
    Android -- "VirtualDisplay" --> Sunshine --> Moonlight
    Android -- "VirtualDisplay" --> Doubletake --> AppleSink
    Android -- "VirtualDisplay" --> DL --> DLSink
```

CMake drives the native C/C++ build under [`./app/src/main/cpp`](app/src/main/cpp). Submodules must be initialized before building. AirPlay is powered by [doubletake](https://github.com/omarroth/doubletake) with custom bindings under [`./doubletake`](doubletake/doubletake). This library needs to be built with [`./build.sh`](build.sh) first. Make sure `go` is available in `$PATH` before building.

```bash
git submodule update --init --recursive
./build.sh && ./gradlew assembleDebug
```

Check out the [CI](https://github.com/jqssun/android-display-mirror/blob/main/.github/workflows/apk.yml) for reproducible build instructions.

## Credits

- [LizardByte](https://github.com/LizardByte/Sunshine) for Sunshine server
- [Moonlight Game Streaming Project](https://github.com/moonlight-stream/moonlight-common-c) for Moonlight headers
- [Omar Roth](https://github.com/omarroth/doubletake) for open-source AirPlay sender `doubletake`
- [Tao Wen](https://github.com/taowen) for the open-source Android bindings of DisplayLink vendor driver
- [Xiph.Org Foundation](https://github.com/xiph/opus) for the Opus codec

---

Disclaimer: This project is not affiliated with Apple Inc or Synaptics Inc.
