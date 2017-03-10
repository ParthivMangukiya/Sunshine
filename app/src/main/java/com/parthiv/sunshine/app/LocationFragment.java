package com.parthiv.sunshine.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Parthiv on 31/12/2016.
 */

public class LocationFragment extends Fragment {

    private static final String LOG_TAG = LocationFragment.class.getSimpleName();
    private CityInfoArrayAdapter cityInfoArrayAdapter;
    private ListView listView;

    public interface CallBack{
        public void onItemSelected();
    }


    public LocationFragment(){

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_location_search,container,false);
        listView = (ListView) rootView.findViewById(R.id.listview_city);
        cityInfoArrayAdapter = new CityInfoArrayAdapter(getActivity(), new ArrayList<CityInfo>());
        listView.setAdapter(cityInfoArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityInfo cityInfo = cityInfoArrayAdapter.getItem(position);
                if(cityInfo != null) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                    editor.putString(getString(R.string.pref_city_id_key), cityInfo.city_id);
                    editor.putString(getString(R.string.pref_city_name_key), cityInfo.city_name);
                    editor.putString(getString(R.string.pref_country_code_key), cityInfo.country_code);
                    editor.putLong(getString(R.string.pref_city_lat_key), Double.doubleToLongBits(cityInfo.city_lat));
                    editor.putLong(getString(R.string.pref_city_lon_key), Double.doubleToLongBits(cityInfo.city_long));
                    editor.apply();
                }
                ((CallBack)getActivity()).onItemSelected();
            }
        });
        return rootView;
    }

    public void onSearchTextChanged(String name){
        if(listView == null){
            return;
        }
        if(name == null || name.length() == 0){
            listView.setVisibility(View.GONE);
        }else{
            listView.setVisibility(View.VISIBLE);
        }
    }

    public void onPerformSearch(String name) {
        if(name.length() > 3) {
            listView.setVisibility(View.VISIBLE);
            new FetchCityTask(getActivity(), cityInfoArrayAdapter).execute("0", name);
        }
    }

}
