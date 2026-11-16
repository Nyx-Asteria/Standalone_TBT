/* Read-only endpoint geometry, exact engine only. Raw coordinate units are not
 * interpreted here. No route search or cluster output is performed. */
struct point_i32 { int x,y; };
struct geometry_row { u64 parcel,link; int status; u32 length,point_count,overpass,underpass; struct point_i32 points[4]; };
struct geometry_api {
 int (*position)(void *,struct array *);
 u32 (*length)(void *);
 void (*points_clear)(struct array *);
 u64 (*node_id)(void *,u8);
 void (*node_ctor)(void *,const void *);void (*node_dtor)(void *);
 int (*node_init)(void *,u64,u64,u32,u16);
 u32 (*node_adjacent)(void *);u32 (*node_count)(void *);
 u64 (*node_map)(void *);u64 (*node_link)(void *,int);
};
int stock_geometry_bind(struct geometry_api *g,resolver find) {
 zero(g,sizeof(*g));
 *(void **)(&g->position)=find("_ZN11CEntityLink11GetPositionEP6mArrayI9mLonLat_tIiEE");
 *(void **)(&g->length)=find("_ZN11CEntityLink9GetLengthEv");
 *(void **)(&g->points_clear)=find("_ZN6mArrayI9mLonLat_tIiEE5clearEv");
 *(void **)(&g->node_id)=find("_ZN11CEntityLink9GetNodeIdEb");
 *(void **)(&g->node_ctor)=find("_ZN11CEntityNodeC2ERK16mEntityAllocator");
 *(void **)(&g->node_dtor)=find("_ZN11CEntityNodeD2Ev");
 *(void **)(&g->node_init)=find("_ZN11CEntityNode10InitializeEyyjt");
 *(void **)(&g->node_adjacent)=find("_ZN11CEntityNode13GetAttrAdjestEv");
 *(void **)(&g->node_count)=find("_ZN11CEntityNode12GetLinkCountEv");
 *(void **)(&g->node_map)=find("_ZN11CEntityNode11GetConMapIdEv");
 *(void **)(&g->node_link)=find("_ZN11CEntityNode12GetConLinkIdEi");
 return g->position && g->length && g->points_clear && g->node_id && g->node_ctor && g->node_dtor
     && g->node_init && g->node_adjacent && g->node_count && g->node_map && g->node_link?0:-1;
}
int stock_geometry_read(struct api *a,struct geometry_api *g,u64 parcel,u64 link,struct geometry_row *out) {
 zero(out,sizeof(*out));out->parcel=parcel;out->link=link;
 if(parcel==~(u64)0 || link==~(u64)0)return out->status=-4;
 union storage allocator,entity;zero(&allocator,sizeof(allocator));zero(&entity,sizeof(entity));
 a->allocator_ctor(&allocator,a->allocator_default());a->entity_ctor(&entity,&allocator);
 if(a->initialize(&entity,parcel,link,0)!=0) {a->entity_dtor(&entity);return out->status=-5;}
 out->length=g->length(&entity);
 out->overpass=a->overpass(&entity);out->underpass=a->underpass(&entity);
 struct array points;zero(&points,sizeof(points));points.grow=16;points.module=0x2100;
 int status=g->position(&entity,&points);
 if(status!=0)out->status=-30;
 else if(!points.items || points.count<2 || points.count>4096)out->status=-31;
 else {
  out->point_count=points.count;
  struct point_i32 *p=(struct point_i32 *)points.items;
  out->points[0]=p[0];out->points[1]=p[1];
  out->points[2]=p[points.count-2];out->points[3]=p[points.count-1];
 }
 g->points_clear(&points);a->entity_dtor(&entity);return out->status;
}

/* Full incident roads, not just passable Spawn entries. Adjacent-parcel joins
 * are refused until their merge semantics are separately validated. */
int stock_geometry_incident(struct api *a,struct geometry_api *g,u64 parcel,u64 link,u32 direction,
        struct connection *roads,u32 *count) {
 *count=0;
 if(direction>1 || parcel==~(u64)0 || link==~(u64)0)return -34;
 union storage allocator,entity,node;
 zero(&allocator,sizeof(allocator));zero(&entity,sizeof(entity));zero(&node,sizeof(node));
 a->allocator_ctor(&allocator,a->allocator_default());a->entity_ctor(&entity,&allocator);
 if(a->initialize(&entity,parcel,link,0)!=0){a->entity_dtor(&entity);return -5;}
 u64 node_id=g->node_id(&entity,(u8)direction);
 g->node_ctor(&node,&allocator);
 int status=-35;
 if(node_id!=~(u64)0 && g->node_init(&node,parcel,node_id,0,0)==0) {
  if(g->node_adjacent(&node))status=-36;
  else {
   u32 n=g->node_count(&node);u64 map=g->node_map(&node);int found=0;
   if(n<1 || n>8 || map==~(u64)0)status=-37;
   else {
    for(u32 i=0;i<n;i++) {
     roads[i].parcel=map;roads[i].link=g->node_link(&node,(int)i);
     if(roads[i].link==~(u64)0)break;
     if(map==parcel && roads[i].link==link)found=1;
     (*count)++;
    }
    status=*count==n && found?0:-38;
   }
  }
 }
 g->node_dtor(&node);a->entity_dtor(&entity);if(status)*count=0;return status;
}
