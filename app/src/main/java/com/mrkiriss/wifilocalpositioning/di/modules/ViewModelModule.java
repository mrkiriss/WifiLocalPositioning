package com.mrkiriss.wifilocalpositioning.di.modules;

import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.sources.ViewModelFactory;
import com.mrkiriss.wifilocalpositioning.di.annotations.ActivityScope;
import com.mrkiriss.wifilocalpositioning.di.annotations.ViewModelKey;
import com.mrkiriss.wifilocalpositioning.ui.detection.LocationDetectionViewModel;
import com.mrkiriss.wifilocalpositioning.ui.MainViewModel;
import com.mrkiriss.wifilocalpositioning.ui.search.SearchViewModel;
import com.mrkiriss.wifilocalpositioning.ui.training.SelectedMapPointViewModel;
import com.mrkiriss.wifilocalpositioning.ui.settings.SettingsViewModel;
import com.mrkiriss.wifilocalpositioning.ui.training.TrainingMapViewModel;
import com.mrkiriss.wifilocalpositioning.ui.scanconsole.TrainingScanViewModel;

import java.util.Map;

import javax.inject.Provider;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {

    @Provides
    @ActivityScope
    static ViewModelFactory provideViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> viewModels) {
        return new ViewModelFactory(viewModels);
    }

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    @ActivityScope
    abstract ViewModel bindMainViewModel(MainViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LocationDetectionViewModel.class)
    @ActivityScope
    abstract ViewModel bindLocationDetectionViewModel(LocationDetectionViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel.class)
    abstract ViewModel bindSearchViewModel(SearchViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(TrainingMapViewModel.class)
    @ActivityScope
    abstract ViewModel bindTrainingMapViewModel(TrainingMapViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SelectedMapPointViewModel.class)
    @ActivityScope
    abstract ViewModel bindSelectedMapPointViewModel(SelectedMapPointViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(TrainingScanViewModel.class)
    @ActivityScope
    abstract ViewModel bindTrainingScanViewModel(TrainingScanViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel.class)
    @ActivityScope
    abstract ViewModel bindSettingsViewModel(SettingsViewModel viewModel);
}
