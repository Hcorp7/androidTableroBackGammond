package com.example.tablerosdejuego.ui.main.controladores;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.example.tablerosdejuego.R;
import com.example.tablerosdejuego.ui.main.Consola;
import com.example.tablerosdejuego.ui.main.controladores.MainFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Establecer tapete  mejoras-> Cargar sólo la cantidad de imagen necesaria para el fondo.
        //BitmapFactory.Options opciones =; Hay que hallar las medidas para crear un recorte o ampliación de la imagen
        //Bitmap tapete = BitmapFactory.decodeResource(getResources(),R.drawable.tapete_hieba_verde,opciones);
        FrameLayout marco = (FrameLayout) findViewById(R.id.container);
        marco.setBackground(getResources().getDrawable(R.drawable.tapete_hierba_verde,getTheme()));
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }
public void teVeo(){

}
    @Override
    protected void onResume() {
        super.onResume();

        //Ocultar la barra de estado
        View decor_view = getWindow().getDecorView();
        decor_view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        //
        decor_view.setKeepScreenOn(true);
        //Esta linea oculta la barra de acciones
        //ActionBar actionBar = getActionBar();
        //actionBar.setElevation(0.5f);
    }
}