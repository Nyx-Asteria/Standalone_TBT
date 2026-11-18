# HMG Phone Projection TBT 1.0.5

[한국어](README.md) · [Download](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/tag/v1.0.5) · [Compatibility](COMPATIBILITY.md) · [Wiki](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki)

An HMG-API mod that forwards CarPlay and Android Auto navigation to Hyundai/Kia Gen5W instrument clusters and HUDs: current/upcoming turns, distances, road and destination names, and supplied lane guidance. Stock-map junction enlargement is an optional experimental feature.

## Changes since 1.0.0

- Keep real turn guidance when a directionless AA notice contains a conflicting short distance.
- Correct the external approach of a fully observed small junction with a short internal link.
- Fall back to ordinary rotary guidance when enlargement branches are missing.
- Reapply toll display state even when its bar level and name are unchanged.
- Select 12-hour, 24-hour or vehicle-system arrival-time formatting.
- Remove the 1.0.4 CN7 PE cluster start-handshake trial.

## Installation

Requires HMG-API >=1.0.2, a matching vehicle map, and `Hooks on (map checked)`.

- Existing installation: place `phone-projection-tbt-1.0.5.zip` unopened in USB `HMGAPI/updates/`, apply and restart.
- First installation: extract `phone-projection-tbt-1.0.5-usb-install.zip`; put `NEMESIS.data` and its inner application ZIP at the USB root. Do not extract the inner ZIP.
- NEMESIS application: `phone-projection-tbt-1.0.5-nemesis-package.zip`.
- Source/checksums: `phone-projection-tbt-1.0.5-source.zip` and `phone-projection-tbt-1.0.5-SHA256SUMS.txt`.

Disable duplicate TBT senders in other projection mods. See [installation](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Installation).

## Settings and limits

Settings cover AA guidance, optional junction enlargement, road/speed enlargement thresholds, experimental toll display and arrival-time formatting. Apply and restart after a setting change. Leading-zero hour selection (`H:MM` vs `HH:MM`) and hiding the trailing `.0` in 10–100km displays are not supported. Integer distance values are transmitted above 10km; the cluster controls text formatting.

39 Java and 10 Python suites, real-input replays, D8 conversion, signatures and ZIP validation passed. Host tests do not replace vehicle testing. See [compatibility](COMPATIBILITY.md) for model-specific reports, including K8 GL3 HEV AA cluster enlargement and HUD operation.

## Logs and source

Create `TBT-LOGS` at the USB root before reproducing an issue, then collect the entire generated folder with the app, connection type and symptom time. Check location information before sharing publicly. [Log guide](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki/Logs-and-Issue-Reporting).

AGPL-3.0. See [build instructions](BUILD.md) and [notices](NOTICE.txt). Proprietary engines, raw vehicle captures, firmware and signing keys are excluded. The project-owned JNI helper source is in `navigation-api/native`.
