package com.mrkiriss.wifilocalpositioning.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.databinding.ItemFindBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchRVAdapter extends RecyclerView.Adapter<SearchRVAdapter.SearchViewHolder>{

    private List<SearchItem> content;

    public SearchRVAdapter() {
        content = new ArrayList<>();
    }

    public void replaceContent(List<SearchItem> newContent) {
        Log.i("searchMode", "adapter got newContent = " + newContent);
        content = newContent;
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new SearchViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_find, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchViewHolder holder, int position) {
        holder.bind(content.get(position));
    }

    @Override
    public int getItemCount() {
        return content.size();
    }

    protected static class SearchViewHolder extends RecyclerView.ViewHolder{

        private final ItemFindBinding binding;

        public SearchViewHolder(@NonNull @NotNull ItemFindBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void bind(SearchItem searchItem) {
            binding.setViewModel(searchItem);
        }
    }
}
