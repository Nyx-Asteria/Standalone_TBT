/* Android LP32 RTLD_DEFAULT ABI. No dlopen: only inspect loaded symbols.
 * Every pointer must belong to the already hash-verified stock engine path.
 */
struct stock_dl_info { const char *file; void *base; const char *name; void *address; };
extern void *dlsym(void *,const char *);
extern int dladdr(const void *,struct stock_dl_info *);
static int resolve_status;
static uptr engine_bias;
static int weak_cleanup_fallback;
static const char *missing_symbol;
static u64 engine_ranges[32];
static u32 engine_range_count;
static int stock_same_string(const char *a,const char *b) {
 if(!a||!b)return 0;while(*a && *a==*b){a++;b++;}return *a==*b;
}
static void *resolve(const char *name) {
 void *p=dlsym((void *)(uptr)0xffffffffu,name);
 /* Only this exact SHA-pinned weak template symbol has a fixed-offset fallback.
  * Anchor is a global symbol from the same owner-checked engine, never a map guess. */
 if(!p && engine_bias && stock_same_string(name,"_ZN6mArrayI13stConnectLinkE5clearEv")) {
  p=(void *)(engine_bias+(uptr)0x246a76d);weak_cleanup_fallback=1;
 }
 if(!p && engine_bias && stock_same_string(name,"_ZN6mArrayI9mLonLat_tIiEE5clearEv"))
  p=(void *)(engine_bias+(uptr)0x22138e1);
 if(!p){resolve_status=-21;missing_symbol=name;return 0;}
 struct stock_dl_info info;zero(&info,sizeof(info));
 u64 address=(u64)(uptr)p & ~(u64)1;
 int mapped=0;
 for(u32 i=0;i<engine_range_count;i+=2)
  if(address>=engine_ranges[i] && address<engine_ranges[i+1])mapped=1;
 if(!dladdr(p,&info) || !mapped ||
    !(stock_same_string(info.file,"/data/data/com.mnsoft.navi/BIN/libExSLNavi.so") ||
      stock_same_string(info.file,"libExSLNavi.so"))) {
  resolve_status=-22;return 0;
 }
 if(stock_same_string(name,"_ZN17CLifecycleManager12NewComponentEt")) {
  uptr pointer=(uptr)p;
  if(pointer>=(uptr)0x24513dd && ((pointer-(uptr)0x24513dd)&4095)==0)
   engine_bias=pointer-(uptr)0x24513dd;
 }
 return p;
}
