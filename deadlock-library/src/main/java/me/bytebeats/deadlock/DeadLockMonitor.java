package me.bytebeats.deadlock;

import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Pan Chen on 2021/7/2 : 11:24
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
public class DeadLockMonitor {

    public static boolean logEnabled = true;
    private static final String TAG = "deadlock-java";

    static {
        System.loadLibrary("deadlock-lib");
    }

    private DeadLockListener listener;

    public DeadLockMonitor setDeadLockListener(DeadLockListener listener) {
        this.listener = listener;
        return this;
    }


    private static String threadName(Thread thread) {
        return thread.getName() + " (state = " + thread.getState().toString() + ")";
    }

    Thread[] allThreads() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        if (group == null) {
            return null;
        }
        while (group.getParent() != null) {
            group = group.getParent();
        }
        int threadCount = group.activeCount();
        Thread[] allThreads = new Thread[threadCount];
        group.enumerate(allThreads);
        return allThreads;
    }

    public void start() {
        if (VERSION.SDK_INT > VERSION_CODES.Q) {
            log("DeadLockMonitor won't work on Android 10, Sooooooorrrrrryyyyyyyy");
            return;
        }
        int initResult = nativeInit(VERSION.SDK_INT);
        log("native init result: " + initResult);
        Map<Integer, DeadLockThreadWrapper> blockedThreads = new HashMap<>();
        Thread[] allThreads = allThreads();
        if (allThreads != null) {
            for (Thread thread : allThreads) {
                if (thread != null && thread.getState() == State.BLOCKED) {
                    Object nativePeer = ReflectUtil.invokeField(thread, "nativePeer");
                    if (nativePeer == null) continue;
                    long thdAddr = (long) nativePeer;
                    if (thdAddr <= 0) {
                        log("thread address not found");
                        continue;
                    }
                    log("blocked Thread[id = " + thread.getId() + ", address = " + thdAddr + "]");
                    int blockThreadId = getContendedThreadIdArt(thdAddr);
                    int currentThreadId = getThreadIdFromNativePeer(thdAddr);
                    log("current thread id = " + currentThreadId + ", blocked thread id = " + blockThreadId);
                    if (currentThreadId != 0 && blockThreadId != 0) {
                        blockedThreads.put(currentThreadId, new DeadLockThreadWrapper(currentThreadId, blockThreadId, thread));
                    }
                }
            }
        }
        List<Map<Integer, Thread>> blockedThreadGroups = blockedThreadGroups(blockedThreads);
        for (Map<Integer, Thread> group : blockedThreadGroups) {
            for (Integer thdId : group.keySet()) {
                DeadLockThreadWrapper wrapper = blockedThreads.get(thdId);
                if (wrapper == null) {
                    continue;
                }
                Thread waitThread = group.get(wrapper.blockedThreadId);
                Thread deadThread = group.get(wrapper.currentThreadId);
                if (waitThread == null) {
                    continue;
                }
                log("waitThread.Name = " + waitThread.getName());
                log("deadThread.Name = " + deadThread.getName());
                DeadLockStackTraces.Error error = new DeadLockStackTraces(threadName(deadThread), deadThread.getStackTrace()).new Error(null);
                DeadLockError deadLockError = new DeadLockError(deadThread, error);
                if (listener != null) {
                    listener.onError(deadLockError);
                }
            }
        }
    }

    private List<Map<Integer, Thread>> blockedThreadGroups(Map<Integer, DeadLockThreadWrapper> blockedThreads) {
        List<Map<Integer, Thread>> blockedThreadGroups = new ArrayList<>();
        Set<Integer> threadIds = new HashSet<>();
        for (Integer thdId : blockedThreads.keySet()) {
            if (threadIds.contains(thdId)) {
                continue;
            }
            threadIds.add(thdId);
            Map<Integer, Thread> blockedThreadGroup = findBlockedThreadGroup(thdId, blockedThreads, new HashMap<Integer, Thread>());
            blockedThreadGroups.add(thdId, blockedThreadGroup);
        }
        return blockedThreadGroups;
    }

    private Map<Integer, Thread> findBlockedThreadGroup(Integer currentThreadId, Map<Integer, DeadLockThreadWrapper> deadLockThreads, Map<Integer, Thread> threadMap) {
        DeadLockThreadWrapper wrapper = deadLockThreads.get(currentThreadId);
        if (wrapper == null) return new HashMap<>();
        if (threadMap.containsKey(currentThreadId)) {
            return threadMap;
        }
        threadMap.put(currentThreadId, wrapper.thread);
        return findBlockedThreadGroup(wrapper.blockedThreadId, deadLockThreads, threadMap);
    }


    public native int nativeInit(int sdkInt);

    public native int getContendedThreadIdArt(long thdNativePeer);

    public native int getThreadIdFromNativePeer(long thdNativePeer);

    static class DeadLockThreadWrapper {
        int currentThreadId;
        int blockedThreadId;
        Thread thread;

        public DeadLockThreadWrapper(int currentThreadId, int blockedThreadId, Thread thread) {
            this.currentThreadId = currentThreadId;
            this.blockedThreadId = blockedThreadId;
            this.thread = thread;
        }
    }

    private void log(String message) {
        if (logEnabled) {
            Log.i(TAG, message);
        }
    }

    private void log(String message, Throwable throwable) {
        if (logEnabled) {
            Log.i(TAG, message, throwable);
        }
    }

}
