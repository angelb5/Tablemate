package pe.edu.pucp.tablemate.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.search.Country;
import com.mapbox.search.Language;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchEngineSettings;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.SearchSelectionCallback;
import com.mapbox.search.result.SearchAddress;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pe.edu.pucp.tablemate.R;

public class AdminCreateRestaurantActivity extends AppCompatActivity {

    private static final String ICON_ID = "restaurantMarker";

    private MapView mapView;
    private MapboxMap mapboxMap;
    private SymbolManager symbolManager;

    SearchOptions options = new SearchOptions.Builder().limit(1).countries(Country.PERU).languages(Language.SPANISH).build();
    private Symbol symbol;

    private EditText etDireccion;
    private double latDireccion;
    private double lngDireccion;

    SearchEngine searchEngine;
    //Text Typing
    private Handler handler = new Handler(Looper.getMainLooper());
    private final long DELAY = 900;
    private Runnable searchGeocode = new Runnable() {
        @Override
        public void run() {
            String direccion = etDireccion.getText().toString().trim();
            searchEngine.search(direccion, options, new SearchSelectionCallback() {
                @Override
                public void onResult(@NonNull SearchSuggestion searchSuggestion, @NonNull SearchResult searchResult, @NonNull ResponseInfo responseInfo) {
                    latDireccion = searchResult.getCoordinate().latitude();
                    lngDireccion = searchResult.getCoordinate().longitude();
                    updateMarker();
                }

                @Override
                public void onCategoryResult(@NonNull SearchSuggestion searchSuggestion, @NonNull List<SearchResult> list, @NonNull ResponseInfo responseInfo) {
                }

                @Override
                public void onSuggestions(@NonNull List<SearchSuggestion> list, @NonNull ResponseInfo responseInfo) {
                    if(!list.isEmpty()){
                        searchEngine.select(list.get(0), this);
                    }else{
                        latDireccion = 0;
                        lngDireccion = 0;
                        updateMarker();
                    }
                }

                @Override
                public void onError(@NonNull Exception e) {
                    latDireccion = 0;
                    lngDireccion = 0;
                    updateMarker();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setea Mapbox
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_admin_create_restaurant);
        searchEngine = SearchEngine.createSearchEngine(new SearchEngineSettings(getString(R.string.mapbox_access_token)));

        mapView = findViewById(R.id.mvAdminCreateRestaurant);
        etDireccion = findViewById(R.id.etAdminCreateRestaurantDireccion);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(Style.OUTDOORS, style -> {
            style.addImage(ICON_ID, BitmapFactory.decodeResource(this.getResources(),R.drawable.marker_restaurant));
            this.mapboxMap = mapboxMap;
            symbolManager = new SymbolManager(mapView, mapboxMap, style, null);
        }));

        etDireccion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(searchGeocode);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 3) {
                    handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(searchGeocode, DELAY);
                }else{
                    latDireccion = 0;
                    lngDireccion = 0;
                    updateMarker();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void updateMarker(){
        if(latDireccion != 0 && lngDireccion !=0){
            SymbolOptions symbolOptions = new SymbolOptions()
                    .withLatLng(new LatLng(latDireccion,lngDireccion))
                    .withIconImage(ICON_ID)
                    .withIconSize(0.5f);
            symbol = symbolManager.create(symbolOptions);
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(latDireccion,lngDireccion)).zoom(14).build());
        }else{
            symbolManager.deleteAll();
        }
    }

    public void goBackToPreviousActivity(View view){
        onBackPressed();
    }
}