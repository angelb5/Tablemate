package pe.edu.pucp.tablemate.Admin;

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
import com.google.firebase.auth.FirebaseUser;

import pe.edu.pucp.tablemate.Anonymus.LoginActivity;
import pe.edu.pucp.tablemate.R;

public class AdminHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
    }

    public void goToCreateRestaurantActivity(View view){
        startActivity(new Intent(AdminHomeActivity.this, AdminCreateRestaurantActivity.class));
    }

    public void goToListRestaurantActivity(View view){
        startActivity(new Intent(AdminHomeActivity.this, AdminListRestaurantActivity.class));
    }

    public void goToBestRestaurantsActivity(View view){
        startActivity(new Intent(AdminHomeActivity.this, AdminBestRestaurantsActivity.class));
    }

    public void logOutAdmin(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(AdminHomeActivity.this, LoginActivity.class));
        ActivityCompat.finishAffinity(AdminHomeActivity.this);
        finish();
    }
}