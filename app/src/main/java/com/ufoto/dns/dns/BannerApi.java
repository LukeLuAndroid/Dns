package com.ufoto.dns.dns;

import retrofit2.Call;
import retrofit2.http.GET;

public interface BannerApi {

    @GET("banner/json")
    Call<Banner> getBanner();
}
