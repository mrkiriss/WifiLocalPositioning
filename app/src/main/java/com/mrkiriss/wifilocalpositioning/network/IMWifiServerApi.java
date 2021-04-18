package com.mrkiriss.wifilocalpositioning.network;

import com.mrkiriss.wifilocalpositioning.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.models.server.StringResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.HTTP;
import retrofit2.http.POST;

public interface IMWifiServerApi {

    @HTTP(method = "POST", path = "define/coordinate", hasBody = true)
    Call<DefinedLocationPoint> defineLocation(@Body CalibrationLocationPoint calibrationLocationPoint);

    @HTTP(method = "POST", path = "define/cabinet", hasBody = true)
    Call<DefinedLocationPoint> defineLocationWithCabinet(@Body CalibrationLocationPoint calibrationLocationPoint);

    @POST("training/coordinate")
    Call<StringResponse> postLPCalibrationWithCoordinateForTraining(@Body CalibrationLocationPoint calibrationLocationPoint);

    @POST("training/cabinet")
    Call<StringResponse> postLPCalibrationWithCabinetForTraining(@Body CalibrationLocationPoint calibrationLocationPoint);

    @DELETE("location")
    Call<StringResponse> cleanServer();
}
