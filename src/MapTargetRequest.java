package hmgapi.mods.projection;
final class MapTargetRequest {
 final float meters;final int id,kind;
 private MapTargetRequest(float m,int i,int k){meters=m;id=i;kind=k;}
 static MapTargetRequest decode(String value,long now) {
  if(value==null||value.length()>100)return null;
  try {
   String[] f=value.split(",");if(f.length!=3&&f.length!=4)return null;
   long age=now-Long.parseLong(f[0]);float m=Float.parseFloat(f[1]);int id=Integer.parseInt(f[2]);
   if(age<0||age>5000||Float.isNaN(m)||Float.isInfinite(m)||m<=0||m>1200||id==0)return null;
   int kind=f.length==4?Integer.parseInt(f[3]):0;if(kind<0||kind>1)return null;
   return new MapTargetRequest(m,id,kind);
  }catch(IllegalArgumentException e){return null;}
 }
}
