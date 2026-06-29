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

This fork is based on the open-source [Screen Mirroring Manager for Android](https://github.com/jqssun/android-display-mirror) project, with the public build focused on Android playback audio to AirPlay-compatible receivers.

## Upstream And Credits

Android2Apple4Music is GPLv3 open source. It keeps attribution to the upstream Android display-mirror project and the underlying AirPlay work it builds on.

- [jqssun/android-display-mirror](https://github.com/jqssun/android-display-mirror) for the original Android mirroring project
- [Omar Roth](https://github.com/omarroth/doubletake) for open-source AirPlay sender `doubletake`
- [Xiph.Org Foundation](https://github.com/xiph/opus) for the Opus codec

Disclaimer: This project is not affiliated with Apple Inc.
