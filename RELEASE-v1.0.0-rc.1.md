# Standalone TBT 1.0.0-rc.1

First fork release candidate, based on upstream Phone Projection TBT 1.0.5 by ch0iiih.

- Separate standalone-tbt mod and NEMESIS identity, English settings, installer instructions and current documentation.
- CarPlay session lifecycle tracking, bridge-aware component 4/5 disconnect handling, authenticated component 5 and late-binding route subscription recovery; retain legacy fallback.
- Inherited Android Auto support retained, with previously ported WCP UTF-8 parser correction, third native recipe and fingerprint gate. Future development focuses on CarPlay.
- Preserve stock-map engine allowlist, geometry matching, lane rendering and existing vehicle compatibility.
- Add CarPlay policy and package identity CI, diagnostic fixture updates and original attribution.
- Original standalone has been used successfully on US K5 alongside WCP with WCP TBT off; this fork's compiled package and physical display changes remain unverified.

Remove original standalone before first install. Keep WCP TBT disabled. Signed build and release publication pending; do not treat this source version as a published package.

## Navigation reliability and Waze experiment

- Correct initial route decoding for component ID zero; handle missing items and empty arrays and bound maneuver list copies.
- Distinguish an absent source-supports-guidance field (item 20) from explicit false.
- Record changed decoded route, maneuver and lane messages (120–122), including 64-bit fields; log subscription results, component enumeration, session generation and disconnect events. Logs can include private route information.
- Reject queued packets and disconnects from previous session generations; reset route caches on a new session and guard subscription restarts against session changes.
- Add opt-in **Experimental Waze guidance recovery** (off by default, restart required). A silent connection gets one Start-only retry after 5 seconds. An identified Waze source with active but incomplete maneuver data gets up to two Start-only retries, at 3 and 10 seconds after detection. Valid data, explicit unsupported capability, source changes and session changes cancel pending Waze retries. No synthetic directions or identity spoofing.
- Add independent Java recovery policy and CI regression cases. The policy was smoke-tested separately; the complete signed package, CI outcome and runtime integration have not been verified.

Waze compatibility remains unresolved. Native US K5 intersection enlargement is still gated by the unknown stock engine fingerprint. Apple Maps and Google Maps are the positive controls for the next vehicle test. See [update and test instructions](UPDATE.md).
