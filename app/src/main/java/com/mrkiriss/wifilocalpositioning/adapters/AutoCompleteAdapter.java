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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoCompleteAdapter
        extends BaseAdapter implements Filterable {

    private List<MapPoint> findResult;
    private Map<Integer, MapPointFindViewHolder> addedViewsIndex;
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
        return findResult.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MapPointFindViewHolder holder;
        if (convertView==null) {
            holder = new MapPointFindViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_map_point_find, parent, false));
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            holder = (MapPointFindViewHolder) convertView.getTag();
//            Log.i("AutoCompleteAdapter", "find last View with hash="+convertView.hashCode()+" list addedViewsIndex="+addedViewsIndex);
        }
        holder.bindMapPoint(findResult.get(position));
        Log.i("AutoCompleteAdapter", "map point data = "+findResult.get(position).toStringAllObject());
        return holder.itemView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint!=null && !constraint.toString().isEmpty()){
                    Log.i("AutoCompleteAdapter", "Начата фильтрация для строки= "+constraint);
                    List<MapPoint> searchResult = selectSuitableMapPoints(constraint.toString());
                    filterResults.values=searchResult;
                    Log.i("AutoCompleteAdapter", "filtering result list="+ searchResult);
                    filterResults.count=searchResult.size();

                    addedViewsIndex=new HashMap<>();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values!=null && results.count>0){
                    findResult = (List<MapPoint>) results.values;
                    notifyDataSetChanged();
                }else{
                    notifyDataSetInvalidated();
                }
            }
        };
    }
    public List<MapPoint> selectSuitableMapPoints(String constraint){
        List<MapPoint> result = new ArrayList<>();

        if (mapPointsData==null) return result;

        for (FloorId floorId:mapPointsData.keySet()){
            for (MapPoint mapPoint:mapPointsData.get(floorId)){
                if (mapPoint.getRoomName().matches(constraint+".*?")){
                    result.add(mapPoint);
                }
            }
        }

        return result;
    }

    static class MapPointFindViewHolder extends RecyclerView.ViewHolder{
        private ItemMapPointFindBinding binding;

        public MapPointFindViewHolder(@NonNull ItemMapPointFindBinding binding) {
            super(binding.container);
            this.binding=binding;
        }

        public void bindMapPoint(MapPoint mapPoint){
            binding.setMapPoint(mapPoint);
        }
    }
}
