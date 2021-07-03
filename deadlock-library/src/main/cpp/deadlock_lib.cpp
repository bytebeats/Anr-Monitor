/**
 *
 * Created by Pan Chen on 2021/7/3 : 15:22
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 *
 */

#include <jni.h>
#include <string>
#include "dl_open.h"

#include <android/log.h>

#define TAG "deadlock-lib"
#define Log_i(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

void *get_contended_monitor;
void *get_lock_owner_thread_id;

jint android_sdk_int;

const char *get_lock_owner_symbol_name(jint sdk_int);

extern "C"
JNIEXPORT jint

JNICALL
Java_me_bytebeats_deadlock_DeadLockMonitor_nativeInit(JNIEnv *env, jobject thiz, jint sdk_int) {
    android_sdk_int = sdk_int;
    //dlopen libart.so
    //init
    ndk_init(env);

    //load libart.so
    void *so_addr = ndk_dl_open("libart.so", RTLD_NOLOAD);
    if (so_addr == nullptr) return 1;
    // Monitor::GetContendedMonitor
    //c++方法地址跟c不一样，c++可以重载，方法描述符会变
    //http://androidxref.com/8.0.0_r4/xref/system/core/libbacktrace/testdata/arm/libart.so

    // nm xxx.so

    //获取Monitor::GetContendedMonitor函数符号地址
    get_contended_monitor = ndk_dl_symbol(so_addr,
                                          "_ZN3art7Monitor19GetContendedMonitorEPNS_6ThreadE");
    if (get_contended_monitor == nullptr) return 2;
    // Monitor::GetLockOwnerThreadId
    //这个函数是用来获取 Monitor的持有者,拥有monitor的是线程
    get_lock_owner_thread_id = ndk_dl_symbol(so_addr, get_lock_owner_symbol_name(android_sdk_int));
    if (get_lock_owner_thread_id == nullptr) return 3;
    return 0;
}

const char *get_lock_owner_symbol_name(jint
sdk_int) {
if (sdk_int <= 29) {
//pre android 9.0
//http://androidxref.com/9.0.0_r3/xref/system/core/libbacktrace/testdata/arm/libart.so 搜索 GetLockOwnerThreadId
return "_ZN3art7Monitor20GetLockOwnerThreadIdEPNS_6mirror6ObjectE";
} else {
//android 10.0+
//Sorry, androidxref/.../libart.so is no available, and in Google Git, I haven't found this so library.
return "_ZN3art7Monitor20GetLockOwnerThreadIdEPNS_6mirror6ObjectE";
}
}

extern "C"
JNIEXPORT jint

JNICALL
Java_me_bytebeats_deadlock_DeadLockMonitor_getContendedThreadIdArt(JNIEnv *env, jobject thiz,
                                                                   jlong thd_native_peer) {
    Log_i("getContendedThreadIdArt");
    int monitor_thread_id = 0;
    if (get_contended_monitor != nullptr && get_lock_owner_thread_id != nullptr) {
        Log_i("get_contended_monitor != null");
        //调用一下获取monitor的函数
        int monitorObj = ((int (*)(long)) get_contended_monitor)(thd_native_peer);
        if (monitorObj != 0) {
            Log_i("monitorObj != 0");
            // 获取这个monitor的持有者，返回一个线程id
            monitor_thread_id = ((int (*)(long)) get_lock_owner_thread_id)(monitorObj);
        } else {
            monitor_thread_id = 0;
        }
    } else {
        Log_i("get_contended_monitor == null || get_lock_owner_thread_id == null");
    }
    return monitor_thread_id;
}

extern "C"
JNIEXPORT jint

JNICALL
Java_me_bytebeats_deadlock_DeadLockMonitor_getThreadIdFromNativePeer(JNIEnv *env, jobject thiz,
                                                                     jlong thd_native_peer) {
    Log_i("getThreadIdFromNativePeer");
    if (thd_native_peer != 0) {
        Log_i("thread id != 0");
        if (android_sdk_int > 20) {//Android 5.0
            //reinterpret_cast 强制类型转换
            int *pInt = reinterpret_cast<int *>(thd_native_peer);
            //地址 +3，就是ThreadId，这个怎么来的呢？
            pInt = pInt + 3;
            return *pInt;//返回 monitor 所使用的Thread id
        }
    } else {
        Log_i("suspendThreadArt failed");
    }
    return 0;
}



