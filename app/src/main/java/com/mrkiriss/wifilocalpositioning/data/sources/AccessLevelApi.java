package com.mrkiriss.wifilocalpositioning.data.sources;

import retrofit2.Call;
import retrofit2.http.Query;

public interface AccessLevelApi {

    Call<Boolean> getAccessLevel(@Query("uuid") String uuid);
}
