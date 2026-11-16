#include <assert.h>
#include <stdio.h>
#include "stock_graph_probe.c"
#include "stock_graph_geometry.c"
static int mode,entities,nodes;
static int dummy;
static void *alloc(void){return &dummy;}
static void ac(void *a,void *b){assert(a&&b);}
static void ec(void *e,const void *a){assert(e&&a);}
static void ed(void *e){assert(e);entities++;}
static int init(void *e,u64 p,u64 l,u16 t){assert(e&&p==123&&l==456&&t==0);return mode==5?1:0;}
static u64 node_id(void *e,u8 d){assert(e&&d==1);return 789;}
static void nd(void *n){assert(n);nodes++;}
static int ni(void *n,u64 p,u64 id,u32 j,u16 t){assert(n&&p==123&&id==789&&j==0&&t==0);return mode==4?1:0;}
static u32 adjacent(void *n){assert(n);return mode==1;}
static u32 count(void *n){assert(n);return mode==2?9:4;}
static u64 map(void *n){assert(n);return 123;}
static u64 link(void *n,int i){assert(n);return (mode==3?999:456)+i;}
int main(void){
 struct api a;zero(&a,sizeof(a));a.allocator_default=alloc;a.allocator_ctor=ac;a.entity_ctor=ec;a.entity_dtor=ed;a.initialize=init;
 struct geometry_api g;zero(&g,sizeof(g));g.node_id=node_id;g.node_ctor=ec;g.node_dtor=nd;g.node_init=ni;g.node_adjacent=adjacent;g.node_count=count;g.node_map=map;g.node_link=link;
 struct connection roads[8];u32 n;
 assert(stock_geometry_incident(&a,&g,123,456,1,roads,&n)==0&&n==4&&roads[3].link==459);
 for(mode=1;mode<=5;mode++)assert(stock_geometry_incident(&a,&g,123,456,1,roads,&n)<0&&n==0);
 assert(entities==6&&nodes==5);
 assert(stock_geometry_incident(&a,&g,123,456,2,roads,&n)==-34);
 puts("Physical node: all4 links, adjacent/oversize/wrong-node/init/direction refusal and cleanup passed");
}
