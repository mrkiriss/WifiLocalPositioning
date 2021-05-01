package com.mrkiriss.wifilocalpositioning.data.sources;

import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.Connections;
import com.mrkiriss.wifilocalpositioning.data.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.ListOfAllMapPoints;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.data.models.server.StringResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IMWifiServerApi {

    @POST("define/room")
    Call<DefinedLocationPoint> defineLocation(@Body CalibrationLocationPoint calibrationLocationPoint);

    @POST("training/room/coordinates")
    Call<StringResponse> postCalibrationLPInfo(@Body LocationPointInfo locationPointInfo);

    @POST("training/room/aps")
    Call<StringResponse> postCalibrationLPWithAPs(@Body CalibrationLocationPoint calibrationLocationPoint);

    @GET("define/room/coordinates")
    Call<ListOfAllMapPoints> getListOfAllMapPoints();

    @DELETE("training/room/coordinates")
    Call<StringResponse> deleteLPInfo(@Query("roomName") String roomName);

    @DELETE("training/room/aps")
    Call<StringResponse> deleteLPAps(@Query("roomName")  String roomName);

    @POST("training/connections")
    Call<StringResponse> postConnections(@Body Connections connections);

    @GET("training/connections")
    Call<Connections> getConnectionsByName(@Query("name") String name);
}
