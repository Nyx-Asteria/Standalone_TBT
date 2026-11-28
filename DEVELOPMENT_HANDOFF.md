# STBT RC4 — Development handoff
Updated 2026-11-27. Consolidated checkpoint on rc4-final.

## Current milestone
RC4: harden generation-scoped durability and recorder health
Queue drop isolation, finalization capacity, atomic markers and interrupted budget protection, health and spool retry.
Original chronological snapshot: 99fa5a3b447eb3bcc950fa700f626064e72f1c1d (source tree preserved except this handoff). Previous consolidated parent: f2b328144e7e1368249aa3e335819c467d43e14d. Original green development branch k5-log2-cleanup at fac2ec367028a97334d548c53d5930d768ec2694 remains untouched; historical handoffs remain there. Baseline 560235ca4e7029b6471f049c325849253be3eb65, tree-identical to RC3 e8770256ff6679546c483af43aed3f7a6b19b998. No intermediate fresh CI claimed. Next: final RC4 release metadata and full regression gate. No release authorized by consolidation alone.

## Ownership
STBT sole navigation publisher when installed; WCP integrated TBT off. WCP owns wireless and iAP2 identity; CPUL owns display/INFO/SETUP/type-130 dispatch. Preserve Apple/Google guidance, no speculative Waze fixes, HMG-API RC10+, IDs and keys. Update this handoff in every commit.
