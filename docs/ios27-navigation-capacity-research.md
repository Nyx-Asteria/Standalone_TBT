# iOS 27.0.1 navigation capacity research

Source: iPhone17,1, build 24A446, ARM64e incremental research archive. Disassembled extracted CarKitNavigation using llvm-objdump --disassemble (the Mach-O-specific mode rejects a reconstructed symbol library ordinal).

## Verified instructions

`-[ACCNavigationAccessoryComponent(CarPlay) CP_maxCapacity_GuidanceManeuver]`, 0x22b03d604:
```
22b03d618 bl 0x230040a10
22b03d61c cbz x0, 0x22b03d640
22b03d63c b 0x230040a10
22b03d640 mov w0, #0x3e8
```

`-[ACCNavigationAccessoryComponent(CarPlay) CP_maxCapacity_LaneGuidance]`, 0x22b0400f8:
```
22b04010c bl 0x230040a20
22b040110 cbz x0, 0x22b040134
22b040130 b 0x230040a20
22b040134 mov w0, #0x3e8
```

Both wrappers test a returned value for zero, return 1000 if zero, and otherwise tail-call the same external target. Objective-C reference inventory includes maxCapacity_GuidanceManeuver and maxCapacity_LaneGuidance. The exact external target mapping is not yet resolved; interpreting these as the raw capacity getters is a hypothesis consistent with method names.

## Implications and limits

- Nonzero restrictive capacity values appear to be preserved, not expanded by these wrappers.
- No evidence yet that capacity is used to determine whether a source supports guidance.
- Prior vehicle observations: Google Maps provides guidance, Waze reports explicit false and no maneuver packet. Do not override the source support flag or fabricate data.
- Static receiver initialization values are not proof of actual negotiated advertisement. Capture/verify serialized component values first.
- Do not advertise zero or 1000 maneuvers as an experiment: actual receiver storage capability has not been established.
- Any future optional experiment must stay within verified receiver capacity and preserve the working identity and Google Maps behavior.

No receiver runtime change is made by this research note. No additional IPSW extraction is requested.

## Follow-up: capacity users and unsupported-source construction

CarKitNavigation disassembly, same build:

- CRAccNavComponent configDescription at 0x22b040678 calls 0x230040a10 at 0x22b040974 and 0x230040a20 at 0x22b0409ac. The corresponding format-string addresses 0x2740f2fd8 and 0x2740f2ff8 resolve in the supplied strings inventory to maxCapacity_GuidanceManeuver and maxCapacity_LaneGuidance.
- CRAccNavController initWithComponent:delegate: at 0x22b040ba4 calls the same targets at 0x22b040cb8 and 0x22b040cf4, passes each return value as x2 to 0x230040970, and stores resulting objects at offsets 0x20 and 0x28. This is consistent with capacity-backed storage initialization; the constructor target is unresolved.
- CRAccNavRouteGuidance loadingForComponent:sourceName:sourceSupported: at 0x22b03fda4 preserves x4 (support argument), passes source name with item 19 at 0x22b03fe00–08 and the converted support argument with item 20 at 0x22b03fe2c–34 to the same setter target 0x230040b50.
- CRAccNavProvider sendNoSupportForAppIdentifier: at 0x22b042134 has a block at 0x22b042224 which constructs items 19 and 20 at 0x22b0422b0–c8 before passing the result onward. Item 20 uses a global object; its precise value is unresolved in the reconstructed image. Method name and field mapping are consistent with an explicit unsupported-source announcement.

These are not resolved Objective-C dynamic call graphs. No evidence yet connects capacity to eligibility. Next prioritize the caller of sendNoSupportForAppIdentifier: and application metadata decision, not a receiver capacity override. No new runtime behavior.
