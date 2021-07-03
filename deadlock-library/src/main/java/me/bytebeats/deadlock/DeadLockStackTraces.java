package me.bytebeats.deadlock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by Pan Chen on 2021/7/3 : 17:39
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
class DeadLockStackTraces implements Serializable {
    private final String name;
    private final StackTraceElement[] stackTraceElements;

    DeadLockStackTraces(String name, StackTraceElement[] stackTraceElements) {
        this.name = name;
        this.stackTraceElements = stackTraceElements;
    }

    class Error extends Throwable {
        public Error(@Nullable Error cause) {
            super(name, cause);
        }

        @NonNull
        @Override
        public synchronized Throwable fillInStackTrace() {
            setStackTrace(stackTraceElements);
            return this;
        }
    }
}
