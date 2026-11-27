# Standalone TBT 1.0.0-rc.1

First fork release candidate, based on upstream Phone Projection TBT 1.0.5 by ch0iiih.

- Separate standalone-tbt mod and NEMESIS identity, English settings, installer instructions and current documentation.
- CarPlay session lifecycle tracking, bridge-aware component 4/5 disconnect handling, authenticated component 5 and late-binding route subscription recovery; retain legacy fallback.
- Inherited Android Auto support retained, with previously ported WCP UTF-8 parser correction, third native recipe and fingerprint gate. Future development focuses on CarPlay.
- Preserve stock-map engine allowlist, geometry matching, lane rendering and existing vehicle compatibility.
- Add CarPlay policy and package identity CI, diagnostic fixture updates and original attribution.
- Original standalone has been used successfully on US K5 alongside WCP with WCP TBT off; this fork's compiled package and physical display changes remain unverified.

Remove original standalone before first install. Keep WCP TBT disabled. Signed build and release publication pending; do not treat this source version as a published package.
