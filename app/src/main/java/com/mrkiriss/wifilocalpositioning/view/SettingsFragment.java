package com.mrkiriss.wifilocalpositioning.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.sources.ViewModelFactory;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentSettingsBinding;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.viewmodel.SettingsViewModel;

import javax.inject.Inject;

public class SettingsFragment extends Fragment {

    @Inject
    protected ViewModelFactory viewModelFactory;

    private SettingsViewModel viewModel;
    private FragmentSettingsBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        App.getInstance().getComponentManager().getViewModelSubcomponent().inject(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(SettingsViewModel.class);

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);

        initObservers();

        binding.setViewModel(viewModel);

        return binding.getRoot();
    }

    private void initObservers(){
        // подписываемся на требования по изменения обозреваемого поля интервала сканирования
        viewModel.getRequestToUpdateScanInterval().observe(getViewLifecycleOwner(), content -> viewModel.getScanInterval().set(content));
        // подписываемся на требования по изменения обозреваемого поля количества сканирований
        viewModel.getRequestToUpdateNumberOfScanning().observe(getViewLifecycleOwner(), content -> viewModel.getNumberOfScanning().set(content));
        // подписываемся на требования по изменения обозреваемого поля уровня доступа
        viewModel.getRequestToUpdateAccessLevel().observe(getViewLifecycleOwner(), content -> viewModel.getAccessLevel().set(content));
        // подписываемся на получение UUID для буфера обмена
        viewModel.getRequestToUpdateCopyUUID().observe(getViewLifecycleOwner(), content -> {
            showToastContent("ID copied");
            setContentToChangeBuffer(content);
        });
        // подписываемся на отоброжение тостов
        viewModel.getToastContent().observe(getViewLifecycleOwner(), this::showToastContent);
    }

    private void showToastContent(String content){
        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
    }
    private void setContentToChangeBuffer(String content){
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("id", content);
        clipboard.setPrimaryClip(clip);
    }
}
