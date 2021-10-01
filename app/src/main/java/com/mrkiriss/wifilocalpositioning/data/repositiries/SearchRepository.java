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
        input = input.toLowerCase();
        String inputTransliterated = transliterate(input);

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
                    if (item.getName().toLowerCase().contains(input)
                            || item.getDescription().toLowerCase().contains(input)
                            || item.getName().equals(inputTransliterated)
                            || item.getDescription().equals(inputTransliterated)) {
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
    private String transliterate(String message) {
        char[] template = {'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'a', 'b', 'v', 'g', 'd', 'e', 'e', 'z', 'i', 'y', 'k', 'l', 'm', 'n', 'o', 'p', 'r', 's', 't', 'u', 'f', 'h', 'i', 'e', 'u', 'a'};
        String[] replacement = {"a", "b", "v", "g", "d", "e", "e", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "", "i", "", "e", "u", "a", "а", "б", "в", "г", "д", "е", "ё", "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ы", "э", "ю", "я"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            boolean replaced = false;
            for (int j = 0; j < template.length; j++) {
                if (message.charAt(i) == template[j]) {
                    builder.append(replacement[j]);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                builder.append(message.charAt(i));
            }
        }

        return builder.toString();
    }
}
