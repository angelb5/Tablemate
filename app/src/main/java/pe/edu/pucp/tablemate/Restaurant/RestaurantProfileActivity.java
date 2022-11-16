package pe.edu.pucp.tablemate.Restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import pe.edu.pucp.tablemate.Anonymus.LoginActivity;
import pe.edu.pucp.tablemate.Helpers.BottomNavigationViewHelper;
import pe.edu.pucp.tablemate.R;

public class RestaurantProfileActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurantprofile);

        setBottomNavigationView();
    }

    public void tiLogout(View view){
        FirebaseAuth.getInstance().signOut();
        Intent logoutIntent = new Intent(RestaurantProfileActivity.this, LoginActivity.class);
        startActivity(logoutIntent);
        ActivityCompat.finishAffinity(RestaurantProfileActivity.this);
        finish();
    }

    public void setBottomNavigationView(){
        bottomNavigationView = findViewById(R.id.bottomNavMenuTiProfileAct);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
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
}