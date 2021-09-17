package com.mrkiriss.wifilocalpositioning.view;

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
import androidx.navigation.fragment.NavHostFragment;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.data.models.search.TypeOfSearchRequester;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentLocationDetectionBindingImpl;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.viewmodel.LocationDetectionViewModel;
import com.ortiz.touchview.TouchImageView;

import java.io.Serializable;

import javax.inject.Inject;

public class LocationDetectionFragment extends Fragment implements Serializable, IProcessingSelectedByFindLocation {

    @Inject
    protected LocationDetectionViewModel viewModel;
    private FragmentLocationDetectionBindingImpl binding;

    private TouchImageView touchImageView;

    //private AutoCompleteAdapter autoCompleteAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        App.getInstance().getComponentManager().getLocationDetectionSubcomponent().inject(this);

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_location_detection, container, false);
        binding.setViewModel(viewModel);

        createAndShowMapView();
        initObservers();
        viewModel.startFloorChanging();
        NavHostFragment.findNavController(this);

        return binding.getRoot();
    }

    private void createAndShowMapView(){
        touchImageView=binding.zoomImageView;
        touchImageView.setMinZoom(1f);
        touchImageView.setMaxZoom(7f);
        touchImageView.setZoom(2f);
    }

    private void initObservers(){
        // прослушиваем получение результата сканирования, вызываем обработчик данных
        viewModel.getCompleteKitsOfScansResult().observe(getViewLifecycleOwner(), scanResults -> viewModel.startProcessingCompleteKitsOfScansResult(scanResults));
        // прослушиваем изменение пола, вызываем перерисовку
        viewModel.getRequestToChangeFloor().observe(getViewLifecycleOwner(), this::drawCurrentFloor);
        // прослышиваем запрос на изменение экрана с показом местоположения
        viewModel.getRequestToChangeFloorByMapPoint().observe(getViewLifecycleOwner(), this::showCurrentLocation);
        // прослушываем запрос на обновление местоположения для строки автодополнения и ...
            // в меню маршрутизации при условии его скрытия в данный момент
        /*viewModel.getRequestToUpdateCurrentLocationOnAutoComplete().observe(getViewLifecycleOwner(), mapPoint -> {
            // обновляем в адаптере для новой строки в поиске
            autoCompleteAdapter.setCurrentLocation(mapPoint);
        });*/
        // прослушиваем обновление строки точки старта в меню построения маршрута
        /*viewModel.getRequestToChangeDepartureInput().observe(getViewLifecycleOwner(), departureInput->{
            if (!viewModel.getShowRoute().get())viewModel.getDepartureInput().set(departureInput);
        });*/
        // прослушиваем обновление строки точки конца в меню построения маршрута
        /*viewModel.getRequestToChangeDestinationInput().observe(getViewLifecycleOwner(), destinationInput->{
            if (!viewModel.getShowRoute().get())viewModel.getDestinationInput().set(destinationInput);
        });*/
        // прослушиваем увеломления через Toast
        viewModel.getToastContent().observe(getViewLifecycleOwner(), this::showToastContent);
        // прослушиваем запрос на обновление текущего этажа
        viewModel.getRequestToRefreshFloor().observe(getViewLifecycleOwner(), s -> {
            showToastContent(s);
            viewModel.startFloorChanging();
        });
        // прослушиваем запрос на добавление данных в строки поиска
        //viewModel.getRequestToAddAllPointsDataInAutoFinders().observe(getViewLifecycleOwner(), data -> autoCompleteAdapter.setDataForFilter(data));
        // прослушиваем состояние включения wifi
        viewModel.getWifiEnabledState().observe(getViewLifecycleOwner(), state->{
            if (!state) viewModel.showWifiOffering(getContext());
        });
        // прослушиваем запрос на скрытие клавиатуры
        viewModel.getRequestToHideKeyboard().observe(getViewLifecycleOwner(), s->hideKeyboard(requireActivity()));
        // прослушиваем запрос на изменение состояние прогресса по построению маршрута
        viewModel.getRequestToUpdateProgressStatusBuildingRoute().observe(getViewLifecycleOwner(), progress->viewModel.getProgressOfBuildingRouteStatus().set(progress));
        // прослушиваем изменением уровня доступа
        //viewModel.getRequestToUpdateAccessLevel().observe(getViewLifecycleOwner(), al->autoCompleteAdapter.setAccessLevel(al));
        // прослушываем запрос на запуск фрагмента поиска локации
        viewModel.getRequestToLaunchSearchMode().observe(getViewLifecycleOwner(), this::launchSearchModeFragment);
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

        float x = (float) mapPoint.getX() / mapPoint.getFloorWithPointer().getFloorSchema().getWidth();
        float y = (float) mapPoint.getY() / mapPoint.getFloorWithPointer().getFloorSchema().getHeight();
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

    private void launchSearchModeFragment(SearchData data) {
        ((IUpButtonNavHost) requireActivity()).navigateTo(this, SearchFragment.newInstance(this, data), "searchFragment");
    }
    @Override
    public void processSelectedByFindLocation(TypeOfSearchRequester typeOfRequester, SearchItem selectedSearchItem) {
        Log.i("searchMode", "start processing selectedSearchItem= "+selectedSearchItem.toString()+" in LocDefFragment");

        viewModel.
    }
}