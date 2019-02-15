package com.ufoto.dns.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class ReflectUtils {
    private static final String TAG = "ReflectUtils";

    /**
     * getStaticFieldInstance
     * @param clazz
     * @param name
     * @return
     */
    public static Object getStaticFieldInstance(Class clazz, String name) {
        Object instance = null;
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            instance = field.get(null);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            //do nothing
        }
        return instance;
    }

    /**
     * getInstanceFieldInstance
     * @param object
     * @param name
     * @return
     */
    public static Object getInstanceFieldInstance(Object object, String name) {
        Object instance = null;
        try {
            Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);
            instance = field.get(object);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            //do nothing
        }
        return instance;
    }

    /**
     * injectFieldInstance HOOK系统API
     * @param srcObject
     * @param name
     * @param destInstance
     */
    public static void injectFieldInstance(Object srcObject, String name, Object destInstance) {
        try {
            Field field = srcObject.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(srcObject, destInstance);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            //do nothing
        }
    }

    /**
     * 执行构造函数 参数Object 构造函数的签名<init>(Object obj)
     * TODO 应该是参数传入 不应该写死
     * TODO public T newInstance(Object... args) 传入数组如果数组个数大一一个的时候 总是抛出异常
     * TODO wrong number of arguments execpted 1 got 2
     * TODO 网上的方案 强制转化成Object 测试还有问题
     * TODO need fix???
     * @param className
     * @param args
     * @return
     */
    public static Object invokeConstructor(String className, Object... args) {
        Object instance = null;
        try {
            Constructor constructor = Class.forName(className).getDeclaredConstructor(Object.class);
            constructor.setAccessible(true);
            instance = constructor.newInstance(args);
        } catch (Exception e) {
            //do nothing
        }
        return instance;
    }
}
