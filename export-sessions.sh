#!/system/bin/sh
# Copy only finalized journals; leave originals available until bounded local retention expires.
umask 077
source_dir=/data/data/com.daudio.app.dipo/files/stbt-carplay-sessions
focused_dir=/data/data/com.daudio.app.dipo/files/stbt-third-party-navigation
usb_name=TBT-LOGS
while true; do
    for root in /storage/usb0 /storage/usb1 /storage/usb2; do
        [ -d "$root" ] || continue
        # Require a mounted volume, not an empty internal mountpoint.
        canonical=$(cd "$root" 2>/dev/null && pwd -P)
        [ -n "$canonical" ] || continue
        grep -F " $root " /proc/mounts >/dev/null 2>&1 ||
            grep -F " $canonical " /proc/mounts >/dev/null 2>&1 || continue
        for kind in sessions third-party-navigation; do
        case "$kind" in
            sessions) from="$source_dir";;
            third-party-navigation) from="$focused_dir";;
        esac
        [ -d "$from" ] || continue
        dest="$root/$usb_name/$kind"
        [ ! -L "$root/$usb_name" ] && [ ! -L "$dest" ] || continue
        mkdir -p "$dest" || continue
        for src in "$from"/session-*.log; do
            [ -f "$src" ] && [ ! -L "$src" ] || continue
            name=${src##*/}
            [ ! -L "$dest/$name" ] && [ ! -L "$dest/$name.part" ] || continue
            [ -f "$dest/$name" ] && cmp "$src" "$dest/$name" >/dev/null 2>&1 && continue
            if cat "$src" > "$dest/$name.part" && cmp "$src" "$dest/$name.part" >/dev/null 2>&1; then
                mv "$dest/$name.part" "$dest/$name" || rm -f "$dest/$name.part"
            else
                rm -f "$dest/$name.part"
            fi
        done
        done
    done
    sleep 5
done
