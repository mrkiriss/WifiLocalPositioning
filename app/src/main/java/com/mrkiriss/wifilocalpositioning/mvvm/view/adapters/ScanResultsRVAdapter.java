package com.mrkiriss.wifilocalpositioning.mvvm.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrkiriss.wifilocalpositioning.R;

import java.util.ArrayList;
import java.util.List;

public class ScanResultsRVAdapter extends RecyclerView.Adapter<ScanResultsRVAdapter.ScanResultsViewHolder> {

    private List<String> scanResults;

    public ScanResultsRVAdapter(){
        scanResults=new ArrayList<>();
    }

    public void addToBack(String scanResult){
        scanResults.add(scanResult);
        notifyItemInserted(getItemCount());
    }
    public void setContent(List<String> scanResults){
        this.scanResults=scanResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScanResultsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_result, parent, false);
        return new ScanResultsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanResultsViewHolder holder, int position) {
        holder.bindScanResult(scanResults.get(position));
    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }


    static class ScanResultsViewHolder extends RecyclerView.ViewHolder{

        private View itemView;

        public ScanResultsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;

        }

        public void bindScanResult(String data){
            ((TextView) itemView.findViewById(R.id.textViewForScanResult)).setText(data);
        }
    }
}
