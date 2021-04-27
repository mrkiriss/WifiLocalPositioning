package com.mrkiriss.wifilocalpositioning.di.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrkiriss.wifilocalpositioning.data.sources.IMWifiServerApi;

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
        String baseUrl = "https://wifilocalpositioning.herokuapp.com/location/";
        //String baseUrl = "http://10.11.162.169:8080/location/";

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
