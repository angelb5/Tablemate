package pe.edu.pucp.tablemate.Cliente;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import pe.edu.pucp.tablemate.Entity.User;
import pe.edu.pucp.tablemate.R;

public class ClienteProfileActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    TextView tvCorreo;
    TextView tvDni;
    EditText etNombre;
    EditText etApellidos;
    SharedPreferences sharedPreferences;
    FirebaseUser user;
    User userG;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_profile);

        tvCorreo = findViewById(R.id.tvClienteProfileCorreo);
        tvDni = findViewById(R.id.tvClienteProfileDni);
        etNombre =  findViewById(R.id.etClienteProfileNombre);
        etApellidos = findViewById(R.id.etClienteProfileApellidos);
        setBottomNavigationView();

        sharedPreferences = getSharedPreferences(getString(R.string.preferences_key),Context.MODE_PRIVATE);
        Gson gson = new Gson();
        userG = gson.fromJson(sharedPreferences.getString("user",""), User.class);
        user = FirebaseAuth.getInstance().getCurrentUser();

        etNombre.setText(userG.getNombre());
        etApellidos.setText(userG.getApellidos());
        tvCorreo.setText(userG.getCorreo());
        tvDni.setText(userG.getDni());
    }

    public void setBottomNavigationView(){
        bottomNavigationView = findViewById(R.id.bottomNavMenuClienteProfileAct);
        bottomNavigationView.clearAnimation();
        bottomNavigationView.setSelectedItemId(R.id.bottomNavMenuClienteProfile);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.bottomNavMenuClienteHome:
                        startActivity(new Intent(getApplicationContext(), ClienteHomeActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    case R.id.bottomNavMenuClienteReservas:
                        startActivity(new Intent(getApplicationContext(), ClienteReservasActivity.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                    case R.id.bottomNavMenuClienteProfile:
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), ClienteHomeActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
}