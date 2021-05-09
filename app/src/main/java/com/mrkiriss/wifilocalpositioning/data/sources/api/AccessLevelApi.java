package com.mrkiriss.wifilocalpositioning.data.sources.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AccessLevelApi {

    @GET("level")
    Call<Integer> getAccessLevel(@Query("uuid") String uuid);
}
