package com.mrkiriss.wifilocalpositioning.data.repositiries;

import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.utils.LiveData.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import lombok.Data;

@Data
public class SearchRepository {

    private List<SearchItem> prevViewedMapPoints;
    private List<SearchItem> availableForSearchMapPoints;
    private SearchItem currentLocation;

    private final MutableLiveData<List<SearchItem>> requestToUpdateSearchContent;
    private final SingleLiveEvent<String> requestToUpdateSearchInformation;

    @Inject
    public SearchRepository() {
        prevViewedMapPoints = new ArrayList<>();
        availableForSearchMapPoints = new ArrayList<>();

        requestToUpdateSearchContent = new MutableLiveData<>();
        requestToUpdateSearchInformation = new SingleLiveEvent<>();
    }

    public void saveSearchData(SearchData data) {
        prevViewedMapPoints = data.getPrevViewedSearchItems();
        availableForSearchMapPoints = data.getAvailableForSearchItems();
        currentLocation = data.getCurrentLocation();

        // переворачиваем, чтобы последний введённый был первым
        Collections.reverse(prevViewedMapPoints);
    }

    public void responseToSearchInput(String input) {
        List<SearchItem> result = new ArrayList<>();

        if (currentLocation != null) result.add(currentLocation);

        if (input.isEmpty()) {
            result.addAll(prevViewedMapPoints);

            // обновление информации о поиске при отсутсвии предыдущих выборов
            if (prevViewedMapPoints == null || prevViewedMapPoints.isEmpty()) {
                requestToUpdateSearchInformation.setValue("История поиска пуста");
            }

        }else {
            if (availableForSearchMapPoints != null) {
                for (SearchItem item : availableForSearchMapPoints) {
                    if (item.getName().toLowerCase().contains(input.toLowerCase())
                            || item.getDescription().toLowerCase().contains(input.toLowerCase())) {
                        result.add(item);
                    }
                }
            }

            // обновление информации о поиске при отсутсвии каких-либо результатов поиска
            if (availableForSearchMapPoints == null || result.isEmpty()) {
                requestToUpdateSearchInformation.setValue("По запросу ничего не найдено");
            }
        }

        if (!result.isEmpty()) {
            requestToUpdateSearchInformation.setValue("");
        }

        requestToUpdateSearchContent.setValue(result);
    }

}
