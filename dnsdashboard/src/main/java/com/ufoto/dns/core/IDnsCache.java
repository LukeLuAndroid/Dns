package com.ufoto.dns.core;

public interface IDnsCache {
    /**
     * 检索host对应的ip
     * @param hostName      域名
     * @return              ip地址
     */
    IDnsCacheEntry lookupIpByHostName(String hostName);

    /**
     * 添加到cache
     * @param hostName
     * @param ip
     */
    void addCache(String hostName,String ip);
}
