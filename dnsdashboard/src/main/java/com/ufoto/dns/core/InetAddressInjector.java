package com.ufoto.dns.core;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.ufoto.dns.util.ReflectUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.List;

public class InetAddressInjector {

    private static final String TAG = "InetAddressInjector";

    /**
     * HOOK系统DNS解析的API
     */
    public static void dnsCacheInject() {
        Class clazz = createInetAddress();
        if (clazz != null) {
            Object addressCacheInstance = ReflectUtils.getStaticFieldInstance(clazz, "addressCache");
            Object cacheInstance = ReflectUtils.getInstanceFieldInstance(addressCacheInstance, "cache");
            ReflectUtils.injectFieldInstance(cacheInstance, "map", new DNSIpLruCache());
        }
    }

    /**
     * 注册自己的解析器
     *
     * @param cache
     */
    public static void injectDnsCache(IDnsCache cache) {
        DnsCacheManager.getInstance().injectDnsCache(cache);
    }

    /**
     * 预加载dns
     *
     * @param hosts
     */
    public static void loadDns(List<String> hosts) {
        if (hosts != null && !hosts.isEmpty()) {
            for (String host : hosts) {
                DnsCacheManager.getInstance().loadDns(host);
            }
        }
    }

    /**
     * 创建InetAddress
     * 需要适配各种版本 目前适配的版本24以及以下的
     * 大于24的版本需要读源代码 目前看到的7.1的源代码跟7.0的一样
     *
     * @return
     */
    private static Class createInetAddress() {
        Class clazz = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            return clazz;
        }
        try {
            clazz = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Class.forName("java.net.Inet6AddressImpl") :
                    InetAddress.class;
        } catch (ClassNotFoundException e) {

        }
        return clazz;
    }

    public void setTTLExpiry(Long time) {
        DnsConfig.TTL_EXPIR = time;
    }

    /**
     * DNSIpLruCache
     * AddressCache --->
     * BasicLruCache<AddressCacheKey, AddressCacheEntry> cache
     * AddressCacheKey      -----> {@link DNSCacheKey}      对应AddressCacheKey(hostname,netId)
     * AddressCacheEntry    -----> {@link DNSCacheEntry}    构造AddressCacheEntry的实例 默认2s过期 用的时候构造绕过2s过期的检验
     * BasicLruCache {LinkedHashMap<K, V> map} 内部是Map实现LruCache
     * 源码路径
     * https://github.com/google/j2objc/blob/master/jre_emul/android/libcore/luni/src/main/java/libcore/util/BasicLruCache.java
     */
    static class DNSIpLruCache extends LinkedHashMap {

        public Object get(Object key) {
            Object result = null;
            DNSCacheKey dnsCacheKey = DNSCacheKey.getInstance(key);
            String ip = DnsCacheManager.getInstance().lookupIpByHostName(dnsCacheKey.hostname);
            Log.e(TAG, dnsCacheKey.hostname + " hostname ip " + ip);
            if (!TextUtils.isEmpty(ip)) { // parse Ip ---> InetAddress
                //dns host->ip www.baidu.com/220.181.112.244
                //dns host->ip www.baidu.com/220.181.111.188
                //存在解析出俩个IP地址的情况 所有这里要用数组
                String[] ips = ip.split(",");
                result = DNSCacheEntry.getInstance(ips);
            }
            return result;
        }

        public Object put(Object key, Object value) {
            if (value != null) {
                InetAddress[] addresses = DNSCacheEntry.getInetAddress(value);
                if (addresses != null) {
                    DnsCacheManager.getInstance().cacheAddress(addresses);
                    return super.put(key, value);
                }
            }
            return null;
        }
    }

    /**
     * AddressCacheKey
     * {@link //java.net.AddressCache$AddressCacheKey} //hide api
     */
    private static class DNSCacheKey {
        private static final String HOST_NAME = "mHostname";
        private static final String NET_ID = "mNetId";

        public String hostname;
        public int netId;

        public static DNSCacheKey getInstance(Object key) {
            DNSCacheKey dnsCacheKey = new DNSCacheKey();
            if (key instanceof String) {
                dnsCacheKey.hostname = (String) key;
            } else {
                dnsCacheKey.init(key);
            }
            return dnsCacheKey;
        }

        private void init(Object key) {
            try {
                hostname = (String) ReflectUtils.getInstanceFieldInstance(key, HOST_NAME);
                netId = (int) ReflectUtils.getInstanceFieldInstance(key, NET_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * AddressCacheEntry
     * {@link //java.net.AddressCache$AddressCacheEntry} //hide api
     * 系统NDS的过期时间是2s
     * 从缓存中获取的时候 通过反射创建AddressCacheEntry 使得缓存永不过期
     */
    private static class DNSCacheEntry {
        private static final String REFLECT_NAME = "java.net.AddressCache$AddressCacheEntry";

        public static InetAddress[] getInetAddress(Object value) {
            InetAddress[] addresses = null;
            if (value != null) {
                try {
                    addresses = (InetAddress[]) ReflectUtils.getInstanceFieldInstance(value, "value");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return addresses;
        }

        /**
         * AddressCacheEntry
         *
         * @return
         */
        public static Object getInstance(String[] ips) {
            if (ips == null || ips.length == 0) {
                Log.e(TAG, "ip cache is null go system dns");
                return null;
            }
            int len = ips.length;
            InetAddress[] addresses = new InetAddress[len];
            try {
                for (int i = 0; i < len; i++) {
                    addresses[i] = InetAddress.getByName(ips[i]);
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            Log.e(TAG, "DNSCache hit hostname ip = " + ips[0] + ",,,ips Size " + ips.length);
            return ReflectUtils.invokeConstructor(REFLECT_NAME, (Object) addresses);
        }
    }
}
