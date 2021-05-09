package com.mrkiriss.wifilocalpositioning.di.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrkiriss.wifilocalpositioning.data.sources.api.AccessLevelApi;
import com.mrkiriss.wifilocalpositioning.data.sources.api.LocationDataApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkModule {

    private final Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    @Provides
    @Singleton
    public LocationDataApi provideIMWifiServerApi(){
        //String baseUrl = "https://wifilocalpositioning.herokuapp.com/location/";
        String baseUrl = "http://192.168.31.136:8080/location/";


        return new Retrofit.Builder().
                baseUrl(baseUrl).
                addConverterFactory(GsonConverterFactory.create(gson)).
                build()
                .create(LocationDataApi.class);
    }

    @Provides
    @Singleton
    public AccessLevelApi provideAccessLevelApi(){
        //String baseUrl = "https://wifilocalpositioning.herokuapp.com/";
        String baseUrl = "http://192.168.31.136:8080/security/";


        return new Retrofit.Builder().
                baseUrl(baseUrl).
                addConverterFactory(GsonConverterFactory.create(gson)).
                build()
                .create(AccessLevelApi.class);
    }
}
