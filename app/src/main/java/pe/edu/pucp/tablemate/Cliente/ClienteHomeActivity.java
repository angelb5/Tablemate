package pe.edu.pucp.tablemate.Cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import pe.edu.pucp.tablemate.Helpers.BottomNavigationViewHelper;
import pe.edu.pucp.tablemate.R;

public class ClienteHomeActivity extends AppCompatActivity {

    final List<Integer> CATEGORY_IMAGES =  Arrays.asList(R.drawable.category_peruana, R.drawable.category_polleria, R.drawable.category_chifa, R.drawable.category_sushi, R.drawable.category_hamburguesas, R.drawable.category_italiana);
    final List<Integer> CATEGORY_TEXTS = Arrays.asList(R.string.category_peruana, R.string.category_polleria, R.string.category_chifa, R.string.category_sushi, R.string.category_hamburguesas, R.string.category_italiana);
    final int CATEGORY_SELECTOR_COLUMNS = CATEGORY_IMAGES.size();
    final int CATEGORY_SELECTOR_MARGIN = 16;
    final int CATEGORY_SELECTOR_MARGIN_HORIZONTAL = 40;
    BottomNavigationView bottomNavigationView;
    TextView tvHola;
    RecyclerView categorySelector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_home);

        setBottomNavigationView();
        categorySelector = findViewById(R.id.rvClienteHomeCategories);
        tvHola = findViewById(R.id.tvClienteHomeHola);

        tvHola.setText("Hola "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName()+"!");
        //Selector de categorias
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        CategorySelectorAdapter selectorAdapter = new CategorySelectorAdapter(this, CATEGORY_IMAGES, CATEGORY_TEXTS);
        selectorAdapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = categorySelector.getChildAdapterPosition(view);
                if (position>=0 && position<CATEGORY_IMAGES.size()){
                    Log.d("msg", getResources().getString(CATEGORY_TEXTS.get(position)));
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
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
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
}