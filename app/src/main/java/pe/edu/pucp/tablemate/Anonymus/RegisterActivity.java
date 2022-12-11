package pe.edu.pucp.tablemate.Anonymus;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import pe.edu.pucp.tablemate.Entity.User;
import pe.edu.pucp.tablemate.R;
import pe.edu.pucp.tablemate.ScreenMessage;
import pe.edu.pucp.tablemate.ScreenMessageActivity;

public class RegisterActivity extends AppCompatActivity {

    final String AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/tablemate-cf5ab.appspot.com/o/profilepics%2Fprofilepic_01.png?alt=media&token=33f838bc-d0b8-4509-ba16-5df32a6bdd07";
    FirebaseAuth firebaseAuth;
    CollectionReference usersRef;
    EditText etNombre;
    EditText etApellidos;
    EditText etCorreo;
    EditText etDni;
    EditText etContrasena;
    ProgressBar progressBar;
    Button btnLogin;
    Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Setea los EditText, ProgressBar y Button
        etNombre = findViewById(R.id.etRegisterNombre);
        etApellidos = findViewById(R.id.etRegisterApellidos);
        etCorreo = findViewById(R.id.etRegisterCorreo);
        etDni = findViewById(R.id.etRegisterDNI);
        etContrasena = findViewById(R.id.etRegisterContrasena);
        progressBar = findViewById(R.id.pbRegister);
        btnLogin = findViewById(R.id.btnRegisterGoToLogin);
        btnRegistrar = findViewById(R.id.btnRegisterRegistrar);


        //Setea Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseFirestore.getInstance().collection("users");
    }

    public void showHidePass(View view){
        if(etContrasena.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
            ((ImageView)(view)).setImageResource(R.drawable.ic_eye_disabled);
            //Show Password
            etContrasena.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        else{
            ((ImageView)(view)).setImageResource(R.drawable.ic_eye_open);
            //Hide Password
            etContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    public void registrarUsuario(View view){
        boolean isInvalid = false;
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String avatarUrl = AVATAR_URL;

        if(nombre.isEmpty()){
            etNombre.setError("No puede estar vacío");
            etNombre.requestFocus();
            isInvalid = true;
        }

        if(nombre.length()>20){
            etNombre.setError("No puede tener más de 20 caracteres");
            etNombre.requestFocus();
            isInvalid = true;
        }

        if(apellidos.isEmpty()){
            etApellidos.setError("No puede estar vacío");
            etApellidos.requestFocus();
            isInvalid = true;
        }

        if(apellidos.length()>40){
            etApellidos.setError("No puede tener más de 40 caracteres");
            etApellidos.requestFocus();
            isInvalid = true;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
            etCorreo.setError("Ingrese un correo válido");
            etCorreo.requestFocus();
            isInvalid = true;
        }

        if(!Patterns.PHONE.matcher(dni).matches() || dni.length()!=8){
            etDni.setError("Ingrese un DNI válido");
            etDni.requestFocus();
            isInvalid = true;
        }

        if(contrasena.length()<6){
            etContrasena.setError("Debe contener al menos 6 caracteres");
            etContrasena.requestFocus();
            isInvalid = true;
        }
        if(isInvalid) return;

        //Verifica que el dni y correo sean únicos
        mostrarCargando();
        User user = new User(nombre,apellidos,correo,dni,"Cliente",avatarUrl);
        crearUsuario(user, contrasena);
    }

    public void crearUsuario(User user, String contrasena){
        firebaseAuth.createUserWithEmailAndPassword(user.getCorreo(),contrasena)
                .addOnSuccessListener(authResult -> actualizarPerfilFireauth(authResult, user))
                .addOnFailureListener(e -> {
                    ocultarCargando();
                    etCorreo.setError("Verifica que el correo no este en uso");
                    etCorreo.requestFocus();
                    Toast.makeText(RegisterActivity.this, "Ocurrió un error en el servidor", Toast.LENGTH_LONG).show();
        });
    };

    public void actualizarPerfilFireauth(AuthResult authResult, User user){
        assert authResult.getUser()!=null;
        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getNombre()).setPhotoUri(Uri.parse(user.getAvatarUrl())).build();

        authResult.getUser().updateProfile(userProfileChangeRequest)
                .addOnSuccessListener(unused -> anadirUsuarioFirestore(authResult.getUser().getUid(),user))
                .addOnFailureListener(e -> {
                    ocultarCargando();
                    Toast.makeText(RegisterActivity.this, "Ocurrió un error al crear el perfil", Toast.LENGTH_LONG).show();
        });
    }

    public void anadirUsuarioFirestore(String uid, User user){
        usersRef.document(uid).set(user)
                .addOnSuccessListener(documentReference -> enviarCorreoVerificacion())
                .addOnFailureListener(e -> {
                    ocultarCargando();
                    Toast.makeText(RegisterActivity.this, "No se pudo completar el registro", Toast.LENGTH_LONG).show();
        });

    }

    public void enviarCorreoVerificacion(){
        assert firebaseAuth.getCurrentUser() !=null;
        firebaseAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(unused -> {
            firebaseAuth.signOut();
            ocultarCargando();
            ScreenMessage screenMessage = new ScreenMessage(R.drawable.ic_chat_smile_48, R.drawable.screenmessage_successful,
                    "Te has registrado en Tablemate",
                    "Verifica tu correo para poder usar la aplicación",
                    "Iniciar Sesión",LoginActivity.class);
            Intent successIntent = new Intent(RegisterActivity.this, ScreenMessageActivity.class);
            successIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            successIntent.putExtra("screenMessage", screenMessage);
            startActivity(successIntent);
            ActivityCompat.finishAffinity(RegisterActivity.this);
            finish();
        }).addOnFailureListener(e -> {
            ocultarCargando();
            Toast.makeText(RegisterActivity.this, "No pudimos enviar el correo de confirmación", Toast.LENGTH_LONG).show();
        });

    };

    public void mostrarCargando(){
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setClickable(false);
        btnRegistrar.setClickable(false);
    }

    public void ocultarCargando(){
        progressBar.setVisibility(View.GONE);
        btnLogin.setClickable(true);
        btnRegistrar.setClickable(true);
    }

    public void goToLoginActivity(View view){
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        ActivityCompat.finishAffinity(RegisterActivity.this);
        finish();
    }
}