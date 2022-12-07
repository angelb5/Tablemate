package pe.edu.pucp.tablemate.Restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import pe.edu.pucp.tablemate.Adapters.ReservaClienteAdapter;
import pe.edu.pucp.tablemate.Entity.Reserva;
import pe.edu.pucp.tablemate.R;

public class RestaurantReservasActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    PagingConfig config = new PagingConfig(5,3,true);
    FirestorePagingOptions<Reserva> options;
    ReservaClienteAdapter reviewAdapter;
    ShimmerFrameLayout shimmerFrameLayout;
    LinearLayout llEmptyView;
    RecyclerView rvReservas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_reservas);

        setBottomNavigationView();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            finish();
            return;
        }

        shimmerFrameLayout = findViewById(R.id.shimmerRestaurantReservas);
        llEmptyView = findViewById(R.id.llRestaurantReservasEmptyView);
        rvReservas = findViewById(R.id.rvRestaurantReservas);

        shimmerFrameLayout.startShimmerAnimation();
        Query query = FirebaseFirestore.getInstance().collection("reservas").whereEqualTo("restaurant.id", firebaseUser.getUid());
        options = new FirestorePagingOptions.Builder<Reserva>()
                .setLifecycleOwner(this)
                .setQuery(query, config, reservaSnapshotParser)
                .build();
        reviewAdapter = new ReservaClienteAdapter(options, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvReservas.setLayoutManager(layoutManager);
        rvReservas.setAdapter(reviewAdapter);

        reviewAdapter.addLoadStateListener(new Function1<CombinedLoadStates, Unit>() {
            @Override
            public Unit invoke(CombinedLoadStates combinedLoadStates) {
                LoadState refresh = combinedLoadStates.getRefresh();
                if (refresh instanceof LoadState.Loading) {
                    rvReservas.setVisibility(View.GONE);
                    llEmptyView.setVisibility(View.GONE);
                    shimmerFrameLayout.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.startShimmerAnimation();
                }else if(refresh instanceof LoadState.NotLoading){
                    shimmerFrameLayout.stopShimmerAnimation();
                    shimmerFrameLayout.removeAllViews();
                    reviewAdapter.removeLoadStateListener(this);
                    if (reviewAdapter.getItemCount()>0){
                        rvReservas.setVisibility(View.VISIBLE);
                        llEmptyView.setVisibility(View.GONE);
                    }else{
                        rvReservas.setVisibility(View.GONE);
                        llEmptyView.setVisibility(View.VISIBLE);
                    }
                }
                return null;
            }
        });
    }

    public void setBottomNavigationView(){
        bottomNavigationView = findViewById(R.id.bottomNavMenuTiHomeAct);
        bottomNavigationView.setSelectedItemId(R.id.bottomNavMenuRestaurantReservas);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.bottomNavMenuRestaurantReservas:
                        return true;
                    case R.id.bottomNavMenuRestaurantReviews:
                        startActivity(new Intent(getApplicationContext(), RestaurantReviewsActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
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

    public SnapshotParser<Reserva> reservaSnapshotParser = snapshot -> {
        Reserva reserva = snapshot.toObject(Reserva.class);
        if (reserva !=null){
            reserva.setId(snapshot.getId());
            return reserva;
        }
        return new Reserva();
    };
}