# Update and vehicle-test guide — 1.0.0-rc.3 (candidate)

## Upgrade
1. Keep known-good packages and record current HMG-API, WCP and CPUL versions.
2. Remove the original ch0iiih Phone Projection TBT package before installing this fork. Its identity is different; it is not an in-place upgrade.
3. Explicitly disable WCP's **Integrated turn-by-turn** setting, leaving **Enable wireless CarPlay** on. Existing saved TBT=true values survive the default change; do not assume an upgrade disables TBT. Save/apply and restart before activating STBT. Verify only one guidance sender is enabled.
4. Install the signed Standalone TBT package (same author key required for subsequent updates). Apply and restart. See [installation](wiki/Installation.md).
5. Start with Experimental Waze guidance recovery OFF. Enable only for the second comparison and restart. Leave unrelated settings unchanged.

## Next vehicle test
Perform interaction while parked, or have a passenger operate the controls and record results on a safe route. Do not use experimental cluster guidance as the sole navigation source.

Create TBT-LOGS at the USB root. Record app, time, connection state and whether a maneuver appears. For each app start a fresh route; note first instruction, next turn, distance, lane data and trip ETA if available. End navigation before switching apps.

| Test | Expected / evidence |
|---|---|
| Apple Maps | Baseline: route and maneuver data; normal cluster guidance |
| Google Maps | Same baseline, source switch |
| Waze, experiment off | Record source, support flag, packet types and missing fields |
| Apple Maps again | Confirm recovery after Waze |
| Waze, experiment on after restart | Compare packet arrival and subscription attempts |
| Wireless disconnect/reconnect | No stale guidance, fresh subscription |
| Music track change after reconnect | WCP Now Playing still works |

Collect the full TBT-LOGS folder, WCP logs and incremental firmware dumper RC4 inventory and files. Logs may contain route names and destinations; share privately after review. Do not publish proprietary firmware.

## Interpretation
- No 120–122 packets: investigate phone policy or accessory negotiation.
- Source support explicitly false: do not override it or manufacture guidance.
- Active route but missing selected maneuver: evaluate the experimental retries.
- Complete data with no cluster output: inspect parser and renderer.

## Rollback
Disable experimental recovery and restart; if needed, remove this fork, apply removal and reinstall the known-good original. Use only one active TBT sender. WCP connectivity need not be changed.

Source changes are not a verified signed release. Host compilation and package CI have passed; vehicle testing and final signed publication remain pending.

## Upcoming session logging (development source)

CarPlay session journals are written asynchronously in DiPO's private storage. Each session has a unique filename, wall-clock and monotonic timestamps, and a start/end marker. Completed files are exported automatically to `TBT-LOGS/sessions/` on a mounted USB drive; a USB drive is not required during recording. The latest 12 completed files are retained locally, each approximately 2 MiB maximum; older files and earlier events in oversized sessions may be discarded. A process restart recovers leftover active files as interrupted sessions. The journal captures this mod's DiPO diagnostics, not all system/native logs. Existing diagnostic mechanisms remain available. Common MAC and credential patterns are redacted on a best-effort basis; logs may contain private navigation or device information, so review before sharing. USB export and full package build remain unverified on the vehicle.
