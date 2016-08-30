package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import upp.foodonet.material.R;

/**
 * Created by Asher on 23.05.2016.
 */
public class MapMarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    LayoutInflater inflater = null;

    public MapMarkerInfoWindowAdapter(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View popup = inflater.inflate(R.layout.marker_info_window_item, null);
        TextView tv = (TextView)popup.findViewById(R.id.tv_map_marker_info_text);
        tv.setText(marker.getTitle());
        return popup;
    }
}