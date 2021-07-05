package me.bytebeats.deadlock;

import androidx.annotation.NonNull;

/**
 *
 * Created by bytebeats on 2021/7/3 : 17:51
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 *
 */
public class DeadLockError extends Error {
    public DeadLockError(Thread thread, DeadLockStackTraces.Error error) {
        super("Thread(id: " + thread.getId() + ", state = " + thread.getState().toString() + ")", error);
    }

    @NonNull
    @Override
    public synchronized Throwable fillInStackTrace() {
        setStackTrace(new StackTraceElement[0]);
        return this;
    }
}
