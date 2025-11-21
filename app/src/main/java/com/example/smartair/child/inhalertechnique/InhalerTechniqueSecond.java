package com.example.smartair.child.inhalertechnique;

import com.example.smartair.R;

public class InhalerTechniqueSecond extends InhalerTechnique{
    @Override
    protected void setup(){
        imageInhalerTechnique.setImageResource(R.drawable.second);
        setButtonNext(InhalerTechniqueThird.class);
    }
}
