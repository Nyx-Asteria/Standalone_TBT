# Troubleshooting

## No cluster or HUD guidance

Confirm projection is connected, an actual route is active and the cluster navigation screen is selected. Check `Hooks on (map checked)`, installed TBT version, and ensure only one mod sends TBT. For NAVER CarPlay, verify its HUD integration option. HUD capabilities vary by vehicle.

If the vehicle map is missing, use **Map this car from its files**, then **Check the map on this car**. Do not substitute another vehicle map on failure.

## Connection or reattachment

Standalone TBT does not provide wireless connectivity. Diagnose WCP or stock projection separately. Keep WCP TBT disabled, but retain bridge and audio as needed. A wireless failure was reported upstream with Korea Projection and later resolved without a confirmed cause. Guidance recovery after bridge reattachment is a fork audit target.

## Missing turns, lanes or enlargement

Next turns require actual direction and distance from the phone. Recommended lanes depend on the app; pocket lanes require fresh compatible stock hints. Enlargement requires a verified engine and matching geometry. Missing branches trigger basic guidance. Unknown engine fingerprints must not be bypassed.

Special facility rendering is not universally verified: upstream codes 181/182 did not establish standalone service-area artwork; 183 showed a service-area exit diagram. Tunnel, overpass, underpass and toll distinctions require data.

## Native patch conflict

Disable other TBT senders. Upstream reported overlapping AA recipes with Korea Projection; disabling its display toggle may not resolve a native patch conflict. Collect versions and exact errors. Do not force patches or replace unverified libraries.

## Install/update failure

Remove original upstream TBT before first fork installation. Check the correct signed ZIP and USB path, apply changes and restart. Verify the installed signer for subsequent updates. See [installation](Installation.md).

If unresolved, collect the complete [TBT-LOGS directory](Logs-and-Issue-Reporting.md).
