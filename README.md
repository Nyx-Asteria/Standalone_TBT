# Standalone TBT — 1.0.0-rc.1

Phone navigation on Hyundai/Kia Gen5W instrument clusters and HUDs, independent of the wireless connection mod. Based on [ch0iiih's Phone Projection TBT 1.0.5](https://github.com/ch0iiih/HMG_Phone_Projection_TBT), with English settings and a separate package identity.

[Features](wiki/Features.md) · [Installation](wiki/Installation.md) · [Compatibility](COMPATIBILITY.md) · [Troubleshooting](wiki/Troubleshooting.md) · [Build](BUILD.md) · [Releases](https://github.com/Nyx-Asteria/Standalone_TBT/releases)

## Features

- Wired and wireless Apple CarPlay and Android Auto guidance: current and upcoming maneuvers, road names, distance, destination distance, estimated arrival time and received lane guidance.
- OEM cluster and HUD output via NavigationCluster; availability depends on vehicle hardware and received data.
- Experimental stock-map intersection enlargement, including highway ramps and roundabouts when geometry is verified. Normal guidance remains the fallback.
- Road-type or speed-based enlargement distance thresholds, experimental toll plaza display, and 12/24-hour or vehicle arrival-time formatting.
- Composite lane arrows, per-direction recommendation colors and guarded stock pocket-lane hints.
- Android Auto transient-notification distance filtering and dedicated USB diagnostic exports.

## Installation and coexistence

Requires HMG-API >=1.0.2 and a checked vehicle hook map. Remove the original Phone Projection TBT package before installing this fork. Its new ID is `standalone-tbt`; the NEMESIS ID is `hmgapi.standalone-tbt`. This is a separate installation, not an update to the original signer or ID.

When using Wireless CarPlay + Android Auto (WCP), disable WCP's **Turn-by-turn on the cluster and HUD** setting, apply and restart. Keep the wireless bridge and audio enabled as needed. The original standalone implementation has been reported working in that configuration on the US K5; the renamed fork itself has not yet been build- or car-validated. Do not enable two navigation senders. This mod does not establish wireless connections.

See [installation](wiki/Installation.md) for package types. No signed fork release has yet been verified.

## Compatibility and development

Basic guidance does not require native stock-map queries. The latter are restricted to exact verified engine hashes; US K5 engine compatibility remains to be established. See [compatibility](COMPATIBILITY.md) and [development](wiki/Development.md).

Upstream 1.0.5 reported 39 Java and 10 Python tests, recorded-input replays, D8 conversion, signing and ZIP validation. These are upstream results, not results of this fork.

## Diagnostics and license

Create `TBT-LOGS` at the USB root before reproducing a problem. Collect the entire folder; review location and route information before sharing. See [log guide](wiki/Logs-and-Issue-Reporting.md).

AGPL-3.0; see [LICENSE](LICENSE) and [NOTICE.txt](NOTICE.txt). Original authorship and third-party notices are retained. Proprietary engines, firmware, private captures and signing keys are not distributed.

## RC1 update and testing

See [upgrade and vehicle-test instructions](UPDATE.md) and [RC1 release notes](RELEASE-v1.0.0-rc.1.md). Experimental Waze guidance recovery is disabled by default and requires restart to apply. Waze compatibility remains unverified.
