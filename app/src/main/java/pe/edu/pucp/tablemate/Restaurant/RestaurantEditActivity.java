package pe.edu.pucp.tablemate.Restaurant;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pe.edu.pucp.tablemate.Adapters.ImageUploadAdapter;
import pe.edu.pucp.tablemate.Admin.AdminCreateRestaurantActivity;
import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.R;

public class RestaurantEditActivity extends AppCompatActivity {

    private static final String ICON_ID = "restaurantMarker";

    private MapView mapView;
    private MapboxMap mapboxMap;
    private SymbolManager symbolManager;

    SearchOptions options = new SearchOptions.Builder().limit(1).countries(Country.PERU).languages(Language.SPANISH).build();
    private Symbol symbol;

    SharedPreferences sharedPreferences;
    Restaurant restaurant;

    private EditText etNombre;
    private EditText etDescripcion;
    private EditText etDireccion;
    private TextView tvCarta;
    private RecyclerView rvFotos;
    private GridLayout glFotos;
    private Spinner spCategorias;

    private ImageUploadAdapter fotosAdapter;
    private ProgressBar pbPDF;
    private ProgressBar pbPhoto;
    private ProgressBar pbLoading;

    private ImageButton btnBack;
    private ImageButton btnPDFAttach;
    private ImageButton btnPhotoAttach;
    private ImageButton btnPhotoCam;
    private Button btnActualizar;
    private boolean isBusy = false;

