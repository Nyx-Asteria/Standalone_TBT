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
    readme=f'''Phone Projection TBT {version} - NEMESIS USB 설치\n\n1. HMG-API 1.0.2 이상을 먼저 설치하고 Hooks on (map checked)를 확인하세요.\n2. 이 USB 설치 묶음만 압축 해제하세요.\n3. NEMESIS.data와 {package.name}을 USB 최상위에 함께 넣으세요.\n   두 파일이 별도 하위 폴더 안에 들어가면 안 됩니다.\n4. 안쪽 {package.name}은 압축 해제하지 마세요.\n5. USB를 연결하고 NEMESIS 설치 확인창에서 설치를 진행하세요.\n6. 완료 후 헤드유닛을 재시작하세요.\n\nNEMESIS.data는 USB 실행용(type=install)입니다.\n안쪽 ZIP의 NEMESIS.data(type=application)는 패키지 관리용이며 USB 루트용이 아닙니다.\n\n이미 TBT가 설치되어 있다면 일반 업데이트 ZIP을 압축 해제 없이\nHMGAPI/updates/에 넣고 HMG-API에서 업데이트하세요.\n'''
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
