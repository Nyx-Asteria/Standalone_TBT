package hmgapi.mods.projection;
/** AA maneuver meanings normalized to the shared geometry matcher, not raw enum casts. */
final class AaJunctionInput {
 final int type,junction,side,exit;
 final boolean hasExit;
 AaJunctionInput(int t,int j,int s,boolean h,int e){type=t;junction=j;side=s;hasExit=h;exit=e;}
 static AaJunctionInput from(int event,int side,int angle) {
  if(event==11||event==12||event==13) {
   if(angle<0||angle>360||(side!=1&&side!=2))return null;
   int e=(180-angle+360)%360;if(e>180)e-=360;
   return new AaJunctionInput(6,1,0,true,e);
  }
  int type,exit;
  switch(event) {
   case 14: case 10:return new AaJunctionInput(3,0,0,true,0);
   case 2:if(side==3)return new AaJunctionInput(3,0,0,true,0); // name change
   case 3: case 4: case 5:
    if(side!=1&&side!=2)return null;
    exit=event==3?30:event==5?135:90;if(side==1)exit=-exit;
    return new AaJunctionInput(side==1?1:2,0,0,false,exit);
   case 9:
    if(side!=1&&side!=2)return null;
    return new AaJunctionInput(side==1?1:2,0,0,false,0);
   case 7: case 8:
    if(side!=1&&side!=2)return null;
    type=event==7?(side==1?52:53):(side==1?22:23);
    return new AaJunctionInput(type,0,0,false,0);
   default:return null;
  }
 }
}
