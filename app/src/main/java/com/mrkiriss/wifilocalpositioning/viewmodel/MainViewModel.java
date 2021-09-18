package com.mrkiriss.wifilocalpositioning.viewmodel;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.repositiries.MainRepository;

import java.util.List;

import lombok.Data;

@Data
public class MainViewModel extends ViewModel {

    private final MainRepository repository;

    private final LiveData<String> requestToOpenInstructionObYouTube;
    private final LiveData<String> toastContent;
    private final LiveData<Intent> requestToStartIntent;

    public MainViewModel(MainRepository repository){
        this.repository=repository;

        requestToOpenInstructionObYouTube=repository.getRequestToOpenInstructionObYouTube();
        toastContent = repository.getToastContent();
        requestToStartIntent = repository.getRequestToStartIntent();
    }

    public Fragment[] createFragments() {
        return repository.createFragments();
    }
    public String[] createFragmentTags() {
        return repository.createFragmentTags();
    }
    public String[] createTypesOfRequestSources() {
        return repository.createTypesOfRequestSources();
    }

    public void watchYoutubeVideo(String id) {
        repository.watchYoutubeVideo(id);
    }

    public void notifyAboutLackOfAccess() {
        repository.notifyAboutLackOfAccess();
    }

    public boolean isPresentAccessPermission(String type){
        return repository.isPresentAccessPermission(type);
    }
    public void setCurrentTypeOfRequestSource(String type){
        repository.setCurrentTypeOfRequestSource(type);
    }
    public void checkAndroidVersionForShowingScanningAbilities(Context context){
        repository.checkAndroidVersionForShowingScanningAbilities(context);
    }
}
