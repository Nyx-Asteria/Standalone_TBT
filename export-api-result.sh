#!/system/bin/sh
# Small private result; write failures must not flood the log or mark a failed copy complete.
umask 077
# Build-generated package version; unknown for old standalone test invocations.
collector_version=unknown
if [ -n "$HMGAPI_MOD_DIR" ] && [ -r "$HMGAPI_MOD_DIR/runtime-version.txt" ]; then
    collector_version=$(cat "$HMGAPI_MOD_DIR/runtime-version.txt")
fi
trace_folders=
trace_ticks=0
error_file=/data/hmgapi/logs/phone-projection-tbt-capture/api-local-write-error.txt
snapshot_dir=/data/hmgapi/logs/phone-projection-tbt-capture/api-snapshots
mkdir -p "$snapshot_dir" || exit 1
mkdir -p /data/hmgapi/logs/phone-projection-tbt-capture || exit 1
last_error=
copy_result() {
    if (cat "$1" > "$2.part" && [ -s "$2.part" ] && mv "$2.part" "$2") 2> "$error_file"; then
        return 0
    fi
    report_error
    return 1
}
report_error() {
    error=$(cat "$error_file" 2>/dev/null)
    if [ "$error" != "$last_error" ]; then
        echo "TBT API local snapshot failed: $error"
        last_error="$error"
    fi
}
while true; do
    boot_id=$(cat /proc/sys/kernel/random/boot_id 2>/dev/null)
    source=/data/data/com.mnsoft.navi/files/hmgapi-stock-road-api.txt
    current_folders=
    for folder in "$snapshot_dir"; do
        [ -d "$folder" ] || continue
        already_copied=0
        case " $trace_folders " in *" $folder "*) already_copied=1 ;; esac
        if [ "$already_copied" -eq 1 ] && [ "$trace_ticks" -ne 0 ]; then
            current_folders="$current_folders $folder"
        else
            copied=1
            for channel in carplay aa aa_input aa_raw cp_output aa_output stock_output stock_input cp_input cp_route stock_graph; do
                if [ "$channel" = stock_graph ]; then
                    trace_base=/data/data/com.mnsoft.navi/files/hmgapi-stock-road-graph.log
                    target_base="$folder/stock-road-graph.log"
                elif [ "$channel" = cp_route ]; then
                    trace_base=/data/data/com.daudio.app.dipo/files/hmgapi-carplay-input.log
                    target_base="$folder/carplay-input.log"
                elif [ "$channel" = stock_input ]; then
                    trace_base=/data/data/com.mnsoft.navi/files/hmgapi-aa-input.log
                    target_base="$folder/stock-cluster-input.log"
                elif [ "$channel" = cp_input ]; then
                    trace_base=/data/data/com.daudio.app.dipo/files/hmgapi-aa-input.log
                    target_base="$folder/carplay-cluster-input.log"
                elif [ "$channel" = cp_output ]; then
                    trace_base=/data/data/com.daudio.app.dipo/files/hmgapi-tbt-output.log
                    target_base="$folder/carplay-tbt-output.log"
                elif [ "$channel" = aa_output ]; then
                    trace_base=/data/data/com.google.android.projection.sink/files/hmgapi-tbt-output.log
                    target_base="$folder/aa-tbt-output.log"
                elif [ "$channel" = stock_output ]; then
                    trace_base=/data/data/com.mnsoft.navi/files/hmgapi-tbt-output.log
                    target_base="$folder/stock-tbt-output.log"
                elif [ "$channel" = aa_raw ]; then
                    trace_base=/data/data/com.google.android.projection.sink/files/hmgapi-aa-raw-packets.log
                    target_base="$folder/aa-raw-packets.log"
                elif [ "$channel" = aa_input ]; then
                    trace_base=/data/data/com.google.android.projection.sink/files/hmgapi-aa-input.log
                    target_base="$folder/aa-input.log"
                elif [ "$channel" = aa ]; then
                    trace_base=/data/data/com.google.android.projection.sink/files/hmgapi-junction-match.log
                    target_base="$folder/aa-junction-match.log"
                else
                    trace_base=/data/data/com.daudio.app.dipo/files/hmgapi-junction-match.log
                    target_base="$folder/junction-match.log"
                fi
                for suffix in .1 ''; do
                    trace_source="$trace_base$suffix"
                    trace_target="$target_base$suffix"
                    if [ -s "$trace_source" ] && ! cmp "$trace_source" "$trace_target" >/dev/null 2>&1; then
                        copy_result "$trace_source" "$trace_target" || copied=0
                    fi
                done
            done
            [ "$copied" -eq 0 ] || current_folders="$current_folders $folder"
        fi
        target="$folder/api-result.txt"
        tmp="$folder/api-result.txt.part"
        if [ -n "$boot_id" ] && [ -s "$source" ] && grep -Fx "boot=$boot_id" "$source" >/dev/null; then
            cmp "$source" "$target" >/dev/null 2>&1 && continue
            copy_result "$source" "$target" || :
        else
            # Render locally first; publication failures are handled by copy_result.
            local_result=/data/hmgapi/logs/phone-projection-tbt-capture/api-waiting.txt
            {
                echo "collector=stock-road-api-$collector_version"
                echo "boot=$boot_id"
                echo 'phase=waiting_for_current_boot_stock_process'
            } > "$local_result"
            cmp "$local_result" "$target" >/dev/null 2>&1 || copy_result "$local_result" "$target" || :
        fi
    done
    trace_folders="$current_folders"
    trace_ticks=$(( (trace_ticks + 1) % 6 ))
    sleep 5
done
