package pe.edu.pucp.tablemate.Cliente;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import pe.edu.pucp.tablemate.Admin.AdminHomeActivity;
import pe.edu.pucp.tablemate.Anonymus.LoginActivity;
import pe.edu.pucp.tablemate.Entity.User;
import pe.edu.pucp.tablemate.R;

public class ClienteProfileActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    TextView tvCorreo;
    TextView tvDni;
    EditText etNombre;
    EditText etApellidos;
    ImageView ivCliente;
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
        ivCliente = findViewById(R.id.ivClienteProfilePfp);
        setBottomNavigationView();

        sharedPreferences = getSharedPreferences(getString(R.string.preferences_key),Context.MODE_PRIVATE);
        Gson gson = new Gson();
        userG = gson.fromJson(sharedPreferences.getString("user",""), User.class);
        user = FirebaseAuth.getInstance().getCurrentUser();

        etNombre.setText(userG.getNombre());
        etApellidos.setText(userG.getApellidos());
        tvCorreo.setText(userG.getCorreo());
        tvDni.setText(userG.getDni());
        Glide.with(ClienteProfileActivity.this).load(userG.getAvatarUrl()).into(ivCliente);
    }

    public void actualizarPerfil(View view){

        String newNombre = etNombre.getText().toString().trim();
        String newApellidos = etApellidos.getText().toString().trim();

        boolean isInvalid = false;

        if(newNombre.isEmpty()){
            etNombre.setError("No puede estar vacío");
            etNombre.requestFocus();
            isInvalid = true;
        }

        if(newNombre.length()>20){
            etNombre.setError("No puede tener más de 20 caracteres");
            etNombre.requestFocus();
            isInvalid = true;
        }

        if(newApellidos.isEmpty()){
            etApellidos.setError("No puede estar vacío");
            etApellidos.requestFocus();
            isInvalid = true;
        }

        if(newApellidos.length()>40){
            etApellidos.setError("No puede tener más de 40 caracteres");
            etApellidos.requestFocus();
            isInvalid = true;
        }

        Map<String, Object> updates = new HashMap<>();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (isInvalid || firebaseUser == null) return;

        if (!userG.getNombre().equals(newNombre)) {
            updates.put("nombre", newNombre);
            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newNombre).build();
            firebaseUser.updateProfile(userProfileChangeRequest);
        }
        if(!userG.getApellidos().equals(newApellidos)) {
            updates.put("apellidos", newApellidos);
        }

        FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid()).update(updates).addOnSuccessListener(unused -> {
            Gson gson = new Gson();
            userG.setNombre(newNombre);
            userG.setApellidos(newApellidos);
            sharedPreferences.edit().putString("user", gson.toJson(userG)).apply();
            Toast.makeText(this, "Se han actualizado los datos", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(ClienteProfileActivity.this, "Hubo un error al actualizar los datos", Toast.LENGTH_SHORT).show();
        });
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
    public void goToChooseAvatar(View view) {
        Intent caIntent = new Intent(ClienteProfileActivity.this, ChooseAvatarActivity.class);
        caIntent.putExtra("avatarUrl", userG.getAvatarUrl());
        launcherChooseAvatar.launch(caIntent);
    }
    ActivityResultLauncher<Intent> launcherChooseAvatar = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) return;
                    Gson gson = new Gson();
                    userG.setAvatarUrl(data.getStringExtra("avatarUrl"));
                    Glide.with(ClienteProfileActivity.this).load(userG.getAvatarUrl()).into(ivCliente);
                    sharedPreferences.edit().putString("user", gson.toJson(userG)).apply();
                }
            }
    );

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        sharedPreferences.edit().remove("user").apply();
        startActivity(new Intent(ClienteProfileActivity.this, LoginActivity.class));
        ActivityCompat.finishAffinity(ClienteProfileActivity.this);
        finish();
    }
}