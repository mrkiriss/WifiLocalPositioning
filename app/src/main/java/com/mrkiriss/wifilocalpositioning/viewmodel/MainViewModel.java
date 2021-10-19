package com.mrkiriss.wifilocalpositioning.viewmodel;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.repositiries.MainRepository;
import com.mrkiriss.wifilocalpositioning.utils.livedata.Event;

import javax.inject.Inject;

import lombok.Data;

@Data
public class MainViewModel extends ViewModel {

    private final MainRepository repository;

    private final LiveData<Event<String>> requestToOpenInstructionObYouTube;
    private final LiveData<Event<String>> toastContent;
    private final LiveData<Event<Intent>> requestToStartIntent;

    @Inject
    public MainViewModel(MainRepository repository){
        this.repository=repository;

        requestToOpenInstructionObYouTube=repository.getRequestToOpenInstructionObYouTube();
        toastContent = repository.getToastContent();
        requestToStartIntent = repository.getRequestToStartIntent();
    }

    public void watchYoutubeVideo(String id) {
        repository.watchYoutubeVideo(id);
    }

    public boolean isPresentAccessPermission(int destinationID){
        return repository.isPresentAccessPermission(destinationID);
    }
    public void setCurrentTypeOfRequestSource(int destinationID){
        repository.setCurrentTypeOfRequestSource(destinationID);
    }
    public void checkAndroidVersionForShowingScanningAbilities(Context context){
        repository.checkAndroidVersionForShowingScanningAbilities(context);
    }
}
