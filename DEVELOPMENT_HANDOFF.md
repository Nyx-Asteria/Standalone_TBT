# STBT — 3.0.0 development handoff

Updated 2026-11-27. Active development branch: `3.0.0`. Frozen preceding candidate: `rc4-final` at `42a07f52821ee3cfbde71bb6bf2b5a457b150b5f`.

## Start here — instructions for a fresh development chat

- This is the Standalone TBT (STBT) workstream, not CPUL. The owner runs separate chats for separate repositories. Verify branch HEAD, read this file and inspect actual source/tests before editing. Do not modify other repositories unless explicitly requested.
- `3.0.0` is the new development branch, not a published package or a version bump. Current manifest remains 1.0.0-rc.4 until the next release preparation. The owner is leaning toward plain patch versions (3.0.0, 3.0.1, …), not a finalized change to the package/release policy.
- The previous cycle is implementation-complete and consolidated. Both original and consolidated candidate CI passed (see verified release checkpoint below). Do not restart completed logging/identity work just because historical status documents contain old percentages. Vehicle testing is a separate gate, not included in implementation progress.
- Update this living handoff in the SAME commit as every development change. State files changed, rationale, actual tests/results, exact source pins, limitations and next concrete action. Keep it current and concise; link historical evidence rather than accumulating an enormous diary. Use meaningful architectural commits, preserve rollback/history, never force-push main or rewrite a published tag.
- On a "keep going" instruction, perform a bounded, substantive development cycle on this branch, commit it with the handoff and report actual progress. Do not claim that a build, publication or background task happened without evidence. Report implementation estimates separately from CI and vehicle evidence.
- For coordinated releases, CPUL determines the version train. Unchanged companions may skip releases and adopt the current train version when next released. Preserve mod IDs, permanent signing identity and independently versioned external dependencies. Successful coordinated release builds may publish automatically when authorized; ordinary development pushes must not publish. Full changelogs, nondraft releases, exact-tree consolidation, fresh CI and verified signed package provenance remain required.
- Target vehicle: 2022 Kia K5 EX, US Gen5W_M DL322 260507, Android 4.4.2/API 19, HMG-API RC10 floor and Nemesis 1.17.1. Do not claim other vehicles or firmware are verified.

## Frozen coordinated source pins

| Component | Branch | Exact consolidated source |
| --- | --- | --- |
| WCP | rc7-final | 973c14ec3a2d169adda665a8ee65b3f347e6e20a |
| STBT | rc4-final | 42a07f52821ee3cfbde71bb6bf2b5a457b150b5f |
| CPUL | rc11-final (original release commit) | 162054a27c492fc421a0cc5e88087b92aede538b |
| PPS | prior audited source | bd57be364d95d9542b5aaf81269fa2b8dc1d35e2 |
| HMG-API | RC10 pinned SDK | daa083717f07565ba16dc8a4d26291b7d68d3bf4 |

CPUL's published release tag resolves to the original-pin release commit above, not the consolidated companion hashes. The owner counts both included companion ZIPs as published; do not wait for separate releases. Do not silently replace assets. On the companion consolidated branches only DEVELOPMENT_HANDOFF.md and CI push branch allowlist differ from the original green trees; restoring both blobs reproduced the exact originals. Historical candidates remain unchanged.

## Verified release checkpoint and active entry point

