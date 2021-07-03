package me.bytebeats.deadlock;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Pan Chen on 2021/7/3 : 15:56
 * E-mail: panchen@itiger.com
 * Company: https://www.itiger.com
 */
public final class ReflectUtil {
    private ReflectUtil() {
    }

    public static <T> T newInstance(Class<T> klazz, Class<?> argTypes, Object[] args) {
        try {
            return klazz.getConstructor(argTypes).newInstance(args);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeMethod(Object obj, String methodName) {
        try {
            return obj.getClass().getMethod(methodName, (Class<?>) null).invoke(obj, (Object[]) null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeStaticMethod(Object obj, String methodName) {
        try {
            ((Class) obj).getMethod(methodName, (Class<?>) null).invoke(obj, (Object[]) null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeMethod(Object obj, String methodName, Class<?> argTypes, Object arg) {
        try {
            return obj.getClass().getMethod(methodName, argTypes).invoke(obj, arg);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeMethod(Object obj, String methodName, Class<?> argTypes1, Object arg1, Class<?> argTypes2, Object arg2) {
        try {
            return obj.getClass().getMethod(methodName, argTypes1, argTypes2).invoke(obj, arg1, arg2);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getField(fieldName);
            field.setAccessible(false);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
