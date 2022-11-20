package pe.edu.pucp.tablemate.Admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

import java.util.List;

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
    private Spinner spCategorias;
    private ProgressBar pbPDF;

    private double latDireccion;
    private double lngDireccion;

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
                    subirArchivoConProgreso(uri);
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

        ArrayAdapter categoriasAdapter = ArrayAdapter.createFromResource(this, R.array.categories, R.layout.item_spinner);
        categoriasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mapView = findViewById(R.id.mvAdminCreateRestaurant);
        etDireccion = findViewById(R.id.etAdminCreateRestaurantDireccion);
        etNombre = findViewById(R.id.etAdminCreateRestaurantNombre);
        spCategorias = findViewById(R.id.spAdminCreateRestauntCategorias);
        pbPDF = findViewById(R.id.pbAdminCreateRestaurant);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(Style.OUTDOORS, style -> {
            style.addImage(ICON_ID, BitmapFactory.decodeResource(this.getResources(),R.drawable.marker_restaurant));
            this.mapboxMap = mapboxMap;
            symbolManager = new SymbolManager(mapView, mapboxMap, style, null);
        }));

        spCategorias.setAdapter(categoriasAdapter);
        spCategorias.setSelection(0, true);
        ((TextView) spCategorias.getSelectedView()).setTextColor(getColor(R.color.font_hint));

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

    public void subirArchivoConProgreso(Uri uri) {
        Log.d("msg-test", String.valueOf(uri));
        String restaurantName = "";
        if(!etNombre.getText().toString().isEmpty()){
            restaurantName = etNombre.toString().replace(" ","_")+"_";
        }
        StorageReference cartaChild = FirebaseStorage.getInstance().getReference().child("cartas/" + "carta_"+restaurantName + Timestamp.now().getSeconds() + ".pdf");
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
}