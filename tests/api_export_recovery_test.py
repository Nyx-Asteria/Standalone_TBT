"""Failed USB creation is logged once and retried after the path becomes writable."""
from pathlib import Path
import os,subprocess,tempfile
r=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory() as tmp:
 p=Path(tmp);usb=p/'storage/usb0/TBT-LOGS';usb.mkdir(parents=True)
 (usb/'api-result.txt.part').mkdir() # reliably rejects creation, even when running as root
 (p/'boot').write_text('this-boot\n');(p/'source').write_text('boot=this-boot\nphase=complete\n')
 bin=p/'bin';bin.mkdir()
 sleep=bin/'sleep';sleep.write_text(f'''#!/bin/sh
n=$(cat "{p}/ticks" 2>/dev/null || echo 0)
n=$((n+1));echo "$n" > "{p}/ticks"
[ "$n" != 2 ] || rmdir "{usb}/api-result.txt.part"
[ "$n" -lt 4 ] || kill -TERM "$PPID"
''');sleep.chmod(0o755)
 script=(r/'mod/export-api-result.sh').read_text().replace('/storage/',str(p/'storage')+'/').replace('/data/hmgapi/logs/phone-projection-tbt-capture',str(p/'runtime')).replace('/proc/sys/kernel/random/boot_id',str(p/'boot')).replace('/data/data/com.mnsoft.navi/files/hmgapi-stock-road-api.txt',str(p/'source'))
 f=p/'export.sh';f.write_text(script)
 q=subprocess.run(['/bin/sh',str(f)],env={**os.environ,'PATH':str(bin)+':'+os.environ['PATH']},capture_output=True,text=True,timeout=10)
 assert q.stdout.count('TBT API USB write failed:')==1,q.stdout
 assert not q.stderr,q.stderr
 assert (usb/'api-result.txt').read_text()==(p/'source').read_text()
 assert not list(usb.glob('*.part'))
 print('API USB write failure: one diagnostic, no stderr flood, successful retry after recovery passed')
