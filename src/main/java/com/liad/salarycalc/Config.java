package com.liad.salarycalc;

import androidx.annotation.StringDef;

import static com.liad.salarycalc.Config.STAGES.*;

public class Config {

    public static final String STAGE = prod;
    private static Config instance;

    public static synchronized Config getInstance(){
        if (instance == null){
            instance = new Config();
        }
        return instance;
    }

    @StringDef({dev , prod})
    public @interface STAGES{
        String dev = "dev";
        String prod = "production";
    }
}
