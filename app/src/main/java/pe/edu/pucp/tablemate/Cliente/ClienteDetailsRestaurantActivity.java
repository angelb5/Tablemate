package pe.edu.pucp.tablemate.Cliente;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.Locale;

import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.R;

public class ClienteDetailsRestaurantActivity extends AppCompatActivity {
    //Mapa
    private static final String ICON_ID = "restaurantMarker";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private SymbolManager symbolManager;

    TextView tvNombre;
    TextView tvCategoria;
    TextView tvDescripcion;
    TextView tvDireccion;
    TextView tvRating;
    TextView tvNumReviews;
    CardView cvDistance;
    TextView tvDistance;
    ImageSlider imgSlider;
    TabLayout tabLayout;
    ScrollView sv;
    Button btnDescargarCarta;

    LinearLayout llInfo;
    LinearLayout llReviews;
    String cartaUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setea Mapbox
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_cliente_details_restaurant);


        Intent intent = getIntent();
        if(intent == null){
            finish();
            return;
        }
        Restaurant restaurant = (Restaurant) intent.getSerializableExtra("restaurant");
        double lat = intent.getDoubleExtra("lat",-12.04318);
        double lng = intent.getDoubleExtra("lng", -77.02824);
        restaurant.setGeoPoint(new GeoPoint(lat,lng));

        mapView = findViewById(R.id.mvClienteDetailsRestaurant);
        tabLayout = findViewById(R.id.tlClienteDetailsRestaurant);
        tvNombre = findViewById(R.id.tvClienteDetailsRestaurantNombre);
        tvCategoria = findViewById(R.id.tvClienteDetailsRestaurantCategoria);
        tvDescripcion = findViewById(R.id.tvClienteDetailsRestaurantDescripcion);
        tvDireccion = findViewById(R.id.tvClienteDetailsRestaurantDireccion);
        tvRating = findViewById(R.id.tvClienteDetailsRestaurantRating);
        tvNumReviews = findViewById(R.id.tvClienteDetailsRestaurantNumReviews);
        cvDistance = findViewById(R.id.cvClienteDetailsRestaurantDistancia);
        tvDistance = findViewById(R.id.tvClienteDetailsRestaurantDistancia);
        imgSlider = findViewById(R.id.isClienteDetailsRestaurant);
        btnDescargarCarta = findViewById(R.id.btnClienteDetailsRestaurantCarta);
        sv = findViewById(R.id.svClienteDetailsRestaurant);

        llInfo = findViewById(R.id.llClienteDetailsRestaurantInfo);
        llReviews = findViewById(R.id.llClienteDetailsRestaurantReviews);

        tvNombre.setText(restaurant.getNombre());
        tvCategoria.setText(restaurant.getCategoria());
        tvDescripcion.setText(restaurant.getDescripcion());
        tvDireccion.setText(restaurant.getDireccion());
        tvNumReviews.setText(String.valueOf(restaurant.getNumReviews()));
        if (restaurant.getNumReviews()>0){
            tvRating.setText(String.format(Locale.getDefault(),"%.1f" ,restaurant.getRating()));
        }else{
            tvRating.setText("--");
        }
        if(restaurant.getDistance() != 0){
            cvDistance.setVisibility(View.VISIBLE);
            tvDistance.setText(String.format(Locale.getDefault(),"%.1f" ,restaurant.getDistance())+" km");
        }else{
            cvDistance.setVisibility(View.GONE);
        }

        cartaUrl = restaurant.getCartaUrl();
        if(cartaUrl.isEmpty()) btnDescargarCarta.setVisibility(View.GONE);

        ArrayList<SlideModel> slideModels = new ArrayList<>();
        for (String url : restaurant.getFotosUrl()){
            Log.d("msg", url);
            slideModels.add(new SlideModel(url, ScaleTypes.CENTER_CROP));
        }

        imgSlider.setImageList(slideModels);

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

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0){
                    llInfo.setVisibility(View.VISIBLE);
                    llReviews.setVisibility(View.GONE);
                }else if(tab.getPosition() == 1){
                    llInfo.setVisibility(View.GONE);
                    llReviews.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }
    public void descargarCarta(View view){
        if (cartaUrl.isEmpty()) return;
        DownloadManager downloadManager = (DownloadManager) ClienteDetailsRestaurantActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(cartaUrl));
        request.setTitle("Descarga iniciada"); //cambiar esto
        request.setDescription("your description");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
    }
    public void backButton(View view){
        onBackPressed();
    }

    public void showAlert(View view){
        MaterialAlertDialogBuilder alertEliminar = new MaterialAlertDialogBuilder(this,R.style.AlertDialogTheme_Center);
        alertEliminar.setIcon(R.drawable.ic_trash);
        alertEliminar.setTitle("Eliminar restaurante");
        alertEliminar.setMessage("¿Está seguro que desea borrar este restaurante? Esta acción es irreversible");
        alertEliminar.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("msgAlert","ELIMINAR");
            }

        });
        alertEliminar.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("msgAlert","CANCELAR");
            }
        });
        alertEliminar.show();
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