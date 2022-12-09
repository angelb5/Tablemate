package pe.edu.pucp.tablemate.Cliente;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

import pe.edu.pucp.tablemate.Adapters.AvatarSelectorAdapter;
import pe.edu.pucp.tablemate.R;

public class ChooseAvatarActivity extends AppCompatActivity {

    List<String> AVATAR_URLS;
    AvatarSelectorAdapter avatarAdapter;
    RecyclerView rvAvatar;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_avatar);

        AVATAR_URLS = Arrays.asList(getResources().getStringArray(R.array.avatar_urls));
        avatarAdapter = new AvatarSelectorAdapter(this, AVATAR_URLS);
        rvAvatar = findViewById(R.id.rvChooseAvatar);
        progressBar = findViewById(R.id.pbChooseAvatar);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);

        Intent intent = getIntent();
        String avatarUrl = intent.getStringExtra("avatarUrl");

        for (int i = 0; i<AVATAR_URLS.size(); i++) {
            if (avatarUrl.equals(AVATAR_URLS.get(i))) {
                avatarAdapter.setSelectedItem(i);
                break;
            }
        }

        avatarAdapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = rvAvatar.getChildAdapterPosition(v);
                if (position>=0 && position<AVATAR_URLS.size()){
                    avatarAdapter.setSelectedItem(position);
                    avatarAdapter.notifyDataSetChanged();
                }
            }
        });

        rvAvatar.setAdapter(avatarAdapter);
        rvAvatar.setLayoutManager(layoutManager);

    }

    public void actualizarPfp(View view) {
        String avatarUrl = AVATAR_URLS.get(avatarAdapter.getSelectedItem());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(avatarUrl)).build();
        progressBar.setVisibility(View.VISIBLE);
        firebaseUser.updateProfile(userProfileChangeRequest).addOnSuccessListener(unused -> {
                FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid()).update("avatarUrl", avatarUrl).addOnSuccessListener(unused1 -> {
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent();
                    intent.putExtra("avatarUrl", avatarUrl);
                    setResult(RESULT_OK, intent);
                    finish();
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d("msg", "error", e);
                    Toast.makeText(ChooseAvatarActivity.this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                });
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Log.d("msg", "error", e);
            Toast.makeText(ChooseAvatarActivity.this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
        });
    }

    public void backButton(View view) {
        onBackPressed();
    }
}