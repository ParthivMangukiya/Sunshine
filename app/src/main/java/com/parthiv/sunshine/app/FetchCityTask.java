package com.parthiv.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Parthiv on 01/01/2017.
 */

public class FetchCityTask extends AsyncTask<String,Void,CityInfo[]> {

    boolean byCityName = true;
    private final Context mContext;
    private CityInfoArrayAdapter cityInfoArrayAdapter;
    private static final String LOG_TAG = FetchCityTask.class.getSimpleName();
    public FetchCityTask(Context context,CityInfoArrayAdapter mCityInfoArrayAdapter){
        mContext = context;
        cityInfoArrayAdapter = mCityInfoArrayAdapter;
    }

    public FetchCityTask(Context context){
        mContext = context;
    }
    @Override
    protected CityInfo[] doInBackground(String... params) {
        if(params.length == 0)
        {
            return null;
        }

        byCityName = params[0].equals("0");
        String locationQuery = "";
        String latitude = "";
        String longitude = "";
        if(byCityName) {
            locationQuery  = params[1];
        }
        else{
            latitude = params[1];

            longitude = params[2];
        }
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String cityJsonStr = null;

        String format = "json";

        String type = "like";

        String cnt = "1";



        try{
            final String CITY_BASE_URL = "http://api.openweathermap.org/data/2.5/find?";
            final String QUERY_PARAM = "q";
            final String LAT_PARAM = "lat";
            final String LON_PARAM = "lon";
            final String FORMAT_PARAM = "mode";
            final String APPID_PARAM = "appid";
            final String TYPE_PARAM = "type";
            final String CNT_PARAM = "cnt";

            Uri builtUri;
            if(byCityName) {

                builtUri = Uri.parse(CITY_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, locationQuery)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(TYPE_PARAM, type)
                        .appendQueryParameter(APPID_PARAM, String.valueOf(R.string.openweathermap_api_key))
                        .build();
            }else {

                builtUri = Uri.parse(CITY_BASE_URL).buildUpon()
                        .appendQueryParameter(LAT_PARAM, latitude)
                        .appendQueryParameter(LON_PARAM, longitude)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(TYPE_PARAM, type)
                        .appendQueryParameter(CNT_PARAM, cnt)
                        .appendQueryParameter(APPID_PARAM, String.valueOf(R.string.openweathermap_api_key))
                        .build();
            }
            URL url = new URL(builtUri.toString());

            Log.d(LOG_TAG,url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            if(inputStream == null)
            {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine()) != null)
            {
                builder.append(line);
                builder.append("\n");
            }
            if(builder.length() == 0)
            {
                return null;
            }
            cityJsonStr = builder.toString();
            return getCityDataFromJson(cityJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private CityInfo[] getCityDataFromJson(String cityJsonStr) {

        final String OWM_LIST = "list";
        final String OWM_CITY_ID = "id";
        final String OWM_CITY_NAME = "name";
        final String OWM_SYS = "sys";
        final String OWM_COUNTRY = "country";
        final String OWM_CITY_COORD = "coord";
        final String OWM_CITY_LAT = "lat";
        final String OWM_CITY_LONG = "lon";
        try {

            String city_id;
            String city_name;
            String country_code;
            double city_lat;
            double city_lon;
            JSONObject forecastJson = new JSONObject(cityJsonStr);
            JSONArray cityArray = forecastJson.getJSONArray(OWM_LIST);

            CityInfo[]  resultArray = new CityInfo[cityArray.length()];

            for(int i=0;i < cityArray.length();i++)
            {
                JSONObject city = cityArray.getJSONObject(i);

                city_id = String.valueOf(city.getInt(OWM_CITY_ID));
                city_name = city.getString(OWM_CITY_NAME);
                JSONObject country = city.getJSONObject(OWM_SYS);
                country_code = country.getString(OWM_COUNTRY);

                JSONObject coord = city.getJSONObject(OWM_CITY_COORD);
                city_lat = coord.getDouble(OWM_CITY_LAT);
                city_lon = coord.getDouble(OWM_CITY_LONG);

                resultArray[i] = new CityInfo(city_name,city_id,country_code,city_lat,city_lon);
                Log.d(LOG_TAG,resultArray[i].toString());
            }

            return resultArray;
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(CityInfo[] cityInfos) {
        if(cityInfos != null){
            if(byCityName) {
                cityInfoArrayAdapter.clear();
                for (CityInfo cityInfo : cityInfos) {
                    cityInfoArrayAdapter.add(cityInfo);
                }
            }else{
                CityInfo cityInfo = cityInfos[0];
                if(cityInfo != null) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                    editor.putString(mContext.getString(R.string.pref_city_id_key), cityInfo.city_id);
                    editor.putString(mContext.getString(R.string.pref_city_name_key), cityInfo.city_name);
                    editor.putString(mContext.getString(R.string.pref_country_code_key), cityInfo.country_code);
                    editor.putLong(mContext.getString(R.string.pref_city_lat_key), Double.doubleToLongBits(cityInfo.city_lat));
                    editor.putLong(mContext.getString(R.string.pref_city_lon_key), Double.doubleToLongBits(cityInfo.city_long));
                    editor.apply();
                }
            }
        }
    }
}
