# HMG Phone Projection TBT 1.0.0

[한국어](README.md) · [Compatibility](COMPATIBILITY.md) · [Wiki](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/wiki) · [Releases](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/releases/latest)

HMG-API mod forwarding CarPlay and Android Auto navigation to Hyundai/Kia Gen5W clusters and HUDs. Supports current/next maneuvers, distance, road names, trip information and available lane data. Stock-map junction enlargement remains an optional experimental feature.

Requires HMG-API 1.0.2+, a matching vehicle map and `Hooks on (map checked)`. Disable duplicate TBT output in Korea Projection when using both mods.

For a first installation, extract the USB-install ZIP and place its NEMESIS.data and intact inner ZIP together at the USB root. For updates, place the intact standard update ZIP in HMGAPI/updates/. Restart the head unit after applying.

Version 1.0.0 includes subscription recovery, enlargement linkage-error containment, stable maneuver tracking across label/index changes, Korean AA ramp handling, immediate same-maneuver text updates, the restored 10-o'clock left-fork icon, and toll/ramp/roundabout rendering improvements. ETA requests use 24-hour time; distances of 10 km or more are sent as integral values. A cluster may still render a trailing .0.

See [release notes](RELEASE-v1.0.0.md) and [compatibility](COMPATIBILITY.md). Historical vehicle reports cover different feature subsets. Latest toll, off-ramp and roundabout fixes passed host replay tests and still require vehicle validation. Unknown engine hashes skip stock-map queries and retain basic TBT.

[Build instructions](BUILD.md). AGPL-3.0; see [notices](NOTICE.txt). Private logs, stock navigation engines, firmware and signing keys are excluded. JNI helper source is included under navigation-api/native.
