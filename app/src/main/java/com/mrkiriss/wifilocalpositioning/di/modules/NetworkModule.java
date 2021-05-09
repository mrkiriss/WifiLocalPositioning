package com.mrkiriss.wifilocalpositioning.di.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrkiriss.wifilocalpositioning.data.sources.LocationDataApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkModule {

    @Provides
    @Singleton
    public LocationDataApi provideIMWifiServerApi(){
        //String baseUrl = "https://wifilocalpositioning.herokuapp.com/location/";
        String baseUrl = "http://192.168.31.136:8080/location/";

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder().
                baseUrl(baseUrl).
                addConverterFactory(GsonConverterFactory.create(gson)).
                build()
                .create(LocationDataApi.class);
    }
}
