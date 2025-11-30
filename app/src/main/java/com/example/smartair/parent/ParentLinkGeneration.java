package com.example.smartair.parent;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.R;
import utils.DatabaseManager;
import utils.ParentProviderLinking;


public class ParentLinkGeneration extends AppCompatActivity{

    private TextView textViewGeneratedCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_code);

        textViewGeneratedCode = findViewById(R.id.textViewGeneratedCode);
        Button buttonGenerate = findViewById(R.id.button);
        buttonGenerate.setOnClickListener(v -> generateCode());

        updateText();

        Button returnButton = findViewById(R.id.button3);
        returnButton.setOnClickListener(v -> finish());

        Button invalidateCode = findViewById(R.id.button10);
        invalidateCode.setOnClickListener(v -> invalidateCode());
    }

    private void generateCode() {
        ParentProviderLinking.generateLinkCode(new DatabaseManager.SuccessFailCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ParentLinkGeneration.this, "Code Successfully Generated!", Toast.LENGTH_SHORT).show();
                updateText();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ParentLinkGeneration.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void invalidateCode() {
        ParentProviderLinking.InvalidateCode(new DatabaseManager.SuccessFailCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ParentLinkGeneration.this, "Referral code invalidated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ParentLinkGeneration.this, "Failed to invalidate code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateText() {
        DatabaseManager.getData("linkingCode.referralCode", new DatabaseManager.DataSuccessFailCallback() {
            @Override
            public void onSuccess(String code) {
                if (code != null && !code.isEmpty()) {
                    textViewGeneratedCode.setText(code);
                } else {
                    textViewGeneratedCode.setText("No code generated");
                }
            }

            @Override
            public void onFailure(Exception e) {
                textViewGeneratedCode.setText("Error loading code");
                Log.e("LinkingCode", "Failed to load code", e);
            }
        });
    }
}