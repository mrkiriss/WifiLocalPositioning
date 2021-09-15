package com.mrkiriss.wifilocalpositioning.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;

import java.io.Serializable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationNameChoosingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationNameChoosingFragment extends Fragment {


    public static LocationNameChoosingFragment newInstance(Fragment startFragment, SearchData searchData) {
        LocationNameChoosingFragment fragment = new LocationNameChoosingFragment();
        Bundle args = new Bundle();
        args.putSerializable("startFragment", (Serializable) startFragment);
        args.putSerializable("searchData", searchData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("searchMode", "start onCreateView this args: " + getArguments());

        ((INameChoosingNavHost) requireActivity()).useUpButton();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_name_choosing, container, false);
    }

}