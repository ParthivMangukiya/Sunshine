package com.parthiv.sunshine.app;

/**
 * Created by Parthiv on 11/1/2016.
 */

public class CityInfo {
    String city_name;
    String city_id;
    String country_code;
    double city_long;
    double city_lat;
    public CityInfo(String city_name,String city_id,String country_code,double city_lat,double city_long){
        this.city_name = city_name;
        this.city_id = city_id;
        this.country_code = country_code;
        this.city_lat = city_lat;
        this.city_long = city_long;
    }

    @Override
    public String toString() {
        return this.city_name + "," + country_code + "," + city_id + "," + city_lat + "," + city_long;
    }
}
