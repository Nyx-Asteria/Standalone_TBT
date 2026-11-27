# Installation and updates

Requires HMG-API 1.0.2 or newer and a matching vehicle map showing `Hooks on (map checked)`.

## First installation of this fork

1. Remove the original Phone Projection TBT (`wired-carplay-tbt-lx2pe`) through its installation manager, apply changes and restart. Do not install both.
2. Disable TBT in WCP or Korea Projection; apply and restart. Keep connectivity and audio features as needed.
3. Once a signed fork build is available, extract only the outer `standalone-tbt-<version>-usb-install.zip`. Put its NEMESIS.data and inner application ZIP together at the USB root.
4. Confirm installation in NEMESIS and restart. Check HMG-API hooks.

## Updates

Place the unopened `standalone-tbt-<version>.zip` in USB `HMGAPI/updates/`, check USB updates, apply and restart. Updates require the same fork signer. The NEMESIS application ZIP is not the outer USB installer. Never extract the inner ZIP.

All five settings are in English. Save, apply and restart after changing them. See [features](Features.md). No signed fork release has yet been verified.
