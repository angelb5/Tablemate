package pe.edu.pucp.tablemate.Anonymus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager.widget.ViewPager;

import pe.edu.pucp.tablemate.R;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(false);
        setContentView(R.layout.activity_onboarding);
    }

    public void goToRegisterActivity(View view){
        Intent registerIntent = new Intent(OnboardingActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }

    public void goToLoginActivity(View view){
        Intent registerIntent = new Intent(OnboardingActivity.this,LoginActivity.class);
        startActivity(registerIntent);
        finish();
    }
}