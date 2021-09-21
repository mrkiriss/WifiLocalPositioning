package com.mrkiriss.wifilocalpositioning.viewmodel;

import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;

import lombok.Data;

@Data
public class SelectedMapPointViewModel extends ViewModel {

    private ObservableField<MapPoint> mapPoint;
    private final ObservableBoolean showAllData;
    private final ObservableField<String> pointsNameInHead;
    private final ObservableInt arrowImageRecourseId;

    public SelectedMapPointViewModel() {
        showAllData = new ObservableBoolean(false);
        pointsNameInHead = new ObservableField<>("");
        arrowImageRecourseId = new ObservableInt(R.drawable.ic_arrow_down);
    }

    public void onClick() {
        showAllData.set(!showAllData.get());

        // скрываем/показываем имя точки в шапке
        if (showAllData.get()) {
            pointsNameInHead.set("");
            arrowImageRecourseId.set(R.drawable.ic_arrow_up);
        } else {
            setRoomNameInHead();
            arrowImageRecourseId.set(R.drawable.ic_arrow_down);
        }
    }

    private void setRoomNameInHead() {
        if (mapPoint != null && mapPoint.get() != null && mapPoint.get().getRoomName() != null) {
            pointsNameInHead.set(mapPoint.get().getRoomName());
        }
    }

    public void setMapPointWithOtherMoves(ObservableField<MapPoint> mapPoint) {
        this.mapPoint = mapPoint;

        mapPoint.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (!showAllData.get()) {
                    setRoomNameInHead();
                }
            }
        });
    }
}
