# STBT RC4 — Development handoff
Updated 2026-11-27. Consolidated checkpoint on rc4-final.

## Current milestone
RC4: replace retired exporter and verify provider-independent SHA-256
Retired shell session exporter cleanup, streaming SHA-256 and provider-removal fixture.
Original chronological snapshot: 516b131ff28722e1687f7bcdb6833cef3ddf851c (source tree preserved except this handoff). Previous consolidated parent: bfb4c918d7e2655899e114764f9b0a19266c1051. Original green development branch k5-log2-cleanup at fac2ec367028a97334d548c53d5930d768ec2694 remains untouched; historical handoffs remain there. Baseline 560235ca4e7029b6471f049c325849253be3eb65, tree-identical to RC3 e8770256ff6679546c483af43aed3f7a6b19b998. No intermediate fresh CI claimed. Next: continue original chronological checkpoints, not feature development. Final matched candidate needs fresh CI. No release authorized by consolidation alone.

## Ownership
STBT sole navigation publisher when installed; WCP integrated TBT off. WCP owns wireless and iAP2 identity; CPUL owns display/INFO/SETUP/type-130 dispatch. Preserve Apple/Google guidance, no speculative Waze fixes, HMG-API RC10+, IDs and keys. Update this handoff in every commit.
