package hmgapi.mods.projection.carplay;
/** Fixed entry distance with a small exit margin; labels cannot reset an active toll approach. */
final class TollDisplay {
 private int icon;private float previous;private long at;
 void clear(){icon=0;}
 float[] draw(int code,float meters,long now){
  boolean known=code==178||code==184;
  boolean valid=known&&!Float.isNaN(meters)&&!Float.isInfinite(meters)&&meters>=0;
  boolean hold=valid&&icon==code&&now>=at&&now-at<=10000&&meters<1100&&meters<=previous+50;
  if(!valid||(!hold&&meters>=1000)){clear();return null;}
  icon=code;previous=meters;at=now;
  float[] f=TollEnlargement.draw(code,Math.min(meters,999));if(f!=null)f[3]=meters;return f;
 }
}
