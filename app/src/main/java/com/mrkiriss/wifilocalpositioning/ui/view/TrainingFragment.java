package com.mrkiriss.wifilocalpositioning.ui.view;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.sources.wifi.OverrideScanningSourceType;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentTrainingBinding;
import com.mrkiriss.wifilocalpositioning.ui.view.adapters.ScanResultsRVAdapter;
import com.mrkiriss.wifilocalpositioning.ui.viewmodel.TrainingViewModel;

import java.util.List;

public class TrainingFragment extends Fragment {

    private FragmentTrainingBinding binding;
    private TrainingViewModel viewModel;
    private ScanResultsRVAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_training, container, false);
        viewModel=new ViewModelProvider(this).get(TrainingViewModel.class);
        binding.setTrainingVM(viewModel);

        adapter=new ScanResultsRVAdapter();
        binding.scanResultsRecyclerView.setAdapter(adapter);
        binding.scanResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        initObservers();

        return binding.getRoot();
    }

    private void initObservers(){
        // подписавыемся на очередные небоработанные результаты сканирования
        viewModel.getCompleteKitsOfScansResult().observe(getViewLifecycleOwner(), scanResults -> viewModel.startProcessingCompleteKitsOfScansResult(scanResults));
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