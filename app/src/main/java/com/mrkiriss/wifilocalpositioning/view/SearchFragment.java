package com.mrkiriss.wifilocalpositioning.view;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.adapters.SearchRVAdapter;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.data.models.search.TypeOfSearchRequester;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentSearchBinding;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.viewmodel.SearchViewModel;

import java.io.Serializable;

import javax.inject.Inject;

public class SearchFragment extends Fragment{

    @Inject
    protected SearchViewModel viewModel;

    private FragmentSearchBinding binding;
    private SearchRVAdapter searchRVAdapter;

    private Fragment startFragment;
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
        Log.i("searchMode", "start onCreateView SearchFragment this args: " + getArguments());

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        App.getInstance().getComponentManager().getSearchSubcomponent().inject(this);
        binding.setViewModel(viewModel);

        initSearchAdapter();
        initObservers();

        if (getArguments() != null) {
            // актуализируем данные для поиска
            SearchData data = (SearchData) getArguments().getSerializable("searchData");
            viewModel.saveSearchData(data);
            // запоминаем пред.фрагмент и тип источника
            startFragment = (Fragment) getArguments().getSerializable("startFragment");
            typeOfRequester = data.getTypeOfRequester();
        }

        return binding.getRoot();
    }

    private void initObservers() {
        viewModel.getRequestToUpdateSearchContent().observe(getViewLifecycleOwner(), content -> searchRVAdapter.replaceContent(content));
        searchRVAdapter.getRequestToProcessSelectedLocation().observe(getViewLifecycleOwner(), this::processSelectedItem);
    }
    private void initSearchAdapter() {
        searchRVAdapter = new SearchRVAdapter();
        binding.findResultsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.findResultsRecyclerView.setAdapter(searchRVAdapter);
    }

    private void processSelectedItem(SearchItem selectedSearchItem) {
        ((IProcessingSelectedByFindLocation) startFragment).processSelectedByFindLocation(typeOfRequester, selectedSearchItem);

        ((IUpButtonNavHost) requireActivity()).navigateBack(this, startFragment);
    }
}