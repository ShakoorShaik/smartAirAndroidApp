package com.example.smartair.child.inhalertechnique;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

public abstract class InhalerTechnique extends AppCompatActivity{
    protected ImageView imageInhalerTechnique;
    protected Button buttonBack;
    protected Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inhaler_technique);
        imageInhalerTechnique = findViewById(R.id.techniqueImage);

        buttonBack = findViewById(R.id.backButton);
        buttonNext = findViewById(R.id.nextButton);

        setButtonBack();

        setup();
    }

    protected abstract void setup();

    protected void setButtonBack(){
        buttonBack.setOnClickListener(v -> {
            finish();
        });
    }

    protected void setButtonNext(Class<?> nextActivity) {
        buttonNext.setOnClickListener(v -> {
            startActivity(new Intent(this, nextActivity));
        });
    }

}
