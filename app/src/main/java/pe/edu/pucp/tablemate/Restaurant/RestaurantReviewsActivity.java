package pe.edu.pucp.tablemate.Restaurant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import pe.edu.pucp.tablemate.Adapters.ReviewAdapter;
import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.Entity.Review;
import pe.edu.pucp.tablemate.R;

public class RestaurantReviewsActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Restaurant restaurant;
    PagingConfig config = new PagingConfig(5,3,true);
    FirestorePagingOptions<Review> options;
    ReviewAdapter reviewAdapter;
    ShimmerFrameLayout shimmerFrameLayout;
    LinearLayout llEmptyView;
    RecyclerView rvReviews;
    TextView tvNumReviews;
    TextView tvRating;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_reviews);
        shimmerFrameLayout = findViewById(R.id.shimmerRestaurantReviews);
        llEmptyView = findViewById(R.id.llRestaurantReviewsEmptyView);
        rvReviews = findViewById(R.id.rvRestaurantReviews);
        tvNumReviews = findViewById(R.id.tvRestaurantReviewsNumReviews);
        tvRating = findViewById(R.id.tvRestaurantReviewsRating);


        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences_key), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        restaurant = gson.fromJson(sharedPreferences.getString("restaurant",""), Restaurant.class);

        shimmerFrameLayout.startShimmerAnimation();
        Query tiQuery = FirebaseFirestore.getInstance().collection("restaurants").document(FirebaseAuth.getInstance().getUid()).collection("reviews");
        options = new FirestorePagingOptions.Builder<Review>()
                .setLifecycleOwner(this)
                .setQuery(tiQuery, config, Review.class)
                .build();
        reviewAdapter = new ReviewAdapter(options, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvReviews.setLayoutManager(layoutManager);
        rvReviews.setAdapter(reviewAdapter);

        reviewAdapter.addLoadStateListener(new Function1<CombinedLoadStates, Unit>() {
            @Override
            public Unit invoke(CombinedLoadStates combinedLoadStates) {
                LoadState refresh = combinedLoadStates.getRefresh();
                if (refresh instanceof LoadState.Loading) {
                    rvReviews.setVisibility(View.GONE);
                    llEmptyView.setVisibility(View.GONE);
                    shimmerFrameLayout.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.startShimmerAnimation();
                }else if(refresh instanceof LoadState.NotLoading){
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.removeAllViews();
                    reviewAdapter.removeLoadStateListener(this);
                    if (reviewAdapter.getItemCount()>0){
                        rvReviews.setVisibility(View.VISIBLE);
                        llEmptyView.setVisibility(View.GONE);
                    }else{
                        rvReviews.setVisibility(View.GONE);
                        llEmptyView.setVisibility(View.VISIBLE);
                    }
                }
                return null;
            }
        });
        setBottomNavigationView();

        mostrarRating(restaurant.getNumReviews(), restaurant.getRating());
        FirebaseFirestore.getInstance().collection("restaurants")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(snap -> {
                    mostrarRating((int) Math.floor(snap.getDouble("numReviews")), snap.getDouble("rating"));
                });
    }

    public void setBottomNavigationView(){
        bottomNavigationView = findViewById(R.id.bottomNavMenuTiDevicesAct);
        bottomNavigationView.setSelectedItemId(R.id.bottomNavMenuRestaurantReviews);
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
                        return true;
                    case R.id.bottomNavMenuRestaurantProfile:
                        startActivity(new Intent(getApplicationContext(), RestaurantProfileActivity.class));
                        overridePendingTransition(0,0);
                        finish();
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

    public void mostrarRating(int numReviews, double rating) {
        tvNumReviews.setText("("+numReviews+" Reseñas)");
        if (numReviews>0){
            tvRating.setText(String.format(Locale.getDefault(),"%.1f", rating));
        }else{
            tvRating.setText("--");
        }
    }
}