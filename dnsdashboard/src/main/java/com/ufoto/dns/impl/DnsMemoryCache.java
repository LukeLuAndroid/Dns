package com.ufoto.dns.impl;

import com.ufoto.dns.core.DnsConfig;
import com.ufoto.dns.core.IDnsCache;
import com.ufoto.dns.core.IDnsCacheEntry;

import java.util.HashMap;
import java.util.Map;

public class DnsMemoryCache implements IDnsCache {

    private static final Map<String, MemoryCacheEntry> CACHE_MAP;

    static {
        CACHE_MAP = new HashMap<>();
    }

    @Override
    public IDnsCacheEntry lookupIpByHostName(String hostName) {
        MemoryCacheEntry cacheEntry = CACHE_MAP.get(hostName);
        return cacheEntry;
    }

    @Override
    public void addCache(String hostName, String ip) {
        CACHE_MAP.remove(hostName);
        CACHE_MAP.put(hostName, new MemoryCacheEntry(ip));
    }

    static class MemoryCacheEntry implements IDnsCacheEntry {
        // Either an InetAddress[] for a positive entry,
        // or a String detail message for a negative entry.
        final String value;

        /**
         * The absolute expiry time in nanoseconds. Nanoseconds from System.nanoTime is ideal
         * because -- unlike System.currentTimeMillis -- it can never go backwards.
         * <p>
         * We don't need to worry about overflow with a TTL_NANOS of 2s.
         */
        final long expiryNanos;

        MemoryCacheEntry(String value) {
            this.value = value;
            this.expiryNanos = System.currentTimeMillis() + DnsConfig.TTL_EXPIR;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Long getExpire() {
            return expiryNanos;
        }
    }

}
