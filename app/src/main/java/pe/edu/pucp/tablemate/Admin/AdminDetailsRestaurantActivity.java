package pe.edu.pucp.tablemate.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.paging.CombinedLoadStates;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchEngineSettings;

import java.util.ArrayList;
import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import pe.edu.pucp.tablemate.Adapters.ReviewAdapter;
import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.Entity.Review;
import pe.edu.pucp.tablemate.R;

public class AdminDetailsRestaurantActivity extends AppCompatActivity {
    //Mapa
    private static final String ICON_ID = "restaurantMarker";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private SymbolManager symbolManager;
    Restaurant restaurant;
    PagingConfig config = new PagingConfig(2,2,true);
    FirestorePagingOptions<Review> options;
    ReviewAdapter reviewAdapter;

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

    LinearLayout llEmptyReviews;
    RecyclerView rvReviews;

    LinearLayout llInfo;
    LinearLayout llReviews;
    String cartaUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setea Mapbox
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_admin_details_restaurant);


        Intent intent = getIntent();
        if(intent == null){
            finish();
            return;
        }
        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");
        double lat = intent.getDoubleExtra("lat",-12.04318);
        double lng = intent.getDoubleExtra("lng", -77.02824);
        restaurant.setGeoPoint(new GeoPoint(lat,lng));

        mapView = findViewById(R.id.mvAdminDetailsRestaurant);
        tabLayout = findViewById(R.id.tlAdminDetailsRestaurant);
        tvNombre = findViewById(R.id.tvAdminDetailsRestaurantNombre);
        tvCategoria = findViewById(R.id.tvAdminDetailsRestaurantCategoria);
        tvDescripcion = findViewById(R.id.tvAdminDetailsRestaurantDescripcion);
        tvDireccion = findViewById(R.id.tvAdminDetailsRestaurantDireccion);
        tvRating = findViewById(R.id.tvAdminDetailsRestaurantRating);
        tvNumReviews = findViewById(R.id.tvAdminDetailsRestaurantNumReviews);
        cvDistance = findViewById(R.id.cvAdminDetailsRestaurantDistancia);
        tvDistance = findViewById(R.id.tvAdminDetailsRestaurantDistancia);
        imgSlider = findViewById(R.id.isAdminDetailsRestaurant);
        btnDescargarCarta = findViewById(R.id.btnAdminDetailsRestaurantCarta);
        sv = findViewById(R.id.svAdminDetailsRestaurant);

        llEmptyReviews = findViewById(R.id.llAdminDetailsRestaurantEmptyView);
        rvReviews = findViewById(R.id.rvAdminDetailsRestaurant);

        llInfo = findViewById(R.id.llAdminDetailsRestaurantInfo);
        llReviews = findViewById(R.id.llAdminDetailsRestaurantReviews);

        tvNombre.setText(restaurant.getNombre());
        tvCategoria.setText(restaurant.getCategoria());
        tvDescripcion.setText(restaurant.getDescripcion());
        tvDireccion.setText(restaurant.getDireccion());
        tvNumReviews.setText("("+restaurant.getNumReviews()+" Reseñas)");
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


        //Setea la pagina reviews
        CollectionReference reviewsRef = FirebaseFirestore.getInstance().collection("restaurants").document(restaurant.getKey()).collection("reviews");
        Query query = reviewsRef.orderBy("rating", Query.Direction.DESCENDING);
        options = new FirestorePagingOptions.Builder<Review>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Review.class)
                .build();
        reviewAdapter = new ReviewAdapter(options, this);
        reviewAdapter.addLoadStateListener(new Function1<CombinedLoadStates, Unit>() {
            @Override
            public Unit invoke(CombinedLoadStates combinedLoadStates) {
                if(reviewAdapter.getItemCount()>0){
                    llEmptyReviews.setVisibility(View.GONE);
                }else{
                    llEmptyReviews.setVisibility(View.VISIBLE);
                }
                return null;
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        rvReviews.setAdapter(reviewAdapter);
        rvReviews.setLayoutManager(layoutManager);
        rvReviews.addItemDecoration(dividerItemDecoration);
    }

    public void descargarCarta(View view){
        if (cartaUrl.isEmpty()) return;
        DownloadManager downloadManager = (DownloadManager) AdminDetailsRestaurantActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(cartaUrl));
        request.setTitle("Carta "+restaurant.getNombre()); //cambiar esto
        request.setDescription("Se está descargando la carta");
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
                FirebaseFirestore.getInstance().collection("restaurants").document(restaurant.getKey()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(AdminDetailsRestaurantActivity.this, "Se ha eliminado el restaurante", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminDetailsRestaurantActivity.this, "No se ha podido eliminar el restaurante", Toast.LENGTH_SHORT).show();
                    }
                });
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