#!/bin/sh
set -eu
base=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project=$(CDPATH= cd -- "$base/../.." && pwd)
: "${JAVA_HOME:?Set JAVA_HOME to a JDK directory}"
zig=${ZIG:-zig}
build="$project/navigation-api/native/link-stubs/build"
mkdir -p "$build"
export ZIG_GLOBAL_CACHE_DIR=/tmp/tbt-zig-global
export ZIG_LOCAL_CACHE_DIR=/tmp/tbt-zig-local
"$zig" cc -target arm-linux-musleabi -mcpu=cortex_a9 -mthumb -mfloat-abi=softfp -Oz -g0 -s -fno-unwind-tables -fno-asynchronous-unwind-tables -ffreestanding -fno-builtin -fPIC -nostdlib -shared -Wl,-soname,libdl.so -Wl,--hash-style=sysv "$base/link-stubs/libdl.c" -o "$build/libdl.so"
jdk=$JAVA_HOME
case "$(uname -s)" in Darwin) jni_os=darwin;; Linux) jni_os=linux;; *) echo "Unsupported JNI host" >&2; exit 1;; esac
"$zig" cc -target arm-linux-musleabi -mcpu=cortex_a9 -mthumb -mfloat-abi=softfp -Oz -g0 -s -fno-unwind-tables -fno-asynchronous-unwind-tables -ffreestanding -fno-builtin -fPIC -nostdlib -shared -Wl,-soname,libstockgraph.so -Wl,--hash-style=sysv -Wl,-z,max-page-size=4096 -Wl,-z,defs -I "$base/include" -I "$jdk/include" -I "$jdk/include/$jni_os" "$base/stock_graph_jni.c" -Wl,--no-as-needed "$build/libdl.so" -o "$base/libstockgraph.so"
