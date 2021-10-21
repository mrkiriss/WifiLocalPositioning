package com.mrkiriss.wifilocalpositioning.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.data.models.search.TypeOfSearchRequester;
import com.mrkiriss.wifilocalpositioning.data.sources.ViewModelFactory;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentSearchBinding;
import com.mrkiriss.wifilocalpositioning.di.App;

import java.io.Serializable;

import javax.inject.Inject;

public class SearchFragment extends Fragment{

    @Inject
    protected ViewModelFactory viewModelFactory;

    private SearchViewModel viewModel;

    private FragmentSearchBinding binding;
    private SearchRVAdapter searchRVAdapter;

    private TypeOfSearchRequester typeOfRequester;

    public static SearchFragment newInstance(Fragment startFragment, SearchData searchData) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putSerializable("startFragment", (Serializable) startFragment);
        args.putSerializable("searchData", searchData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        App.getInstance().getComponentManager().getViewModelSubcomponent().inject(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(SearchViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        binding.setViewModel(viewModel);

        initSearchAdapter();
        initObservers();

        if (getArguments() != null) {
            // актуализируем данные для поиска
            SearchData data = (SearchData) getArguments().getSerializable("searchData");
            viewModel.saveSearchData(data);
            // запоминаем тип источника
            typeOfRequester = data.getTypeOfRequester();
        }

        viewModel.initEmptyInput();

        return binding.getRoot();
    }

    private void initSearchAdapter() {
        searchRVAdapter = new SearchRVAdapter();
        binding.findResultsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.findResultsRecyclerView.setAdapter(searchRVAdapter);
    }
    private void initObservers() {
        // подписываемся на обновление контента в строке поиска
        viewModel.getRequestToUpdateSearchContent().observe(getViewLifecycleOwner(), content -> searchRVAdapter.replaceContent(content));
        // подписываемся на обработку выбора пользователя
        searchRVAdapter.getRequestToProcessSelectedLocation().observe(getViewLifecycleOwner(), this::processSelectedItem);
        // подписываемся на запрос на обновление информации о состоянии поиска
        viewModel.getRequestToUpdateSearchInformation().observe(getViewLifecycleOwner(), content -> viewModel.getSearchInformation().set(content));
    }

    private void processSelectedItem(SearchItem selectedSearchItem) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedSearchItem", selectedSearchItem);
        bundle.putSerializable("typeOfRequester", typeOfRequester);

        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_nav_search_to_nav_definition, bundle);
    }
}