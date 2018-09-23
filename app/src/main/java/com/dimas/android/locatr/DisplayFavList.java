package com.dimas.android.locatr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dimas.android.locatr.FavoritePlace.PlaceFavAdapter;
import com.dimas.android.locatr.FavoritePlace.PlaceInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DisplayFavList extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<PlaceInfo> placeInfos;
    private PlaceFavAdapter mAdapter;
    private PlaceInfo placeInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("My Favorite List");
        setContentView(R.layout.activity_display_fav_list);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        initComponent();
    }

    private void initComponent() {
        mRecyclerView = (RecyclerView) findViewById(R.id.favListRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextView notice = (TextView) findViewById(R.id.emptyNotice);

        Intent intent = getIntent();
        Bundle intentExtra = intent.getExtras();

        retrieveArrayList();
        if (intentExtra != null) {
            placeInfo = (PlaceInfo) intent.getSerializableExtra("placeInfoObj");
            if (placeInfos == null)
                placeInfos = new ArrayList<>();
            placeInfos.add(placeInfo);
            saveArrayList();
        }


        if (placeInfos == null || placeInfos.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            notice.setVisibility(View.VISIBLE);
            notice.setText("No item has been added here");
        }
        else {
            notice.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter = new PlaceFavAdapter(placeInfos, this);
            mRecyclerView.setAdapter(mAdapter);
        }


    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.option_get_place:
                    Intent intent = new Intent(DisplayFavList.this, MapActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    public void saveArrayList() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(placeInfos);
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString("MyObject", jsonString);
        prefsEditor.apply();
    }

    private void retrieveArrayList() {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        Gson gson = new Gson();
        String jsonPlaces = appSharedPrefs.getString("MyObject", "");
        Type type = new TypeToken<List<PlaceInfo>>(){}.getType();
        placeInfos = gson.fromJson(jsonPlaces, type);
    }
}