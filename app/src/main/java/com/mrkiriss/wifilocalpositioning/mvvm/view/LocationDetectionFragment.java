package com.mrkiriss.wifilocalpositioning.mvvm.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.adapters.AutoCompleteAdapter;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentLocationDetectionBindingImpl;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.LocationDetectionViewModel;
import com.ortiz.touchview.TouchImageView;

public class LocationDetectionFragment extends Fragment {

    private TouchImageView touchImageView;
    private LocationDetectionViewModel viewModel;
    private FragmentLocationDetectionBindingImpl binding;

    private AutoCompleteAdapter autoCompleteAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel=new ViewModelProvider(this).get(LocationDetectionViewModel.class);

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_location_detection, container, false);
        binding.setViewModel(viewModel);

        createAndShowMapView();
        createSearchAdapterAndSetSettings();
        initObservers();
        viewModel.startFloorChanging();

        return binding.getRoot();
    }

    private void createAndShowMapView(){
        touchImageView=binding.zoomImageView;
        touchImageView.setMinZoom(1f);
        touchImageView.setMaxZoom(7f);
        touchImageView.setZoom(2f);
    }
    private void createSearchAdapterAndSetSettings(){
        autoCompleteAdapter=new AutoCompleteAdapter();
        binding.autoCompleteTextView.setAdapter(autoCompleteAdapter);
        binding.autoCompleteTextView2.setAdapter(autoCompleteAdapter);
        binding.autoCompleteTextView3.setAdapter(autoCompleteAdapter);
        binding.autoCompleteTextView3.setThreshold(2);
    }

    private void initObservers(){
        // прослушываем получение результата сканирования, вызываем обработчик данных
        viewModel.getCompleteKitsOfScansResult().observe(getViewLifecycleOwner(), scanResults -> viewModel.startProcessingCompleteKitsOfScansResult(scanResults));
        // прослушываем изменение пола, вызываем перерисовку
        viewModel.getRequestToChangeFloor().observe(getViewLifecycleOwner(), this::drawCurrentFloor);
        // прослышиваем запрос на изменение экрана с показом местоположения
        viewModel.getRequestToChangeFloorByMapPoint().observe(getViewLifecycleOwner(), mapPoint -> {
            // обновляем в адаптере для новой строки в поиске
            autoCompleteAdapter.setCurrentLocation(mapPoint.copy());

            // меняем номер во всей вьюмодели и вчастности на табло со стрелками
            viewModel.getFloorNumber().set(mapPoint.getFloorIdInt());

            showCurrentLocation(mapPoint);
        });
        // прослушиваем увеломления через Toast
        viewModel.getToastContent().observe(getViewLifecycleOwner(), this::showToastContent);
        // прослушываем запрос на обновление текущего этажа
        viewModel.getRequestToRefreshFloor().observe(getViewLifecycleOwner(), s -> {
            showToastContent(s);
            viewModel.startFloorChanging();
        });
        // прослушиваем запрос на добавление данных в строки поиска
        viewModel.getRequestToAddAllPointsDataInAutoFinders().observe(getViewLifecycleOwner(), data -> autoCompleteAdapter.setDataForFilter(data));
        // прослушываем состояние включения wifi
        viewModel.getWifiEnabledState().observe(getViewLifecycleOwner(), state->{
            if (!state) viewModel.showWifiOffering(getContext());
        });
        // просшлушываем запрос на скрытие клавиатуры
        viewModel.getRequestToHideKeyboard().observe(getViewLifecycleOwner(), s->hideKeyboard(requireActivity()));
        // прослушываем запрос на изменение состояние прогресса по построению маршрута
        viewModel.getRequestToUpdateProgressStatusBuildingRoute().observe(getViewLifecycleOwner(), progress->viewModel.getProgressOfBuildingRouteStatus().set(progress));
        // прослушываем изменением уровня доступа
        viewModel.getRequestToUpdateAccessLevel().observe(getViewLifecycleOwner(), al->autoCompleteAdapter.setAccessLevel(al));
    }

    private void showToastContent(String content){
        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
    }

    private void drawCurrentLocation(MapPoint mapPoint){
        if (mapPoint==null || mapPoint.getFloorWithPointer()==null || mapPoint.getFloorWithPointer().getFloorSchema()==null) return;

        touchImageView.setImageBitmap(mapPoint.getFloorWithPointer().getFloorSchema());
    }
    private void drawCurrentFloor(Floor floor){
        if (floor==null || floor.getFloorSchema()==null) return;

        touchImageView.setImageBitmap(floor.getFloorSchema());

    }
    private void showCurrentLocation(MapPoint mapPoint){

        hideKeyboard(requireActivity());

        viewModel.getFloorNumber().set(mapPoint.getFloorIdInt());
        drawCurrentLocation(mapPoint);
        Log.i("LocationDetectionFrg", "showCurrentLocation");

        float x = (float)mapPoint.getX()/mapPoint.getFloorWithPointer().getFloorSchema().getWidth();
        float y = (float)mapPoint.getY()/mapPoint.getFloorWithPointer().getFloorSchema().getHeight();
        Log.i("changeZoom", "x="+x+" y="+y);
        touchImageView.setZoom(6, x, y);

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}