package com.mrkiriss.wifilocalpositioning.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.databinding.ItemMapPointBinding;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.ItemMapPointViewModel;

import java.util.ArrayList;
import java.util.List;

public class MapPointsRVAdapter extends RecyclerView.Adapter<MapPointsRVAdapter.MapPointViewHolder>{

    private List<MapPoint> content;
    private final MutableLiveData<MapPoint> requestToDeleteMapPoint;

    public MapPointsRVAdapter(){
        content=new ArrayList<>();
        requestToDeleteMapPoint=new MutableLiveData<>();
    }

    public void setContent(List<MapPoint> content){
        this.content=content;
        notifyDataSetChanged();
    }
    public void deleteMapPoint(MapPoint mapPoint){
        int index=0;
        for (MapPoint currentMapPint: content){
            if (currentMapPint.equals(mapPoint)){
                content.remove(index);
                notifyItemRemoved(index);
                break;
            }
            index++;
        }
    }
    public void addMapPoint(MapPoint mapPoint){
        content.add(mapPoint);
        notifyItemInserted(content.size()-1);
    }

    @NonNull
    @Override
    public MapPointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMapPointBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_map_point, parent, false);
        return new MapPointViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MapPointViewHolder holder, int position) {
        holder.bindMapPoint(content.get(position));
    }

    @Override
    public int getItemCount() {
        return content.size();
    }

    class MapPointViewHolder extends RecyclerView.ViewHolder {
        private ItemMapPointBinding binding;

        public MapPointViewHolder(@NonNull ItemMapPointBinding binding) {
            super(binding.cardView);
            this.binding=binding;
        }

        public void bindMapPoint(MapPoint mapPoint){
            binding.setViewModel(new ItemMapPointViewModel(mapPoint, requestToDeleteMapPoint));
        }
    }

    public MutableLiveData<MapPoint> getRequestToDeleteMapPoint() {
        return requestToDeleteMapPoint;
    }
}
