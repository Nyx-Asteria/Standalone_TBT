#include <assert.h>
#include <stdio.h>
#include "stock_graph_probe.c"
#include "stock_graph_resolver.c"
static int mode,lookups;
static char target;
void *dlsym(void *h,const char *name) {
 assert((uptr)h==(uptr)0xffffffffu);assert(name);lookups++;
 return mode==1 || (mode==5 && stock_same_string(name,"_ZN6mArrayI13stConnectLinkE5clearEv"))?0:(void *)(uptr)0x724513dd;
}
int dladdr(const void *p,struct stock_dl_info *out) {
 assert(p);
 out->file=mode==4?"libExSLNavi.so":mode==2?"/other/libExSLNavi.so":"/data/data/com.mnsoft.navi/BIN/libExSLNavi.so";
 return mode!=3;
}
int main(void) {
 struct api a;
 engine_range_count=2;engine_ranges[0]=0x70000000;engine_ranges[1]=0x73000000;
 assert(stock_graph_bind(&a,resolve)==0 && lookups==17);
 mode=4;assert(stock_graph_bind(&a,resolve)==0);
 mode=0;engine_ranges[0]+=0x10000000;engine_ranges[1]+=0x10000000;assert(stock_graph_bind(&a,resolve)==-1 && resolve_status==-22);
 engine_ranges[0]-=0x10000000;engine_ranges[1]-=0x10000000;
 mode=5;weak_cleanup_fallback=0;assert(stock_graph_bind(&a,resolve)==0 && weak_cleanup_fallback==1);
 assert((uptr)a.array_clear==(uptr)0x7246a76d);
 for(mode=1;mode<=3;mode++) {
  resolve_status=0;
  assert(stock_graph_bind(&a,resolve)==-1);
  assert(resolve_status==(mode==1?-21:-22));
 }
 puts("Loaded-symbol resolver: LP32 global handle, 17 owner-checked symbols, missing/wrong/unknown owner refused");
}
