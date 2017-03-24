package com.parthiv.sunshine.app;

/**
 * Created by Parthiv on 10/09/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.parthiv.sunshine.app.data.WeatherContract;
import com.parthiv.sunshine.app.sync.SunshineSyncAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    //@return this method return selected location id.
    public static String getPreferredLocationCityId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_city_id_key),context.getString(R.string.pref_city_id_def));
    }

    public static boolean checkLocationInDatabase(Context context)
    {
        boolean ret=true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String location = prefs.getString(context.getString(R.string.pref_city_name_key),
                context.getString(R.string.pref_city_name_def));
        Cursor cursor = context.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING},
                WeatherContract.LocationEntry.COLUMN_CITY_NAME + " = ? ",
                new String[]{location},
                null);
        if (cursor == null || !cursor.moveToFirst() || cursor.getCount() == 0) {
                ret = false;
        }else{
            cursor.close();
        }
        return ret;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //@return this method return selected location name as String.

    public static String getPrefLocation(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_city_name_key),
                context.getString(R.string.pref_city_name_def));
    }

    /* @return this method return location ,country code as String.
     */
    public  static  String getPrefLocationWithCountryCode(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_city_name_key),
                context.getString(R.string.pref_city_name_def)) + "," + prefs.getString(context.getString(R.string.pref_country_code_key),context.getString(R.string.pref_country_code_def));
    }

    public static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
        } else {
            temp = temperature;
        }
        return context.getString(R.string.format_temperature, temp);
    }


    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For <></>he next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Calendar calendar = Calendar.getInstance();
        int currentJulianDay = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTimeInMillis(dateInMillis);
        int julianDay = calendar.get(Calendar.DAY_OF_YEAR);


        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(formatId),
                    today,
                    getFormattedMonthDay(context, dateInMillis));
        } else if ( julianDay < currentJulianDay + 7 ) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", Locale.getDefault());
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Calendar calendar = Calendar.getInstance();
        int currentJulianDay = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTimeInMillis(dateInMillis);
        int julianDay = calendar.get(Calendar.DAY_OF_YEAR);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        } else {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE",Locale.getDefault());
            return dayFormat.format(dateInMillis);
        }
    }


    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd",Locale.getDefault());
        return monthDayFormat.format(dateInMillis);
    }


    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pre_unit_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        final String[] directionsText = { "N", "NE", "E", "SE", "S", "SW", "W", "NW" };
        final int DEGREES_TOTAL = 360;
        final int DIR_TOTAL = 8;

        String direction = directionsText[Math.round(degrees / (DEGREES_TOTAL / DIR_TOTAL)) % DIR_TOTAL];

        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    /**
     * Helper method to provide the art urls according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param context Context to use for retrieving the URL format
     * @param weatherId from OpenWeatherMap API response
     * @return url for the corresponding weather artwork. null if no relation is found.
     */
    public static String getArtUrlForWeatherCondition(Context context, int weatherId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String formatArtUrl = prefs.getString(context.getString(R.string.pref_art_pack_key),
                context.getString(R.string.pref_art_pack_sunshine));

        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return String.format(Locale.US, formatArtUrl, "storm");
        } else if (weatherId >= 300 && weatherId <= 321) {
            return String.format(Locale.US, formatArtUrl, "light_rain");
        } else if (weatherId >= 500 && weatherId <= 504) {
            return String.format(Locale.US, formatArtUrl, "rain");
        } else if (weatherId == 511) {
            return String.format(Locale.US, formatArtUrl, "snow");
        } else if (weatherId >= 520 && weatherId <= 531) {
            return String.format(Locale.US, formatArtUrl, "rain");
        } else if (weatherId >= 600 && weatherId <= 622) {
            return String.format(Locale.US, formatArtUrl, "snow");
        } else if (weatherId >= 701 && weatherId <= 761) {
            return String.format(Locale.US, formatArtUrl, "fog");
        } else if (weatherId == 761 || weatherId == 781) {
            return String.format(Locale.US, formatArtUrl, "storm");
        } else if (weatherId == 800) {
            return String.format(Locale.US, formatArtUrl, "clear");
        } else if (weatherId == 801) {
            return String.format(Locale.US, formatArtUrl, "light_clouds");
        } else if (weatherId >= 802 && weatherId <= 804) {
            return String.format(Locale.US, formatArtUrl, "clouds");
        }
        return null;
    }

    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_rain;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    public static boolean isNetworkAvailable(Context c){
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activateNetwork = cm.getActiveNetworkInfo();
        return  activateNetwork != null && activateNetwork.isConnectedOrConnecting();
    }

    @SuppressWarnings("ResourceType")
    public static @SunshineSyncAdapter.LocationStatus
    int getLocationStatus(Context c){
        SharedPreferences sp  = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_location_status_key), SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }

}