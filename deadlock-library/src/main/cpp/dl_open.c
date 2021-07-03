/**
 *
 * Created by Pan Chen on 2021/7/2 : 11:36
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 *
 */

#include "dl_open.h"
#include <stdlib.h>
#include <limits.h>
#include <sys/mman.h>
#include <sys/system_properties.h>
#include <android/log.h>

#define TAG "deadlock-dl-open"
#define UNSUPPORTED "Not Supported"
#define PAGE_SIZE 4096
#define Log_i(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

static volatile int SDK_INT = 0;
static void *quick_on_stack_back;

static union {
    void *stub;

    void *
    (*quick_on_stack_replace)(const void *param1, const void *param2, const void *fake_trampoline,
                              const void *called);
} STUBS;

void JNIEXPORT
ndk_init(JNIEnv
*env){
if(SDK_INT <= 0) {
char sdk[PROP_VALUE_MAX];
__system_property_get("ro.build.version.sdk", sdk);
SDK_INT = atoi(sdk);
if(SDK_INT >= 24) {
static __attribute__((__aligned__(PAGE_SIZE))) uint8_t __insns[PAGE_SIZE];
STUBS.
stub = __insns;
mprotect(__insns,
sizeof(__insns), PROT_READ | PROT_WRITE | PROT_EXEC);
// we are currently hijacking "FatalError" as a fake system-call trampoline
uintptr_t pv = (uintptr_t)(*env)->FatalError;
uintptr_t pu = (pv | (PAGE_SIZE - 1)) + 1u;
uintptr_t pd = pv & ~(PAGE_SIZE - 1);
mprotect((void *)pd, pv + 8u >= pu ? PAGE_SIZE * 2U : PAGE_SIZE, PROT_READ | PROT_WRITE | PROT_EXEC);
quick_on_stack_back = (void *) pv;
#if defined(__i386__)
/*
 DEFINE_FUNCTION art_quick_on_stack_replace
    movl  12(REG_VAR(esp)), REG_VAR(eax)
    movl  (REG_VAR(esp)), REG_VAR(edx)
    movl  REG_VAR(eax), (REG_VAR(esp))
    movl  REG_VAR(edx), 12(REG_VAR(esp))
    pushl 16(REG_VAR(esp))
    ret
 END_FUNCTION art_quick_on_stack_replace
*/
memcpy(__insns, "\x8B\x44\x24\x0C\x8B\x14\x24\x89\x04\x24\x89\x54\x24\x0C\xFF\x74\x24\x10\xC3", 19);
/*
  DEFINE_FUNCTION art_quick_on_stack_back
    push  8(REG_VAR(esp))
    ret
  END_FUNCTION art_quick_on_stack_back
*/
 memcpy(quick_on_stack_back, "\xC3\xFF\x74\x24\x08\xC3", 6);
 quick_on_stack_back = (void *)(pv + 1);// inserts `ret` at first
#elif defined(__x86_64__)
// rdi, rsi, rdx, rcx, r8, r9
/*
 0x0000000000000000:     52      push rdx
 0x0000000000000001:     52      push rdx
 0x0000000000000002:     FF E1   jmp rcx
*/
memcpy(__insns, "\x52\x52\xFF\xE1", 4);
/*
 0x0000000000000000:     5A      pop rdx
 0x0000000000000000:     C3      ret
*/
memcpy(quick_on_stack_back, "\x5A\xC3", 2);
#elif defined(__aarch64__)
// x0~x7
/*
 0x0000000000000000:     FD 7B BF A9     stp x29, x30, [sp, #-0x10]!
 0x0000000000000004:     FD 03 00 91     mov x29, sp
 0x0000000000000008:     FE 03 02 AA     mov x30, x2
 0x000000000000000C:     60 00 1F D6     br x3
*/
memcpy(__insns, "\xFD\x7B\xBF\xA9\xFD\x03\x00\x91\xFE\x03\x02\xAA\x60\x00\x1F\xD6", 16);
/*
 0x0000000000000000:     FD 7B C1 A8     ldp x29, x30, [sp], #0x10
 0x0000000000000004:     C0 03 5F D6     ret
*/
memcpy(quick_on_stack_back, "\xFD\x7B\xC1\xA8\xC0\x03\x5F\xD6", 8);
#elif defined(__arm__)
// r0~r3
/*
 0x0000000000000000:     08 E0 2D E5     str lr, [sp, #-8]!
 0x0000000000000004:     02 E0 A0 E1     mov lr, r2
 0x0000000000000008:     13 FF 2F E1     bx r3
*/
memcpy(__insns, "\x08\xE0\x2D\xE5\x02\xE0\xA0\xE1\x13\xFF\x2F\xE1", 12);
if((pv & 1u) != 0u) {//Thumb
    /*
     0x0000000000000000:     0C BC   pop {r2, r3}
     0x0000000000000002:     10 47   bx r2
    */
    memcpy((void *)(pv - 1), "\x0C\xBC\x10\x47", 4);
} else {
    /*
     0x0000000000000000:     0C 00 BD E8     pop {r2, r3}
     0x0000000000000004:     12 FF 2F E1     bx r2
    */
    memcpy(quick_on_stack_back, "\x0C\x00\xBD\xE8\x12\xFF\x2F\xE1", 8);
}
#else
# error UNSUPPORTED
#endif
Log_i("init done! quick_on_stack_replace = %p, quick_on_stack_back = %p", STUBS.stub,
      quick_on_stack_back);
}
}
}

void *JNIEXPORT

ndk_dl_open(const char *filename, int flag) {
    if (SDK_INT >= 24) {
#if defined(__i386__) || defined(__x86_64__) || defined(__aarch64__) || defined(__arm__)
        return STUBS.quick_on_stack_replace(filename, (void *)flag, quick_on_stack_back, dlopen);
#else
# error UNSUPPORTED
#endif
    }
    return dlopen(filename, flag);
}

int JNIEXPORT

ndk_dl_close(void *handle) {
    if (SDK_INT >= 24) {
#if defined(__i386__) || defined(__x86_64__) || defined(__aarch64__) || defined(__arm__)
        return (int)STUBS.quick_on_stack_replace(handle, NULL, quick_on_stack_back, dlclose);
#else
# error UNSUPPORTED
#endif
    }
    return dlclose(handle);
}

const char *JNIEXPORT

ndk_dl_error(void) {
    if (SDK_INT >= 24) {
#if defined(__i386__) || defined(__x86_64__) || defined(__aarch64__) || defined(__arm__)
        return STUBS.quick_on_stack_replace(NULL, NULL, quick_on_stack_back, dlerror);
#else
# error UNSUPPORTED
#endif
    }
    return dlerror();
}

void *JNIEXPORT

ndk_dl_symbol(void *handle, const char *symbol) {
    if (SDK_INT >= 24) {
#if defined(__i386__) || defined(__x86_64__) || defined(__aarch64__) || defined(__arm__)
        return STUBS.quick_on_stack_replace(handle, symbol, quick_on_stack_back, dlsym);
#else
# error UNSUPPORTED
#endif
    }
    return dlsym(handle, symbol);
}

int JNIEXPORT

ndk_dl_addr(const void *addr, Dl_info *info) {
    if (SDK_INT >= 24) {
#if defined(__i386__) || defined(__x86_64__) || defined(__aarch64__) || defined(__arm__)
        return (int)STUBS.quick_on_stack_replace(addr, info, quick_on_stack_back, dladdr);
#else
# error UNSUPPORTED
#endif
    }
    return dladdr(addr, info);
}


