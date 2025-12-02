package com.example.smartair.provider;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.LoginActivityView;
import com.example.smartair.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import utils.ParentProviderLinking;

public class ProviderCodeLinking extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_dashboard);

        Button Logout = findViewById(R.id.buttonLogout1);
        Button Link = findViewById(R.id.button4);

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProviderDashboardActivity.this, LoginActivityView.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        Link.setOnClickListener(v -> {

            TextInputEditText editCode = findViewById(R.id.Code1);
            String Code = editCode.getText().toString().trim();

            if (Code.isEmpty()) {
                editCode.setError("Enter a Code");
                return;
            }

            ParentProviderLinking.redeemCode(Code, new ParentProviderLinking.RedeemCallback() {
                @Override
                public void onSuccess(String parentEmail) {
                    Intent intent = new Intent(ProviderCodeLinking.this, ProviderHomePage.class);
                    intent.putExtra("parentEmail", parentEmail);

                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    editCode.setError((e.getMessage()));
                }
            });
        });
    }
}