- [CPUL RC11 published release](https://github.com/Nyx-Asteria/carplay-ultra-lite-gen5w/releases/tag/v1.0.0-rc.11) is non-draft, tag commit 162054a27c492fc421a0cc5e88087b92aede538b. GitHub API lists testing bundle, wireless-carplay-aa-2.2.1-rc.7.zip, standalone-tbt-1.0.0-rc.4.zip, BUILD-PROVENANCE.txt, SHA256SUMS.txt, TESTING.md and theme-pairing-validation.md. Owner explicitly confirmed bundled companion packages count as published. Separate release pages were absent at last check; do not make them a development prerequisite. No ZIP bytes/signatures independently downloaded or reverified in this turn.
- Original WCP pin 3c3d2489b5bd4b8f4b8d37a5a451341701f78bce passed CI 46090592763; consolidated 973c14ec3a2d169adda665a8ee65b3f347e6e20a [fresh CI 46093788023 passed](https://github.com/Nyx-Asteria/hmgapi-wireless-cp-aa/actions/runs/46093788023).
- Original STBT pin fac2ec367028a97334d548c53d5930d768ec2694 passed CI 46090509762; consolidated 42a07f52821ee3cfbde71bb6bf2b5a457b150b5f [fresh CI 46093859782 passed](https://github.com/Nyx-Asteria/Standalone_TBT/actions/runs/46093859782). Original pins built into RC11; consolidated histories equivalent per preserved tree proof. No release changes required.
- This commit on 3.0.0 updates handoff and adds the quoted branch name to ordinary development CI push allowlist only. Parent 17051b5850679a902788b7cae49c869d14cd7c02; prior consolidated CI 46093859782 green. New CI pending. No runtime, package version, signing, release workflow, tag or frozen-branch edits. No new version format selected.
- No RC11 vehicle logs received in this workstream yet. First collect existing RC11 WCP-LOGS, TBT-LOGS and CPUL USB session logs plus reported observations; correlate session/generation, exact versions and settings. Use published TESTING.md and theme-pairing-validation.md. Parked pairing/reconnect/fallback and diagnostic checks; compare prior Apple/Google positive controls with Waze. Protect route/device data and credentials. Use existing private dumps; request only specifically missing data. Do not claim actual vehicle results from host CI.

## STBT next-cycle technical checklist

- Preserve single navigation publisher (WCP tbt=false including persisted values, bridge=true), stock/CarPlay and guarded AA behavior, lifecycle, subscription recovery, distance/next-turn/lane freshness, stock-map fallback. Inspect src/ProjectionTbt.java, CpSessionLifecycle, CpSubscriptionRecovery, CpDistancePolicy, CpWazeRecovery, ThirdPartyTrace and tests. No FileTransfer work here.
- Prior positive Apple/Google guidance; Waze previously explicit sourceSupported=false and no maneuvers. Do not override explicit unsupported or fabricate directions. experimental_waze_recovery=false default, verbose_diagnostics=false default; opt-in focused third-party traces when useful, redact route identifiers. Compare advertised component fields, source support, subscription, first maneuver/lane, stop and reconnect across apps. No claim of Waze fix before vehicle evidence.
- Read docs/ios27-navigation-capacity-research.md: examined CarKitNavigation wrappers return 1000 on zero; unresolved external targets, no established causal link between capacity and eligibility. Prioritize caller of sendNoSupportForAppIdentifier: and app metadata decision; verify actual serialized component advertisement, do not force zero/1000. Reopen actual binaries before making new disassembly claims.
- Preserve RC4 directory journal, bounded pre-session and generation-specific events, finalized bundles, root USB verified publication, provider-independent SHA-256, mount checks, retention protection, legacy evidence recovery and API result export. Test startup/finalization, unplug/reinsert and stale navigation on new logs. Inspect src/SessionExporter.java and regression tests. Keep unexported evidence.
- Full policy/package CI required, Android API19, DEX, disposable-key signing, installer version parity; signed release uses full CI. Retain mod.json and installer/NEMESIS.data parity, permanent author ID. Audit actual WCP iAP2 ranges and AA native ownership after any change. Consult RELEASE-v1.0.0-rc.4.md, RELEASE_PROCESS.md, UPDATE.md, COMPATIBILITY.md. No competing native dispatcher or API change.

## First 3.0.0 cycle priorities

1. Release checkpoint is complete; start with actual RC11 vehicle evidence when available. Do not reopen the frozen candidates or repeat completed release checks as development tasks.
2. Review RC11 vehicle test evidence when available: prioritize reproducible regressions and correlate CPUL/WCP/STBT session logs by session and generation. Keep known-working wireless reconnect, standard fallback, native apps/sidebars and Apple/Google guidance intact.
3. Confirm shared version format before changing package manifests or installer/release metadata; audit upgrade semantics and enable ordinary branch CI for future code changes without enabling automatic release on this branch.
4. Investigate Waze eligibility and stale guidance only from actual route/session evidence; preserve the single navigation publisher and bounded logging. FileTransfer is not an STBT work item.
5. Reuse existing private firmware/IPSW archives and session logs; collect only missing files or fields. Do not include secrets, private captures or raw identifiers in commits.

## Historical RC state (preserved baseline proof; pending statements superseded by checkpoint above)
Version 1.0.0-rc.4, ID standalone-tbt, HMG-API >=1.1.0-rc.10. Package/installer metadata, permanent author keys, release notes and release workflow unchanged.
Original green development head fac2ec367028a97334d548c53d5930d768ec2694 retained on k5-log2-cleanup; [full historic handoffs](https://github.com/Nyx-Asteria/Standalone_TBT/blob/fac2ec367028a97334d548c53d5930d768ec2694/DEVELOPMENT_HANDOFF.md). Original [CI 46090509762](https://github.com/Nyx-Asteria/Standalone_TBT/actions/runs/46090509762) passed policy and package. New candidate must pass fresh CI; no intermediate fresh testing claimed.
Reconstructed RC3 baseline 560235ca4e7029b6471f049c325849253be3eb65, original RC3 e8770256ff6679546c483af43aed3f7a6b19b998, identical tree 0c0a382cbeeacb1fe0bd78139cb1aefd2a085fe9. Consolidated 20 development commits into 10 chronological architectural milestones. Original final tree d5fc69b79d005f515cc6133fd40cbedc74a59c7c. Only final intentional differences are this handoff and rc4-final CI push allowlist. Restoring both original blobs must reproduce exact original tree before push. No runtime changes, tags or publication.
WCP consolidated rc7-final 973c14ec3a2d169adda665a8ee65b3f347e6e20a needs fresh CI; original RC7 3c3d2489b5bd4b8f4b8d37a5a451341701f78bce passed CI 46090592763. CPUL RC11 162054a27c492fc421a0cc5e88087b92aede538b pins original green companions; existing v1.0.0-rc.11 tag resolves there, actual release assets not confirmed. Never rewrite existing tags or replace published assets silently.

## Architecture and implementation
STBT sole navigation publisher when installed; WCP integrated TBT off, bridge on. WCP owns wireless/iAP2 identity/StartSession; CPUL owns native INFO/SETUP/type-130 dispatcher, display and vehicle services. No physical cluster graphics modifications.
RC4 logging overhaul: provider-independent streaming SHA-256, categorized bounded pre-session events, persistent directory writer/unexported retention, mount-guarded verified USB publication, root discovery, DiPO startup/finalization capture, read-only retired flat-journal recovery, generation-scoped drops and finalization capacity, atomic markers, interrupted storage protection, recorder health and spool retry, root cross-process export. Regression fixtures cover snapshot, interrupted copy and replacement recovery. Preserve session-end USB logs and historical evidence.
Apple Maps and Google Maps previously worked; Waze unresolved, not a proven STBT or iAP2 bug. Keep focused opt-in third-party diagnostics and preserve stock behavior. No speculative patches.
Original matched source audit: WCP/STBT iAP2 stock verify/write regions disjoint; all three AA recipe pairs identical normalized directives (not permission for concurrent ownership); WCP API v3 unchanged. CPUL Stage 1 passive discovery only; inactive Stage 2 FileTransfer outside STBT scope.

## Historical RC closeout tasks (superseded by active priorities)
Check fresh rc4-final Actions policy/package CI on exact head; obtain run URL if connector cannot list push jobs. Fix only genuine blockers with updated handoff and retest. Relay final SHA, tree proof and fresh CI to CPUL; no standalone signing/publication triggered by consolidation.
Post-release vehicle tests: Apple/Google/Waze routes, start/change/end/reconnect, stale distance clearing, lane/turn publication, USB finalization and startup failure/recovery. Correlate WCP/CPUL logs. No vehicle testing counted as implementation. Existing firmware/IPSW archives private; collect only missing evidence.
Next release train begins 3.0.0 across CPUL/WCP/STBT. Owner considering plain patch versions (3.0.0, 3.0.1 ... 3.0.17) rather than prior 3.0.0-rc.1 plan; not finalized. CPUL controls train number; unchanged companions may skip and adopt current number when next released. Preserve IDs and signing keys; don't alter RC4 version. After choice audit manifest/installer parity, upgrade comparisons, workflow/tag gates, package names, prerelease flags and changelogs together. Independent external dependency versions unchanged.
Preserve original histories, meaningful architectural milestones, full changelogs, exact-tree proof and matched CI. Successful coordinated CPUL build authorizes publication per owner; standalone companion publication follows current coordinated instruction. Do not disclose signing secrets, private firmware or identifiers. Report CI and vehicle evidence separately.
