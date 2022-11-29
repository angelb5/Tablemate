package pe.edu.pucp.tablemate.Cliente;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.Entity.Review;
import pe.edu.pucp.tablemate.R;

public class ClienteDetailsRestaurantActivity extends AppCompatActivity {
    DateFormat df = new SimpleDateFormat("EEE dd MMM yyy", Locale.getDefault());
    //Mapa
    private static final String ICON_ID = "restaurantMarker";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private SymbolManager symbolManager;

    Restaurant restaurant;
    Review userReview;
    List<Review> otherReviews = new ArrayList<>();

    ImageSlider imgSlider;
    TabLayout tabLayout;
    LinearLayout llInfo;
    LinearLayout llReserva;
    LinearLayout llReviews;
    //Pagina Info
    TextView tvNombre;
    TextView tvCategoria;
    TextView tvDescripcion;
    TextView tvDireccion;
    TextView tvRating;
    TextView tvNumReviews;
    CardView cvDistance;
    TextView tvDistance;
    Button btnDescargarCarta;
    //Pagina Reviews
    LinearLayout llEscribir;
    LinearLayout llUserReview;
    RecyclerView rvReviews;
    LinearLayout llEmptyReviews;
    ImageView ivUserPfp;
    TextView tvUserNombre;
    TextView tvUserRating;
    TextView tvUserFecha;
    TextView tvUserReviewContent;
    ShapeableImageView ivUserReview;

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
        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");
        double lat = intent.getDoubleExtra("lat",-12.04318);
        double lng = intent.getDoubleExtra("lng", -77.02824);
        restaurant.setGeoPoint(new GeoPoint(lat,lng));

        llInfo = findViewById(R.id.llClienteDetailsRestaurantInfo);
        llReviews = findViewById(R.id.llClienteDetailsRestaurantReviews);
        llReserva = findViewById(R.id.llClienteDetailsRestaurantReserva);
        imgSlider = findViewById(R.id.isClienteDetailsRestaurant);
        tabLayout = findViewById(R.id.tlClienteDetailsRestaurant);

        mapView = findViewById(R.id.mvClienteDetailsRestaurant);
        tvNombre = findViewById(R.id.tvClienteDetailsRestaurantNombre);
        tvCategoria = findViewById(R.id.tvClienteDetailsRestaurantCategoria);
        tvDescripcion = findViewById(R.id.tvClienteDetailsRestaurantDescripcion);
        tvDireccion = findViewById(R.id.tvClienteDetailsRestaurantDireccion);
        tvRating = findViewById(R.id.tvClienteDetailsRestaurantRating);
        tvNumReviews = findViewById(R.id.tvClienteDetailsRestaurantNumReviews);
        cvDistance = findViewById(R.id.cvClienteDetailsRestaurantDistancia);
        tvDistance = findViewById(R.id.tvClienteDetailsRestaurantDistancia);
        btnDescargarCarta = findViewById(R.id.btnClienteDetailsRestaurantCarta);

        llEscribir = findViewById(R.id.llClienteDetailsRestaurantEscribir);
        llUserReview = findViewById(R.id.llClienteDetailsRestaurantUserReview);
        rvReviews = findViewById(R.id.rvClienteDetailsRestaurantReviews);
        llEmptyReviews = findViewById(R.id.llClienteDetailsRestaurantEmptyView);
        ivUserPfp = findViewById(R.id.ivClienteDetailsRestaurantPfp);
        tvUserNombre = findViewById(R.id.tvClienteDetailsRestaurantReviewNombre);
        tvUserRating = findViewById(R.id.tvClienteDetailsRestaurantReviewRating);
        tvUserFecha = findViewById(R.id.tvClienteDetailsRestaurantReviewFecha);
        tvUserReviewContent = findViewById(R.id.tvClienteDetailsRestaurantReviewContent);
        ivUserReview = findViewById(R.id.ivClienteDetailsRestaurantReview);

