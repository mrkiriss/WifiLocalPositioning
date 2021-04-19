package com.mrkiriss.wifilocalpositioning.network;

import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.data.models.server.StringResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;

public interface IMWifiServerApi {

    @POST("define/room")
    Call<DefinedLocationPoint> defineLocationWithCabinet(@Body CalibrationLocationPoint calibrationLocationPoint);

    @POST("training/room/coordinates")
    Call<StringResponse> postLPCalibrationWithCoordinateForTraining(@Body LocationPointInfo locationPointInfo);

    @POST("training/room/aps")
    Call<StringResponse> postLPCalibrationWithCabinetForTraining(@Body CalibrationLocationPoint calibrationLocationPoint);

    @DELETE("location")
    Call<StringResponse> cleanServer();
}
