# Development and validation

See [build instructions](../BUILD.md) and [changelog](../CHANGELOG.md).

## Architecture

- `ProjectionTbt.java`: process dispatch, cluster transport observations, stock navigation and lane support, native recipe application.
- `Dipo.java` and `CpTurns.java`: iAP2 route guidance notifications 120–122, subscription setup/recovery, maneuver/trip/lane conversion and cluster output.
- `AndroidAuto` and `AaTurns.java`: AA hooks, raw navigation input, distance filtering, maneuver and lane conversion.
- `StockGraphNative.java`, `EngineProfiles.java` and `StockNavigationApi.java`: exact engine hash validation, executable mapping checks, helper staging and bounded native map queries in the stock navigation process.
- `StockMapJunction` and geometry helpers: live request matching, lookahead, compound junctions and ordinary-guidance fallback. Queries are non-overlapping and approximately one second apart while valid guidance requests exist.
- `export-logs.sh` and `export-api-result.sh`: bounded rotating diagnostic snapshots and USB export.
- `navigation-api/native`: project-owned ARM32 JNI helper source; proprietary engine is not included.

## Planned work (not implemented)

Audit WCP component-5 reattachment and guidance recovery, avoid clearing active guidance on bridge-only disconnects, compare the additional AA recipe, and verify US K5 engine ABI and fingerprints before extending the allowlist. Keep WCP connectivity separate. Do not run native queries against an unverified engine.

## Validation

Upstream 1.0.5 reported 39 Java / 10 Python tests, input replays, D8, signing and ZIP verification. Fork results are pending. Separate host tests, replay results and physical vehicle checks. Preserve original authorship and notices; never publish private firmware or signing keys.