    private double latDireccion;
    private double lngDireccion;
    private Uri cameraUri;
    private List<String> listFotos = new ArrayList<>();
    private String cartaUrl = "";

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
                    Toast.makeText(RestaurantEditActivity.this, "Debe seleccionar un archivo", Toast.LENGTH_SHORT).show();
                }
            }
    );

    ActivityResultLauncher<Intent> launcherPhotoDocument = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri uri = result.getData().getData();
                    compressImageAndUpload(uri,50);
                } else {
                    Toast.makeText(RestaurantEditActivity.this, "Debe seleccionar un archivo", Toast.LENGTH_SHORT).show();
                }
            }
    );

    ActivityResultLauncher<Intent> launcherPhotoCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    compressImageAndUpload(cameraUri,25);
                } else {
                    Toast.makeText(RestaurantEditActivity.this, "Debe seleccionar un archivo", Toast.LENGTH_SHORT).show();
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setea Mapbox
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_restaurant_edit);

        searchEngine = SearchEngine.createSearchEngine(new SearchEngineSettings(getString(R.string.mapbox_access_token)));
        //Setea adapters
        ArrayAdapter categoriasAdapter = ArrayAdapter.createFromResource(this, R.array.categories, R.layout.item_spinner);
        categoriasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        //Setea elementos
        mapView = findViewById(R.id.mvRestaurantEdit);
        etNombre = findViewById(R.id.etRestaurantEditNombre);
        etDireccion = findViewById(R.id.etRestaurantEditDireccion);
        etDescripcion = findViewById(R.id.etRestaurantEditDescripcion);
        tvCarta = findViewById(R.id.tvRestaurantEditCarta);
        pbPDF = findViewById(R.id.pbRestaurantEditPDF);
        pbPhoto = findViewById(R.id.pbRestaurantEditPhoto);
        pbLoading = findViewById(R.id.pbRestaurantEditLoading);
        rvFotos = findViewById(R.id.rvRestaurantEditFotos);
        glFotos = findViewById(R.id.glRestaurantEdit);
        spCategorias = findViewById(R.id.spRestauntEditCategorias);
        btnBack = findViewById(R.id.ibRestaurantEditBack);
        btnPDFAttach = findViewById(R.id.ibRestaurantEditPDFAttach);
        btnPhotoAttach = findViewById(R.id.ibRestaurantEditPhotoAttach);
        btnPhotoCam = findViewById(R.id.ibRestaurantEditPhotoCam);
        btnActualizar = findViewById(R.id.btnRestaurantEditActualizar);


        sharedPreferences = getSharedPreferences(getString(R.string.preferences_key), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        restaurant = gson.fromJson(sharedPreferences.getString("restaurant",""), Restaurant.class);
        restaurant.setGeoPoint(gson.fromJson(sharedPreferences.getString("geopoint",""), GeoPoint.class));
        listFotos = restaurant.getFotosUrl();
        etNombre.setText(restaurant.getNombre());
        etDireccion.setText(restaurant.getDireccion());
        etDescripcion.setText(restaurant.getDescripcion());
        latDireccion = restaurant.getGeoPoint().getLatitude();
        lngDireccion = restaurant.getGeoPoint().getLongitude();

        if (!restaurant.getCartaUrl().isEmpty()) {
            cartaUrl = restaurant.getCartaUrl();
            tvCarta.setText("carta_"+restaurant.getNombre().toLowerCase(Locale.ROOT).replace(" ","_")+".pdf");
        }
        //Implementa adapters
        fotosAdapter = new ImageUploadAdapter(this, listFotos);
        rvFotos.setAdapter(fotosAdapter);
        rvFotos.setLayoutManager(gridLayoutManager);
        spCategorias.setAdapter(categoriasAdapter);
        spCategorias.setSelection(0, true);
        String[] categoryArray = getResources().getStringArray(R.array.categories);
        for (int i = 0; i<categoryArray.length; i++) {
            if (categoryArray[i].equals(restaurant.getCategoria())) {
                spCategorias.setSelection(i, true);
                break;
            }
        }
        ((TextView) spCategorias.getSelectedView()).setTextColor(getColor(R.color.font_hint));
        evaluarEmpty();

        //Sobrescribe metodos
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(Style.OUTDOORS, style -> {
            style.addImage(ICON_ID, BitmapFactory.decodeResource(this.getResources(),R.drawable.marker_restaurant));
            this.mapboxMap = mapboxMap;
            symbolManager = new SymbolManager(mapView, mapboxMap, style, null);
            SymbolOptions symbolOptions = new SymbolOptions()
                    .withLatLng(new LatLng(restaurant.getGeoPoint().getLatitude(),restaurant.getGeoPoint().getLongitude()))
                    .withIconImage(ICON_ID)
                    .withIconSize(0.5f);
            symbolManager.create(symbolOptions);
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(restaurant.getGeoPoint().getLatitude(),restaurant.getGeoPoint().getLongitude())).zoom(14).build());
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
        if (pbPDF.getVisibility()==View.VISIBLE){
            Toast.makeText(RestaurantEditActivity.this, "Espera a que se termine de subir la carta", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/pdf");
            launcherPDFFile.launch(intent);
        }
    }

    public void uploadPhotoFromDocument(View view) {
        if (pbPhoto.getVisibility()==View.VISIBLE){
            Toast.makeText(RestaurantEditActivity.this, "Espera a que se termine de subir la foto", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            launcherPhotoDocument.launch(intent);
        }
    }

    public void uploadPhotoFromCamera(View view) {
        if (pbPhoto.getVisibility()==View.VISIBLE){
            Toast.makeText(RestaurantEditActivity.this, "Espera a que se termine de subir la foto", Toast.LENGTH_SHORT).show();
        }else{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                photoFromCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                photoFromCamera();
            } else {
                Toast.makeText(RestaurantEditActivity.this, "No se garantizaron los permisos", Toast.LENGTH_SHORT).show();
            }
        });

    public void photoFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cameraUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        launcherPhotoCamera.launch(cameraIntent);
    }

    public void updateMarker(){
        if(latDireccion != 0 && lngDireccion !=0 && symbolManager != null){
            symbolManager.deleteAll();
            SymbolOptions symbolOptions = new SymbolOptions()
                    .withLatLng(new LatLng(latDireccion,lngDireccion))
                    .withIconImage(ICON_ID)
                    .withIconSize(0.5f);
            symbol = symbolManager.create(symbolOptions);
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(latDireccion,lngDireccion)).zoom(14).build());
        }else if(symbolManager!=null){
            symbolManager.deleteAll();
            symbol = null;
        }
    }

    public void subirPDFConProgresoAFirebase(Uri uri) {
        Log.d("msg-test", String.valueOf(uri));
        StorageReference cartaChild = FirebaseStorage.getInstance().getReference().child("cartas/" + "carta_" + Timestamp.now().getSeconds() + ".pdf");
        pbPDF.setVisibility(View.VISIBLE);
        cartaChild.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            Log.d("msg-test", "Subido correctamente");
            pbPDF.setVisibility(View.GONE);
            cartaChild.getDownloadUrl().addOnSuccessListener(uri1 -> {
                cartaUrl = uri1.toString();
            }).addOnFailureListener(e->{
                Log.d("msg-test", "error",e);
                Toast.makeText(RestaurantEditActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
            });

        }).addOnFailureListener(e -> {
            Log.d("msg-test", "error", e);
            pbPDF.setVisibility(View.GONE);
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
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
        StorageReference photoChild = FirebaseStorage.getInstance().getReference().child("restaurantphotos/" + "photo_" + Timestamp.now().getSeconds() + ".jpg");
        pbPhoto.setVisibility(View.VISIBLE);
        photoChild.putBytes(imageBytes).addOnSuccessListener(taskSnapshot -> {
            pbPhoto.setVisibility(View.GONE);
            photoChild.getDownloadUrl().addOnSuccessListener(uri -> {
                listFotos.add(uri.toString());
                fotosAdapter.notifyDataSetChanged();
                evaluarEmpty();
            }).addOnFailureListener(e ->{
                Log.d("msg-test", "error",e);
                Toast.makeText(RestaurantEditActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Log.d("msg-test", "error",e);
            pbPhoto.setVisibility(View.GONE);
            Toast.makeText(RestaurantEditActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
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

    public void backButton(View view){
        onBackPressed();
    }

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

    public void actualizarRestaurante(View view){
        if(pbPDF.getVisibility()==View.VISIBLE){
            Toast.makeText(RestaurantEditActivity.this, "Espera a que se termine de subir la carta", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pbPhoto.getVisibility()==View.VISIBLE){
            Toast.makeText(RestaurantEditActivity.this, "Espera a que se termine de subir la foto", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isInvalid = false;

        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();

        if(nombre.isEmpty()){
            etNombre.setError("El nombre no puede estar vacío");
            etNombre.requestFocus();
            isInvalid = true;
        }

        if(nombre.length()>50){
            etNombre.setError("El nombre puede contener hasta 50 caracteres");
            etNombre.requestFocus();
            isInvalid = true;
        }

        if(descripcion.isEmpty()){
            etDescripcion.setError("La descripción no puede estar vacía");
            etDescripcion.requestFocus();
            isInvalid = true;
        }

        if(descripcion.length()>255){
            etDescripcion.setError("La descripción puede  contener hasta 255 caracteres");
            etDescripcion.requestFocus();
            isInvalid = true;
        }

        if(latDireccion == 0 || lngDireccion == 0 || direccion.length()<5){
            etDireccion.requestFocus();
            etDireccion.setError("Ingresa una dirección válida");
            isInvalid = true;
        }

        if(direccion.length()>150){
            etDireccion.requestFocus();
            etDireccion.setError("La dirección puede  contener hasta 150 caracteres");
            isInvalid = true;
        }

        if(listFotos.size()<3 || listFotos.size()>5){
            rvFotos.requestFocus();
            Toast.makeText(RestaurantEditActivity.this, "Se deben subir entre 3 a 5 fotos", Toast.LENGTH_SHORT).show();
            isInvalid = true;
        }

        if(spCategorias.getSelectedItemPosition()==0){
            spCategorias.requestFocus();
            Toast.makeText(RestaurantEditActivity.this, "Selecciona una categoría", Toast.LENGTH_SHORT).show();
            isInvalid = true;
        }

        if(isInvalid) return;

        String categoria = spCategorias.getSelectedItem().toString();
        mostrarCargando();
        Restaurant newRestaurant = restaurant;
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Map<String, Object> updates = new HashMap<>();
        if (!nombre.equals(restaurant.getNombre())) {
            updates.put("nombre", nombre);
            newRestaurant.setNombre(nombre);
            List<String> keywords = generateKeywords(nombre);
            updates.put("searchKeywords", keywords);
            newRestaurant.setSearchKeywords(keywords);
        }
        if (!descripcion.equals(restaurant.getDescripcion())) {
            updates.put("descripcion", descripcion);
            newRestaurant.setDescripcion(descripcion);
        }
        if (!categoria.equals(restaurant.getCategoria())) {
            updates.put("categoria", categoria);
            newRestaurant.setCategoria(categoria);
        }
        if (!cartaUrl.equals(restaurant.getCartaUrl())) {
            updates.put("cartaUrl", cartaUrl);
            newRestaurant.setCartaUrl(cartaUrl);
        }

        updates.put("fotosUrl", listFotos);
        newRestaurant.setFotosUrl(listFotos);

        if (!direccion.equals(restaurant.getDireccion())) {
            updates.put("direccion", direccion);
            newRestaurant.setDireccion(direccion);
        }
        if (latDireccion != restaurant.getGeoPoint().getLatitude() || lngDireccion != restaurant.getGeoPoint().getLongitude()) {
            GeoPoint newGeopoint = new GeoPoint(latDireccion, lngDireccion);
            updates.put("geoPoint", newGeopoint);
            editor.putString("geopoint", gson.toJson(newGeopoint));
        }
        editor.putString("restaurant", gson.toJson(newRestaurant));
        Log.d("msg", updates.toString());
        DocumentReference restaurantRef = FirebaseFirestore.getInstance().collection("restaurants").document(FirebaseAuth.getInstance().getUid());
        restaurantRef.update(updates).addOnSuccessListener(unused -> {
            ocultarCargando();
            editor.apply();
            Toast.makeText(RestaurantEditActivity.this, "Se ha actualizado la información", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("restaurant", newRestaurant);
            if (latDireccion != restaurant.getGeoPoint().getLatitude() || lngDireccion != restaurant.getGeoPoint().getLongitude()) {
                intent.putExtra("lat", latDireccion);
                intent.putExtra("lng", lngDireccion);
            }
            setResult(RESULT_OK, intent);
            finish();
        }).addOnFailureListener(e -> {
            ocultarCargando();
            Toast.makeText(RestaurantEditActivity.this, "No se pudo actualizar la información", Toast.LENGTH_SHORT).show();
        });
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

    public void mostrarCargando(){
        isBusy = true;
        pbLoading.setVisibility(View.VISIBLE);
        btnPDFAttach.setClickable(false);
        btnPhotoAttach.setClickable(false);
        btnPhotoCam.setClickable(false);
        btnBack.setClickable(false);
        btnActualizar.setClickable(false);
    }

    public void ocultarCargando(){
        isBusy = false;
        pbLoading.setVisibility(View.GONE);
        btnPDFAttach.setClickable(true);
        btnPhotoAttach.setClickable(true);
        btnPhotoCam.setClickable(true);
        btnBack.setClickable(true);
        btnActualizar.setClickable(true);
    }

    @Override
    public void onBackPressed() {
        if (!isBusy){
            super.onBackPressed();
        }
    }

    private List<String> generateKeywords(String inputString){
        inputString = inputString.toLowerCase();
        List<String> keywords = new ArrayList<>();

        List<String> words = Arrays.asList(inputString.split(" "));
        for (String word : words){
            String appendString = "";

            for (char c : inputString.toCharArray()){
                appendString+=c;
                keywords.add(appendString);
            }

            inputString = inputString.replace(word+" ","");
        }

        return keywords;
    }
}