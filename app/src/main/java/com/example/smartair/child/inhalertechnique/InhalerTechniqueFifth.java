package com.example.smartair.child.inhalertechnique;

import com.example.smartair.R;

public class InhalerTechniqueFifth extends InhalerTechnique{
    @Override
    protected void setup(){
        imageInhalerTechnique.setImageResource(R.drawable.fifth);
        setButtonNext(InhalerTechniqueSixth.class);
    }
}
