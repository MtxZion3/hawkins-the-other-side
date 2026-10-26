# Hawkins — Field Receiver

Revision 6, September 24, 2026. Incremental application of `web-to-spatialui` to the existing Stranger Things inspired miniature. Authoritative source: `design-spec.json`. The exported room and native volumetric container are preserved.

## Design decision

The strongest place to demonstrate SpatialUI is the receiver beneath the room. It gives persistent controls a purpose in the fiction: contact the other world, transmit through the alphabet wall, and inspect the miniature. The wall supplies spatial feedback while the receiver supplies readable state and input.

The palette follows 1980s equipment: olive charcoal housing, inset near-black readout, amber signal and primary key, muted coral serif title, cream labels and monospaced instrument captions. Native controls retain their own feedback. The system Toolbar retains its glass border; the colored housing is an inset surface, with no opaque replacement for the volumetric root.

| Part | Native component | Purpose |
| --- | --- | --- |
| Receiver housing | System Toolbar at BottomFront | Attached controls stay spatially associated with the miniature |
| Contact selection | SegmentControl / SegmentItem | Hawkins and Upside Down read as two channels; selected state stays visible |
| Send RUN | Button | Amber primary action; disabled while transmitting |
| Slow sweep | Switch | Explicit on/off state for bounded inspection motion |
| Reset | Button | Return home, cancel transmission, reset angle; retry after load failure |
| Signal window | SpatialUI Text | Time-dependent R / U / N readout complements the alphabet wall |

All 16 PicoTheme color roles are explicit fixed values in the spec. Brand tokens define the housing and readout. Display is serif 30/34, labels monospaced 12/18, body sans 16/24, readout monospaced 30/36. Text colors are explicit or inherit a native component's documented content color.

## Porting boundary

`receiver-design.json` is the runtime-consumed UI subset of the same spec. `RoomToolbar.kt` maps its bounded graph to native Toolbar, Text, Button, SegmentControl and Switch. The ViewModel handles semantic selection and checked state, including same-channel no-op and idempotent switch events. Geometry continues through the existing three.js → GLB → Spatial entity route.

Default receiver width is 880dp; below the 760dp breakpoint it becomes 560dp. The compact layout has one row for channel/transmission and another for sweep/reset. Content height is measured by native layout. The room remains 1.4 × .8 × .8m, with existing uniform resize limits and ±14° bounded yaw.

Loading disables dependent controls. Error retains Reset for retry. Selecting another world cancels transmission and preserves the inspection angle. Selecting the current world preserves transmission. Send RUN advances letters at 650ms intervals. Reset returns to Hawkins, zero angle and paused sweep.

## Verification and scope

Revision 6 builds and all 9 JVM tests pass. The style verifier reports zero errors and warnings. A fresh simulator scene-ready log confirms 9 bindings / 81 entities, and the native screenshot shows the receiver rendered under the room. See `../evidence/history/revision-6-receiver/` for the revision-scoped logs and screenshot; current evidence status and remaining revision 7 gaps are documented in `../evidence/README.md`.

The generated preview embeds the exact revision 6 spec and validates against the design schema. Its browser execution was blocked by the Browser URL security policy; no alternate browser route was attempted. Previous browser validation under `artifacts/validation-report.json` is historical revision 4 evidence, not validation of this new panel. Revised compact layout, headset hand/ray input, physical comfort and device performance remain unverified.

Additional persistent panels would dilute the miniature. A future case-file card could fit as an on-demand secondary view if the app gains clues or story content; it is not necessary for the present interaction.
