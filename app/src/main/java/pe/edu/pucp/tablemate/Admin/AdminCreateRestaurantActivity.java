package pe.edu.pucp.tablemate.Admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.search.Country;
import com.mapbox.search.Language;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchEngineSettings;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.SearchSelectionCallback;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pe.edu.pucp.tablemate.Adapters.ImageUploadAdapter;
import pe.edu.pucp.tablemate.R;

public class AdminCreateRestaurantActivity extends AppCompatActivity {

    private static final String ICON_ID = "restaurantMarker";

    private MapView mapView;
    private MapboxMap mapboxMap;
    private SymbolManager symbolManager;

    SearchOptions options = new SearchOptions.Builder().limit(1).countries(Country.PERU).languages(Language.SPANISH).build();
    private Symbol symbol;

    private EditText etDireccion;
    private EditText etNombre;
    private TextView tvCarta;
    private RecyclerView rvFotos;
    private GridLayout glFotos;

    private ImageUploadAdapter fotosAdapter;
    private Spinner spCategorias;
    private ProgressBar pbPDF;

    private double latDireccion;
    private double lngDireccion;
    private List<String> listFotos = new ArrayList<>();

    SearchEngine searchEngine;
    //Text Typing
    private Handler handler = new Handler(Looper.getMainLooper());
    private final long DELAY = 900;
    private Runnable searchGeocode = new Runnable() {
        @Override
        public void run() {
            String direccion = etDireccion.getText().toString().trim();
            searchEngine.search(direccion, options, new SearchSelectionCallback() {
                @Override
                public void onResult(@NonNull SearchSuggestion searchSuggestion, @NonNull SearchResult searchResult, @NonNull ResponseInfo responseInfo) {
                    latDireccion = searchResult.getCoordinate().latitude();
                    lngDireccion = searchResult.getCoordinate().longitude();
                    updateMarker();
                }

                @Override
                public void onCategoryResult(@NonNull SearchSuggestion searchSuggestion, @NonNull List<SearchResult> list, @NonNull ResponseInfo responseInfo) {
                }

                @Override
                public void onSuggestions(@NonNull List<SearchSuggestion> list, @NonNull ResponseInfo responseInfo) {
                    if(!list.isEmpty()){
                        searchEngine.select(list.get(0), this);
                    }else{
                        latDireccion = 0;
                        lngDireccion = 0;
                        updateMarker();
                    }
                }

                @Override
                public void onError(@NonNull Exception e) {
                    latDireccion = 0;
                    lngDireccion = 0;
                    updateMarker();
                }
            });
        }
    };

