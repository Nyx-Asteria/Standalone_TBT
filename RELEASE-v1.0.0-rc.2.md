# Standalone TBT 1.0.0-rc.2

CarPlay-focused test candidate. Keep WCP integrated TBT disabled. Preserve existing Android Auto implementation; no new AA validation.

## Navigation reliability
- Invalidate stale maneuver distances after maneuver index changes or numeric-only distance updates.
- Skip experimental startup retry if no route-guidance components are registered; preserve component count when supplemental queries fail.
- Skip source-switch refresh on explicit unsupported capability.
- Clear route freshness, junction trace state and experimental incomplete-guidance recovery on confirmed route stop.

## Session diagnostics
- Default concise logging; verbose input/native guidance logs opt-in, exclude repetitive stock clock packets and deduplicate.
- Asynchronous bounded session journal, wall/monotonic timestamps, redaction, queue/drop markers, interrupted-session recovery and disk failure handling.
- Export completed recordings to TBT-LOGS on mounted USB, including delayed insertion; symlink safeguards.
- Opt-in focused third-party navigation journal, exclude Apple Maps and Google Maps, record packet types 120/121/122, source capability and component attribution without route text. Unattributed packets remain explicitly labeled; no attribution is proof of an app identity.
- Focused USB export under TBT-LOGS/third-party-navigation.
- Regression coverage for distance, lifecycle, subscription recovery, Waze policy, source attribution, session journals and exporters.
- Full Android API 19 compilation, DEX, disposable-key signing and ZIP wrapper verification in CI.

## Known limitations
- Apple Maps and Google Maps are positive controls; Google Maps distance issue requires retesting.
- Waze explicitly reports unsupported guidance in prior tests; no forced capability or fabricated maneuvers. MapQuest untested.
- Native US K5 intersection enlargement remains gated by verified stock library fingerprint.
- No AirPlay or projection identity changes. EWF removal and HMG-API file conflicts require separate recovery.
- RC2 runtime validation pending. Preserve existing signing identity and installation backups.
