"""Check Android ARM helper dependencies without loading ARM code on the host."""
from pathlib import Path
import struct,zipfile,sys
p=Path(__file__).resolve().parents[1]
b=(p/'mod/libstockgraph.so').read_bytes()
assert b[:6]==b'\x7fELF\x01\x01' and struct.unpack_from('<H',b,18)[0]==40
assert not (struct.unpack_from('<I',b,36)[0]&0x400), 'hard-float ABI'
off=struct.unpack_from('<I',b,32)[0];ent,num,ni=struct.unpack_from('<HHH',b,46)
sections=[struct.unpack_from('<10I',b,off+i*ent) for i in range(num)]
needed=[];imports=set();exports=set();tags=set()
for s in sections:
 if s[1] not in (6,11):continue
 t=sections[s[6]];strings=b[t[4]:t[4]+t[5]]
 def name(n):return strings[n:strings.index(0,n)].decode()
 if s[1]==6:
  for i in range(s[4],s[4]+s[5],8):
   tag,v=struct.unpack_from('<iI',b,i);tags.add(tag)
   if tag==1:needed.append(name(v))
 else:
  for i in range(s[4]+16,s[4]+s[5],16):
   n,v,size,info,o,idx=struct.unpack_from('<IIIBBH',b,i)
   if n:(imports if idx==0 else exports).add(name(n))
assert needed==['libdl.so'],needed
assert imports=={'dlsym','dladdr'},imports
assert 'Java_hmgapi_mods_projection_StockGraphNative_query' in exports
assert 4 in tags and 15 not in tags and 29 not in tags, 'SysV hash and no rpath'
if len(sys.argv)>1:
 with zipfile.ZipFile(sys.argv[1]) as z:
  assert z.read('libstockgraph.so')==b
  assert not any('libdl' in n or 'link-stubs' in n for n in z.namelist()),'stub must never ship'
print('ARM32 soft-float helper: real libdl dependency, exactly 2 imports, JNI export, SysV hash and no rpath passed')
