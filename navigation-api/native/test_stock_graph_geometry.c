#include <assert.h>
#include <stdio.h>
#include "stock_graph_probe.c"
#include "stock_graph_geometry.c"
static int mode,freed,cleared;
static struct point_i32 coordinates[4]={{-100,20},{-80,40},{0,40},{10,40}};
static void *alloc(void){return coordinates;}
static void ac(void *a,void *b){assert(a && b);}
static void ec(void *e,const void *a){assert(e && a);}
static void ed(void *e){assert(e);freed++;}
static int init(void *e,u64 p,u64 l,u16 type){assert(e && p==123 && l==456 && type==0);return mode==1?1:0;}
static u32 over(void *e){assert(e);return 0;}
static u32 under(void *e){assert(e);return 1;}
static u32 length(void *e){assert(e);return 345;}
static int pos(void *e,struct array *a){assert(e && a->grow==16);a->items=coordinates;a->count=mode==3?5000:mode==4?1:4;return mode==2?-2:0;}
static void clear(struct array *a){assert(a);cleared++;}
int main(void) {
 struct api a;zero(&a,sizeof(a));a.allocator_default=alloc;a.allocator_ctor=ac;a.entity_ctor=ec;a.entity_dtor=ed;a.initialize=init;a.overpass=over;a.underpass=under;
 struct geometry_api g={pos,length,clear};struct geometry_row row;
 assert(stock_geometry_read(&a,&g,123,456,&row)==0);
 assert(row.overpass==0 && row.underpass==1);
 assert(row.point_count==4 && row.length==345 && row.points[0].x==-100 && row.points[3].x==10);
 for(mode=1;mode<=4;mode++)assert(stock_geometry_read(&a,&g,123,456,&row)!=0);
 assert(freed==5 && cleared==4);
 assert(stock_geometry_read(&a,&g,~(u64)0,456,&row)==-4 && freed==5);
 puts("Geometry mock: endpoints/order, raw coordinates, bounds, init/read failures and cleanup passed");
}
