package com.dimas.android.locatr;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dimas.android.locatr.FavoritePlace.PlaceInfo;
import com.dimas.android.locatr.Modules.MapWrapperLayout;
import com.dimas.android.locatr.Modules.OnInterInfoWindowTouchListener;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.WeatherResponse;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final LatLngBounds BOUNDS_HCMC = new LatLngBounds(new LatLng(10.500793, 106.379839),
            new LatLng(11.185964, 107.021166));
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String LOG_TAG = "Map2";
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Button btnFind;
    private PlaceInfo placeInfo;
    private PlaceAutoCompleteAdapter mAdapter;
    private AutoCompleteTextView placeToGo;
    private String info;
    protected LatLng start = null;
    protected LatLng curWeather = null;
    private boolean gpsEnabled = true;

    private static final int REQUEST_ERROR = 0;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private static final int LOCATION_SETTINGS_REQUEST = 2;
    private MapWrapperLayout mapWrapperLayout;
    private View contentView;
    private Button btnClick;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = apiAvailability.getErrorDialog(this, errorCode, REQUEST_ERROR, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            errorDialog.show();
        }
        getLocationPermission();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    private void initComponent() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();
        btnFind = (Button) findViewById(R.id.findPlace);
        placeToGo = (AutoCompleteTextView) findViewById(R.id.inputPlace);
        mAdapter = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, BOUNDS_HCMC, null);
        placeToGo.setAdapter(mAdapter);
        setAutoSuggestion();

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    if (!placeToGo.getText().toString().equals("")) {
                        moveToPosition();
                        curWeather = null;
                        placeToGo.setText("");
                    } else {
                        Toast.makeText(MapActivity.this, "Please input a place", Toast.LENGTH_SHORT).show();
                    }
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }else {
                    new android.app.AlertDialog.Builder(MapActivity.this)
                            .setTitle("Network needed")
                            .setMessage("This app needs any network connection!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    placeToGo.setText("");
                                    return;
                                }
                            })
                            .create()
                            .show();
                }
            }
        });


    }

    private void initWeather() {
        try {
            Awareness.getSnapshotClient(this).getWeather().addOnSuccessListener(MapActivity.this, new OnSuccessListener<WeatherResponse>() {
                @Override
                public void onSuccess(WeatherResponse weatherResponse) {
                    placeInfo = new PlaceInfo();
                    Weather weather = weatherResponse.getWeather();
                    placeInfo.setTemperature(Math.round(weather.getTemperature(Weather.CELSIUS)));
                    switch (weather.getConditions()[0]) {
                        case Weather.CONDITION_CLEAR:
                            placeInfo.setWeather("Чистое небо");
                            break;
                        case Weather.CONDITION_CLOUDY:
                            placeInfo.setWeather("Облачно");
                            break;
                        case Weather.CONDITION_FOGGY:
                            placeInfo.setWeather("Густой туман");
                            break;
                        case Weather.CONDITION_HAZY:
                            placeInfo.setWeather("Туманно");
                            break;
                        case Weather.CONDITION_ICY:
                            placeInfo.setWeather("Мерзлота");
                            break;
                        case Weather.CONDITION_RAINY:
                            placeInfo.setWeather("Дождливо");
                            break;
                        case Weather.CONDITION_SNOWY:
                            placeInfo.setWeather("Cнег");
                            break;
                        case Weather.CONDITION_STORMY:
                            placeInfo.setWeather("Сильный ветер, шторм");
                            break;
                        case Weather.CONDITION_WINDY:
                            placeInfo.setWeather("Сильный ветер");
                            break;
                        case Weather.CONDITION_UNKNOWN:
                            placeInfo.setWeather("Нет данных");
                            break;
                        default:
                            placeInfo.setWeather("ошибка");
                            break;
                    }
                }
            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void setAutoSuggestion() {
        placeToGo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);
                Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);
            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            // Request did not complete successfully
                            Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                            places.release();
                            return;
                        }
                        // Get the Place object from the buffer.
                        final Place place = places.get(0);

                        start = place.getLatLng();
                        info = place.getName().toString() + ", " + place.getAddress().toString();
                        if (!info.equals("")) {
                            placeInfo = new PlaceInfo(place.getName().toString(), place.getAddress().toString());
                        }
                    }
                });
            }
        });
        placeToGo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int startNum, int before, int count) {
                if (start != null) {
                    start = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void moveToPosition() {
        // add a marker
        LatLng wantToGoTo = start;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(wantToGoTo).title(info)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.personstanding)));

        // create an animation for map while moving to this location
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(wantToGoTo)
                .zoom(18)
                .bearing(90)
                .tilt(30)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private void getCurPos() {
        try {
            if (gpsEnabled) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    start = new LatLng(location.getLatitude(), location.getLongitude());
                                    curWeather = new LatLng(location.getLatitude(), location.getLongitude());
                                    if (info == null)
                                        info = getAddress(MapActivity.this, location.getLatitude(), location.getLongitude());
                                    moveToPosition();
                                }
                            }
                        })
                        .addOnFailureListener(this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                initWeather();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_wrapper);

        mMap = googleMap;
        mapWrapperLayout.init(mMap, this);
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
        //getCurPos();

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                turnOnGps();
                getCurPos();
                return true;
            }
        });

        getPopupInfo();
    }

    public void getPopupInfo() {
        contentView = LayoutInflater.from(this).inflate(R.layout.content, null);
        btnClick = (Button) contentView.findViewById(R.id.ClickToSeeMore);
        btnSave = (Button) contentView.findViewById(R.id.ClickToSave);

        final OnInterInfoWindowTouchListener isClick = new OnInterInfoWindowTouchListener(btnClick) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                String url2Load = "https://www.google.com/search?q=" + info.replace(" ", "+").replace(",", "%2C");
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(MapActivity.this, Uri.parse(url2Load));
            }
        };

        final OnInterInfoWindowTouchListener isClick2 = new OnInterInfoWindowTouchListener(btnClick) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
                alertDialog.setTitle("Confirm Save Place");
                alertDialog.setMessage("Press OK will save place to favorite");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        savePlaceToFavorite();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        };

        btnClick.setOnTouchListener(isClick);
        btnSave.setOnTouchListener(isClick2);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                isClick.setMarker(marker);
                TextView name = (TextView) contentView.findViewById(R.id.name);
                if (curWeather == null) {
                    name.setText(marker.getTitle());
                }else {
                    String str = marker.getTitle()+" "+ placeInfo.getTemperatureToString()+" "+placeInfo.getWeather();
                    name.setText(str);
                }
                mapWrapperLayout.setMarkerWithInfoWindow(marker, contentView);
                return contentView;
            }

        });
    }

    private void savePlaceToFavorite() {
        if (placeInfo.getTitle() == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder.setTitle("Input Name For Place");
            final EditText input = new EditText(MapActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (curWeather == null) {
                        placeInfo = new PlaceInfo(input.getText().toString(), info);
                    } else {
                        placeInfo.setTitle(input.getText().toString());
                        placeInfo.setBody(info);
                    }
                    Intent intent = new Intent(MapActivity.this, DisplayFavList.class);
                    intent.putExtra("placeInfoObj", placeInfo);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            Intent intent = new Intent(this, DisplayFavList.class);
            intent.putExtra("placeInfoObj", placeInfo);
            startActivity(intent);
        }

    }

    public String getAddress(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String add = obj.getAddressLine(0);
            add = add + "," + obj.getAdminArea();
            add = add + "," + obj.getCountryName();

            return add;
        } catch (IOException e) {
            e.printStackTrace();
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage(e.getMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create()
                    .show();
            return null;
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();
                mLocationPermissionGranted = false;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            mLocationPermissionGranted = true;
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
            mapFragment.getMapAsync(this);
            initComponent();
            //turnOnGps();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = true;
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
                        mapFragment.getMapAsync(this);
                        initComponent();
                        turnOnGps();
                    }
                } else {
                    getLocationPermission();
                }
            }
        }
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityMgr.getActiveNetworkInfo();
        /// if no network is available networkInfo will be null
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void turnOnGps() {
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);
        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        settingsBuilder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingsBuilder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response =
                            task.getResult(ApiException.class);
                } catch (ApiException ex) {
                    switch (ex.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException =
                                        (ResolvableApiException) ex;
                                resolvableApiException
                                        .startResolutionForResult(MapActivity.this, LOCATION_SETTINGS_REQUEST);
                            } catch (IntentSender.SendIntentException e) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCATION_SETTINGS_REQUEST: {
                switch (resultCode) {
                    case AppCompatActivity.RESULT_OK: {
                        getCurPos();
                        gpsEnabled = true;
                        break;
                    }
                    case AppCompatActivity.RESULT_CANCELED: {
                        gpsEnabled = false;
                        break;
                    }
                }
                break;
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    Intent intent = new Intent(MapActivity.this, DisplayFavList.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
