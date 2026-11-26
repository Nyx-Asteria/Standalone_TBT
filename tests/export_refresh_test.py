"""Current capture wins over old history; daemon IPC is forbidden; connected refresh completes."""
import os,pathlib,subprocess,tempfile
source=pathlib.Path(__file__).resolve().parents[1]/'export-logs.sh'
with tempfile.TemporaryDirectory(prefix='tbt-export-') as tmp:
 p=pathlib.Path(tmp);usb=p/'storage/usb0/TBT-LOGS';usb.mkdir(parents=True)
 data=p/'data';mod=data/'hmgapi/mods/standalone-tbt';mod.mkdir(parents=True)
 (mod/'mod.json').write_text('{"version":"1.0.0-rc.1"}')
 history=data/'hmgapi/logs/hmgapi';history.mkdir(parents=True)
 (history/'log.log.1').write_text('[standalone-tbt] OLD_058\n'*30000)
 (history/'log.log').write_text('[standalone-tbt] SAVED_RECENT\n')
 capture=data/'hmgapi/logs/phone-projection-tbt-capture';capture.mkdir()
 (capture/'capture.log').write_text('[standalone-tbt] Android Auto TBT CURRENT_085\n[ wired marker ]\n')
 (p/'uptime').write_text('1500 0\n');(p/'boot').write_text('current-boot\n')
 bindir=p/'bin';bindir.mkdir()
 commands={
 'getprop':'echo TEST', 'settings':f'echo BAD > "{p}/settings_called"; exit 99',
 'stat':'''python3 -c 'import os,sys;print(os.stat(sys.argv[-1]).st_size)' "$@"''',
 'hmgapid':f'echo BAD > "{p}/daemon_called"; exit 99',
 'logcat':f'''case " $* " in *" -d "*)
echo snapshot >> "{p}/snapshots"
echo '[standalone-tbt] Android Auto TBT CURRENT_083'
echo '[standalone-tbt] TBT transport app=com.google.android.projection.sink args=[112]'
;; esac''',
 'sleep':f'''[ "$1" = 2 ] || {{ /bin/sleep 1; exit 0; }}
n=$(cat "{p}/ticks" 2>/dev/null || echo 0)
n=$((n+1));echo "$n" > "{p}/ticks"
[ "$n" -lt 17 ] || kill -TERM "$PPID"
exit 0''',
 }
 for name,code in commands.items():
  f=bindir/name;f.write_text('#!/bin/sh\n'+code+'\n');f.chmod(0o755)
 script=source.read_text().replace('/storage/',str(p/'storage')+'/').replace('/data/',str(data)+'/').replace('/proc/uptime',str(p/'uptime')).replace('/proc/sys/kernel/random/boot_id',str(p/'boot'))
 f=p/'export.sh';f.write_text(script)
 result=subprocess.run(['/bin/sh',str(f)],env={**os.environ,'PATH':str(bindir)+':'+os.environ['PATH']},capture_output=True,text=True,timeout=15)
 assert result.returncode==0,result.stderr
 assert not (p/'daemon_called').exists()
 assert not (p/'settings_called').exists()
 assert result.stdout.count('Exported current TBT logs to')==2
 log=(usb/'phone-projection-tbt.log').read_text()
 assert all(x in log for x in ['collector_runtime=1.0.0-rc.1','CURRENT_085','current-boot','"version":"1.0.0-rc.1"'])
 assert 'OLD_058' not in log and log.endswith('finished=1\n')
 assert 'CURRENT_085' in (usb/'android-auto.log').read_text()
 assert (usb/'export-status.txt').read_text().endswith('phase=complete\nfinished=1\n')
 assert (usb/'stock-guidance.log').stat().st_size<2*1024*1024
 assert not list(usb.glob('*.part'))
 print('USB export: fresh-first, bounded old history, no daemon IPC, AA output, connected refresh and completed status passed')
