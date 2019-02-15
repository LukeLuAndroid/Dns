package com.ufoto.dns.dns;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ufoto.dns.Dns;
import com.ufoto.dns.detection.DNSInfor;
import com.ufoto.dns.detection.DnsDashboard;
import com.ufoto.dns.detection.InformationListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private TextView mTvDnsDomain;
    private TextView mTvDnsResolution;
    private TextView mTvDnsStartTime;
    private TextView mTvDnsEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvDnsDomain = findViewById(R.id.tv_dns_domain);
        mTvDnsResolution = findViewById(R.id.tv_dns_resolution);
        mTvDnsStartTime = findViewById(R.id.tv_dns_start_time);
        mTvDnsEndTime = findViewById(R.id.tv_dns_end_time);

        Dns.dnsCacheInject(this);
        //调用DnsDashboard.hookDNS()，传入InformationListener对象，在onMonitor()中DNSInfor中就是DNS解析相关信息。
        DnsDashboard.getInstance().hookDNS(new InformationListener() {
            @Override
            public void onMonitor(final DNSInfor dnsInfor) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        printLog(dnsInfor);
                    }
                });
            }
        });
    }

    private void printLog(DNSInfor dnsInfor) {
        if (dnsInfor.isHookSuccess()) {
            mTvDnsDomain.setText("请求站点：" + dnsInfor.getDomain());
            mTvDnsResolution.setText("解析用时：" + dnsInfor.getResolutionTime());
            mTvDnsStartTime.setText("开始解析：" + dnsInfor.getStartTime());
            mTvDnsEndTime.setText("结束解析：" + dnsInfor.getEndTime());

            Log.i("DnsSDK", "请求站点：" + dnsInfor.getDomain());
            Log.i("DnsSDK", "解析用时：" + dnsInfor.getResolutionTime());
            Log.i("DnsSDK", "开始解析：" + dnsInfor.getStartTime());
            Log.i("DnsSDK", "结束解析：" + dnsInfor.getEndTime());
        } else {
            mTvDnsDomain.setText("请求站点：" + dnsInfor.getDomain());
            mTvDnsResolution.setText("解析用时：" + dnsInfor.getResolutionTime());
            mTvDnsStartTime.setText("开始解析：" + dnsInfor.getStartTime());
            mTvDnsEndTime.setText("结束解析：" + dnsInfor.getEndTime());

            Log.i("DnsSDK", "请求站点：" + dnsInfor.getDomain());
            Log.i("DnsSDK", "解析用时：" + dnsInfor.getResolutionTime());
            Log.i("DnsSDK", "开始解析：" + dnsInfor.getStartTime());
            Log.i("DnsSDK", "结束解析：" + dnsInfor.getEndTime());
        }
    }

    public void onRequestClick(View v) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://blog.csdn.net/zhujianlin1990/article/details/51469359/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BannerApi bannerApi = retrofit.create(BannerApi.class);
        final Call<Banner> banner = bannerApi.getBanner();
        banner.enqueue(new Callback<Banner>() {
            @Override
            public void onResponse(Call<Banner> call, Response<Banner> response) {
                if (null != response && null != response.body()) {
                    Log.i("DNS", "Title： " + response.body().getData().get(0).getTitle());
                }
            }

            @Override
            public void onFailure(Call<Banner> call, Throwable t) {
                Log.i("DNS", "请求Error： " + t.getMessage());
            }
        });

        List<String> list = new ArrayList<>();
        list.add("www.baidu.com");
        Dns.loadDns(list);
    }

}
