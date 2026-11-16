package hmgapi.mods.projection;
public final class EngineProfilesTest {
 public static void main(String[] args) {
  String original="de162d7015504906105037ae306d1514655791bc04d5e8a5caed10ff476b73b3";
  String variant="9ac94499c66fb56f6acf6214c5cfb9abaf7b04396d06262376549f17506bbcd9";
  if(!"lx2-260902".equals(EngineProfiles.identify(original)))throw new AssertionError();
  if(!"dn8-dl3-260902".equals(EngineProfiles.identify(variant)))throw new AssertionError();
  for(String invalid:new String[]{null,"",variant.substring(1),variant+"0",variant.replace('9','8'),"0000000000000000000000000000000000000000000000000000000000000000"})
   if(EngineProfiles.identify(invalid)!=null)throw new AssertionError("Unknown engine accepted");
  System.out.println("Engine profiles: verified LX2 and identical DN8/DL3 variants accepted; missing/unknown/modified hashes refused");
 }
}