    ActivityResultLauncher<Intent> launcherPDFFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri uri = result.getData().getData();
                    try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            if (index>0){
                                subirPDFConProgresoAFirebase(uri);
                                tvCarta.setText(cursor.getString(index));
                            }else{
                                tvCarta.setText("carta.pdf");
                            }
                        }
                    }
                    subirPDFConProgresoAFirebase(uri);
                } else {
                    Toast.makeText(AdminCreateRestaurantActivity.this, "Debe seleccionar un archivo", Toast.LENGTH_SHORT).show();
                }
            }
    );

    ActivityResultLauncher<Intent> launcherPhoto = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri uri = result.getData().getData();
                    try{
                        Bitmap originalImage = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        originalImage.compress(Bitmap.CompressFormat.JPEG,50,stream);
                        subirImagenAFirebase(stream.toByteArray());
                    }catch (Exception e){
                        Log.d("msg","eror",e);
                    }
                    subirPDFConProgresoAFirebase(uri);
                } else {
                    Toast.makeText(AdminCreateRestaurantActivity.this, "Debe seleccionar un archivo", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setea Mapbox
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_admin_create_restaurant);
        searchEngine = SearchEngine.createSearchEngine(new SearchEngineSettings(getString(R.string.mapbox_access_token)));

        List<String> testList = Arrays.asList("https://prod-ripcut-delivery.disney-plus.net/v1/variant/disney/74DE16E75861EFD79EC06613E9F2CFCC2D4301EBCF9F331C89C8ED55BCEA4371/scale?width=2880&aspectRatio=1.78&format=jpeg",
                "https://www.freshoffthegrid.com/wp-content/uploads/Skillet-ratatouille-16.jpg",
                "https://i.blogs.es/9c3012/ratatouille/450_1000.jpg");
        listFotos.addAll(testList);
        //Setea adapters
        ArrayAdapter categoriasAdapter = ArrayAdapter.createFromResource(this, R.array.categories, R.layout.item_spinner);
        categoriasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        fotosAdapter = new ImageUploadAdapter(this, listFotos);
        //Setea elementos
        mapView = findViewById(R.id.mvAdminCreateRestaurant);
        etDireccion = findViewById(R.id.etAdminCreateRestaurantDireccion);
        etNombre = findViewById(R.id.etAdminCreateRestaurantNombre);
        tvCarta = findViewById(R.id.tvAdminCreateRestaurantCarta);
        spCategorias = findViewById(R.id.spAdminCreateRestauntCategorias);
        pbPDF = findViewById(R.id.pbAdminCreateRestaurant);
        rvFotos = findViewById(R.id.rvAdminCreateRestaurant);
        glFotos = findViewById(R.id.glAdminCreateRestaurant);
        //Implementa adapters
        rvFotos.setAdapter(fotosAdapter);
        rvFotos.setLayoutManager(gridLayoutManager);
        spCategorias.setAdapter(categoriasAdapter);
        spCategorias.setSelection(0, true);
        ((TextView) spCategorias.getSelectedView()).setTextColor(getColor(R.color.font_hint));
        evaluarEmpty();
        //Sobrescribe metodos
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(Style.OUTDOORS, style -> {
            style.addImage(ICON_ID, BitmapFactory.decodeResource(this.getResources(),R.drawable.marker_restaurant));
            this.mapboxMap = mapboxMap;
            symbolManager = new SymbolManager(mapView, mapboxMap, style, null);
        }));

        etDireccion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(searchGeocode);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 3) {
                    handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(searchGeocode, DELAY);
                }else{
                    latDireccion = 0;
                    lngDireccion = 0;
                    updateMarker();
                }
            }
        });
        spCategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i==0){
                    ((TextView) view).setTextColor(getColor(R.color.font_hint));
                }else{
                    ((TextView) view).setTextColor(getColor(R.color.font_light));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    public void uploadCarta(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        launcherPDFFile.launch(intent);
    }

    public void uploadPhotoFromDocument(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        launcherPhoto.launch(intent);
    }

    public void updateMarker(){
        if(latDireccion != 0 && lngDireccion !=0){
            SymbolOptions symbolOptions = new SymbolOptions()
                    .withLatLng(new LatLng(latDireccion,lngDireccion))
                    .withIconImage(ICON_ID)
                    .withIconSize(0.5f);
            symbol = symbolManager.create(symbolOptions);
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(latDireccion,lngDireccion)).zoom(14).build());
        }else if(symbolManager!=null){
            symbolManager.deleteAll();
        }
    }

    public void subirPDFConProgresoAFirebase(Uri uri) {
        Log.d("msg-test", String.valueOf(uri));
        StorageReference cartaChild = FirebaseStorage.getInstance().getReference().child("cartas/" + "carta_" + Timestamp.now().getSeconds() + ".pdf");
        pbPDF.setVisibility(View.VISIBLE);
        cartaChild.putFile(uri).addOnSuccessListener(taskSnapshot -> Log.d("msg-test", "Subido correctamente"))
                .addOnFailureListener(e -> {
                    Log.d("msg-test", "error");
                    pbPDF.setVisibility(View.GONE);
                })
                .addOnCompleteListener(task -> {
                    pbPDF.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Log.d("msg-test", "ruta archivo: " + task.getResult());
                    }
                })
                .addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onPaused(@NonNull UploadTask.TaskSnapshot snapshot) {
                        Log.d("msg-test", "paused");
                        pbPDF.setVisibility(View.GONE);
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        long bytesTransferred = snapshot.getBytesTransferred();
                        long totalByteCount = snapshot.getTotalByteCount();
                        double progreso = (100.0 * bytesTransferred) / totalByteCount;
                        Long round = Math.round(progreso);
                        pbPDF.setProgress(round.intValue());
                    }
                });
    }

    public void subirImagenAFirebase(byte[] imageBytes) {
        StorageReference photoChild = FirebaseStorage.getInstance().getReference().child("restaurant_photos/" + "photo_" + Timestamp.now().getSeconds() + ".jpg");

        photoChild.putBytes(imageBytes).addOnSuccessListener(taskSnapshot -> {
            photoChild.getDownloadUrl().addOnSuccessListener(uri -> {
                listFotos.add(uri.toString());
                fotosAdapter.notifyDataSetChanged();
            }).addOnFailureListener(e ->{
                Log.d("msg-test", "error",e);
                Toast.makeText(AdminCreateRestaurantActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Log.d("msg-test", "error",e);
            Toast.makeText(AdminCreateRestaurantActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
        });
    }

    public void goBackToPreviousActivity(View view){
        onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void removerFoto(int position){
        listFotos.remove(position);
        fotosAdapter.notifyDataSetChanged();
        evaluarEmpty();
    }

    public void evaluarEmpty(){
        if (listFotos.size()>0){
            rvFotos.setVisibility(View.VISIBLE);
            glFotos.setVisibility(View.GONE);
        }else{
            rvFotos.setVisibility(View.GONE);
            glFotos.setVisibility(View.VISIBLE);
        }
    }
}