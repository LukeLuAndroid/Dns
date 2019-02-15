# DnsSDK

全局监控DNS解析性能的库。

# 使用

## 导入Library

下载```dnsdashboard```Android Library，配置gradle并Sync。

1. 将```dnsdashboard```拷到项目根目录下；
2. 在项目```settings.gradle```配置，```':dnsdashboard'```，如有多个模块以','分隔：

```gradle
include ':app', ':dnsdashboard'
```

3. 在项目主模块的```build.gradle```中添加依赖。例如：

```gradle
dependencies {
    ...
    implementation project(':dnsdashboard')
}
```

# 示例

在onMonitor()方法中拿到的DNSInfor，就是DNS相关信息。获取时，先通过```javaisHookSuccess()```判断是否监听成功。true表示监听成功，会拿到相关信息；false表示失败，会拿到DNSInfor的默认值：

```java
DnsDashboard.getInstance().hookDNS(new InformationListener() {
    @Override
    public void onMonitor(DNSInfor dnsInfor) {
        if (dnsInfor.isHookSuccess()) {
            Log.i("DnsSDK", "请求站点：" + dnsInfor.getDomain());
            Log.i("DnsSDK", "解析用时：" + dnsInfor.getResolutionTime());
            Log.i("DnsSDK", "开始解析：" + dnsInfor.getStartTime());
            Log.i("DnsSDK", "结束解析：" + dnsInfor.getEndTime());
        }else {
            Log.i("DnsSDK", "请求站点：" + dnsInfor.getDomain());
            Log.i("DnsSDK", "解析用时：" + dnsInfor.getResolutionTime());
            Log.i("DnsSDK", "开始解析：" + dnsInfor.getStartTime());
            Log.i("DnsSDK", "结束解析：" + dnsInfor.getEndTime());
        }
    }
});
```

# TODO

## 二期

1. 兼容6.0及其以下版本。






dns:
application中：

Dns.dnsCacheInject(Context context);
//预加载
Dns.loadDns(List<String> hosts);
//注册新的额缓存器
Dbs.injectDnsCache(IDnsCache cache);
//设置过期时间
Dns.setTTLExpiry(Long time);