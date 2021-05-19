package com.mrkiriss.wifilocalpositioning.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.server.ScanInformation;
import com.mrkiriss.wifilocalpositioning.databinding.ItemScanningInfoBinding;

import java.util.ArrayList;
import java.util.List;

public class ScanInfoRVAdapter extends RecyclerView.Adapter<ScanInfoRVAdapter.ScanInfoViewHolder> {

    private List<ScanInformation> content;

    public ScanInfoRVAdapter(){
        content =new ArrayList<>();
    }

    public void setContent(List<ScanInformation> content){
        this.content =content;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScanInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScanningInfoBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_scanning_info, parent, false);
        return new ScanInfoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanInfoViewHolder holder, int position) {
        holder.bindScanResult(content.get(position));
    }

    @Override
    public int getItemCount() {
        return content.size();
    }


    static class ScanInfoViewHolder extends RecyclerView.ViewHolder{

        private ItemScanningInfoBinding binding;

        public ScanInfoViewHolder(@NonNull ItemScanningInfoBinding binding) {
            super(binding.cardView);
            this.binding=binding;
        }

        public void bindScanResult(ScanInformation data){
            binding.setViewModel(data);
        }
    }
}
