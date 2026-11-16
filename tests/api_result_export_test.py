"""Small exporter runs independently and refuses last-boot diagnostics."""
import os,pathlib,tempfile,subprocess
root=pathlib.Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory(prefix='tbt-api-') as temp:
 p=pathlib.Path(temp); (p/'usb0/TBT-LOGS').mkdir(parents=True);(p/'bin').mkdir()
 (p/'boot').write_text('current-boot\n')
 sleep=p/'bin/sleep';sleep.write_text('#!/bin/sh\nkill -TERM "$PPID"\n');sleep.chmod(0o755)
 trace=p/'trace';trace.write_text('0.1.76\tprivate replay row\n')
 aa_trace=p/'aa-trace';aa_trace.write_text('0.1.84\tAA/ private replay row\n')
 raw=p/'aa-raw';raw.write_text('1.0.0\tS 6 0a040a020824\n')
 source=p/'result';target=p/'usb0/TBT-LOGS/api-result.txt'
 script=(root/'mod/export-api-result.sh').read_text().replace('/data/hmgapi/logs/phone-projection-tbt-capture',str(p/'runtime')).replace('/storage/',str(p)+'/').replace('/proc/sys/kernel/random/boot_id',str(p/'boot')).replace('/data/data/com.mnsoft.navi/files/hmgapi-stock-road-api.txt',str(source)).replace('/data/data/com.daudio.app.dipo/files/hmgapi-junction-match.log',str(trace)).replace('/data/data/com.google.android.projection.sink/files/hmgapi-junction-match.log',str(aa_trace)).replace('/data/data/com.google.android.projection.sink/files/hmgapi-aa-input.log',str(aa_trace)).replace('/data/data/com.google.android.projection.sink/files/hmgapi-aa-raw-packets.log',str(raw)).replace('/data/data/com.google.android.projection.sink/files/hmgapi-tbt-output.log',str(aa_trace)).replace('/data/data/com.daudio.app.dipo/files/hmgapi-tbt-output.log',str(trace)).replace('/data/data/com.mnsoft.navi/files/hmgapi-tbt-output.log',str(trace)).replace('/data/data/com.mnsoft.navi/files/hmgapi-aa-input.log',str(trace)).replace('/data/data/com.mnsoft.navi/files/hmgapi-stock-road-graph.log',str(trace)).replace('/data/data/com.daudio.app.dipo/files/hmgapi-carplay-input.log',str(trace)).replace('/data/data/com.daudio.app.dipo/files/hmgapi-aa-input.log',str(trace))
 (p/'export.sh').write_text(script)
 for data,expected in [(None,'waiting_for_current_boot'),('boot=old-boot\nphase=complete\n','waiting_for_current_boot'),('boot=current-boot\nphase=complete\nroad=[0]\n','phase=complete')]:
  if data is not None:source.write_text(data)
  result=subprocess.run(['/bin/sh',str(p/'export.sh')],env={**os.environ,'PATH':str(p/'bin')+':'+os.environ['PATH']},capture_output=True,timeout=5)
  assert (target.parent/'junction-match.log').read_text()==trace.read_text()
  assert (target.parent/'aa-junction-match.log').read_text()==aa_trace.read_text()
  assert (target.parent/'aa-raw-packets.log').read_text()==raw.read_text()
  assert (target.parent/'aa-input.log').read_text()==aa_trace.read_text()
  assert (target.parent/'aa-tbt-output.log').read_text()==aa_trace.read_text()
  assert (target.parent/'carplay-tbt-output.log').read_text()==trace.read_text()
  assert (target.parent/'stock-tbt-output.log').read_text()==trace.read_text()
  assert (target.parent/'stock-cluster-input.log').read_text()==trace.read_text()
  assert (target.parent/'stock-road-graph.log').read_text()==trace.read_text()
  assert (target.parent/'carplay-cluster-input.log').read_text()==trace.read_text()
  assert (target.parent/'carplay-input.log').read_text()==trace.read_text()
  text=target.read_text();assert expected in text and 'old-boot' not in text
  assert not list(target.parent.glob('*.part'))
 print('Small API exporter: missing/old boot rejection, current result copy and atomic completion passed')
