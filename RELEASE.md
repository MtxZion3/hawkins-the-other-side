# VibeLab release preparation — 1.0.1

## Included changes

- Dedicated radio-and-signal icon shared by Android launcher and VibeLab listing.
- Template 3D launcher layers removed to avoid unrelated default artwork.
- Public Maven build configuration, public source, English play/build instructions and Chinese listing copy.
- Existing native scene, semantic bindings and puzzle logic preserved.

## Validation on 2026-10-26

- `assembleDebug`: passed; `testDebugUnitTest`: 13 passed, 0 failures.
- Physical Swan, Spatial runtime 6.3, Android 16, arm64.
- Application ID `com.example.hawkins`; version `1.0.1`, code `2`.
- Installation and launch succeeded. Custom radio icon observed in the device Dock.
- Live public accessibility actions verified Reset, Upside Down, frequency tuning to 83 MHz, Listen, R/U/N entry, and Transmit.
- Read-back confirmed `CONTACT ESTABLISHED` and `You found a way through. The room is safe for now.`
- This is one physical-device acceptance run, not broad headset coverage or a long-duration performance certification. Controller/hand input was not exhaustively tested.

## Publishing material

Preparation mode: agent-prepared. Target: a VibeLab cloud draft backed by a public GitHub commit. No emulator images, HTML previews or old-version screenshots are used.

| Asset | Content | Output |
| --- | --- | --- |
| `assets/icon.png` | Original radio-and-signal launcher artwork | 1024×1024, Display P3 |
| `assets/landscape.png` | Runtime alphabet wall and locked receiver close-up | 2560×1440, Display P3 |
| `assets/detail-1.png` | Normal-world room and receiver overview | 2560×1440, Display P3 |
| `assets/detail-2.png` | Upside Down and partial letter reply | 2560×1440, Display P3 |
| `assets/detail-3.png` | Receiving state and receiver controls | 2560×1440, Display P3 |
| `assets/detail-4.png` | Successful transmission and safe room | 2560×1440, Display P3 |
| `assets/demo.mp4` | Continuous physical-headset gameplay, world switch through successful reply | 72 seconds, 2560×1440, 30 fps, P3 D65 |
| `assets/demo-cover.png` | Runtime recording frame with completed reply ready to transmit | 2560×1440, Display P3 |

Still captures came from `pico-cli capture screenshot --spatial`; video came from the physical device's native `screenrecord`, invoked through `pico-cli shell`. Native recording was bounded to 85 seconds and retrieved using `pico-cli files pull`. It was trimmed, rotated, cropped to one eye, framed to preserve the whole receiver, and converted to P3. The original device video reports BT.470BG primaries/matrix with SMPTE 170M transfer; FFmpeg performed an actual color-space transform to P3 D65/sRGB transfer. Frames extracted from that converted video carry a matching Display P3 ICC profile. Still screenshots were converted from their embedded sRGB profile with ColorSync.

The video retains the headset compositor's optical distortion and uses side borders to avoid cutting off controls. It is a real continuous capture, not a slideshow or a generated gameplay reconstruction. This visual-only prototype has no recorded game audio. Screenshot crops and resizing improve focus but do not add scene content. A small background headset overlay remains visible in some frames; no account details or desktop/chat content is included.

All seven image files are below 5 MB (the icon below 4 MB), the video is below 100 MB, and images have distinct hashes. `assets-manifest.json` records exact deliverable hashes and dimensions. Raw captures, device identifiers, credentials and local upload state are not published.

Original code-generation model was not recorded by the source project. This release preparation used Codex / GPT-6 for build configuration, documentation and publishing, and Image Generation for the custom icon. Public source availability and a saved draft do not imply that VibeLab review or store release has been approved.
