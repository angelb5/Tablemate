package pe.edu.pucp.tablemate.Cliente;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pe.edu.pucp.tablemate.Adapters.NearbyRestaurantCardAdapter;
import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.R;

public class ClienteNearbyRestaurantsActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {
    private static final String SYMBOL_ICON_ID = "SYMBOL_ICON_ID";
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private PermissionsManager permissionsManager;
    public MapboxMap mapboxMap;
    private MapView mapView;
    private RecyclerView recyclerView;
    private NearbyRestaurantCardAdapter restaurantCardAdapter;
    private Button btnRadius;
    private List<Restaurant> restaurantList = new ArrayList<>();
    private FeatureCollection featureCollection;
    private Gson gson;
    private GeoJsonSource source;
    private boolean enabled = false;
    private double radius = 3;
    private double userLat;
    private double userLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_cliente_nearby_restaurants);
        mapView = findViewById(R.id.mvClienteNearbyRestaurants);
        recyclerView = findViewById(R.id.rvClienteNearbyRestaurants);
        btnRadius = findViewById(R.id.btnRadius);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        gson = new Gson();
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        ClienteNearbyRestaurantsActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.OUTDOORS, onStyleLoaded);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            Location lastKnownLocation = locationComponent.getLastKnownLocation();
            assert lastKnownLocation != null;
            userLat = lastKnownLocation.getLatitude();
            userLng = lastKnownLocation.getLongitude();
            enabled = true;
            getRestaurantes();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    public void getRestaurantes() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
            "https://us-central1-tablemate-cf5ab.cloudfunctions.net/nearbyRestaurants?lat="+userLat+"&lng="+userLng+"&radius="+radius,
            response -> {
                try {
                    JSONObject responseJson = new JSONObject(response);
                    if (!responseJson.has("status") || !responseJson.getString("status").equals("OK")) return;
                    restaurantList.clear();
                    JSONArray jsonArray = responseJson.getJSONArray("restaurants");
                    if (jsonArray.length()==0){
                        Toast.makeText(ClienteNearbyRestaurantsActivity.this, "No se encontrar restaurantes, intenta ajustar el radio", Toast.LENGTH_SHORT).show();
                    }

                    for (int i=0; i<jsonArray.length(); i++) {
                        Restaurant restaurant = gson.fromJson(jsonArray.getString(i), Restaurant.class);
                        JSONObject geoObject = jsonArray.getJSONObject(i).getJSONObject("geoPoint");
                        restaurant.setGeoPoint(new GeoPoint(geoObject.getDouble("_latitude"), geoObject.getDouble("_longitude")));
                        restaurantList.add(restaurant);
                    }
                    generateFeatureCollection();
                    if (mapboxMap!=null && mapboxMap.getStyle()!=null && mapboxMap.getStyle().isFullyLoaded()){
                        if (source!=null){
                            source.setGeoJson(featureCollection);
                        }else{
                            source = new GeoJsonSource(SOURCE_ID, featureCollection);
                            mapboxMap.getStyle().addSource(source);
                        }
                    }
                    restaurantCardAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(0);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(userLat,userLng))
                            .zoom(11)
                            .tilt(10)
                            .build();
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } catch (JSONException e) {
                    Toast.makeText(ClienteNearbyRestaurantsActivity.this, "No se pudo cargar los restaurantes", Toast.LENGTH_SHORT).show();
                    Log.d("msg", "error", e);
                }
            },
        error -> {
            Toast.makeText(ClienteNearbyRestaurantsActivity.this, "No se pudo cargar los restaurantes", Toast.LENGTH_SHORT).show();
            Log.e("msg","error", error);
        });
        requestQueue.add(stringRequest);
    }

    public void ajustarRadius(View view) {
        if (!enabled) return;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this,R.style.AlertDialogTheme_Center);
        LayoutInflater inflater = this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_select_radius, (ViewGroup) findViewById(R.id.rlDialogRoot));
        SeekBar seekBar = layout.findViewById(R.id.seekbarDialog);
        TextView tv = layout.findViewById(R.id.tvDialog);
        builder.setView(layout);
        builder.setTitle("Ajustar radio");
        builder.setMessage("Modifica el radio para ver otros restaurantes cerca a tu zona");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                double newRadius = seekBar.getProgress()/100.0 * 17.0 + 3;
                if (radius != newRadius) {
                    radius = newRadius;
                    btnRadius.setText(String.format("%.1f km", radius));
                    dialog.dismiss();
                    getRestaurantes();
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("msgAlert","CANCELAR");
            }
        });
        builder.show();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv.setText(String.format(Locale.getDefault(),"%.2f km",progress/100.0 * 17.0 + 3 ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onExplanationNeeded(List<String> list) {
        Toast.makeText(this, "Necesitamos este permiso para mostrar su ubicación", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    Style.OnStyleLoaded onStyleLoaded = new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
            initAdapter();
            initMarkerIcons(style);
            enableLocationComponent(style);
        }
    };

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(onStyleLoaded);
        } else {
            Toast.makeText(this, "No nos garantizó el permiso :((", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initAdapter() {
        restaurantCardAdapter = new NearbyRestaurantCardAdapter(restaurantList, mapboxMap);
        recyclerView.setAdapter(restaurantCardAdapter);
    }

    private void initMarkerIcons(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage(SYMBOL_ICON_ID, BitmapFactory.decodeResource(this.getResources(), R.drawable.marker_restaurant));
        loadedMapStyle.addLayer(new SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
            iconImage(SYMBOL_ICON_ID),
            iconAllowOverlap(true),
            iconOffset(new Float[] {0f, -4f}),
            iconSize(0.5f)
        ));
    }

    public void generateFeatureCollection() {
        List<Feature> featureList = new ArrayList<>();
        for (Restaurant restaurant : restaurantList) {
            featureList.add(Feature.fromGeometry(Point.fromLngLat(restaurant.getGeoPoint().getLongitude(), restaurant.getGeoPoint().getLatitude())));
        }
        featureCollection = FeatureCollection.fromFeatures(featureList);
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

    public void backButton(View view) { onBackPressed(); }
}