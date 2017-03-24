package com.parthiv.sunshine.app;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Parthiv on 11/1/2016.
 */

public class CityInfoArrayAdapter extends ArrayAdapter<CityInfo> {

    public CityInfoArrayAdapter(Activity context, List<CityInfo> cityInfoList){
        super(context,0,cityInfoList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        CityInfo cityInfo = getItem(position);
        View rootView = convertView;
        if(rootView == null)
        {
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.autocomplete_item_location,parent,false);
        }

        TextView city_name = (TextView) rootView.findViewById(R.id.item_location);

        city_name.setText(cityInfo.city_name + " , " + cityInfo.country_code);
        return rootView;
    }
}
