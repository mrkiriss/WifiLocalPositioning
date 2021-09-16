package com.mrkiriss.wifilocalpositioning.di.modules.search;

import com.mrkiriss.wifilocalpositioning.data.repositiries.SearchRepository;
import com.mrkiriss.wifilocalpositioning.viewmodel.SearchViewModel;

import dagger.Module;
import dagger.Provides;

@Module
public class SearchRepositoryAndVM {

    @Provides
    SearchRepository provideSearchRepo(){
        return new SearchRepository();
    }

    @Provides
    SearchViewModel provideSearchVM(SearchRepository rep){
        return new SearchViewModel(rep);
    }
}
