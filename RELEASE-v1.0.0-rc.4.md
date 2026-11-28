# Standalone TBT 1.0.0-rc.4 — coordinated development candidate

Requires HMG-API >=1.1.0-rc.10, pinned SDK daa083717f07565ba16dc8a4d26291b7d68d3bf4. Companion candidates: WCP 2.2.1-rc.7 and CPUL 1.0.0-rc.11. This release candidate has not been tested in the vehicle.

## Session logging and diagnostics
- Port the CPUL-derived categorized event policy, bounded pre-session buffer and directory writer, with STBT-specific milestones.
- Connect recording to DiPO session lifecycle; collect startup/finalization failures, generation-scoped dropped-event counters and independent recorder health. Retry unavailable spool on subsequent sessions.
- Reserve finalization capacity and protect unexported and interrupted recordings from automatic eviction. Preserve recovered flat journal evidence without pruning or rewriting it.
- Publish verified local readiness markers atomically and migrate focused recordings to directory-based session bundles.
- Consolidate cross-process USB publication under the root exporter; automatically export finalized bundles to TBT-LOGS. Verify copied data with provider-independent streaming SHA-256, USB mount identity guards and completion markers. Recover interrupted transfers and preserve conflicting or incomplete evidence.
- Extended navigation diagnostics remains opt-in; detailed logs may include private route information.

## Navigation and compatibility
- Preserve existing CarPlay and guarded Android Auto guidance, stock-map compatibility and subscription lifecycle policies.
- Experimental Waze recovery remains disabled by default and requires restart; no new Waze compatibility claim, no fabricated maneuvers or overrides of explicit unsupported guidance.
- Keep WCP Integrated turn-by-turn disabled, including saved settings; leave wireless bridge enabled. STBT is the navigation owner, WCP owns wireless and identity, CPUL owns display and native RemoteControl dispatcher.
- Exact source audit with WCP 809936b and CPUL 3c7ff11 found disjoint iAP2 verification/write ranges, identical normalized AA recipe pairs (one owner only), and no CPUL/STBT shared native library. API v3 stays WCP-owned. Static audit is not runtime validation.

## Release and installation
- Version 1.0.0-rc.4 in runtime manifest and NEMESIS wrapper; package ID standalone-tbt and established author key unchanged.
- Full branch CI reused by signed release gate, including API result export, log policy and directory writer regressions, Android API 19 build and disposable signing. Production author signature and final package checks remain release-time gates.
- Prior Apple Maps and Google Maps results are positive controls; verify guidance, session transitions, Waze diagnostics, USB insertion/removal and reconnect on the coordinated vehicle build.
- No live FileTransfer dependency; CPUL Stage 1 passive discovery is independent. No publication before explicit approval.
