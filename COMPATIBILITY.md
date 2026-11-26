# Compatibility

Standalone TBT has no installer model or region restriction. It requires Hyundai/Kia Gen5W, HMG-API >=1.0.2 and a checked vehicle hook map. Basic guidance and native stock-map enlargement have separate compatibility requirements.

## Fork validation

The maintainer reports that the original standalone TBT works alongside WCP with WCP TBT disabled on the US K5. This establishes coexistence in that configuration, not full validation of the renamed fork or every display mode. US K5 engine hash/ABI verification, fork build validation and detailed Apple Maps/Google Maps testing remain pending.

## Upstream vehicle reports

These reports are inherited from Phone Projection TBT, not newly verified with this fork. They span earlier versions; the upstream 1.0.5 release did not establish every feature on every vehicle.

| Vehicle | Firmware and reported behavior | Remaining validation |
| --- | --- | --- |
| 2023 Palisade LX2 PE (development vehicle) | LX2PE.KOR.0000.V039.001.260902; CarPlay and AA NAVER/TMAP basic guidance, Korean text, remaining distance and termination; enlargement reported working | Latest enlargement fixes |
| 2022 Palisade | Wired CarPlay TMAP basic guidance and general enlargement report; firmware unknown | HUD, special geometry, other apps |
| 2023 Stinger Meister 3.3 | CK_PE_22.KOR.S5W_M.V039.001.260902; wired/wireless CarPlay NAVER cluster and HUD; enlargement reported | AA and specific geometry/driving conditions |
| 2021 K7 Premier 2.5 | YGPE.KOR.0000.V039.001.26.09.02 (reported spelling); wireless CarPlay NAVER 6.12.3 basic cluster guidance; no HUD | Enlargement pending, other apps and AA |
| 2022 K5 DL3 1.6T | DL322.KOR.0000.V039.001.260902; AA NAVER basic guidance and enlargement; engine collected and compared | Latest camera-distance fix and special geometry |
| 2022 Sonata DN8 HEV | DN822HEV.KOR.0000.V039.001.260902; AA NAVER cluster/HUD enlargement and distance bar | Geometry, timing, toll and ramp fixes |
| 2023 K8 GL3 HEV | GL3HEV.KOR.0000.V039.001.260902; v1.0.0 wired AA NAVER/TMAP; photos show cluster enlargement/bar and HUD direction/distance/name | Intermittent refresh, CarPlay and latest changes |

Source: [upstream compatibility report](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/blob/daeaf32a6fa3c3540c74c93611dad7dedb6d417e/COMPATIBILITY.md). See also [K7 report](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/issues/4) and [K8 report](https://github.com/ch0iiih/HMG_Phone_Projection_TBT/issues/5).

Kakao CarPlay/AA, upcoming turns, recommended lanes and special facility artwork require additional app-specific tests.

## Stock map engine allowlist

The code checks SHA-256 of /data/data/com.mnsoft.navi/BIN/libExSLNavi.so before native queries. Unknown hashes retain ordinary guidance.

| Profile | Bytes | SHA-256 |
| --- | --- | --- |
| LX2 PE | 73,071,824 | `de162d7015504906105037ae306d1514655791bc04d5e8a5caed10ff476b73b3` |
| DN8 HEV / K5 DL3 / CK PE | 73,071,816 | `9ac94499c66fb56f6acf6214c5cfb9abaf7b04396d06262376549f17506bbcd9` |

Upstream reports that the three second-profile captures are identical and differ from the LX2 PE file only in the last eight bytes. Exact hash matching does not establish every display behavior. Do not add a US engine hash without ABI and symbol verification. Proprietary engines are not distributed.

## Known limitations

See [Known Issues](wiki/Known-Issues.md). Report vehicle, year, complete firmware, app, transport, versions and symptom time. Review location data before sharing logs.
