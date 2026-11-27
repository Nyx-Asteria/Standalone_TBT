# Known issues and limitations

- Enlargement thresholds are upper bounds; shape and maneuver matching must succeed. Some T-junction approaches have missing or differing branches; traffic islands are not a confirmed cause.
- Roundabouts fall back to ordinary guidance when geometry is incomplete. Toll-state refresh and highway exit blue-path fixes need further vehicle retesting.
- AA left forks use the upstream 10 o'clock mapping (71/330), independently of ramp classification.
- Arrival-time format is selectable; leading zero and decimal formatting are controlled by vehicle UI. A straight HUD arrow can remain when the next turn is at least 3 km away.
- Service-area standalone artwork remains unresolved; special facility distinctions depend on received data.
- Upcoming turns, recommended lanes and pocket markers are app- and vehicle-dependent. Missing data is not invented.
- US K5 native map engine compatibility is unverified; basic TBT is separate.
- WCP TBT must be off. Wireless guidance reattachment improvements remain planned. The fork has not yet been car-tested.

See [compatibility](../COMPATIBILITY.md) and [log collection](Logs-and-Issue-Reporting.md).
