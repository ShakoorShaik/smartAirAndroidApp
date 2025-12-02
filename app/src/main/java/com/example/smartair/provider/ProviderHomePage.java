package com.example.smartair.provider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.LoginActivityView;
import com.example.smartair.R;
import com.example.smartair.provider.utils.ProviderCodeLinking;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class ProviderHomePage extends AppCompatActivity {

    /*
    Home <-> Trigger <-> Symptom <-> Triage <-> Rescue <-> PEF <-> Adherence <-> Home
     */

    private ProviderDataReading providerData;
    private TextView linkedText;
    private TextView currentChildText;
    private LinearLayout childrenContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_viewinfo);

        providerData = new ProviderDataReading(this);
        linkedText = findViewById(R.id.linkedText);
        currentChildText = findViewById(R.id.textView11);
        childrenContainer = findViewById(R.id.childrenListContainer);

        loadParentData();
        updateCurrentChildDisplay();
        setupButtons();
    }

    private void loadParentData() {
        providerData.getParentUid(new ProviderDataReading.ParentUidCallback() {
            @Override
            public void onSuccess(String parentUid, String parentEmail) {
                linkedText.setText("Linked with: " + parentEmail);
                loadChildren(parentUid);
            }

            @Override
            public void onFailure(String message) {
                linkedText.setText("Not linked to parent");
            }
        });
    }

    private void loadChildren(String parentUid) {
        providerData.getChildrenForParent(parentUid, new ProviderDataReading.ChildrenListCallback() {
            @Override
            public void onSuccess(List<ProviderCurrentChildData> children) {
                createChildWidgets(children);
            }

            @Override
            public void onFailure(String message) {
                TextView noChildren = new TextView(ProviderHomePage.this);
                noChildren.setText("No children found");
                childrenContainer.addView(noChildren);
            }
        });
    }

    private void createChildWidgets(List<ProviderCurrentChildData> children) {
        childrenContainer.removeAllViews();

        for (ProviderCurrentChildData child : children) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            TextView nameText = new TextView(this);
            nameText.setText(child.childName);
            nameText.setTextSize(18);
            nameText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            ));

            Button selectButton = new Button(this);
            selectButton.setText("Select");
            selectButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            selectButton.setOnClickListener(v -> {
                providerData.setCurrentChild(child.childUid, child.childName);
                updateCurrentChildDisplay();
            });

            row.addView(nameText);
            row.addView(selectButton);
            childrenContainer.addView(row);
        }
    }

    private void updateCurrentChildDisplay() {
        String childName = providerData.getCurrentChildName();
        currentChildText.setText("Currently viewing: " + childName);
    }

    private void setupButtons() {
        Button logout = findViewById(R.id.TopRightButton);
        Button linkNew = findViewById(R.id.TopLeftButton);
        Button left = findViewById(R.id.BottomLeftButton);
        Button right = findViewById(R.id.BottomRightButton);

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivityView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        linkNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProviderCodeLinking.class);
            startActivity(intent);
            finish();
        });

        left.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProviderAdherencePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        right.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProviderTriggerPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentChildDisplay();
    }
}