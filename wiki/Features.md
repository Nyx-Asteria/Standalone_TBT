# Features and settings

Standalone TBT forwards received navigation data to the vehicle's NavigationCluster interface. CarPlay and Android Auto have separate input paths, with common OEM-style display concepts.

| Feature | CarPlay | Android Auto |
| --- | --- | --- |
| Current turn, road name, maneuver distance | Implemented; upstream confirmed | Implemented; upstream confirmed |
| Upcoming maneuvers | Implemented, data-dependent | Implemented, further app testing needed |
| Trip distance and arrival time | Implemented | Implemented; remaining distance upstream confirmed |
| Lane geometry and recommended lanes | Implemented, app-dependent | Implemented, further testing needed |
| Cluster and HUD | Upstream examples confirmed | Cluster confirmed; HUD examples exist, vehicle-dependent |
| Stock-map enlargement | Experimental | Experimental |

## Guidance and display

Current turn output includes the received direction, icon, distance and road or junction text. CarPlay supports up to three received upcoming turns; AA supports next-turn metadata when supplied. The renderer never invents missing turn directions or distances. Slight turns, sharp turns, road-end turns and roundabouts have fallback mappings. Special facilities such as tunnels and toll plazas depend on received identifiers or text. The standalone service-area icon remains unresolved.

Lane arrows use gray for off-route directions, white for permissible directions and blue for recommended directions. Composite arrows retain per-direction state. Pocket markers require fresh compatible stock lane data matching count and directions. CarPlay includes a native lane fix guarded by library fingerprint. AA includes native receiver recipes and raw-packet handling for supplied lanes and upcoming turns; transient directionless notifications cannot replace a valid turn with a conflicting short distance.

Stock display protection leases preserve phone guidance while monitoring OEM output. Route termination clears owned display state. Korean text and remaining distance were confirmed upstream with NAVER and TMAP AA; this does not establish identical results for every app.

## Settings

| Setting | Default | Behavior |
| --- | --- | --- |
| Android Auto turn-by-turn | On | Enables AA receiver; CarPlay has its own path. |
| CarPlay / Android Auto junction enlargement | On | Experimental road geometry lookup; ordinary guidance if unavailable. |
| Junction enlargement start distance | Road | Local roads 400 m / highway ramps 1,050 m; alternatively speed-based 100/400/500 m. These are upper limits, not guarantees. |
| Toll plaza approach display | On | Experimental state and distance bar within 1 km for recognized toll maneuvers. |
| Estimated arrival time format | 24-hour | 12-hour, 24-hour or vehicle system format. |

All settings require restart. Unknown map engine hashes disable native stock-map queries, not basic guidance. Roundabouts require sufficient branches and received exit information. No geometry is fabricated. Above 10 km integer distance is transmitted; the cluster controls decimal text. Leading-zero formatting is not selectable.

See [compatibility](../COMPATIBILITY.md) and [known issues](Known-Issues.md).
