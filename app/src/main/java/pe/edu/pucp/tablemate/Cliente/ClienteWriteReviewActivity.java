package pe.edu.pucp.tablemate.Cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.Entity.Review;
import pe.edu.pucp.tablemate.R;

public class ClienteWriteReviewActivity extends AppCompatActivity {
    Restaurant restaurant;
    CollectionReference reviewsRef;
    LinearLayout llRating;
    EditText etContent;
    TextView tvRestaurantNombre;
    TextView tvRestaurantCategoria;
    ShapeableImageView ivRestaurant;

    String fotoUrl = "";
    int rating = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_write_review);

        Intent intent = getIntent();
        if (intent==null || !intent.hasExtra("restaurant")){
            finish();
            return;
        }

        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");
        reviewsRef = FirebaseFirestore.getInstance().collection("restaurants").document(restaurant.getKey()).collection("reviews");

        llRating = findViewById(R.id.llClienteWriteReviewRating);
        etContent = findViewById(R.id.etClienteWriteReviewContent);
        tvRestaurantNombre = findViewById(R.id.tvClienteWriteReviewRestNombre);
        tvRestaurantCategoria = findViewById(R.id.tvClienteWriteReviewRestCategoria);
        ivRestaurant = findViewById(R.id.ivClienteWriteReviewRest);

        showRating();
        tvRestaurantNombre.setText(restaurant.getNombre());
        tvRestaurantCategoria.setText(restaurant.getCategoria());
        Glide.with(ClienteWriteReviewActivity.this).load(restaurant.getFotosUrl().get(0)).into(ivRestaurant);
    }

    public void crearReviewFirebase(View view) {
        String content = etContent.getText().toString().trim();
        //TODO: validar data;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Review review = new Review(new Review.RevUser(user.getDisplayName(), user.getUid(), user.getPhotoUrl().toString()), rating, content, fotoUrl, Timestamp.now());
        reviewsRef.add(review).addOnSuccessListener(documentReference -> {
            Intent intent = new Intent();
            int newNumReviews = restaurant.getNumReviews() + 1;
            intent.putExtra("numReviews", newNumReviews);
            double newRating = (restaurant.getRating() * restaurant.getNumReviews() + rating) / newNumReviews;
            intent.putExtra("rating", newRating);
            intent.putExtra("userReview", review);
            intent.putExtra("tNano", review.getTimestamp().getNanoseconds());
            intent.putExtra("tSec", review.getTimestamp().getSeconds());
            setResult(RESULT_OK, intent);
            finish();
        }).addOnFailureListener(e -> {
            Log.d("msg", "error", e);
        });
    }

    public void starPressed(View view) {
        if (llRating == null) return;
        rating = llRating.indexOfChild(view) + 1;
        showRating();
    }

    public void showRating() {
        for (int i = 0; i<llRating.getChildCount(); i++) {
            if (i<rating){
                ((ImageButton) llRating.getChildAt(i)).setColorFilter(getColor(R.color.yellow));
            } else {
                ((ImageButton) llRating.getChildAt(i)).setColorFilter(getColor(R.color.font_light));
            }
        }
    }

    public void backButton(View view){
        onBackPressed();
    }
}