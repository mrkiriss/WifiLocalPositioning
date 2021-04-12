package com.mrkiriss.wifilocalpositioning.network;

import com.mrkiriss.wifilocalpositioning.models.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.models.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.models.StringResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;

public interface IMWifiServerApi {

    @HTTP(method = "POST", path = "/location/define", hasBody = true)
    Call<DefinedLocationPoint> defineLocation(@Body CalibrationLocationPoint calibrationLocationPoint);

    @POST("/location")
    Call<StringResponse> postLocationPointForCalibrationTraining(@Body CalibrationLocationPoint calibrationLocationPoint);

    @DELETE("/location")
    Call<StringResponse> cleanServer();
}
