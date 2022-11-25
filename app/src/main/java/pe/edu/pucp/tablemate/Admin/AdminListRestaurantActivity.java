package pe.edu.pucp.tablemate.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.type.LatLng;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchSelectionCallback;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;


import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import pe.edu.pucp.tablemate.Adapters.RestaurantCardAdapter;
import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.Modals.ModalBottomSheetFilter;
import pe.edu.pucp.tablemate.R;

public class AdminListRestaurantActivity extends AppCompatActivity implements PermissionsListener {
    final int RADIUS = 6371;
    private PermissionsManager permissionsManager;
    PagingConfig config = new PagingConfig(8,4,true);
    RestaurantCardAdapter restaurantCardAdapter;
    FirestorePagingOptions<Restaurant> options;
    Query restaurantQuery = FirebaseFirestore.getInstance().collection("restaurants");

    RecyclerView recyclerView;
    ShimmerFrameLayout shimmerFrameLayout;
    LinearLayout emptyView;
    EditText etSearch;

    private LatLng userLatLng = null;
    private String categoryFilter = "";
    private String searchText = "";

    private ModalBottomSheetFilter modalBottomSheet = new ModalBottomSheetFilter();
    //Text Typing
    private Handler handler = new Handler(Looper.getMainLooper());
    private final long DELAY = 900;

    private Runnable searchRestaurant = new Runnable() {
        @Override
        public void run() {
            String searchTextFromEt = etSearch.getText().toString().trim();
            if(!searchTextFromEt.isEmpty()){
                searchText = searchTextFromEt;
                ejecutarQuery();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list_restaurant);

        recyclerView = findViewById(R.id.rvAdminListRestaurant);
        shimmerFrameLayout = findViewById(R.id.shimmerAdminListRestaurant);
        emptyView = findViewById(R.id.llAdminListRestaurantEmptyView);
        etSearch = findViewById(R.id.etAdminListRestaurantSearch);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(searchRestaurant);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(searchRestaurant, DELAY);
            }
        });

        enableLocationDistances();
    }

    public void populateRv(){
        options = new FirestorePagingOptions.Builder<Restaurant>()
                .setLifecycleOwner(this)
                .setQuery(restaurantQuery, config, restaurantSnapshotParser)
                .build();
        restaurantCardAdapter = new RestaurantCardAdapter(options, AdminDetailsRestaurantActivity.class);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(restaurantCardAdapter);
        restaurantCardAdapter.startListening();

        restaurantCardAdapter.addLoadStateListener(new Function1<CombinedLoadStates, Unit>() {
            @Override
            public Unit invoke(CombinedLoadStates combinedLoadStates) {
                LoadState refresh = combinedLoadStates.getRefresh();
                if (refresh instanceof LoadState.Loading) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    shimmerFrameLayout.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.startShimmerAnimation();
                }else if(refresh instanceof LoadState.NotLoading){
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    if (restaurantCardAdapter.getItemCount()>0){
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }else{
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                }
                return null;
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

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationDistances();
        }
    }

    @SuppressLint("MissingPermission")
    public void enableLocationDistances(){
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(this);
            locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    if (result.getLastLocation() != null) {
                        userLatLng = LatLng.newBuilder().setLatitude(result.getLastLocation().getLatitude()).setLongitude(result.getLastLocation().getLongitude()).build();
                    }
                    populateRv();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(restaurantCardAdapter!=null) restaurantCardAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(restaurantCardAdapter!=null)  restaurantCardAdapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(restaurantCardAdapter!=null) restaurantCardAdapter.stopListening();
    }

    public void goBackToPreviousActivity(View view){
        onBackPressed();
    }

    public void showFilters(View view){
        modalBottomSheet.show(getSupportFragmentManager(), modalBottomSheet.getTag());
    }

    public void setCategoryFilter(String categoryFilter) {
        if(!this.categoryFilter.equals(categoryFilter)){
            this.categoryFilter = categoryFilter;
            ejecutarQuery();
        }
    }


    public void ejecutarQuery(){
        restaurantQuery = FirebaseFirestore.getInstance().collection("restaurants");
        if(!categoryFilter.isEmpty()){
            restaurantQuery = restaurantQuery.whereEqualTo("categoria",categoryFilter);
        }
        if(!searchText.isEmpty()){
            restaurantQuery = restaurantQuery.whereArrayContains("searchKeywords", searchText);
        }

        options = new FirestorePagingOptions.Builder<Restaurant>()
                .setLifecycleOwner(this)
                .setQuery(restaurantQuery, config, restaurantSnapshotParser)
                .build();
        restaurantCardAdapter.updateOptions(options);
    }

    public double calculateDistance(double lat1, double lat2, double lon1, double lon2){
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = RADIUS * c; // convert to kilometers
        distance = Math.pow(distance, 2);
        return Math.sqrt(distance);
    }

    public SnapshotParser<Restaurant> restaurantSnapshotParser = new SnapshotParser<Restaurant>() {
        @NonNull
        @Override
        public Restaurant parseSnapshot(@NonNull DocumentSnapshot snapshot) {
            Restaurant restaurant = snapshot.toObject(Restaurant.class);
            if (restaurant !=null){
                restaurant.setKey(snapshot.getId());
                if (userLatLng!=null){
                    restaurant.setDistance(calculateDistance(userLatLng.getLatitude(),restaurant.getGeoPoint().getLatitude(),
                            userLatLng.getLongitude(), restaurant.getGeoPoint().getLongitude()));
                }
                return restaurant;
            }
            return new Restaurant();
        }
    };

}