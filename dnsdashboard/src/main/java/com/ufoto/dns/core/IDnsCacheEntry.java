package com.ufoto.dns.core;

public interface IDnsCacheEntry {
    /**
     * value
     * @return
     */
    String getValue();

    /**
     * expire
     * @return
     */
    Long getExpire();
}
