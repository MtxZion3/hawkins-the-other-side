# Hawkins: The Other Side

A native PICO Spatial miniature mystery: tune a vintage field receiver, watch a message travel across the alphabet lights, and send a reply from a tiny 1980s living room.

An independent, unofficial fan-inspired prototype. No affiliation with the television production or its rights holders. The scene is authored procedurally in Three.js and runs natively through PICO Spatial SDK; it is not a WebView.

![Hawkins app icon](assets/icon.png)

## Play

1. Place the miniature comfortably in front of you and keep the entire receiver panel visible.
2. Switch between **Hawkins** and **Upside Down** to inspect the two versions of the room.
3. Move the frequency slider until the receiver locks onto the signal and **Listen** becomes available.
4. Press **Listen**. Watch the alphabet wall and remember the sequence.
5. Use the letter keys to enter the reply, **Erase** to correct it, and **Transmit** to send it.
6. **Slow Sweep** gently rotates the miniature. **Reset** starts the puzzle again.

The current interface is English. The experience uses visual signals; it does not require a microphone, external audio service, account, or application network connection. Headset system services may have their own network requirements.

## Build and install

Requirements: JDK 21, Android SDK Platform 35, and an arm64 PICO device with a compatible Spatial runtime. The project pins Spatial SDK **6.1.9**. The current release package has been installed on a Swan device with runtime **6.3**; this is not a claim of validation on every PICO model or older ROM.

```sh
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug testDebugUnitTest
pico-cli app install app/build/outputs/apk/debug/app-debug.apk --replace --device YOUR_DEVICE
pico-cli app launch com.example.hawkins --device YOUR_DEVICE
```

The application ID is `com.example.hawkins`; release-preparation version is `1.0.1` / version code `2`. Dependencies resolve from Google Maven, Maven Central, Gradle Plugin Portal, and the public PICO SDK artifact repository. No internal Maven mirror or private signing key is included. VibeLab builds from the pinned Git commit; local debug APKs are not committed.

## Source layout

- `app/`: native Kotlin application, preserved GLB scene, declarative receiver layout, and unit tests.
- `authoring/`: procedural Three.js scene factory, exporter, semantic bindings, and export tests.
- `delivery/`: versioned scene bundles and `latest.json` selecting the exact asset revision.
- `design/`: offline design reference and renderer sources. These are design artifacts, not publishing screenshots.
- `assets/`: app icon and approved runtime-derived VibeLab media.
- `vibelab.yaml`: reproducible publishing metadata and relative media paths. `.vibelab/` is local state and is ignored.

```sh
cd authoring
npm ci
npm test
npm run export
node sync-native.mjs
```

The native scene preserves 9 semantic bindings and 81 entities. Do not flatten independently controlled objects into one mesh: world visibility, alphabet lights, rotation, and puzzle state depend on those identities.

## Validation and release material

The `1.0.1` preparation build and all **13** native unit tests passed. Current runtime/media acceptance is recorded in `RELEASE.md`; a design preview or an older screenshot is never evidence for the new release. A dedicated radio-and-signal icon replaces the template launcher art, including removal of the template 3D icon metadata.

No claim is made about completed headset-model coverage, long-duration performance, or full hand/controller acceptance. Public availability of the repository does not imply a VibeLab review or store release has been approved.
