package com.mrkiriss.wifilocalpositioning.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.databinding.ItemMapPointFindBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AutoCompleteAdapter
        extends BaseAdapter implements Filterable {

    private List<MapPoint> findResult;
    private Map<FloorId, List<MapPoint>> mapPointsData;

    public AutoCompleteAdapter(){
        this.findResult=new ArrayList<>();
    }
    public void setDataForFilter(Map<FloorId, List<MapPoint>> mapPointsData){
        this.mapPointsData=mapPointsData;
    }

    @Override
    public int getCount() {
        return findResult.size();
    }

    @Override
    public Object getItem(int position) {
        return findResult.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MapPointFindViewHolder holder;
        if (convertView == null) {
            holder = new MapPointFindViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_map_point_find, parent, false));
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            holder = (MapPointFindViewHolder) convertView.getTag();
        }
        holder.bindMapPoint(findResult.get(position));
        return holder.itemView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint!=null && !constraint.toString().isEmpty()){
                    Log.i("AutoCompleteFiltering", "Начата фильтрация для строки= "+constraint);
                    List<MapPoint> searchResult = selectSuitableMapPoints(constraint);
                    filterResults.values=searchResult;
                    filterResults.count=searchResult.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values!=null && results.count>0){
                    findResult = (List<MapPoint>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }

    public List<MapPoint> selectSuitableMapPoints(CharSequence constraint){
        List<MapPoint> result = new ArrayList<>();

        if (mapPointsData==null) return result;

        for (FloorId floorId:mapPointsData.keySet()){
            for (MapPoint mapPoint:mapPointsData.get(floorId)){
                if (mapPoint.getRoomName().contains(constraint)){
                    result.add(mapPoint);
                }
            }
        }

        return result;
    }

    static class MapPointFindViewHolder extends RecyclerView.ViewHolder{
        private ItemMapPointFindBinding binding;

        public MapPointFindViewHolder(@NonNull ItemMapPointFindBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }

        public void bindMapPoint(MapPoint mapPoint){
            binding.setMapPoint(mapPoint);
        }
    }
}
