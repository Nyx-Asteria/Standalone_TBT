package hmgapi.mods.projection.carplay;
/** A transport unsubscribe is not an explicit phone route-end notification. */
final class CpSubscriptionRecovery {
 private long stopped=-1,lastAttempt=-10000;
 boolean begin(long now,boolean visible,long lastGood) {
  if(!visible||now<lastGood||now-lastGood>10000)return false;
  if(stopped<0)stopped=now;
  return true;
 }
 boolean resume(long now) {
  if(stopped<0||now<stopped||now-stopped<250||now-lastAttempt<10000)return false;
  lastAttempt=now;return true;
 }
 long remaining(long now){return stopped<0?0:Math.max(0,5000-(now-stopped));}
 boolean pending(){return stopped>=0;}
 boolean expired(long now){return stopped>=0&&now>=stopped&&now-stopped>=5000;}
 void received(){stopped=-1;}
 void clear(){stopped=-1;}
}
