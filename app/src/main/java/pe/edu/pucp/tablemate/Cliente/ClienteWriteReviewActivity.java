package pe.edu.pucp.tablemate.Cliente;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
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
    ShapeableImageView ivReview;

    String fotoUrl = "";
    int rating = 5;
    Review review;

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
        ivReview = findViewById(R.id.ivClienteWriteReviewReview);

        tvRestaurantNombre.setText(restaurant.getNombre());
        tvRestaurantCategoria.setText(restaurant.getCategoria());
        Glide.with(ClienteWriteReviewActivity.this).load(restaurant.getFotosUrl().get(0)).into(ivRestaurant);

        if (intent.hasExtra("userReview")) {
            review = (Review) intent.getSerializableExtra("userReview");
            rating = review.getRating();
            etContent.setText(review.getContent());
            fotoUrl = review.getFotoUrl();
            if (!fotoUrl.isEmpty()) {
                ivReview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(ClienteWriteReviewActivity.this).load(review.getFotoUrl()).into(ivReview);
            }
        }
        showRating();
    }

    public void enviarReview(View view) {
        String content = etContent.getText().toString().trim();
        //TODO: validar data;
        if (review == null){
            crearNuevaReviewFirestore(content);
        } else {
            actualizarReviewFirestore(content);
        }
    }

    public void crearNuevaReviewFirestore(String content) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        review = new Review(new Review.RevUser(user.getDisplayName(), user.getUid(), user.getPhotoUrl().toString()), rating, content, fotoUrl, Timestamp.now());
        reviewsRef.add(review).addOnSuccessListener(documentReference -> {
            int newNumReviews = restaurant.getNumReviews() + 1;
            double newRating = (restaurant.getRating() * restaurant.getNumReviews() + rating) / newNumReviews;
            returnToDetails(newNumReviews, newRating);
        }).addOnFailureListener(e -> {
            Log.d("msg", "error", e);
        });
    }

    public void actualizarReviewFirestore(String content) {
        review.setTimestamp(Timestamp.now());
        int oldRating = review.getRating();
        review.setRating(rating);
        review.setContent(content);
        review.setFotoUrl(fotoUrl);

        reviewsRef.document(review.getKey()).set(review).addOnSuccessListener(documentReference -> {
            double newRating = (restaurant.getRating() * restaurant.getNumReviews() + rating - oldRating) / restaurant.getNumReviews();
            returnToDetails(restaurant.getNumReviews(), newRating);
        }).addOnFailureListener(e -> {
            Log.d("msg", "error", e);
        });
    }

    public void returnToDetails(int numReviews, double rating) {
        Intent intent = new Intent();
        intent.putExtra("numReviews", numReviews);
        intent.putExtra("rating", rating);
        intent.putExtra("userReview", review);
        intent.putExtra("tNano", review.getTimestamp().getNanoseconds());
        intent.putExtra("tSec", review.getTimestamp().getSeconds());
        setResult(RESULT_OK, intent);
        finish();
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