package com.example.smartair.child.inhalertechnique;

import com.example.smartair.R;

public class InhalerTechniqueThird extends InhalerTechnique{
    @Override
    protected void setup(){
        imageInhalerTechnique.setImageResource(R.drawable.third);
        setButtonNext(InhalerTechniqueFourth.class);
    }
}
