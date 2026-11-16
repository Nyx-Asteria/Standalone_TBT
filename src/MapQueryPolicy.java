package hmgapi.mods.projection;
/** The parked diagnostic gate must not block explicitly requested live guidance. */
final class MapQueryPolicy {
 static boolean live(boolean enabled,long now,long until){return enabled && until>now && until-now<=6000;}
 static boolean diagnostic(float speed){return !Float.isNaN(speed)&&!Float.isInfinite(speed)&&speed==0f;}
}
