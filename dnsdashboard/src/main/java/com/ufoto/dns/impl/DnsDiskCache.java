package com.ufoto.dns.impl;

import android.text.TextUtils;

import com.ufoto.dns.Dns;
import com.ufoto.dns.core.DnsConfig;
import com.ufoto.dns.core.IDnsCache;
import com.ufoto.dns.core.IDnsCacheEntry;
import com.ufoto.dns.util.DiskUtil;


public class DnsDiskCache implements IDnsCache {

    @Override
    public IDnsCacheEntry lookupIpByHostName(String hostName) {
        if (Dns.getContext() != null) {
            Long expire = 0L;
            try {
                String expirestr = DiskUtil.get(Dns.getContext(), DiskUtil.KEY_TTL_TIME);
                if (TextUtils.isEmpty(expirestr)) {
                    expire = 0L;
                } else {
                    expire = Long.parseLong(expirestr);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String ip = DiskUtil.get(Dns.getContext(), hostName);
            return new DnsDiskEntry(ip, expire);
        }
        return null;
    }

    @Override
    public void addCache(String hostName, String ip) {
        if (Dns.getContext() != null) {
            DiskUtil.put(Dns.getContext(), hostName, ip);
            DiskUtil.put(Dns.getContext(), DiskUtil.KEY_TTL_TIME, String.valueOf(System.currentTimeMillis() + DnsConfig.TTL_EXPIR));
        }
    }

    static class DnsDiskEntry implements IDnsCacheEntry {
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

        DnsDiskEntry(String value) {
            this.value = value;
            this.expiryNanos = System.currentTimeMillis() + DnsConfig.TTL_EXPIR;
        }

        DnsDiskEntry(String value, Long expiry) {
            this.value = value;
            this.expiryNanos = expiry;
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
