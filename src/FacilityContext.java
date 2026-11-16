package hmgapi.mods.projection.carplay;
/** Only an explicit fee/fuel label can reuse the last positively identified facility. */
public final class FacilityContext {
 private String hint;private int kind;private long seen;private float last;
 public void clear(){hint=null;}
 public boolean recent(long now){return hint!=null&&now>=seen&&now-seen<=10000;}
 public static boolean auxiliary(String value){
  if(value==null)return false;String s=value.replaceAll("\\s+","");
  return s.matches("[₩$€¥]?[0-9][0-9,]*(?:\\.[0-9]+)?(?:원|KRW|USD|EUR|JPY|달러|엔)(?:/L|/ℓ)?")
      ||s.matches("(?:휘발유|경유|LPG|고급휘발유)[:：]?[₩]?[0-9][0-9,]*(?:원)?(?:/L|/ℓ)?");
 }
 public String resolve(int type,int junction,int exit,String description,String road,float meters,long now){
  int code=FacilityGlyphs.road(type,junction,exit,description,road);
  if(code==178||code==184||code==183){
   hint=(description==null?"":description)+" "+(road==null?"":road);kind=type;seen=now;last=meters;return hint;
  }
  boolean valid=hint!=null&&junction==0&&type==kind&&now>=seen&&now-seen<=10000
      &&!Float.isNaN(meters)&&!Float.isInfinite(meters)&&meters>=0&&meters<=last+30;
  if(valid&&(auxiliary(road)||auxiliary(description))){last=meters;return hint;}
  clear();return (description==null?"":description)+" "+(road==null?"":road);
 }
}
