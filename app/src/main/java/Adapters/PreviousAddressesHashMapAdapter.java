package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Map;

import upp.foodonet.material.R;

/**
 * Created by artyomshapet on 5/31/16.
 */
public class PreviousAddressesHashMapAdapter extends BaseAdapter {
    private final ArrayList mData;

    public PreviousAddressesHashMapAdapter(Map<String, LatLng> map) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, LatLng> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_dialog_used_list_item, parent, false);
        } else {
            result = convertView;
        }

        Map.Entry<String, LatLng> item = getItem(position);

        ((TextView) result.findViewById(R.id.tv_address_history_item_text)).setText(item.getKey());

        return result;
    }


}