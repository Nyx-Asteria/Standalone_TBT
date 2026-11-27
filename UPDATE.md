# Update and vehicle-test guide — 1.0.0-rc.1

## Upgrade
1. Keep known-good packages and record current HMG-API, WCP and CPUL versions.
2. Remove the original ch0iiih Phone Projection TBT package before installing this fork. Its identity is different; it is not an in-place upgrade.
3. Disable WCP's cluster/HUD TBT setting, leaving wireless bridge and Now Playing enabled. Save/apply and restart.
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

Source changes are not a verified signed release. Full compilation and vehicle testing remain pending.
