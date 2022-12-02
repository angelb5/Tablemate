package pe.edu.pucp.tablemate.Restaurant;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import pe.edu.pucp.tablemate.Adapters.ImageAdapter;
import pe.edu.pucp.tablemate.Admin.AdminDetailsRestaurantActivity;
import pe.edu.pucp.tablemate.Anonymus.LoginActivity;
import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.R;

public class RestaurantProfileActivity extends AppCompatActivity {
    //Mapa
    private static final String ICON_ID = "restaurantMarker";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private SymbolManager symbolManager;
    private Symbol symbol;

    SharedPreferences sharedPreferences;
    BottomNavigationView bottomNavigationView;
    Restaurant restaurant;

    TextView tvNombre;
    TextView tvCategoria;
    TextView tvDescripcion;
    TextView tvDireccion;
    Button btnDescargarCarta;
    RecyclerView rvFotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setea Mapbox
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_restaurant_profile);
        sharedPreferences = getSharedPreferences(getString(R.string.preferences_key), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        restaurant = gson.fromJson(sharedPreferences.getString("restaurant",""), Restaurant.class);
        restaurant.setGeoPoint(gson.fromJson(sharedPreferences.getString("geopoint",""), GeoPoint.class));

        setBottomNavigationView();
        mapView = findViewById(R.id.mvRestaurantProfile);
        tvNombre = findViewById(R.id.tvRestaurantProfileNombre);
        tvCategoria = findViewById(R.id.tvRestaurantProfileCategoria);
        tvDescripcion = findViewById(R.id.tvRestaurantProfileDescripcion);
        tvDireccion = findViewById(R.id.tvRestaurantProfileDireccion);
        btnDescargarCarta = findViewById(R.id.btnRestaurantProfileCarta);
        rvFotos = findViewById(R.id.rvRestaurantProfile);

        llenarTvs();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(Style.OUTDOORS, style -> {
            style.addImage(ICON_ID, BitmapFactory.decodeResource(this.getResources(),R.drawable.marker_restaurant));
            this.mapboxMap = mapboxMap;
            symbolManager = new SymbolManager(mapView, mapboxMap, style, null);
            SymbolOptions symbolOptions = new SymbolOptions()
                    .withLatLng(new LatLng(restaurant.getGeoPoint().getLatitude(),restaurant.getGeoPoint().getLongitude()))
                    .withIconImage(ICON_ID)
                    .withIconSize(0.5f);
            symbolManager.create(symbolOptions);
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(restaurant.getGeoPoint().getLatitude(),restaurant.getGeoPoint().getLongitude())).zoom(14).build());
        }));

        ImageAdapter imageAdapter = new ImageAdapter(this, restaurant.getFotosUrl());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvFotos.setLayoutManager(layoutManager);
        rvFotos.setAdapter(imageAdapter);
    }

    private ActivityResultLauncher<Intent> launcherEdit = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), (ActivityResultCallback<ActivityResult>) result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null || !data.hasExtra("restaurant")) return;
                    Restaurant restaurantFromIntent = (Restaurant) data.getSerializableExtra("restaurant");

                    restaurant.setDescripcion(restaurantFromIntent.getDescripcion());
                    restaurant.setDireccion(restaurantFromIntent.getDireccion());
                    restaurant.setCartaUrl(restaurantFromIntent.getCartaUrl());
                    restaurant.setFotosUrl(restaurantFromIntent.getFotosUrl());
                    llenarTvs();
                    if (data.hasExtra("lat") && data.hasExtra("lng")) {
                        double latDireccion = data.getDoubleExtra("lat", restaurant.getGeoPoint().getLatitude());
                        double lngDireccion = data.getDoubleExtra("lng", restaurant.getGeoPoint().getLongitude());
                        restaurant.setGeoPoint(new GeoPoint(latDireccion, lngDireccion));
                        SymbolOptions symbolOptions = new SymbolOptions()
                                .withLatLng(new LatLng(latDireccion, lngDireccion))
                                .withIconImage(ICON_ID)
                                .withIconSize(0.5f);
                        symbol = symbolManager.create(symbolOptions);
                        mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(latDireccion, lngDireccion)).zoom(14).build());
                    }
                }
            }
    );

    private void llenarTvs() {
        tvNombre.setText(restaurant.getNombre());
        tvCategoria.setText(restaurant.getCategoria());
        tvDescripcion.setText(restaurant.getDescripcion());
        tvDireccion.setText(restaurant.getDireccion());

        if(restaurant.getCartaUrl().isEmpty()) btnDescargarCarta.setVisibility(View.GONE);
    }

    public void descargarCarta(View view) {
        if (restaurant.getCartaUrl().isEmpty()) return;
        DownloadManager downloadManager = (DownloadManager) RestaurantProfileActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(restaurant.getCartaUrl()));
        request.setTitle("Carta "+restaurant.getNombre()); //cambiar esto
        request.setDescription("Se está descargando la carta");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
    }

    public void restaurantLogout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent logoutIntent = new Intent(RestaurantProfileActivity.this, LoginActivity.class);
        sharedPreferences.edit().clear().apply();
        startActivity(logoutIntent);
        ActivityCompat.finishAffinity(RestaurantProfileActivity.this);
        finish();
    }

    public void goToEditRestaurant(View view) {
        Intent editIntent = new Intent(RestaurantProfileActivity.this, RestaurantEditActivity.class);
        launcherEdit.launch(editIntent);
    }

    public void setBottomNavigationView(){
        bottomNavigationView = findViewById(R.id.bottomNavMenuTiProfileAct);
        bottomNavigationView.setSelectedItemId(R.id.bottomNavMenuRestaurantProfile);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.bottomNavMenuRestaurantReservas:
                        startActivity(new Intent(getApplicationContext(), RestaurantReservasActivity.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                    case R.id.bottomNavMenuRestaurantReviews:
                        startActivity(new Intent(getApplicationContext(), RestaurantReviewsActivity.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                    case R.id.bottomNavMenuRestaurantProfile:
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), RestaurantReservasActivity.class));
        overridePendingTransition(0,0);
        finish();
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
}