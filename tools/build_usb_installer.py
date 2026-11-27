"""Wrap an application package with the separate NEMESIS USB install descriptor."""
from pathlib import Path
import argparse,configparser,io,json,re,zipfile

def build(package: Path, output: Path):
    body=package.read_bytes()
    if not re.fullmatch(r'[A-Za-z0-9._-]+\.zip',package.name):raise ValueError('unsafe USB package filename')
    with zipfile.ZipFile(io.BytesIO(body)) as z:
        if z.testzip() is not None:raise ValueError('corrupt NEMESIS package')
        cfg=configparser.ConfigParser(interpolation=None);cfg.read_string(z.read('NEMESIS.data').decode('utf-8'))
        if cfg['nemesis']['type']!='application' or not cfg['nemesis'].get('id'):raise ValueError('not an application package')
        for key in ('preinstall','postinstall','preuninstall'):
            if cfg['nemesis'][key] not in z.namelist():raise ValueError('missing installer script')
        with zipfile.ZipFile(io.BytesIO(z.read('mod/package.zip'))) as mod:
            if mod.testzip() is not None:raise ValueError('corrupt mod')
            info=json.loads(mod.read('mod.json'))
            if z.read('mod/id').decode().strip()!=info['id']:raise ValueError('mod ID mismatch')
            version=info['version']
    launcher=f'[nemesis]\nversion=1.0\ntype=install\npath={package.name}\n'
    readme=f'''Standalone TBT {version} - NEMESIS USB installation\n\n1. Install HMG-API 1.0.2 or newer and verify Hooks on (map checked).\n2. Extract only this outer USB installer ZIP.\n3. Place NEMESIS.data and {package.name} together at the USB root, not in a subfolder.\n4. Do not extract {package.name}.\n5. Connect the USB drive and confirm installation in NEMESIS.\n6. Restart the head unit.\n\nThe outer NEMESIS.data is type=install. The inner ZIP uses type=application and must not be extracted to the USB root.\n\nFor updates, place the signed update ZIP unopened in HMGAPI/updates/ and update through HMG-API. Remove the upstream Phone Projection TBT package before installing this fork. Disable WCP TBT to avoid duplicate senders.\n'''
    output.parent.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(output,'w',zipfile.ZIP_STORED) as z:
        z.writestr('NEMESIS.data',launcher)
        z.writestr(package.name,body)
        z.writestr('README-USB.txt',readme)
    with zipfile.ZipFile(output) as z:
        assert z.testzip() is None and z.read(package.name)==body
        assert all(i.compress_type==zipfile.ZIP_STORED for i in z.infolist())
    print(f'{output}: USB install descriptor + intact application ZIP, mod {version}')

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('package',type=Path);p.add_argument('output',type=Path);a=p.parse_args();build(a.package,a.output)
