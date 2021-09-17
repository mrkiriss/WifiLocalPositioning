package com.mrkiriss.wifilocalpositioning.view;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.adapters.SearchRVAdapter;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentSearchBinding;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.viewmodel.SearchViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import javax.inject.Inject;

public class SearchFragment extends Fragment{

    @Inject
    protected SearchViewModel viewModel;

    private FragmentSearchBinding binding;
    private SearchRVAdapter searchRVAdapter;

    private Fragment startFragment;

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

        // добавляем кнопку возврата на пред. фрагмент
        ((ISearchNavHost) requireActivity()).useUpButton();

        initSearchAdapter();
        initObservers();

        if (getArguments() != null) {
            // актуализируем данные для поиска
            viewModel.saveSearchData((SearchData) getArguments().getSerializable("searchData"));
            // запоминаем пред.фрагмент
            startFragment = (Fragment) getArguments().getSerializable("startFragment");
        }

        return binding.getRoot();
    }

    private void initObservers() {
        viewModel.getRequestToUpdateSearchContent().observe(getViewLifecycleOwner(), content -> searchRVAdapter.replaceContent(content));
        searchRVAdapter.getRequestToProcessSelectedLocation().observe(getViewLifecycleOwner(), selectedItem -> viewModel.processSelectedItem(selectedItem));
    }
    private void initSearchAdapter() {
        searchRVAdapter = new SearchRVAdapter();
        binding.findResultsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.findResultsRecyclerView.setAdapter(searchRVAdapter);
    }

}