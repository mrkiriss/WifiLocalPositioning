package com.mrkiriss.wifilocalpositioning.ui.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.databinding.ItemFindBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SearchRVAdapter extends RecyclerView.Adapter<SearchRVAdapter.SearchViewHolder>{

    private SortedList<SearchItem> sortedList;

    private final MutableLiveData<SearchItem> requestToProcessSelectedLocation;
    public LiveData<SearchItem> getRequestToProcessSelectedLocation() {
        return requestToProcessSelectedLocation;
    }

    public SearchRVAdapter() {
        requestToProcessSelectedLocation = new MutableLiveData<>();

        initSortedList();
    }
    private void initSortedList() {
        sortedList = new SortedList<>(SearchItem.class, new SortedList.Callback<SearchItem>() {
            @Override
            public int compare(SearchItem a, SearchItem b) {
                return a.compareTo(b);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(SearchItem oldItem, SearchItem newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(SearchItem item1, SearchItem item2) {
                return item1.getName().equals(item2.getName());
            }
        });
    }

    public void replaceContent(List<SearchItem> models) {
        sortedList.beginBatchedUpdates();
        for (int i = sortedList.size() - 1; i >= 0; i--) {
            final SearchItem model = sortedList.get(i);
            if (!models.contains(model)) {
                sortedList.remove(model);
            }
        }
        sortedList.addAll(models);
        sortedList.endBatchedUpdates();
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
        SearchItem item = sortedList.get(position);
        item.setRequestToProcessSelectedLocation(requestToProcessSelectedLocation);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return sortedList.size();
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
