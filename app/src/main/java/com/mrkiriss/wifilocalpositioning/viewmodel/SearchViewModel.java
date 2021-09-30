package com.mrkiriss.wifilocalpositioning.viewmodel;

import android.util.Log;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.data.repositiries.SearchRepository;

import java.util.List;

import javax.inject.Inject;

import lombok.Data;

@Data
public class SearchViewModel extends ViewModel {

    private final SearchRepository rep;

    private final ObservableField<String> searchLineHint;
    private final ObservableField<String> searchLineInput;
    private final ObservableField<String> searchInformation;

    private final LiveData<List<SearchItem>> requestToUpdateSearchContent;
    private final LiveData<String> requestToUpdateSearchInformation;

    @Inject
    public SearchViewModel(SearchRepository repository) {
        this.rep = repository;

        requestToUpdateSearchContent = rep.getRequestToUpdateSearchContent();
        requestToUpdateSearchInformation = rep.getRequestToUpdateSearchInformation();

        searchLineHint = new ObservableField<>("");
        searchLineInput = new ObservableField<>("");
        searchInformation = new ObservableField<>("");

        initSearchInputCallback();
    }

    private void initSearchInputCallback() {
        searchLineInput.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Log.i("searchMode", "searchLineInput change, new content = "+searchLineInput.get());
                rep.responseToSearchInput(searchLineInput.get());
            }
        });
    }

    public void saveSearchData(SearchData data) {
        rep.saveSearchData(data);
        searchLineHint.set(data.getHintInSearchLine());
    }

    public void initEmptyInput(){
        rep.responseToSearchInput("");
    }

}
