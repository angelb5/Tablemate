package pe.edu.pucp.tablemate.Cliente;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

import pe.edu.pucp.tablemate.Adapters.CategorySelectorAdapter;
import pe.edu.pucp.tablemate.Decorations.CategorySelectorMargin;
import pe.edu.pucp.tablemate.R;

public class ClienteHomeActivity extends AppCompatActivity {

    final List<Integer> CATEGORY_IMAGES = Arrays.asList(R.drawable.category_peruana, R.drawable.category_polleria, R.drawable.category_chifa, R.drawable.category_sushi, R.drawable.category_hamburguesas, R.drawable.category_italiana, R.drawable.category_cafe, R.drawable.category_mexicana, R.drawable.category_pescados, R.drawable.category_veg, R.drawable.category_bar, R.drawable.category_carnes);
    final List<Integer> CATEGORY_TEXTS = Arrays.asList(R.string.category_peruana, R.string.category_polleria, R.string.category_chifa, R.string.category_sushi, R.string.category_hamburguesas, R.string.category_italiana, R.string.category_cafe, R.string.category_mexicana, R.string.category_pescados, R.string.category_veg, R.string.category_bar, R.string.category_carnes);
    final int CATEGORY_SELECTOR_COLUMNS = CATEGORY_IMAGES.size();
    final int CATEGORY_SELECTOR_MARGIN = 16;
    final int CATEGORY_SELECTOR_MARGIN_HORIZONTAL = 40;
    BottomNavigationView bottomNavigationView;
    TextView tvHola;
    RecyclerView categorySelector;
    EditText etSearch;

    //Text Typing
    private Handler handler = new Handler(Looper.getMainLooper());
    private final long DELAY = 900;

    private Runnable searchRestaurants = new Runnable() {
        @Override
        public void run() {
            String searchTextFromEt = etSearch.getText().toString().trim();
            if(!searchTextFromEt.isEmpty()){
                Intent searchIntent = new Intent(ClienteHomeActivity.this, ClienteListRestaurantActivity.class);
                searchIntent.putExtra("searchText", searchTextFromEt);
                startActivity(searchIntent);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_home);

        setBottomNavigationView();
        categorySelector = findViewById(R.id.rvClienteHomeCategories);
        tvHola = findViewById(R.id.tvClienteHomeHola);
        etSearch = findViewById(R.id.etClienteHomeSearch);

        tvHola.setText("Hola "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName()+"!");

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(searchRestaurants);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(searchRestaurants, DELAY);
            }
        });
        //Selector de categorias
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        CategorySelectorAdapter selectorAdapter = new CategorySelectorAdapter(this, CATEGORY_IMAGES, CATEGORY_TEXTS);
        selectorAdapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = categorySelector.getChildAdapterPosition(view);
                if (position>=0 && position<CATEGORY_IMAGES.size()){
                    Intent categoryIntent = new Intent(ClienteHomeActivity.this, ClienteListRestaurantActivity.class);
                    categoryIntent.putExtra("categoryFilter", getString(CATEGORY_TEXTS.get(position)));
                    startActivity(categoryIntent);
                }
            }
        });
        CategorySelectorMargin categorySelectorMargin = new CategorySelectorMargin(CATEGORY_SELECTOR_COLUMNS,CATEGORY_SELECTOR_MARGIN, CATEGORY_SELECTOR_MARGIN_HORIZONTAL);

        categorySelector.setLayoutManager(layoutManager);
        categorySelector.setAdapter(selectorAdapter);
        categorySelector.addItemDecoration(categorySelectorMargin);
    }

    public void setBottomNavigationView(){
        bottomNavigationView = findViewById(R.id.bottomNavMenuClienteHomeAct);
        bottomNavigationView.clearAnimation();
        bottomNavigationView.setSelectedItemId(R.id.bottomNavMenuClienteHome);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.bottomNavMenuClienteHome:
                        return true;
                    case R.id.bottomNavMenuClienteReservas:
                        startActivity(new Intent(getApplicationContext(), ClienteReservasActivity.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                    case R.id.bottomNavMenuClienteProfile:
                        startActivity(new Intent(getApplicationContext(), ClienteProfileActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }

    public void goToBestRestaurantsActivity(View view) {
        startActivity(new Intent(ClienteHomeActivity.this, ClienteBestRestaurantsActivity.class));
    }
    public void goToNearbyRestaurantsActivity(View view) {
        startActivity(new Intent(ClienteHomeActivity.this, ClienteNearbyRestaurantsActivity.class));
    }
}