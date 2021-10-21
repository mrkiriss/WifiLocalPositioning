package com.mrkiriss.wifilocalpositioning.ui.scanconsole;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.sources.ViewModelFactory;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentTrainingScanBinding;
import com.mrkiriss.wifilocalpositioning.di.App;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class TrainingScanFragment extends Fragment {

    @Inject
    protected ViewModelFactory viewModelFactory;

    private TrainingScanViewModel viewModel;
    private FragmentTrainingScanBinding binding;
    private ScanResultsRVAdapter adapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        App.getInstance().getComponentManager().getViewModelSubcomponent().inject(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(TrainingScanViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_training_scan, container, false);
        binding.setTrainingVM(viewModel);

        adapter=new ScanResultsRVAdapter();
        binding.scanResultsRecyclerView.setAdapter(adapter);
        binding.scanResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        initObservers();

        return binding.getRoot();
    }

    private void initObservers(){
        // подписавыемся на очередные небоработанные результаты сканирования
        viewModel.getCompleteKitsOfScansResult().observe(getViewLifecycleOwner(), event -> {
            if (event.isNotHandled()) viewModel.startProcessingCompleteKitsOfScansResult(event.getValue());
        });
        // подписываемся на добавление обработанных результатов сканирвоания на экран
        viewModel.getRequestToAddAPs().observe(getViewLifecycleOwner(), this::addKitOfAPsOnRecyclerView);
        // подписываемся на результат калибровки наборов сканирования
        viewModel.getResultOfScanningAfterCalibration().observe(getViewLifecycleOwner(), s -> {
            if (s==null) return;
            binding.scanningResult.setText("Получен результат от сервера:\n"+s);
            viewModel.changeScanningStatus(false);
            //viewModel.resetElements();
        });
        //подписываемся на контент для тостов
        viewModel.getToastContent().observe(getViewLifecycleOwner(), s -> Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());
        // подписываемся на очищение rv
        viewModel.getRequestToClearRV().observe(getViewLifecycleOwner(), strings -> adapter.setContent(strings));
        // подписываемся на изменение количества оставшихся сканирований
        viewModel.getRemainingNumberOfScanningLD().observe(getViewLifecycleOwner(), integer -> viewModel.getRemainingNumberOfScanning().set(integer));
    }

    private void addKitOfAPsOnRecyclerView(String apInfo){
        adapter.addToBack(apInfo);
    }
}