# Logs and issue reporting

1. Create `TBT-LOGS` at the USB root, connect the drive and reproduce the symptom.
2. Collect the entire directory after export completes; include vehicle, full firmware, HMG-API/WCP/TBT versions, navigation app, wired/wireless transport and symptom time.

| File | Contents |
| --- | --- |
| phone-projection-tbt.log, stock-guidance.log | Installed manifest, capture metadata and general logs |
| export-status.txt | Export progress and secondary errors |
| carplay-input.log, carplay-tbt-output.log | CarPlay input and vehicle output |
| aa-input.log, aa-raw-packets.log, aa-tbt-output.log | AA input, raw packets and output |
| stock-cluster-input.log, stock-tbt-output.log | Stock display and warnings |
| aa-junction-match.log, junction-match.log, stock-road-graph.log | Geometry and matching diagnostics |
| api-result.txt | Stock road API initialization |

Some files may be absent depending on transport or active guidance. Rotated logs can include previous boots; finished=1 means export completed, not necessarily that navigation worked. Review location, road names and route details before sharing. Never post proprietary stock binaries or signing keys.

[Report an issue](https://github.com/Nyx-Asteria/Standalone_TBT/issues)
