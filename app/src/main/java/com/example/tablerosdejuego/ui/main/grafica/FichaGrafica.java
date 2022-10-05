package com.example.tablerosdejuego.ui.main.grafica;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.tablerosdejuego.ui.main.enumerados.ColorFicha;
import com.example.tablerosdejuego.ui.main.logica.FichaLogica;

import java.util.Random;

public class FichaGrafica extends AppCompatImageView {
    private FichaLogica mFicha;
    private MediaPlayer mMediaPlayer;
    public static final float AJUSTE_X = 56f;
    public static final float AJUSTE_Y = 53f;
    public static final float CORRECCION_ESCALA = 0.053f;
    public static final float ESCALA_NORMAL = 0.8f;
    public static final float ESCALA_MAYOR = 1.8f;

    public FichaGrafica(Context context, FichaLogica fichaLogica) {
        super(context);
        setRotation((int) ((new Random()).nextDouble() * 360));
        setScaleX(ESCALA_NORMAL - (ESCALA_NORMAL * CORRECCION_ESCALA));
        setScaleY(ESCALA_NORMAL);
        mFicha = fichaLogica;
    }

    public void memorizarPosicion(float x, float y) {
        mFicha.setHogarX(x);
        mFicha.setHogarY(y);
    }

    public ColorFicha getColorFicha() {
        return mFicha.color();
    }

    public int getNumCasilla() {
        return mFicha.getNumCasilla();
    }

    public FichaLogica getFichaLogica() {
        return mFicha;
    }

    public void recuperarPosicion() {
        moverHastaXY(mFicha.getHogarX(), mFicha.getHogarY(), false);
    }

    public boolean esNegra() {
        return mFicha.esNegra();
    }

    public boolean esBlanca() {
        return mFicha.esBlanca();
    }

    public int getLugar() {
        return mFicha.getLugar();
    }

    public void setLugar(int lugar) {
        mFicha.setLugar(lugar);
    }

    public void ampliar(int grado) {
        float escala = ESCALA_NORMAL + (grado * 0.015f);
        setScaleX(escala - (escala * CORRECCION_ESCALA));
        setScaleY(escala);
    }

    public void moverHastaXY(float destinoX, float destinoY, boolean cantar) {
        memorizarPosicion(destinoX, destinoY);
        /*if (cantar) {
            if (mMediaPlayer !=null) {
                Consola.mostrar("player position: "+  mMediaPlayer.getCurrentPosition());
                mMediaPlayer.stop();
            }
            mMediaPlayer = MediaPlayer.create(getContext(), R.raw.ficha_mueve);
            mMediaPlayer.start();
            Consola.mostrar("Cantaaaaaaaaaaaaaaaaa");
        }*/
        animate().setDuration(350)
                .x(destinoX)
                .y(destinoY)
                .setInterpolator(new DecelerateInterpolator())
                .scaleX(ESCALA_NORMAL - (ESCALA_NORMAL * CORRECCION_ESCALA))
                .scaleY(ESCALA_NORMAL)
                .start();
    }

    public boolean estaEnBarra() {
        return mFicha.estaEnBarra();
    }

    public boolean estaFuera() {
        return mFicha.estaFuera();
    }
}

