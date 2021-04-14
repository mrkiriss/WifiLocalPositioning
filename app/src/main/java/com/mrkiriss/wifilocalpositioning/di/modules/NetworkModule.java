package com.mrkiriss.wifilocalpositioning.di.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrkiriss.wifilocalpositioning.network.IMWifiServerApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkModule {

    @Provides
    @Singleton
    public IMWifiServerApi provideIMWifiServerApi(){
        String baseUrl = "https://indoormappingbywifi.herokuapp.com/location/";
    //String baseUrl = "http://192.168.43.231:8080/";

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder().
                baseUrl(baseUrl).
                addConverterFactory(GsonConverterFactory.create(gson)).
                build()
                .create(IMWifiServerApi.class);
    }
}
