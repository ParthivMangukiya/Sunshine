package com.parthiv.sunshine.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class LocationActivity extends AppCompatActivity implements LocationFragment.CallBack,OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CHECK_SETTINGS = 0;
    private static final int MY_LOCATION_PERMISSON_COARSE_LOCATION = 1;
    private static final String MAP_FRAGMENT_TAG = "mapTag";
    private static final String LIST_FRAGMENT_TAG = "listTag";
    private final String LOG_TAG = LocationActivity.class.getSimpleName();
//    private CityInfoArrayAdapter cityInfoArrayAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private GoogleMap mMap;
    private SupportMapFragment supportMapFragment;
    private LocationFragment locationFragment;
    private EditText textView;
    private FloatingActionButton fButton;
    private ImageButton search_button;
    private ImageButton back_button;
    private ImageButton home_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().hide();


        supportMapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container,supportMapFragment,MAP_FRAGMENT_TAG).commit();
        supportMapFragment.getMapAsync(this);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        textView = (EditText) findViewById(R.id.editTextSearch);
        fButton = (FloatingActionButton) findViewById(R.id.floatingLocationButton);
        search_button = (ImageButton) findViewById(R.id.action_search_button);
        back_button = (ImageButton) findViewById(R.id.action_back_button);
        home_button = (ImageButton) findViewById(R.id.action_home_button);

        textView.setText(Utility.getPrefLocationWithCountryCode(getApplication()));

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitFragment(true);
            }
        });

        search_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(locationFragment != null){
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    textView.clearFocus();
                    locationFragment.onPerformSearch(textView.getText().toString());
                }
            }
        });

        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(supportMapFragment != null) {
                    transitFragment(false);
                }
                return false;
            }
        });


        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(locationFragment != null){
                    locationFragment.onSearchTextChanged(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String text = v.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(locationFragment != null){
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                        textView.clearFocus();
                        locationFragment.onPerformSearch(text);
                    }
                }
                return false;
            }
        });

        fButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleApiClient.connect();
            }
        });

        home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onItemSelected() {
        transitFragment(true);
    }

    @Override
    public void onBackPressed() {
        if(locationFragment != null){
            transitFragment(true);
        }else {
            super.onBackPressed();
        }
    }

    public void transitFragment(boolean mapFrgmentTransit){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(mapFrgmentTransit){
            if(supportMapFragment == null) {
                if(locationFragment != null){
                    supportMapFragment = new SupportMapFragment();
                    fragmentTransaction.replace(R.id.container,supportMapFragment,MAP_FRAGMENT_TAG).commit();
                    back_button.setVisibility(View.GONE);
                    home_button.setVisibility(View.VISIBLE);
                    search_button.setVisibility(View.GONE);
                    textView.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    fButton.setVisibility(View.VISIBLE);
                    textView.setText(Utility.getPrefLocationWithCountryCode(getApplication()));
                    supportMapFragment.getMapAsync(this);
                    locationFragment = null;
                }
            }
        }else{
            if(locationFragment == null) {
                if(supportMapFragment != null)
                {
                    locationFragment = new LocationFragment();
                    fragmentTransaction.replace(R.id.container,locationFragment,LIST_FRAGMENT_TAG);
//                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    textView.setHint(textView.getText());
                    textView.setText("");
                    back_button.setVisibility(View.VISIBLE);
                    home_button.setVisibility(View.GONE);
                    search_button.setVisibility(View.VISIBLE);
                    fButton.setVisibility(View.GONE);
                    supportMapFragment = null;
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        changeLocationOnMap();
    }

    protected void changeLocationOnMap(){
        if(supportMapFragment != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
            double latitude = Double.longBitsToDouble(prefs.getLong(getString(R.string.pref_city_lat_key), Double.doubleToLongBits(Double.parseDouble(getString(R.string.pref_city_lat_def)))));
            double longitude = Double.longBitsToDouble(prefs.getLong(getString(R.string.pref_city_lon_key), Double.doubleToLongBits(Double.parseDouble(getString(R.string.pref_city_lon_def)))));
            String city_name = prefs.getString(getString(R.string.pref_city_name_key),
                    getString(R.string.pref_city_name_def));
            String country_code = prefs.getString(getString(R.string.pref_country_code_key), getString(R.string.pref_country_code_def));
            String marker = city_name + "," + country_code;

            LatLng latLng = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(marker));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));
        }
    }

    protected void createLocationRequest() {
        final LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates mLocationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        getLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    LocationActivity.this,REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
//            case REQUEST_CODE_RESOLUTION:
//                retryConnecting();
//                break;
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    getLocation();
                } else {
                    Toast.makeText(this,"Turn on Location to use this feature.",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    protected void getLocation() {
        if(Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"You don't have permisson for Access Location.",Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            Log.d(LOG_TAG,"In the get location.");
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                String latitude = String.valueOf(mLastLocation.getLatitude());
                String longitude = String.valueOf(mLastLocation.getLongitude());
                Log.d(LOG_TAG, "Latitude = " + latitude);
                Log.d(LOG_TAG, "Longitude = " + longitude);
                new FetchCityTask(getApplication()).execute("1",latitude,longitude);
            }
        }
        mGoogleApiClient.disconnect();
    }

    public void onConnected(Bundle connectionHint) {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_LOCATION_PERMISSON_COARSE_LOCATION);
        }
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
