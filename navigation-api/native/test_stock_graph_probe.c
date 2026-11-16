#include <assert.h>
#include <stdio.h>
#include "stock_graph_probe.c"
static int freed,position_freed,entity_freed,arrays_freed,mode;
static int component_value,info_value;
static void *vtable[32];
static void *component[2];
static unsigned char edge_data[2*0x58];
static void *new_component(u16 id){assert(id==0x7200);return mode==1?0:component;}
static void free_component(u16 id,void *c){assert(id==0x7200 && c==component);freed++;}
static void *get_info(void *c){assert(c==component);return mode==2?0:&info_value;}
static void ctor(void *p){zero(p,1024);}
static void pdtor(void *p){assert(p);position_freed++;}
static void display(void *info,void *p,u8 raw){assert(info==&info_value && info!=component && raw==0);(void)p;}
static u64 parcel(void *p){(void)p;return mode==3?~(u64)0:123;}
static u64 link(void *p){(void)p;return 456;}
static u32 direction(void *p){(void)p;return 1;}
static void *allocator(void){return &component_value;}
static void allocator_ctor(void *a,void *b){assert(a && b==&component_value);}
static void entity_ctor(void *e,const void *a){assert(e && a);}
static void edtor(void *e){assert(e);entity_freed++;}
static int init(void *e,u64 p,u64 l,u16 t){assert(e && p==123 && l==456 && t==0);return mode==4?-1:0;}
static u32 attr(void *e){assert(e);return 0;}
static int spawn(void *e,struct array *a,u8 d){assert(e && d==1 && a->grow==16 && a->module==0x2100);a->items=edge_data;a->count=mode==5?33:2;return 0;}
static void clear(struct array *a){assert(a);arrays_freed++;}
int main(void){
 component[0]=vtable;vtable[0x20/sizeof(void*)]=get_info;
 *(u64 *)(edge_data+0)=1;*(u64 *)(edge_data+8)=2;
 *(u64 *)(edge_data+0x58)=3;*(u64 *)(edge_data+0x60)=4;
 struct api a={new_component,free_component,ctor,pdtor,display,parcel,link,direction,allocator,allocator_ctor,entity_ctor,edtor,init,spawn,clear,attr,attr};
 struct snapshot out;
 assert(stock_graph_current(&a,&out)==0 && out.count==2 && out.edges[1].link==4);
 assert(freed==1 && position_freed==1 && entity_freed==1 && arrays_freed==1);
 for(mode=1;mode<=5;mode++)assert(stock_graph_current(&a,&out)<0);
 assert(freed==5 && position_freed==6 && entity_freed==3 && arrays_freed==2);
 puts("native probe mock: component/info separation, ID/edge copying, cleanup and bounded output passed");
}
