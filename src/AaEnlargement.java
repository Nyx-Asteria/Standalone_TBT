package hmgapi.mods.projection.carplay;
import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import hmgapi.hook.ModContext;
import hmgapi.mods.projection.StockNavigationApi;
/** Android Auto uses the same target validation, distance policy and display latch as CarPlay. */
public final class AaEnlargement {
 private final Context context;private final ModContext mod;
 private final TollDisplay tollDisplay=new TollDisplay();
 private final JunctionTarget junctionTarget=new JunctionTarget();
 private final MapJunctionDisplay display=new MapJunctionDisplay();
 private String lastTrace;
 private boolean linkageFailed;
 private void trace(String message) {
  if(mod!=null && !message.equals(lastTrace)) {
   mod.log("Android Auto TBT enlargement "+message);lastTrace=message;
  }
 }
 private long published=-10000;private int target;private boolean leased;
 public AaEnlargement(Context c,ModContext m){context=c;mod=m;}
 public void clear(){tollDisplay.clear();junctionTarget.clear();display.clear();release();}
 private void release(){
  if(!leased)return;leased=false;
  try{
   String value=Settings.Global.getString(context.getContentResolver(),StockMapJunction.DETAILS);
   if(StockMapJunction.ownsRequest(value,target)) {
    Settings.Global.putLong(context.getContentResolver(),StockMapJunction.REQUEST,0);
    Settings.Global.putString(context.getContentResolver(),StockMapJunction.DETAILS,null);
   }
  }catch(RuntimeException ignored){}
 }
 public static int facility(int type,int junction,int exit,String road) {
  return FacilityGlyphs.road(type,junction,exit,road,null);
 }
 public float[] draw(String token,int type,int junction,int side,boolean hasExit,int exit,
         String road,float meters) {
  if(linkageFailed)return null;
  try{return drawChecked(token,type,junction,side,hasExit,exit,road,meters);}
  catch(RuntimeException | LinkageError error){linkageFailed=true;display.clear();
   try{release();}catch(RuntimeException | LinkageError ignored){}
   trace("state=disabled_linkage_error error="+error.getClass().getSimpleName());return null;}
 }
 private float[] drawChecked(String token,int type,int junction,int side,boolean hasExit,int exit,
         String road,float meters) {
  long now=SystemClock.elapsedRealtime();
  if(mod==null) {trace("state=disabled");clear();return null;}
  int facility=facility(type,junction,exit,road);
  if(facility==178 || facility==184 || facility==183) {
   display.clear();release();
   float[] toll=tollDisplay.draw(facility,meters,now);
   trace("state=facility code="+facility+" enlarged="+(toll!=null));return toll;
  }
  int limit=PhoneJunction.roadThreshold(type);
  if(limit<=0){trace("state=unsupported_threshold");clear();return null;}
  tollDisplay.clear();
  float[] ramp=RampEnlargement.draw(type,junction,hasExit,exit,meters,limit);
  if(ramp!=null){trace("state=factory_exit");display.clear();release();return ramp;}
  float[] grade=GradeEnlargement.draw(facility,meters,limit);
  if(grade!=null){trace("state=factory_grade");display.clear();release();return grade;}
  String stableToken=junctionTarget.select("AA/"+type+"/"+junction+"/"+side+"/"+hasExit+"/"+exit,now,meters);
  int id=StockMapJunction.targetId(stableToken);
  boolean wanted=StockMapJunction.requestedRange("Android Auto",meters,PhoneJunction.roadThreshold(type));
  if(wanted && (!leased||id!=target||now-published>=1000)) {
   try{
    boolean detailsSaved=Settings.Global.putString(context.getContentResolver(),StockMapJunction.DETAILS,now+","+meters+","+id+","+((junction==1||type==6||type==7)&&side==0?1:0));
    boolean leaseSaved=detailsSaved && Settings.Global.putLong(context.getContentResolver(),StockMapJunction.REQUEST,now+5000);
    if(!detailsSaved||!leaseSaved){trace("state=request_write_refused details="+detailsSaved+" lease="+leaseSaved);clear();return null;}
    target=id;published=now;leased=true;
   }catch(RuntimeException error){trace("state=request_error error="+error.getClass().getSimpleName());clear();return null;}
  }else if(!wanted)release();
  if(FacilityGlyphs.grade(road)) {
   trace("state=grade_or_tunnel_not_implemented");display.clear();return null;
  }
  try{
   StockNavigationApi.Position p=StockNavigationApi.currentPosition(context);
   String geometry=Settings.Global.getString(context.getContentResolver(),StockMapJunction.KEY);
   float[] frame=p==null?null:StockMapJunction.drawTarget(geometry,now,p.longitude,p.latitude,p.heading,
       "Android Auto",type,junction,hasExit,exit,meters,limit,id);
   String reason=p==null?"position-stale-or-missing":StockMapJunction.diagnostic();
   frame=display.apply(stableToken,StockMapJunction.node(),frame,now,meters,limit,
       p==null?Double.NaN:p.longitude,p==null?Double.NaN:p.latitude);
   trace("state="+display.state+" reason="+reason+" request="+leased+" geometry="+(geometry!=null)
       +" position="+(p!=null)+" threshold="+limit+" api="
       +Settings.Global.getString(context.getContentResolver(),"hmgapi_stock_navigation_status_v1"));
   JunctionMatchTrace.record(context,"AA/"+token,now,meters,reason,display.state,frame,p,geometry);
   if(frame==null){
    float[] round=RoundaboutEnlargement.draw("Android Auto",type,junction,side,hasExit,exit,null,meters,limit);
    if(round!=null){trace("state=rotary_direction_only reason="+reason);return round;}
   }
   return frame;
  }catch(RuntimeException error){trace("state=render_error error="+error.getClass().getSimpleName());display.clear();return null;}
 }
}
