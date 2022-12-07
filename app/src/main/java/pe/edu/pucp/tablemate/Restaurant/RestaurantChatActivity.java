package pe.edu.pucp.tablemate.Restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import pe.edu.pucp.tablemate.Adapters.MensajesAdapter;
import pe.edu.pucp.tablemate.Entity.Mensaje;
import pe.edu.pucp.tablemate.Entity.Reserva;
import pe.edu.pucp.tablemate.R;

public class RestaurantChatActivity extends AppCompatActivity {
    Reserva reserva;
    boolean finalizada = false;
    String action = "Chat";
    Gson gson;
    ShapeableImageView ivFoto;
    TextView tvNombre;
    TextView tvEnviado;
    TextView tvNumPersonas;
    TextView tvFechaReserva;
    RecyclerView rvChat;
    LinearLayout llInputs;
    LinearLayout llFinalizada;
    LinearLayout llOptions;
    EditText etMensaje;
    DocumentReference chatRef;
    MensajesAdapter mensajesAdapter;
    private List<Mensaje> listaMensajes = new ArrayList<>();

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es"));
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    DateFormat df = new SimpleDateFormat("EEE dd MMM yyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_chat);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("reserva")){
            finish();
            return;
        }

        gson = new Gson();
        reserva = (Reserva) intent.getSerializableExtra("reserva");
        int tNano = intent.getIntExtra("tNano", 0);
        long tSec = intent.getLongExtra("tSec", 0);
        reserva.setSendTime(new Timestamp(tSec, tNano));
        int rNano = intent.getIntExtra("rNano", 0);
        long rSec = intent.getLongExtra("rSec", 0);
        reserva.setReservaTime(new Timestamp(rSec, rNano));

        if (reserva.getEstado().equals("Cancelada") || Timestamp.now().compareTo(reserva.getReservaTime())>=0) finalizada = true;

        ivFoto = findViewById(R.id.ivRestaurantChatFoto);
        tvNombre = findViewById(R.id.tvRestaurantChatNombre);
        tvEnviado = findViewById(R.id.tvRestaurantChatEnviado);
        tvNumPersonas = findViewById(R.id.tvRestaurantChatNumPersonas);
        tvFechaReserva = findViewById(R.id.tvRestaurantChatFechaReserva);
        rvChat = findViewById(R.id.rvRestaurantChat);
        llInputs = findViewById(R.id.llRestaurantChatInputs);
        llFinalizada = findViewById(R.id.llRestaurantChatFinalizada);
        llOptions = findViewById(R.id.llRestaurantChatOptions);
        etMensaje = findViewById(R.id.etRestaurantChat);

        Glide.with(this).load(reserva.getCliente().getAvatarUrl()).into(ivFoto);
        tvNombre.setText(reserva.getCliente().getNombre());
        tvNumPersonas.setText(String.valueOf(reserva.getNumPersonas()));
        tvFechaReserva.setText(reserva.getFecha()+" "+reserva.getHora().toUpperCase(Locale.ROOT));
        String fecha = df.format(reserva.getSendTime().toDate());
        tvEnviado.setText("Enviado "+fecha);

        if (finalizada) {
            llInputs.setVisibility(View.GONE);
            llFinalizada.setVisibility(View.VISIBLE);
        } else if (reserva.getEstado().equals("Pendiente")) {
            llOptions.setVisibility(View.VISIBLE);
            llInputs.setVisibility(View.GONE);
        }

        mensajesAdapter = new MensajesAdapter(FirebaseAuth.getInstance().getUid(), listaMensajes);
        rvChat.setAdapter(mensajesAdapter);
        rvChat.setLayoutManager(new LinearLayoutManager(RestaurantChatActivity.this));
        rvChat.setHasFixedSize(true);

        chatRef = FirebaseFirestore.getInstance().collection("reservas").document(reserva.getId());

        chatRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                assert snapshot !=null;
                Log.d("msg", String.valueOf(snapshot.get("messages")));
                List<HashMap<String,String>> mensajesMaps = (List<HashMap<String,String>>) snapshot.get("messages");
                if(mensajesMaps != null  && !mensajesMaps.isEmpty() && mensajesMaps.size()>listaMensajes.size()){
                    for(int i=listaMensajes.size(); i<mensajesMaps.size();i++){
                        HashMap<String,String> mensajeMap = mensajesMaps.get(i);
                        Mensaje mensaje = gson.fromJson(gson.toJsonTree(mensajeMap),Mensaje.class);
                        listaMensajes.add(mensaje);
                        mensajesAdapter.notifyItemInserted(listaMensajes.size()-1);
                        rvChat.smoothScrollToPosition(listaMensajes.size()-1);
                    }
                }
            }
        });
    }

    public void backButton(View view) { onBackPressed(); }

    public void aceptarReserva(View view) {
        if (finalizada) return;
        action = "Accept";
        llInputs.setVisibility(View.VISIBLE);
        etMensaje.setHint("Escribe un mensaje de confirmación");
    }

    public void cancelarReserva(View view) {
        if (finalizada) return;
        action = "Cancel";
        llInputs.setVisibility(View.VISIBLE);
        etMensaje.setHint("Especifica el motivo del rechazo");
    }

    public void enviarMensaje(View view){
        if (finalizada) return;

        String mensajeStr = etMensaje.getText().toString().trim();
        if (mensajeStr.isEmpty()) return;
        Mensaje mensaje = new Mensaje(mensajeStr, FirebaseAuth.getInstance().getUid(), Timestamp.now());

        if (action.equals("Accept")) {
            chatRef.update("messages", FieldValue.arrayUnion(mensaje), "estado", "Aceptada").addOnSuccessListener(unused -> {
                action = "Chat";
                etMensaje.setHint("Mensaje...");
                llOptions.setVisibility(View.GONE);
            }).addOnFailureListener(e -> {
                Toast.makeText(RestaurantChatActivity.this, "No se pudo enviar el mensaje", Toast.LENGTH_SHORT).show();
            });
        } else if (action.equals("Cancel")) {
            chatRef.update("messages", FieldValue.arrayUnion(mensaje),"estado", "Cancelada").addOnSuccessListener(unused -> {
                action = "Chat";
                etMensaje.setHint("Mensaje...");
                llOptions.setVisibility(View.GONE);
            }).addOnFailureListener(e -> {
                Toast.makeText(RestaurantChatActivity.this, "No se pudo enviar el mensaje", Toast.LENGTH_SHORT).show();
            });
        } else {
            chatRef.update("messages", FieldValue.arrayUnion(mensaje)).addOnFailureListener(e -> {
                Toast.makeText(RestaurantChatActivity.this, "No se pudo enviar el mensaje", Toast.LENGTH_SHORT).show();
            });
        }

        etMensaje.setText("");
    }
}