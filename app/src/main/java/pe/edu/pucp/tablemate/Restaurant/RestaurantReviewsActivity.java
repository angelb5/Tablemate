package pe.edu.pucp.tablemate.Restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import pe.edu.pucp.tablemate.R;

public class RestaurantReviewsActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurantreviews);

        setBottomNavigationView();
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
}