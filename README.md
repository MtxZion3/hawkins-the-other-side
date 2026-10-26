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

The interface supports English and Simplified Chinese, following the system language or Android per-app language preference. All receiver copy, phase hints and accessibility labels resolve through Android string resources (`values/strings.xml` and `values-zh/strings.xml`). Puzzle letters remain Latin symbols in both languages. The experience uses visual signals; it does not require a microphone, external audio service, account, or application network connection. Headset system services may have their own network requirements.

## Build and install

Requirements: JDK 21, Android SDK Platform 35, and an arm64 PICO device with a compatible Spatial runtime. The project pins Spatial SDK **6.1.9**. The current release package has been installed on a Swan device with runtime **6.3**; this is not a claim of validation on every PICO model or older ROM.

```sh
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug testDebugUnitTest
pico-cli app install app/build/outputs/apk/debug/app-debug.apk --replace --device YOUR_DEVICE
pico-cli app launch com.example.hawkins --device YOUR_DEVICE
```

The application ID is `com.example.hawkins`; release-preparation version is `1.0.2` / version code `3`. Dependencies resolve from Google Maven, Maven Central, Gradle Plugin Portal, and the public PICO SDK artifact repository. No internal Maven mirror or private signing key is included. VibeLab builds from the pinned Git commit; local debug APKs are not committed.

## Source layout

- `app/`: native Kotlin application, preserved GLB scene, declarative receiver layout, and unit tests.
- `authoring/`: procedural Three.js scene factory, exporter, semantic bindings, and export tests.
- `delivery/`: versioned scene bundles and `latest.json` selecting the exact asset revision.
- `design/`: offline design reference and renderer sources. These are design artifacts, not publishing screenshots.
- `assets/`: app icon and versioned runtime-derived VibeLab media (see `RELEASE.md` for current acceptance status).
- `vibelab.yaml`: reproducible publishing metadata and relative media paths. `.vibelab/` is local state and is ignored.

```sh
cd authoring
npm ci
npm test
npm run export
node sync-native.mjs
```

The native scene preserves its 9 semantic bindings and independently controlled entities. Do not flatten independently controlled objects into one mesh: world visibility, alphabet lights, rotation, and puzzle state depend on those identities.

## Validation and release material

The `1.0.2` build and all **13** native unit tests passed. Run `python3 scripts/check-localization.py` for the seven resource, formatting, accessibility and locale-declaration checks. Current runtime/media acceptance is recorded in `RELEASE.md`; a design preview or an older screenshot is never evidence for the new release. A dedicated radio-and-signal icon replaces the template launcher art, including removal of the template 3D icon metadata.

No claim is made about completed headset-model coverage, long-duration performance, or full hand/controller acceptance. Public availability of the repository does not imply a VibeLab review or store release has been approved.

## 中文说明

把复古客厅放到面前的微缩空间中，在“霍金斯”和“颠倒世界”之间切换。调谐到灯光稳定的频率后，点击“收听”，观察字母灯墙依次亮起的字母，输入回复并点击“发送”。“删除”可修改回复，“重置”可重新开始，“缓慢转动”可查看场景。

界面、错误提示和无障碍标签支持简体中文及英文，默认跟随系统语言，也可使用 Android 的应用语言设置。灯墙和按键上的拉丁字母是谜题符号，不做翻译。场景基座只保留年份，避免把英文世界名称烘焙到模型中。应用无账号、无联网功能；这是非官方粉丝灵感原型，非影视作品官方产品。

### Localization maintenance

`receiver-design.json` owns layout and references `@string/...` keys for visible copy. `ReceiverStrings.kt` maps those keys to compiled Android resource IDs; add both languages and the typed reference when adding copy. `LocalConfiguration` changes invalidate the localized resources. Domain state IDs, asset binding names, diagnostic logs and puzzle symbols are not translated. Exception details are never displayed directly; the UI uses localized recovery copy.

`design/` contains the original English authoring previews, not runtime language resources or current publishing evidence. Export synchronization copies the 3D scene and bindings; it does not overwrite the localized receiver layout.

The current release-cli CN provider supports only the `zh-CN` listing slot. The description includes both languages; this is not a claim of a second English locale record or an international release channel.
