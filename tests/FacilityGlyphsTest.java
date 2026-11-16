package hmgapi.mods.projection.carplay;
public final class FacilityGlyphsTest {
    static int checks;
    static void eq(int wanted, int actual) {
        checks++;
        if(wanted!=actual)throw new AssertionError(wanted+" != "+actual);
    }
    static void road(int wanted,int type,int exit,String text){eq(wanted,FacilityGlyphs.road(type,0,exit,text,""));}
    public static void main(String[] args) {
        road(84,50,0,"연미 지하차도 / 세종");
        road(82,49,0,"고가차도 옆길");
        road(87,2,0,"고가차도 진입");
        road(90,2,0,"지하차도 진입");
        road(184,3,0,"천안TG");road(184,3,0,"풍세TG");
        road(178,3,0,"무정차자동징수 요금소");road(65,3,0,"천안터널");
        road(0,3,0,"터널 진입 금지");
        eq(160,FacilityGlyphs.highway(9,0,0));eq(161,FacilityGlyphs.highway(53,0,0));
        eq(162,FacilityGlyphs.highway(52,0,0));eq(163,FacilityGlyphs.highway(8,0,0));
        eq(164,FacilityGlyphs.highway(23,0,0));eq(165,FacilityGlyphs.highway(22,0,0));
        eq(162,FacilityGlyphs.highway(9,0,-30));eq(164,FacilityGlyphs.highway(8,0,30));
        eq(0,FacilityGlyphs.highway(9,1,30));
        road(88,3,0,"고가 차도로 진입하세요");road(87,14,30,"고가차도 진입");
        road(91,3,0,"지하차도로 진입하세요");road(90,14,30,"지하차도 진입");
        road(81,14,30,"고가차도 옆 길");road(82,13,-30,"고가차도 옆길");
        road(84,14,30,"지하차도 옆길");road(85,13,-30,"지하 차도 측도");
        road(0,3,0,"지하차도 옆길");road(0,14,30,"지하차도 진입 금지");
        road(0,14,30,"지하차도로 진입하지 마세요");road(0,14,30,"고가차도 지하차도 진입");
        road(0,14,30,"지하차도 사거리");road(0,14,30,"오른쪽 방향");
        road(183,3,0,"정안알밤휴게소");road(183,14,30,"졸음쉼터");
        road(0,3,0,"휴게소 이후 요금소");road(0,1,-90,"휴게소 근처 사거리");
        eq(0,FacilityGlyphs.road(3,1,0,"지하차도 진입",null));
        road(88,3,0,"고가도로로 진입");road(91,3,0,"<masked>지하도로");
        road(81,14,30,"고가도로 옆길");road(85,13,-30,"지하도로 옆길");
        road(0,3,0,"지하도로 입구 사거리");road(0,3,0,"고가도로 방면");
        road(0,3,0,"지하도로로 진입하지 마세요");
        System.out.println("FacilityGlyphs: "+checks+" checks passed");
    }
}
