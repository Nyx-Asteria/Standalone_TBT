# Standalone TBT 1.0.0-rc.3 — development candidate

Requires HMG-API >=1.1.0-rc.10; SDK pinned to daa083717f07565ba16dc8a4d26291b7d68d3bf4. No release or vehicle validation is implied by these notes.

## Navigation and settings
- Core guidance, road-type junction thresholds, toll enlargement and 12-hour ETA remain enabled. Remove the retired junction trial, capture and stock map toggles from runtime decisions; keep only experimental Waze recovery and verbose diagnostics configurable.
- Remove speed-based junction enlargement selection; retain stock-map compatibility guards and ordinary guidance fallback.
- Capture junction input independently of verbose diagnostics; extended third-party tracing remains opt-in.
- Preserve prior Waze capability policy: explicit unsupported guidance is not overridden; no fabricated maneuvers. No verified Waze fix is claimed.
- Android Auto expanded parser is requested only for recognized stock library fingerprints. Basic callback guidance remains available on the fallback path; AA runtime has not been validated for this candidate.

## Session diagnostics
- Isolate per-generation dropped-event counters, capture timestamps at enqueue, preserve bounded recording and redaction.
- Recover interrupted journals on later starts; retry failures without unbounded file growth. Retain up to twelve completed and twelve orphaned recordings plus the active recording per journal directory, subject to filesystem availability.
- Isolate filesystem permission failures; count successful retention deletions and refuse new recordings if the retention budget cannot be enforced.
- Bound ordinary events to 256 and lifecycle tasks to 32, reserve finalization capacity, and report skipped recording attempts after backlog recovery.
- Export finalized journals through a separate Java service; retain originals under local retention policy. Verify destination contents with SHA-256, publish per-file completion manifests, preserve conflicts, and continue exporting other eligible files.
- Guard USB mount identity including mount ID, check during copying, and retry incomplete transfers after remount. Retire known stale manifests as .complete.invalid while preserving conflicting payloads.
- Avoid redundant full destination digest reads on existing verified exports. File synchronization does not guarantee directory metadata persistence on abrupt power loss.
- Include runtime package version in diagnostics; keep legacy shell export paths separate from Java journal exports.

## Installation and integration
- Separate standalone-tbt package identity and existing author key; remove original Phone Projection TBT package before installing this fork.
- When WCP is installed, explicitly disable Integrated turn-by-turn and keep Enable wireless CarPlay on; save, apply and restart. Prior saved true values survive default changes. Do not run two guidance senders.
- WCP owns connectivity and shared identity; this mod does not establish wireless sessions. CPUL is under separate active development and its final integration is pending.

## Validation
- Host policy, session journal and exporter regressions, package identity checks, Android API 19 compilation, DEX, disposable-key signing and installer wrapper validation are part of CI.
- Final release must pass policy tests and configured author signing validation. Actual production-signed publication has not been performed.
- Apple Maps and Google Maps are prior positive controls, not evidence of RC3 vehicle validation. Waze remains unverified; native stock-map enlargement requires compatible engine fingerprints.
- USB removal tests simulate changes in mount identity. Physical removal, abrupt power loss and actual vehicle functionality remain separate validation items.
