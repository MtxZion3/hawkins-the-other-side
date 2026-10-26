# VibeLab release preparation — 1.0.2

## Application and localization

Version `1.0.2`, code `3`, package `com.example.hawkins`, Spatial SDK `6.1.9`.
The receiver layout, six puzzle phases, loading/error guidance, action labels and accessibility descriptions use English and Simplified Chinese Android string resources. Numeric formats use resource placeholders. The app follows system/per-app language settings; unsupported languages fall back to English. Puzzle letters and internal binding IDs remain unchanged. The scene retains 9 semantic bindings and 81 entities; its plaque uses the language-neutral year 1983.

The dedicated radio-and-signal icon is shared by the Android launcher and VibeLab listing. Template 3D icon metadata was removed. No new app code was needed for this media refresh; captures use application source commit `79001fb74670bb1269b3ba7d581f451356c3af53`.

## Verified behavior

- Build and 13 native unit tests passed in the localization pass.
- Seven localization checks and seven asset-export tests passed.
- Four Android instrumentation tests passed on the physical Swan, including English/Chinese resource resolution, formatting, accessibility and fallback language.
- `lintDebug` passed with zero errors; template/dependency/icon warnings remain.
- On the emulator, Chinese Reset → world switch → tune to 83 MHz → Listen → letter reply → Transmit reached `连接已建立`.
- The English run also submitted an incorrect reply, verified `03 / NO RESPONSE`, erased it, and submitted a correct reply. Readback confirmed `CONTACT ESTABLISHED`.
- Changing app language after solving preserved the solved state in this run. Process-death persistence is not claimed.
- Both runs were continuous real-time recordings. Camera positioning was checked against actual images. Public accessibility actions drove the controls; this does not certify physical hand/controller input or independent visual puzzle solving.

The 1.0.2 physical APK was installed, but the sleeping headset prevented a new foreground visual acceptance run. Earlier 1.0.1 physical-device validation is historical evidence, not proof that every localized screen was checked on hardware. Long-duration performance and other headset models remain unverified.

## Publishing media

Preparation mode: agent-prepared. All product images below come from the running 1.0.2 emulator application, not a design preview. The icon is original purpose-built artwork. `assets-manifest.json` records exact hashes, dimensions, color profiles and durations.

| Asset | Content |
| --- | --- |
| `assets/icon.png` | Radio-and-signal launcher artwork, 1024×1024 Display P3 |
| `assets/landscape.png` | English completed scene from a separate oblique camera view |
| `assets/detail-1.png` | Chinese normal-world receiver and room |
| `assets/detail-2.png` | Chinese Upside Down environment |
| `assets/detail-3.png` | Active letter-wall signal and receiving state |
| `assets/detail-4.png` | English incorrect reply with recovery instructions |
| `assets/detail-5.png` | Chinese successful contact and restored room |
| `assets/demo.mp4` | About 46 seconds of continuous Chinese gameplay |
| `assets/demo-en.mp4` | About 64 seconds of continuous English gameplay including error recovery |
| `assets/demo-cover.png`, `assets/demo-en-cover.png` | Frames extracted from the corresponding recordings |

All landscape/detail/cover images and both videos are 2560×1440 at 16:9. Images use Display P3; videos use P3 D65 primaries with sRGB transfer, encoded at 30 fps. Source recording averaged about 28.5 fps on this emulator. FFmpeg performs an actual BT.709-to-P3 conversion; screenshot pixels are converted from sRGB using ColorSync. Cropping/resizing preserves the application content. Videos are not slideshows, are not sped up, and contain no invented game audio. The application has no audio-based gameplay.

There are nine images including the icon and two videos. Each image is below 5 MB; each video is below 100 MB. Raw captures, local device identifiers, credentials and upload state are not published.

## Follow-up improvements

- The default distant view makes text and letter clues small. Close camera framing helps, but a user-facing inspection mode or adjustable panel placement would improve regular use.
- Signals run at 650 ms per letter. Replay is available; a slower mode would improve readability.
- Controls have localized accessibility descriptions, but the 3D visual clue sequence has no equivalent nonvisual mode. Do not describe the entire puzzle as screen-reader accessible.
- Progress persistence across process death remains a design improvement to evaluate, not a reproduced locale-switch bug.

## Publication boundary

The destination is the existing VibeLab draft backed by the public GitHub repository. Saving a draft is not formal review submission or store approval. The CN provider exposes a Chinese listing slot; the description includes both languages without claiming a second English listing record. Upload state must be verified on the creator page when the provider's read-only state endpoint returns a server error.

Original code-generation model was not recorded by the source project. Release preparation used Codex / GPT-6, with Image Generation for the custom icon. This is an independent, unofficial fan-inspired technical prototype with procedurally authored scene geometry.