        ArrayList<SlideModel> slideModels = new ArrayList<>();
        for (String url : restaurant.getFotosUrl()){
            Log.d("msg", url);
            slideModels.add(new SlideModel(url, ScaleTypes.CENTER_CROP));
        }
        imgSlider.setImageList(slideModels);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    llInfo.setVisibility(View.VISIBLE);
                    llReserva.setVisibility(View.GONE);
                    llReviews.setVisibility(View.GONE);
                }else if (tab.getPosition() == 1) {
                    llInfo.setVisibility(View.GONE);
                    llReserva.setVisibility(View.VISIBLE);
                    llReviews.setVisibility(View.GONE);
                }else if (tab.getPosition() == 2) {
                    llInfo.setVisibility(View.GONE);
                    llReserva.setVisibility(View.GONE);
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
        //Setea la pagina info
        tvNombre.setText(restaurant.getNombre());
        tvCategoria.setText(restaurant.getCategoria());
        tvDescripcion.setText(restaurant.getDescripcion());
        tvDireccion.setText(restaurant.getDireccion());
        mostrarRating(restaurant.getNumReviews(), restaurant.getRating());
        if(restaurant.getDistance() != 0){
            cvDistance.setVisibility(View.VISIBLE);
            tvDistance.setText(String.format(Locale.getDefault(),"%.1f" ,restaurant.getDistance())+" km");
        }else{
            cvDistance.setVisibility(View.GONE);
        }
        cartaUrl = restaurant.getCartaUrl();
        if(cartaUrl.isEmpty()) btnDescargarCarta.setVisibility(View.GONE);
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

        //Setea pagina de reviews
        CollectionReference reviewsRef = FirebaseFirestore.getInstance().collection("restaurants").document(restaurant.getKey()).collection("reviews");
        reviewsRef.whereEqualTo("user.uid", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()){
                userReview = queryDocumentSnapshots.iterator().next().toObject(Review.class);
                userReview.setKey(queryDocumentSnapshots.iterator().next().getId());
                mostrarUserReview();
            }
        }).addOnFailureListener(e -> {
            llEscribir.setVisibility(View.GONE);
            llReserva.setVisibility(View.VISIBLE);
        });
        reviewsRef.whereNotEqualTo("user.uid", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()){
                llEmptyReviews.setVisibility(View.VISIBLE);
                return;
            }
            for (DocumentSnapshot snap : queryDocumentSnapshots){
                otherReviews.add(snap.toObject(Review.class));
                //TODO: llamar notifydatasetchanged
            }
        }).addOnFailureListener(e -> {
            llEmptyReviews.setVisibility(View.VISIBLE);
            Log.d("msg", "error", e);
        });

    }

    public void mostrarRating(int numReviews, double rating) {
        tvNumReviews.setText("("+numReviews+" Reseñas)");
        if (numReviews>0){
            tvRating.setText(String.format(Locale.getDefault(),"%.1f", rating));
        }else{
            tvRating.setText("--");
        }
    }

    public void mostrarUserReview(){
        if (userReview == null) {
            llUserReview.setVisibility(View.GONE);
            llEscribir.setVisibility(View.VISIBLE);
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        llEscribir.setVisibility(View.GONE);
        llUserReview.setVisibility(View.VISIBLE);
        Glide.with(ClienteDetailsRestaurantActivity.this).load(user.getPhotoUrl().toString()).into(ivUserPfp);
        tvUserNombre.setText(user.getDisplayName());
        tvUserRating.setText(String.valueOf(userReview.getRating()));
        tvUserReviewContent.setText(userReview.getContent());
        tvUserReviewContent.setText(userReview.getContent());
        if (!userReview.getFotoUrl().isEmpty()) {
            ivUserReview.setVisibility(View.VISIBLE);
            Glide.with(ClienteDetailsRestaurantActivity.this).load(userReview.getFotoUrl()).into(ivUserReview);
        } else {
            ivUserReview.setVisibility(View.GONE);
        }
        String fecha = df.format(userReview.getTimestamp().toDate());
        tvUserFecha.setText(fecha);
    }

    public void descargarCarta(View view){
        if (cartaUrl.isEmpty()) return;
        DownloadManager downloadManager = (DownloadManager) ClienteDetailsRestaurantActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(cartaUrl));
        request.setTitle("Carta "+restaurant.getNombre()); //cambiar esto
        request.setDescription("Espera a que termine la descarga");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
    }
    public void backButton(View view){
        onBackPressed();
    }
    public void goToWriteReviewActivity(View view){
        Intent writeReviewIntent = new Intent(ClienteDetailsRestaurantActivity.this, ClienteWriteReviewActivity.class);
        writeReviewIntent.putExtra("restaurant", restaurant);
        writeReviewActivityResultLauncher.launch(writeReviewIntent);
    }
    public void goToEditReview(View view){
        Intent writeReviewIntent = new Intent(ClienteDetailsRestaurantActivity.this, ClienteWriteReviewActivity.class);
        writeReviewIntent.putExtra("restaurant", restaurant);
        writeReviewIntent.putExtra("userReview", userReview);
        writeReviewActivityResultLauncher.launch(writeReviewIntent);
    }
    ActivityResultLauncher<Intent> writeReviewActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) return;
                        int numReviews = data.getIntExtra("numReviews", restaurant.getNumReviews());
                        double rating = data.getDoubleExtra("rating", restaurant.getRating());
                        mostrarRating(numReviews, rating);
                        userReview = (Review) data.getSerializableExtra("userReview");
                        int tNano = data.getIntExtra("tNano", Timestamp.now().getNanoseconds());
                        long tSec = data.getLongExtra("tSec", Timestamp.now().getSeconds());
                        userReview.setTimestamp(new Timestamp(tSec, tNano));
                        mostrarUserReview();
                    }
                }
            }
    );


    public void eliminarReviewAlert(View view){
       if (userReview == null) return;

        MaterialAlertDialogBuilder alertEliminar = new MaterialAlertDialogBuilder(this,R.style.AlertDialogTheme_Center);
        alertEliminar.setIcon(R.drawable.ic_trash);
        alertEliminar.setTitle("Eliminar reseña");
        alertEliminar.setMessage("¿Estás seguro que deseas borrar tu reseña? Esta acción es irreversible");
        alertEliminar.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int newNumReviews = restaurant.getNumReviews() - 1;
                double newRating = 0;
                if (newNumReviews>0) {
                    newRating = (restaurant.getRating() * restaurant.getNumReviews() - userReview.getRating()) / newNumReviews;
                }

                FirebaseFirestore.getInstance().collection("restaurants").document(restaurant.getKey()).collection("reviews").document(userReview.getKey()).delete();

                mostrarRating(newNumReviews, newRating);
                userReview = null;
                mostrarUserReview();
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