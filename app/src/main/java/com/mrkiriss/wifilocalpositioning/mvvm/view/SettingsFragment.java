package com.mrkiriss.wifilocalpositioning.mvvm.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentSettingsBinding;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.SettingsViewModel;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private FragmentSettingsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel=new ViewModelProvider(this).get(SettingsViewModel.class);
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);

        initObservers();

        binding.setViewModel(viewModel);

        return binding.getRoot();
    }

    private void initObservers(){
        // подписываемся на требования по изменения обозреваемого поля интервала сканирования
        viewModel.getRequiredToUpdateScanInterval().observe(getViewLifecycleOwner(), content -> viewModel.getScanInterval().set(content));
        // подписываемся на требования по изменения обозреваемого поля количества сканирований
        viewModel.getRequiredToUpdateNumberOfScanning().observe(getViewLifecycleOwner(), content -> viewModel.getNumberOfScanning().set(content));
        // подписываемся на отоброжение тостов
        viewModel.getToastContent().observe(getViewLifecycleOwner(), this::showToastContent);
    }

    private void showToastContent(String content){
        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
    }
}
