#!/system/bin/sh
umask 077
history_dir=/data/hmgapi/logs/phone-projection-tbt-capture
capture_pid=
mkdir -p "$history_dir" || exit 1
stage="$history_dir/export.$$"
mkdir -p "$stage" || exit 1
logcat -v threadtime -f "$history_dir/capture.log" -r 2048 -n 5 -s HMG-API:I '*:S' &
capture_pid=$!
cleanup() {
    [ -z "$capture_pid" ] || kill "$capture_pid" 2>/dev/null
    [ -z "$reader" ] || kill "$reader" 2>/dev/null
    [ -z "$watchdog" ] || kill "$watchdog" 2>/dev/null
    rm -rf "$stage"
}
trap cleanup EXIT
trap 'exit 0' HUP INT TERM
# Snapshot a fixed number of blocks. Never chase EOF on a file that is still growing.
recent_file() {
    [ -r "$1" ] || return 0
    size=$(stat -c %s "$1" 2>/dev/null)
    case "$size" in ''|*[!0-9]*)
        tail -c 262144 "$1" 2>/dev/null | dd bs=4096 count=64 2>/dev/null || :
        return 0
    ;; esac
    blocks=$(( (size + 4095) / 4096 ))
    first=$(( blocks - 64 )); [ "$first" -ge 0 ] || first=0
    count=$(( blocks - first ))
    [ "$count" -gt 0 ] || return 0
    dd if="$1" bs=4096 skip="$first" count="$count" 2>/dev/null || :
}
publish() {
    # Never move files between filesystems; copy first, then rename on the USB itself.
    if (cat "$1" > "$2.part" && mv "$2.part" "$2") 2> "$stage/write-error"; then
        return 0
    fi
    error=$(cat "$stage/write-error")
    if [ "$error" != "$last_write_error" ]; then
        echo "TBT USB export failed: $error"
        last_write_error="$error"
    fi
    return 1
}
last_folders=
refresh_ticks=0
while true; do
    folders=
    for folder in /storage/usb[012]/TBT-LOGS; do
        [ -d "$folder" ] || continue
        folders="$folders $folder"
    done
    for folder in $folders; do
        case "$folder" in /storage/usb[012]/TBT-LOGS) ;; *) continue ;; esac
        case " $last_folders " in *" $folder "*) [ "$refresh_ticks" -eq 0 ] || continue ;; esac
        {
            echo 'collector_runtime=1.0.0'
            echo "captured=$(date)"
            echo 'phase=collecting_current_logs'
        } > "$stage/status"
        publish "$stage/status" "$folder/export-status.txt" || continue
        recent_file "$history_dir/capture.log" > "$stage/current.raw"
        {
            echo 'capture_method=bounded_file_no_binder_or_logcat_wait'
            if kill -0 "$capture_pid" 2>/dev/null; then echo 'capture_process=running'; else echo 'capture_process=not_running'; fi
        } > "$stage/capture-status"
        : > "$stage/logcat-error.txt"
        {
            echo "build=$(getprop ro.build.product)"
            echo "captured=$(date)"
            echo 'collector_runtime=1.0.0'
            echo "boot_id=$(cat /proc/sys/kernel/random/boot_id 2>/dev/null)"
            echo "uptime=$(cat /proc/uptime 2>/dev/null)"
            cat "$stage/capture-status" "$stage/logcat-error.txt"
            echo '### Installed mod manifest'
            cat /data/hmgapi/mods/wired-carplay-tbt-lx2pe/mod.json 2>/dev/null || :
            echo '### Stock API runtime status'
            echo 'api_status=see_api-result.txt'
            echo '### HMG-API service state'
            recent_file /data/hmgapi/state/services.txt
            echo '### Current logcat snapshot (bounded)'
            recent_file "$stage/current.raw" | grep -e '\[wired-carplay-tbt-lx2pe\]' -e 'cluster: .*failed' -e 'cluster: no NavigationCluster' || :
            echo '### Recent capture snapshot (fixed-size, current file first)'
            recent_file "$history_dir/capture.log" | grep -F '[wired-carplay-tbt-lx2pe]' || :
            echo '### Recent HMG-API saved snapshot (fixed-size)'
            recent_file /data/hmgapi/logs/hmgapi/log.log | grep -F '[wired-carplay-tbt-lx2pe]' || :
            echo 'finished=1'
        } > "$stage/phone-projection-tbt.log"
        if ! publish "$stage/phone-projection-tbt.log" "$folder/phone-projection-tbt.log"; then
            echo 'phase=primary_write_failed' >> "$stage/status"
            publish "$stage/status" "$folder/export-status.txt"
            continue
        fi
        # The current session is already available even if a later history copy fails.
        {
            cat "$stage/phone-projection-tbt.log"
            echo '### Bounded previous capture files (recent first; historical, not proof of current hooks)'
            for history in "$history_dir"/capture.log.1 "$history_dir"/capture.log.2 \
                    "$history_dir"/capture.log.3 "$history_dir"/capture.log.4 "$history_dir"/capture.log.5 \
                    /data/hmgapi/logs/hmgapi/log.log.1; do
                echo "source=$history"
                recent_file "$history" | grep -F '[wired-carplay-tbt-lx2pe]' || :
            done
            echo 'finished=1'
        } > "$stage/stock-guidance.log"
        {
            echo 'collector=android-auto-1.0.0'
            grep -E 'captured=|Android Auto TBT|Android Auto turn-by-turn|TBT transport app=com.google.android.projection.sink|TBT transport app=com.daudio.app.gpm' "$stage/stock-guidance.log" || :
            echo 'finished=1'
        } > "$stage/android-auto.log"
        {
            echo 'collector=junction-capture-1.0.0'
            grep -E 'captured=|Stock graph probe|Stock navigation API|Phone junction|Junction trial|Junction capture|TBT transport|Cluster trace|guidance source|received route| turn index=|maneuver input' "$stage/stock-guidance.log" || :
            echo 'finished=1'
        } > "$stage/junction-capture.log"
        failed=0
        for name in stock-guidance.log android-auto.log junction-capture.log; do
            publish "$stage/$name" "$folder/$name" || failed=1
        done
        {
            cat "$stage/status"
            echo "secondary_write_failed=$failed"
            if [ "$failed" -eq 0 ]; then echo 'phase=complete'; else echo 'phase=complete_with_secondary_errors'; fi
            echo 'finished=1'
        } > "$stage/status-final"
        publish "$stage/status-final" "$folder/export-status.txt"
        # Superseded old exporter temporary files are removed only after current log succeeds.
        rm -f "$folder/.phone-projection-tbt.log.part" "$folder/.stock-guidance.log.part" \
            "$folder/.android-auto.log.part" "$folder/.junction-capture.log.part"
        echo "Exported current TBT logs to $folder"
    done
    last_folders="$folders"
    refresh_ticks=$(( (refresh_ticks + 1) % 15 ))
    sleep 2
done
