package hmgapi.mods.projection;
/** Exact received engine variants. The second file equals the first minus eight trailing 0x08 bytes. */
final class EngineProfiles {
 static String identify(String sha256) {
  if("de162d7015504906105037ae306d1514655791bc04d5e8a5caed10ff476b73b3".equals(sha256))return "lx2-260902";
  if("9ac94499c66fb56f6acf6214c5cfb9abaf7b04396d06262376549f17506bbcd9".equals(sha256))return "dn8-dl3-260902";
  return null;
 }
}
