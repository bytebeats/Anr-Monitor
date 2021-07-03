/**
 *
 * Created by Pan Chen on 2021/7/2 : 11:36
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 *
 */
#pragma once

#include <jni.h>
#include <dlfcn.h>

#ifdef __cplusplus
extern "C" {
#endif

void ndk_init(JNIEnv *env);

void *ndk_dl_open(const char *filename, int flag);

int ndk_dl_close(void *handle);

const char *ndk_dl_error(void);

void *ndk_dl_symbol(void *handle, const char *symbol);

int ndk_dl_addr(const void *addr, Dl_info *info);

#ifdef __cplusplus
}
#endif
//#ifndef ANR_MONITOR_DL_OPEN_H
//#define ANR_MONITOR_DL_OPEN_H

//#endif //ANR_MONITOR_DL_OPEN_H
