package com.ufoto.dns.detection;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.InetAddress;

/**
 * @Describe: DNS解析性能监控
 * @Time: 2018/8/9 20:28
 * @Author: hApple
 */

public class DnsDashboard {
    private static final String DNS_INCOMPATIBLE = "6.0及其以下版本不兼容";
    private static String TAG = "DNS";

    //单例化对象
    private DnsDashboard() {
    }

    public static DnsDashboard getInstance() {
        return InstanceHolder.mInstance;
    }

    private static class InstanceHolder {
        private static final DnsDashboard mInstance = new DnsDashboard();
    }

    /**
     * 公有的根方法，减少变动。
     */
    public void hookDNS(InformationListener listener) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            hookM(listener);
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DNSInfor dnsInfor = new DNSInfor();
            dnsInfor.setStartTime(0L);
            dnsInfor.setEndTime(0L);
            dnsInfor.setDomain(DNS_INCOMPATIBLE);
            dnsInfor.setHookSuccess(false);
            listener.onMonitor(dnsInfor);
        } else {
            DNSInfor dnsInfor = new DNSInfor();
            dnsInfor.setStartTime(0L);
            dnsInfor.setEndTime(0L);
            dnsInfor.setDomain(DNS_INCOMPATIBLE);
            dnsInfor.setHookSuccess(false);
            listener.onMonitor(dnsInfor);
        }
    }

    /**
     * Android 4.0~7.0间。
     *
     * @param listener TODO: 没有Hook对象，待寻找解决方案。
     */
    private void proxyI2M(InformationListener listener) {
        DNSInfor dnsInfor = new DNSInfor();
        dnsInfor.setStartTime(0L);
        dnsInfor.setEndTime(0L);
        dnsInfor.setDomain(DNS_INCOMPATIBLE);
        dnsInfor.setHookSuccess(false);
        listener.onMonitor(dnsInfor);
    }

    /**
     * @describe Android7.0及以上。
     * <p>
     * 1. 反射代理类的相关信息；
     * 2. 获取原对象并生成代理对象；
     * 3. 替换代理对象；
     * 4. 还原原类信息；
     * @TODO: 9.0未确定。
     */
    private void hookM(final InformationListener listener) {
        try {
            final DNSInfor dnsInfor = new DNSInfor();
            //修改impl对象可见。
            Field impl = InetAddress.class.getDeclaredField("impl");
            impl.setAccessible(true);
            Field accessField = Field.class.getDeclaredField("accessFlags");
            accessField.setAccessible(true);
            //去除final
            accessField.setInt(impl, impl.getModifiers() & ~Modifier.FINAL);
            //获取原InetAddressImpl对象
            final Object originImpl = impl.get(null);

            Object proxyObject = Proxy.newProxyInstance(originImpl.getClass().getClassLoader(),
                    originImpl.getClass().getInterfaces(), new InvocationHandler() {

                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            Object interObject;
                            if (method.getName().equals("lookupAllHostAddr") && args != null && args.length >= 1) {
                                //拿到DNS解析前时间点。
                                dnsInfor.setStartTime(System.currentTimeMillis());
//                                URL url = new URL(args[0].toString());
                                dnsInfor.setDomain(args[0].toString());
                                //调用原方法
                                interObject = method.invoke(originImpl, args);
                                //拿到DNS解析完成后返回Ip时间点。
                                dnsInfor.setEndTime(System.currentTimeMillis());
                                listener.onMonitor(dnsInfor);
                                dnsInfor.setHookSuccess(true);
                            } else {
                                dnsInfor.setStartTime(0L);
                                dnsInfor.setEndTime(0L);
                                dnsInfor.setDomain(DNS_INCOMPATIBLE);
                                dnsInfor.setHookSuccess(false);
                                interObject = method.invoke(originImpl, args);
                            }

//                            Log.i(TAG, "开始节点：" + dnsInfor.getStartTime() + "--------------" + "结束节点：" + dnsInfor.getEndTime() + "---总耗时：" + dnsInfor.getResolutionTime());
                            return interObject;
                        }
                    });

            impl.set(null, proxyObject);
            accessField.setInt(impl, impl.getModifiers() & Modifier.FINAL);
        } catch (NoSuchFieldException e) {
            Log.e("TAG", "InetAddress NoSuchFieldException：");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e("TAG", "IllegalAccessException：");
            e.printStackTrace();
        }
    }
}
