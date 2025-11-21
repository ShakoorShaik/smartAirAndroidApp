package com.example.smartair.child.inhalertechnique;

import com.example.smartair.R;

public class InhalerTechniqueFourth extends InhalerTechnique{
    @Override
    protected void setup(){
        imageInhalerTechnique.setImageResource(R.drawable.fourth);
        setButtonNext(InhalerTechniqueFifth.class);
    }
}
