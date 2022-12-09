package pe.edu.pucp.tablemate.Cliente;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.paging.CombinedLoadStates;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import pe.edu.pucp.tablemate.Adapters.ReviewAdapter;
import pe.edu.pucp.tablemate.Anonymus.LoginActivity;
import pe.edu.pucp.tablemate.Entity.Reserva;
import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.Entity.Review;
import pe.edu.pucp.tablemate.Entity.User;
import pe.edu.pucp.tablemate.R;

public class ClienteDetailsRestaurantActivity extends AppCompatActivity {
    DateFormat df = new SimpleDateFormat("EEE dd MMM yyy", Locale.getDefault());
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es"));
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    FirebaseUser firebaseUser;
    User user;
    //Mapa
    private static final String ICON_ID = "restaurantMarker";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private SymbolManager symbolManager;

    Restaurant restaurant;
    Review userReview;
    PagingConfig config = new PagingConfig(2,2,true);
    FirestorePagingOptions<Review> options;
    ReviewAdapter reviewAdapter;

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
    //Pagina Reservas
    EditText etNumPersonas;
    EditText etFecha;
    EditText etHora;
    LocalDate localDate;
    LocalTime localTime;
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
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(intent == null || firebaseUser == null){
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

        etNumPersonas = findViewById(R.id.etClienteDetailsRestaurantCantidad);
        etFecha = findViewById(R.id.etClienteDetailsRestaurantFecha);
        etHora = findViewById(R.id.etClienteDetailsRestaurantHora);

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
        reviewsRef.whereEqualTo("user.uid", firebaseUser.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()){
                userReview = queryDocumentSnapshots.iterator().next().toObject(Review.class);
                userReview.setKey(queryDocumentSnapshots.iterator().next().getId());
                mostrarUserReview();
            }
        }).addOnFailureListener(e -> {
            llEscribir.setVisibility(View.GONE);
            llReserva.setVisibility(View.VISIBLE);
        });

        Query query = reviewsRef.whereNotEqualTo("user.uid", firebaseUser.getUid());
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

        //Setea la pagina de reservas
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.add(Calendar.MONTH, 2);
        CalendarConstraints constraintsBuilder = new CalendarConstraints.Builder()
                .setStart(today)
                .setEnd(calendar.getTimeInMillis()).build();
        MaterialDatePicker<Long> datepicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona una fecha")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder)
                .build();

        etFecha.setOnClickListener(v -> {
            if (!datepicker.isAdded()) datepicker.show(getSupportFragmentManager(), "Datepicker");
        });

        datepicker.addOnPositiveButtonClickListener(selection -> {
            localDate = Instant.ofEpochMilli(selection).atZone(ZoneId.of("GMT")).toLocalDate();
            etFecha.setText(dateFormatter.format(localDate));
            etFecha.setError(null);
        });

        MaterialTimePicker timepicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(10)
                .setTitleText("Selecciona una hora")
                .build();
        etHora.setOnClickListener(v -> {
            if (!timepicker.isAdded()) timepicker.show(getSupportFragmentManager(), "timepicker");
        });
        timepicker.addOnPositiveButtonClickListener(v -> {
            localTime = LocalTime.of(timepicker.getHour(), timepicker.getMinute());
            etHora.setText(timeFormatter.format(localTime));
            etHora.setError(null);
        });

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences_key), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        user = gson.fromJson(sharedPreferences.getString("user",""), User.class);
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
        llEscribir.setVisibility(View.GONE);
        llUserReview.setVisibility(View.VISIBLE);
        Glide.with(ClienteDetailsRestaurantActivity.this).load(firebaseUser.getPhotoUrl().toString()).into(ivUserPfp);
        tvUserNombre.setText(userReview.getUser().getNombre());
        tvUserRating.setText(String.valueOf(userReview.getRating()));
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
        request.setTitle("Carta "+restaurant.getNombre());
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Carta_"+restaurant.getNombre().replace(" ","_")+".pdf");
        request.allowScanningByMediaScanner();
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

    public void reservar(View view) {
        String strNumPersonas = etNumPersonas.getText().toString().trim();
        int numPersonas = 1;
        boolean isInvalid = false;

        if (localDate == null) {
            etFecha.setError("Ingresa una fecha");
            isInvalid = true;
        }

        if (localTime == null) {
            etHora.setError("Ingresa una hora");
            isInvalid = true;
        }

        if(!strNumPersonas.matches("[0-9]+")){
            etNumPersonas.setError("Ingrese el número de personas");
            etNumPersonas.requestFocus();
            isInvalid = true;
        } else {
            numPersonas = Integer.parseInt(strNumPersonas);
            if (numPersonas<=0) {
                etNumPersonas.setError("Debe ser mayor a 0");
                etNumPersonas.requestFocus();
                isInvalid = true;
            }

            if (numPersonas>25) {
                etNumPersonas.setError("Lo sentimos, el límite es de 25 personas");
                etNumPersonas.requestFocus();
                isInvalid = true;
            }
        }

        if (isInvalid) return;

        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        if (localDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            Toast.makeText(ClienteDetailsRestaurantActivity.this, "La reservas se deben realizar con 2 horas de anticipación", Toast.LENGTH_SHORT).show();
            return;
        }

        Date date = Date.from(localDateTime.toInstant(ZonedDateTime.now().getOffset()));
        Reserva reserva = new Reserva(localDate.format(dateFormatter), localTime.format(timeFormatter), Timestamp.now(), new Timestamp(date),
                "Pendiente", numPersonas,
                new Reserva.RUser(user.getNombre()+" "+user.getApellidos(), user.getAvatarUrl(), user.getDni(), FirebaseAuth.getInstance().getUid()),
                new Reserva.RRestaurant(restaurant.getNombre(), restaurant.getFotosUrl().get(0), restaurant.getKey()));
        FirebaseFirestore.getInstance().collection("reservas").add(reserva).addOnSuccessListener(documentReference -> {
            startActivity(new Intent(ClienteDetailsRestaurantActivity.this, ClienteReservasActivity.class));
            ActivityCompat.finishAffinity(ClienteDetailsRestaurantActivity.this);
            finish();
        }).addOnFailureListener(e -> {
            Log.d("msg", reserva.getCliente().getUid());
            Log.d("msg", "error", e);
        });
    }

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