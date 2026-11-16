/* Link-time ABI stub ONLY. Never deploy. Android provides the real libdl.so. */
void *dlopen(const char *path,int flags){(void)path;(void)flags;return (void *)0;}
void *dlsym(void *handle,const char *symbol){(void)handle;(void)symbol;return (void *)0;}
int dlclose(void *handle){(void)handle;return 0;}

int dladdr(const void *p,void *info){(void)p;(void)info;return 0;}
