package pe.edu.pucp.tablemate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.Objects;

import pe.edu.pucp.tablemate.Admin.AdminHomeActivity;
import pe.edu.pucp.tablemate.Anonymus.OnboardingActivity;
import pe.edu.pucp.tablemate.Cliente.ClienteHomeActivity;
import pe.edu.pucp.tablemate.Entity.User;
import pe.edu.pucp.tablemate.Restaurant.RestaurantReservasActivity;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    CollectionReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        usersRef = FirebaseFirestore.getInstance().collection("users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser  == null || currentUser.isAnonymous()){
            Intent intentAnonymus = new Intent(MainActivity.this, OnboardingActivity.class);
            startActivity(intentAnonymus);
            finish();
        }else{
            accesoEnBaseARol(currentUser);
        }
    }

    public void accesoEnBaseARol(FirebaseUser firebaseUser){
        sharedPreferences = getSharedPreferences(getString(R.string.preferences_key),Context.MODE_PRIVATE);
        usersRef.document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snap) {
                if (!snap.exists()){
                    FirebaseAuth.getInstance().signOut();
                    Intent intentAnonymus = new Intent(MainActivity.this, OnboardingActivity.class);
                    startActivity(intentAnonymus);
                    finish();
                }
                Gson gson = new Gson();
                Intent intentPermisos;
                switch (Objects.requireNonNull(snap.getString("permisos"))){
                    case "Cliente":
                        Toast.makeText(MainActivity.this, "Hola Cliente", Toast.LENGTH_SHORT).show();
                        User user = snap.toObject(User.class);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user", gson.toJson(user));
                        editor.apply();
                        intentPermisos  = new Intent(MainActivity.this, ClienteHomeActivity.class);
                        startActivity(intentPermisos);
                        finish();
                        break;
                    case "Admin":
                        Toast.makeText(MainActivity.this, "Hola Admin", Toast.LENGTH_SHORT).show();
                        intentPermisos  = new Intent(MainActivity.this, AdminHomeActivity.class);
                        startActivity(intentPermisos);
                        finish();
                        break;
                    case "Restaurant":
                        Toast.makeText(MainActivity.this, "Hola Restaurante", Toast.LENGTH_SHORT).show();
                        intentPermisos  = new Intent(MainActivity.this, RestaurantReservasActivity.class);
                        startActivity(intentPermisos);
                        finish();
                        break;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseAuth.getInstance().signOut();
                Intent intentAnonymus = new Intent(MainActivity.this, OnboardingActivity.class);
                startActivity(intentAnonymus);
                finish();
            }
        });;
    }
}