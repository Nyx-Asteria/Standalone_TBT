#include <jni.h>
#include "stock_graph_probe.c"
#include "stock_graph_resolver.c"
#include "stock_graph_geometry.c"
JNIEXPORT jlongArray JNICALL Java_hmgapi_mods_projection_StockGraphNative_query(JNIEnv *env,jclass cls,jlongArray ranges) {
 (void)cls;
 struct api a;struct snapshot s;zero(&s,sizeof(s));
 /* Do not start or reload the map engine. Read only an already loaded instance. */
 resolve_status=0;engine_range_count=0;engine_bias=0;weak_cleanup_fallback=0;missing_symbol=0;
 int status=-23;
 jsize size=ranges?(*env)->GetArrayLength(env,ranges):0;
 if(size>0 && size<=32 && (size&1)==0) {
  jlong input[32];(*env)->GetLongArrayRegion(env,ranges,0,size,input);
  if((*env)->ExceptionCheck(env))return 0;
  int valid=1;
  for(jsize i=0;i<size;i+=2) {
   if(input[i]<=0 || input[i+1]<=input[i] || (u64)input[i+1]>0x100000000ULL)valid=0;
   engine_ranges[i]=(u64)input[i];engine_ranges[i+1]=(u64)input[i+1];
  }
  if(valid){engine_range_count=(u32)size;status=stock_graph_bind(&a,resolve);}
 }
 if(status && resolve_status)status=resolve_status;
 if(status==0)status=stock_graph_current(&a,&s);
 jlong values[7+64];zero(values,sizeof(values));
 values[0]=status;
 if(status==0){values[1]=s.parcel;values[2]=s.link;values[3]=s.direction;values[4]=s.overpass;
  values[5]=s.underpass;values[6]=s.count;
  for(u32 i=0;i<s.count;i++){values[7+2*i]=s.edges[i].parcel;values[8+2*i]=s.edges[i].link;}}
 int count=status==0?7+2*s.count:1;
 jlongArray result=(*env)->NewLongArray(env,count);
 if(result)(*env)->SetLongArrayRegion(env,result,0,count,values);
 return result;
}

JNIEXPORT jstring JNICALL Java_hmgapi_mods_projection_StockGraphNative_resolutionDiagnostic(JNIEnv *env,jclass cls) {
 (void)cls;
 return (*env)->NewStringUTF(env,missing_symbol?missing_symbol:
   weak_cleanup_fallback?"weak array cleanup fallback used":"all symbols resolved without fallback");
}

JNIEXPORT jlongArray JNICALL Java_hmgapi_mods_projection_StockGraphNative_queryGeometry(JNIEnv *env,jclass cls,jlongArray road) {
 (void)cls;
 jlong input[71],values[5+9*15];zero(values,sizeof(values));values[0]=4;values[1]=-32;
 jsize size=road?(*env)->GetArrayLength(env,road):0;int rows=0;
 if(size>=7 && size<=71 && engine_range_count>0 && engine_bias) {
  (*env)->GetLongArrayRegion(env,road,0,size,input);if((*env)->ExceptionCheck(env))return 0;
  if(input[0]==0 && input[6]>=0 && input[6]<=32 && size==7+2*input[6]) {
   struct api a;struct geometry_api g;resolve_status=0;missing_symbol=0;
   int bound=stock_graph_bind(&a,resolve);
   if(bound==0)bound=stock_geometry_bind(&g,resolve);
   if(bound!=0)values[1]=resolve_status?resolve_status:-1;
   else {
    struct connection incident[8];u32 incident_count=0;
    values[1]=stock_geometry_incident(&a,&g,(u64)input[1],(u64)input[2],(u32)input[3],incident,&incident_count);
    if(values[1]==0){rows=1+(int)incident_count;values[2]=rows;values[3]=incident_count;values[4]=1;}

    for(int i=0;i<rows;i++) {
     struct geometry_row row;
     u64 parcel=(u64)(i==0?input[1]:(jlong)incident[i-1].parcel);
     u64 link=(u64)(i==0?input[2]:(jlong)incident[i-1].link);
     stock_geometry_read(&a,&g,parcel,link,&row);
     int at=5+i*15;values[at]=row.parcel;values[at+1]=row.link;values[at+2]=row.status;
     values[at+3]=row.length;values[at+4]=row.point_count;
     for(int j=0;j<4;j++){values[at+5+j*2]=row.points[j].x;values[at+6+j*2]=row.points[j].y;}
     values[at+13]=row.overpass;values[at+14]=row.underpass;
    }
   }
  }
 }
 int count=5+rows*15;jlongArray result=(*env)->NewLongArray(env,count);
 if(result)(*env)->SetLongArrayRegion(env,result,0,count,values);return result;
}
