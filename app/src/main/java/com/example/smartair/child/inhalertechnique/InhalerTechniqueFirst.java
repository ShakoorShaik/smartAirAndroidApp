package com.example.smartair.child.inhalertechnique;

import com.example.smartair.R;

public class InhalerTechniqueFirst extends InhalerTechnique {
    @Override
    protected void setup(){
        imageInhalerTechnique.setImageResource(R.drawable.first);
        setButtonNext(InhalerTechniqueSecond.class);
    }
}
