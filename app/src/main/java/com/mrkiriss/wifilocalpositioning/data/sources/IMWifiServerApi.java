package com.mrkiriss.wifilocalpositioning.data.sources;

import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.ListOfAllMapPoints;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.data.models.server.StringResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface IMWifiServerApi {

    @POST("define/room")
    Call<DefinedLocationPoint> defineLocation(@Body CalibrationLocationPoint calibrationLocationPoint);

    @POST("training/room/coordinates")
    Call<StringResponse> postCalibrationLPInfo(@Body LocationPointInfo locationPointInfo);

    @POST("training/room/aps")
    Call<StringResponse> postCalibrationLPWithAPs(@Body CalibrationLocationPoint calibrationLocationPoint);

    @GET("define/room/coordinates")
    Call<ListOfAllMapPoints> getListOfAllMapPoints();
}
