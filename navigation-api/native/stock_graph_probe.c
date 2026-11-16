/* Private offline prototype for the exact 260902 engine dump.
 * Not loaded by the mod: ABI and engine-thread execution still require runtime validation.
 * Output contains current matched link and its immediate connections, not route targets.
 */
typedef unsigned int u32;
typedef unsigned short u16;
typedef unsigned char u8;
typedef unsigned long long u64;
typedef __UINTPTR_TYPE__ uptr;
typedef void *(*resolver)(const char *);
struct connection { u64 parcel, link; };
struct snapshot { u64 parcel,link; u32 direction,overpass,underpass,count; struct connection edges[32]; };
union storage { u64 align; u8 bytes[1024]; };
struct array { void *items; u32 count,capacity,grow; u16 module,pad; };
struct api {
 void *(*component_new)(u16); void (*component_delete)(u16,void *);
 void (*position_ctor)(void *); void (*position_dtor)(void *);
 void (*display_position)(void *,void *,u8);
 u64 (*parcel)(void *); u64 (*link)(void *); u32 (*direction)(void *);
 void *(*allocator_default)(void); void (*allocator_ctor)(void *,void *);
 void (*entity_ctor)(void *,const void *); void (*entity_dtor)(void *);
 int (*initialize)(void *,u64,u64,u16);
 int (*spawn)(void *,struct array *,u8); void (*array_clear)(struct array *);
 u32 (*overpass)(void *); u32 (*underpass)(void *);
};
static void zero(void *p,u32 n){u8 *b=p;while(n--)*b++=0;}
#define BIND(field,name) do { *(void **)(&a->field)=find(name); if(!a->field)return -1; } while(0)
int stock_graph_bind(struct api *a,resolver find) {
 if(!a||!find)return -1;zero(a,sizeof(*a));
 BIND(component_new,"_ZN17CLifecycleManager12NewComponentEt");
 BIND(component_delete,"_ZN17CLifecycleManager15DeleteComponentEtP10CComponent");
 BIND(position_ctor,"_ZN16CVehiclePositionC2Ev");
 BIND(position_dtor,"_ZN16CVehiclePositionD2Ev");
 BIND(display_position,"_ZN13CPositionInfo18GetDisplayPositionER16CVehiclePositionb");
 BIND(parcel,"_ZN16CVehiclePosition11GetParcelIdEv");
 BIND(link,"_ZN16CVehiclePosition9GetLinkIdEv");
 BIND(direction,"_ZN16CVehiclePosition12GetDirectionEv");
 BIND(allocator_default,"_ZN14mByteAllocator19GetDefaultAllocatorEv");
 BIND(allocator_ctor,"_ZN16mEntityAllocatorC2EP14mByteAllocator");
 BIND(entity_ctor,"_ZN11CEntityLinkC2ERK16mEntityAllocator");
 BIND(entity_dtor,"_ZN11CEntityLinkD2Ev");
 BIND(initialize,"_ZN11CEntityLink10InitializeEyyt");
 BIND(spawn,"_ZN11CEntityLink5SpawnEP6mArrayI13stConnectLinkEh");
 BIND(array_clear,"_ZN6mArrayI13stConnectLinkE5clearEv");
 BIND(overpass,"_ZN11CEntityLink15GetAttrOverPassEv");
 BIND(underpass,"_ZN11CEntityLink16GetAttrUnderPassEv");
 return 0;
}
/* Execute only inside the stock engine process, on a proven safe engine callback.
 * Caller must verify the exact libExSLNavi.so hash before binding or invoking.
 */
int stock_graph_current(struct api *a,struct snapshot *out) {
 if(!a||!out)return -1;
 zero(out,sizeof(*out));
 union storage position,allocator,entity;
 zero(&position,sizeof(position));zero(&allocator,sizeof(allocator));zero(&entity,sizeof(entity));
 a->position_ctor(&position);
 void *component=a->component_new(0x7200);
 if(!component){a->position_dtor(&position);return -2;}
 /* Same readiness check used by Navi::VI::CService::GetVehiclePosition. */
 void **vt=*(void ***)component;
 void *(*position_info)(void *)=(void *(*)(void *))vt[0x20/sizeof(void *)];
 void *info=position_info(component);
 if(!info) {
  a->component_delete(0x7200,component);a->position_dtor(&position);return -3;
 }
 a->display_position(info,&position,0);
 out->parcel=a->parcel(&position);out->link=a->link(&position);out->direction=a->direction(&position);
 a->component_delete(0x7200,component);a->position_dtor(&position);
 if(out->parcel==~(u64)0||out->link==~(u64)0)return -4;
 a->allocator_ctor(&allocator,a->allocator_default());
 a->entity_ctor(&entity,&allocator);
 if(a->initialize(&entity,out->parcel,out->link,0)<0){a->entity_dtor(&entity);return -5;}
 out->overpass=a->overpass(&entity);out->underpass=a->underpass(&entity);
 struct array edges;zero(&edges,sizeof(edges));edges.grow=16;edges.module=0x2100;
 a->spawn(&entity,&edges,(u8)out->direction);
 int status=0;
 if(edges.count>32 || (edges.count&&!edges.items))status=-6;
 else {
  out->count=edges.count;
  for(u32 i=0;i<edges.count;i++) {
   /* stConnectLink stride 0x58 confirmed from the engine's own Spawn caller. */
   u8 *entry=(u8 *)edges.items+i*0x58;
   out->edges[i].parcel=*(u64 *)(entry+0);
   out->edges[i].link=*(u64 *)(entry+8);
  }
 }
 a->array_clear(&edges);a->entity_dtor(&entity);
 if(status)zero(out,sizeof(*out));
 return status;
}
