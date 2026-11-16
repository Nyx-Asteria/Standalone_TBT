"""Combine reviewed NEMESIS scripts with a signed update ZIP."""
from pathlib import Path
import argparse,zipfile,json,configparser
p=argparse.ArgumentParser();p.add_argument('update',type=Path);p.add_argument('output',type=Path);a=p.parse_args()
r=Path(__file__).resolve().parents[1]/'installer'
with zipfile.ZipFile(a.update) as z:
 assert z.testzip() is None
 info=json.loads(z.read('mod.json'))
assert (r/'mod/id').read_text().strip()==info['id']
config=configparser.ConfigParser(interpolation=None);config.read(r/'NEMESIS.data');assert config['nemesis']['package-version']==info['version']
with zipfile.ZipFile(a.output,'w',zipfile.ZIP_STORED) as z:
 for f in sorted(r.rglob('*')):
  if f.is_file():z.write(f,f.relative_to(r))
 z.write(a.update,'mod/package.zip')
with zipfile.ZipFile(a.output) as z:assert z.testzip() is None
print(a.output)
