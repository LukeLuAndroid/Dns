package com.ufoto.dns;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ufoto.dns.core.IDnsCache;
import com.ufoto.dns.core.InetAddressInjector;

import java.util.List;

/**
 * Dns初始化类
 */
public class Dns {
    private static Context mContext;
    private static InetAddressInjector injector = new InetAddressInjector();

    /**
     * 获取
     *
     * @return
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * 初始化dns
     */
    public static void dnsCacheInject(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        String property = System.getProperty("http.proxyHost", "");
        String property2 = System.getProperty("http.proxyPort", "");
        if (!(TextUtils.isEmpty(property2) || TextUtils.isEmpty(property))) {
            Log.e("Dns", "has proxy will do not work");
            return;
        }
        injector.dnsCacheInject();
    }

    /**
     * 预加载dns
     *
     * @param hosts
     */
    public static void loadDns(List<String> hosts) {
        injector.loadDns(hosts);
    }

    /**
     * 注册dns缓存策略
     *
     * @param cache
     */
    public static void injectDnsCache(IDnsCache cache) {
        injector.injectDnsCache(cache);
    }

    /**
     * 设置过期时间
     * 单位为ms(毫秒级别)
     *
     * @param time
     */
    public static void setTTLExpiry(Long time) {
        injector.setTTLExpiry(time);
    }
}
