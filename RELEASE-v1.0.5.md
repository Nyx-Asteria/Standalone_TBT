# Upstream Phone Projection TBT 1.0.5 — historical release notes

This document describes the original upstream release, not a signed Standalone TBT release.

## Changes

- Preserve valid AA turn icons and distances when directionless transient notices contain conflicting short distances.
- Correct external approach geometry and distance when a fully observed small junction selects an internal connection as its approach.
- Fall back to ordinary roundabout guidance rather than rendering an empty circle when branches are unavailable.
- Reapply toll display state even when name and distance bar are unchanged.
- Allow 12-hour, 24-hour or vehicle-system arrival-time formats.
- Remove the experimental CN7 PE cluster start-signal option and related code introduced in 1.0.4.

## Installation and validation

The original release required HMG-API >=1.0.2 and a checked map; it used phone-projection-tbt-1.0.5 ZIP filenames. For this fork's distinct package ID and installation instructions, see [Installation](wiki/Installation.md).

Upstream reported 39 Java and 10 Python tests, recorded AA transient/camera input and junction replays, D8 conversion, signature and ZIP validation. These results are not vehicle-wide validation and do not certify the fork. Leading-zero hour and distance decimal formatting remain unsupported. Proprietary engines, raw vehicle logs and private keys were excluded.

[Original release](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/tag/v1.0.5).
