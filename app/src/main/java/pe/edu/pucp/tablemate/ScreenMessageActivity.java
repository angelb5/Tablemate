package pe.edu.pucp.tablemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

public class ScreenMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_screen_message);

        ImageView ivTop = findViewById(R.id.ivScreenmessageTop);
        ImageView ivBottom = findViewById(R.id.ivScreenmessageBottom);
        TextView tvTitle = findViewById(R.id.tvScreenmessageTitle);
        TextView tvContent = findViewById(R.id.tvScreenmessageContent);
        Button btn = findViewById(R.id.btnScreenmessage);

        Intent intent = getIntent();
        ScreenMessage screenMessage = (ScreenMessage) intent.getSerializableExtra("screenMessage");
        ivTop.setImageResource(screenMessage.getTopDrawable());
        ivBottom.setImageResource(screenMessage.getBottomImage());
        tvTitle.setText(screenMessage.getTitleText());
        tvContent.setText(screenMessage.getMessageText());
        btn.setText(screenMessage.getBtnText());

        btn.setOnClickListener(view -> {
            Intent btnIntent = new Intent(ScreenMessageActivity.this, screenMessage.getNextActivity());
            startActivity(btnIntent);
            finish();
        });
    }
}