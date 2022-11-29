package pe.edu.pucp.tablemate.Cliente;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;

import pe.edu.pucp.tablemate.Admin.AdminCreateRestaurantActivity;
import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.Entity.Review;
import pe.edu.pucp.tablemate.Entity.User;
import pe.edu.pucp.tablemate.R;

public class ClienteWriteReviewActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    Restaurant restaurant;
    CollectionReference reviewsRef;
    LinearLayout llRating;
    EditText etContent;
    TextView tvRestaurantNombre;
    TextView tvRestaurantCategoria;
    ShapeableImageView ivRestaurant;
    ShapeableImageView ivReview;
    ProgressBar pbPhoto;

    FirebaseUser user;
    User userG;

    String fotoUrl = "";
    int rating = 5;
    Review review;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_write_review);

        sharedPreferences = getSharedPreferences(getString(R.string.preferences_key),Context.MODE_PRIVATE);
        Gson gson = new Gson();
        userG = gson.fromJson(sharedPreferences.getString("user",""), User.class);

        Intent intent = getIntent();
        if (intent==null || !intent.hasExtra("restaurant")){
            finish();
            return;
        }


        restaurant = (Restaurant) intent.getSerializableExtra("restaurant");
        user = FirebaseAuth.getInstance().getCurrentUser();
        reviewsRef = FirebaseFirestore.getInstance().collection("restaurants").document(restaurant.getKey()).collection("reviews");

        llRating = findViewById(R.id.llClienteWriteReviewRating);
        etContent = findViewById(R.id.etClienteWriteReviewContent);
        tvRestaurantNombre = findViewById(R.id.tvClienteWriteReviewRestNombre);
        tvRestaurantCategoria = findViewById(R.id.tvClienteWriteReviewRestCategoria);
        ivRestaurant = findViewById(R.id.ivClienteWriteReviewRest);
        ivReview = findViewById(R.id.ivClienteWriteReviewReview);
        pbPhoto = findViewById(R.id.pbClienteWriteReviewPhoto);

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
                Glide.with(ClienteWriteReviewActivity.this).load(review.getFotoUrl()).placeholder(R.drawable.ic_image_placeholder_48).into(ivReview);
            }
        }
        showRating();
    }

    public void enviarReview(View view) {
        if (pbPhoto.getVisibility()==View.VISIBLE){
            Toast.makeText(ClienteWriteReviewActivity.this, "Espera a que se termine de subir la foto", Toast.LENGTH_SHORT).show();
            return;
        }
        String content = etContent.getText().toString().trim();
        //TODO: validar data;
        if (review == null){
            crearNuevaReviewFirestore(content);
        } else {
            actualizarReviewFirestore(content);
        }
    }

    public void crearNuevaReviewFirestore(String content) {
        review = new Review(new Review.RevUser(userG.getNombre()+" "+userG.getApellidos(), user.getUid(), user.getPhotoUrl().toString()), rating, content, fotoUrl, Timestamp.now());
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

    public void goToCameraActivity(View  view) {
        Intent cameraIntent = new Intent(ClienteWriteReviewActivity.this, ClienteCameraReviewActivity.class);
        cameraIntent.putExtra("restaurant", restaurant);
        cameraActivityLauncher.launch(cameraIntent);
    }

    ActivityResultLauncher<Intent> cameraActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) return;
                        byte[] bytearray = data.getByteArrayExtra("bytearray");
                        subirImagenAFirebase(bytearray);
                    }
                }
            }
    );

    public void subirImagenAFirebase(byte[] imageBytes) {
        StorageReference photoChild = FirebaseStorage.getInstance().getReference().child("reviewphotos/"+ user.getUid()+ "/"+restaurant.getKey()+ ".jpg");
        pbPhoto.setVisibility(View.VISIBLE);
        photoChild.putBytes(imageBytes).addOnSuccessListener(taskSnapshot -> {
            pbPhoto.setVisibility(View.GONE);
            photoChild.getDownloadUrl().addOnSuccessListener(uri -> {
                fotoUrl = uri.toString();
                Glide.with(ClienteWriteReviewActivity.this).load(fotoUrl).into(ivReview);
                ivReview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }).addOnFailureListener(e ->{
                Log.d("msg-test", "error",e);
                Toast.makeText(ClienteWriteReviewActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Log.d("msg-test", "error",e);
            pbPhoto.setVisibility(View.GONE);
            Toast.makeText(ClienteWriteReviewActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                long bytesTransferred = snapshot.getBytesTransferred();
                long totalByteCount = snapshot.getTotalByteCount();
                double progreso = (100.0 * bytesTransferred) / totalByteCount;
                Long round = Math.round(progreso);
                pbPhoto.setProgress(round.intValue());
            }
        });
    }

    public void uploadPhotoFromDocument(View view) {
        if (pbPhoto.getVisibility()==View.VISIBLE){
            Toast.makeText(ClienteWriteReviewActivity.this, "Espera a que se termine de subir la foto", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            launcherPhotoDocument.launch(intent);
        }
    }

    ActivityResultLauncher<Intent> launcherPhotoDocument = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri uri = result.getData().getData();
                    compressImageAndUpload(uri,50);
                } else {
                    Toast.makeText(ClienteWriteReviewActivity.this, "Debe seleccionar un archivo", Toast.LENGTH_SHORT).show();
                }
            }
    );

    public void compressImageAndUpload(Uri uri, int quality){
        try{
            Bitmap originalImage = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            originalImage.compress(Bitmap.CompressFormat.JPEG,quality,stream);
            subirImagenAFirebase(stream.toByteArray());
        }catch (Exception e){
            Log.d("msg","error",e);
        }
    }

    public void backButton(View view){
        onBackPressed();
    }
}