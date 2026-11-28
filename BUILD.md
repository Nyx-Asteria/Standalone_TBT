# Building Standalone TBT

Requires JDK 11+ (Java 8 bytecode target), Android API 19 android.jar, Android SDK d8, zip, and the HMG-API source tree. Use the pinned Open-HMG/HMG-API RC10 source at daa083717f07565ba16dc8a4d26291b7d68d3bf4. The build expects its core/runtime/java and core/runtime/stubs directories. Upstream 1.0.5 release build checked against commit `9b57914818e93cf62887349b60dacbca83396bfe` (1.0.2 runtime).

```sh
export HMG_API=/path/to/HMG-API
export ANDROID_JAR=/path/to/android-19/android.jar
export D8=/path/to/android-sdk/build-tools/VERSION/d8
export SIGNING_KEY=/private/path/to/author.key
./build.sh
```

The standard update ZIP appears under dist/standalone-tbt-<version>.zip. The fork uses a distinct mod ID and must be signed with its own configured author key; remove upstream TBT before installation. Host CI has previously verified disposable-key fork builds; see current development branch CI for the exact candidate. The author key is not included. A differently signed build cannot replace the maintainer-signed installation without the appropriate uninstall/reinstall procedure. Do not change the installed mod identity to bypass signature checks.

## Native helper

native/prebuilt/libstockgraph.so is the project-owned, tested ARM32 JNI helper, not a stock navigation engine. Full source and link-only libdl stubs are in navigation-api/native. To rebuild with Zig and a JDK:

```sh
export JAVA_HOME=/path/to/jdk
export ZIG=/path/to/zig
sh navigation-api/native/build-helper.sh
```

The output is navigation-api/native/libstockgraph.so. The build uses the tested prebuilt helper because NativePayload pins its exact digest. Rebuilding with a different compiler can change the bytes; review the resulting ABI/imports and update the digest only after verification before replacing the prebuilt file. Never package the link-only libdl stub. The helper expects only dlsym and dladdr from the device libdl.so. Vehicle stock engines and firmware are not redistributed.

## Tests

Java regression tests are under tests/. Compile against the app classes, Android API 19, runtime and source stubs. Run each package-qualified test main; NativePayloadTest takes native/prebuilt/libstockgraph.so as its argument. CompoundLoaderTest expects classes/ and tests/ output directories as siblings. Python tests exercise callback replay and exporter behavior with temporary data. They use the development layout (mod/, build-105/classes, runtime/, stubs/, android.jar); map the public root configuration/scripts and prebuilt helper into mod/ when running them. No private vehicle logs are required for the default test cases.

## Installer

The standard ZIP is the signed HMG-API update. A NEMESIS application wrapper includes this intact ZIP and the reviewed install/uninstall scripts from installer/. Build it with `python3 tools/build_application_package.py dist/standalone-tbt-1.0.0-rc.1.zip dist/standalone-tbt-1.0.0-rc.1-nemesis-package.zip`. tools/build_usb_installer.py wraps that application package in a separate type=install USB descriptor. Do not put an application descriptor directly at the USB root.

All release ZIPs use stored entries for compatibility with the target ZIP reader. Verify the HMG-API signature and ZIP CRCs after packaging. Keep signing keys, raw captures and stock binaries out of source archives.

## Release procedure

Follow [RELEASE_PROCESS.md](RELEASE_PROCESS.md).
