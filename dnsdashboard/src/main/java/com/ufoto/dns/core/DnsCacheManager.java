package com.ufoto.dns.core;

import android.text.TextUtils;

import com.ufoto.dns.impl.DnsDiskCache;
import com.ufoto.dns.impl.DnsMemoryCache;
import com.ufoto.dns.impl.DnsNetCache;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class DnsCacheManager {

    private static final String TAG = "DnsCacheManager";
    private final LinkedList<IDnsCache> caches;
    private DnsNetCache netCache = new DnsNetCache();
    private ExecutorService EXECUTORS = Executors.newSingleThreadExecutor();
    /**
     * need to load new dns info
     */
    private volatile List<String> shouldReloadDnsInfo = new ArrayList<>();

    private DnsCacheManager() {
        caches = new LinkedList<>();
        caches.add(new DnsMemoryCache());
        caches.add(new DnsDiskCache());
    }

    /**
     * 注册dns池
     *
     * @param cache
     */
    public void injectDnsCache(IDnsCache cache) {
        synchronized (caches) {
            caches.addFirst(cache);
        }
    }

    /**
     * 预加载dns
     *
     * @param host
     */
    public void loadDns(final String host) {
        EXECUTORS.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    shouldReloadDnsInfo.add(host);
                    InetAddress.getAllByName(host);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 缓存解析好的数据
     *
     * @param addresses
     */
    public void cacheAddress(InetAddress[] addresses) {
        if (addresses != null && addresses.length > 0) {
            String host = "";
            StringBuilder ipBuilder = new StringBuilder();
            for (int i = 0; i < addresses.length; i++) {
                if (i == 0) {
                    host = addresses[i].getHostName();
                    ipBuilder.append(addresses[i].getHostAddress());
                } else {
                    ipBuilder.append("," + addresses[i].getHostAddress());
                }
            }
            String ip = ipBuilder.toString();
            cacheIpByHost(host, ip);
        }
    }

    /**
     * 缓存ip到host
     *
     * @param host
     * @param ip
     */
    private void cacheIpByHost(String host, String ip) {
        if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(ip)) {
            for (IDnsCache dnsCache : caches) {
                dnsCache.addCache(host, ip);
            }
        }
    }

    /**
     * 根据hostname解析出ip
     * if reload then return null to get new dns info
     *
     * @param hostName
     * @return
     */
    public String lookupIpByHostName(String hostName) {
        if (shouldReloadDnsInfo.contains(hostName)) {
            shouldReloadDnsInfo.remove(hostName);
            return null;
        }
        IDnsCacheEntry cacheEntry = null;
        for (IDnsCache dnsCache : caches) {
            cacheEntry = dnsCache.lookupIpByHostName(hostName);
            if (cacheEntry != null && cacheEntry.getExpire() > System.currentTimeMillis()) {
                break;
            }
        }

        //如果过期了,则返回旧的ip,重新去解析
        if (cacheEntry != null && !TextUtils.isEmpty(cacheEntry.getValue())) {
            if (cacheEntry.getExpire() < System.currentTimeMillis()) {
                loadDns(hostName);
            }
            return cacheEntry.getValue();
        }

        String ip = netCache.lookupIpByHostName(hostName);
        cacheIpByHost(hostName, ip);
        return ip;
    }

    /**
     * Holder for Singleton
     */
    private static final class DnsCacheManagerHolder {
        private static final DnsCacheManager INSTANCE = new DnsCacheManager();
    }

    public static DnsCacheManager getInstance() {
        return DnsCacheManagerHolder.INSTANCE;
    }

}
