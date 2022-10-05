package com.example.tablerosdejuego.ui.main;

import android.util.Log;

public class Consola {
    private static final String TAG = "Hcorp";
    public static void mostrar(Object object){

        Log.d(TAG, object.toString());
    }
}
