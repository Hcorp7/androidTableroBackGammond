package com.example.tablerosdejuego.ui.main.grafica;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import java.util.Random;

public class Dado extends AppCompatImageView implements View.OnClickListener {
    private int mValor;
    private final ManejadorDado handler = new ManejadorDado();
    private final Random rnd = new Random();
    private Dado mPareja = null;
    private boolean mEnClick;
    private final float mEscalaOrigen = 0.5f;
    private final int mTiempoAnimacion = 1125;
    private boolean dadoTrucado = false;
    private int valorTrucado = 5;
    private int numTirada=0;
    private int mId;

    //Dialogo entre hilos que mueven el dado
    private final int mMensajes = 15;        //Número de mensajes para cambiar de cara (los mensajes decrecen hasta el cero)
    private final int mMensajePareja = 1;   //Número de mensaje que activa al dado pareja
    private final int mRetrasoMensaje = 75;

    public Dado(@NonNull Context context,int id) {
        super(context);
        mId = id;
        escalarDado();
        mEnClick = false;
        setOnClickListener(this);
    }

    public int getId(){
        return mId;
    }

    public int numTirada(){
        return numTirada;
    }

    public void girarDado() {
        float g = rnd.nextFloat() * 90;
        setRotation(g);
    }
    public float getEscalaOrigen(){
        return mEscalaOrigen;
    }
    public void escalarDado() {
        setScaleX(mEscalaOrigen);
        setScaleY(mEscalaOrigen);
    }

    public int resetValor() {
        int v = mValor;
        mValor = 0;
        return v;
    }

    public int getValor(){
        return mValor;
    }

    public boolean tieneValor() {
        return mValor != 0;
    }

    public int getTiempoAnimacion() {
        return mTiempoAnimacion;
    }

    public void setPareja(Dado dado) {
        mPareja = dado;
    }

    public void hacerClick() {
        if (!mEnClick) callOnClick();
    }

    @Override
    public void onClick(View v) {
        if (!tieneValor()) {
            mEnClick = true;
            setRotation(0);
            Interpolator interpolator = null;
            switch (rnd.nextInt(6)) {
                case 0:
                    interpolator = new LinearInterpolator();
                    break;
                case 1:
                    interpolator = new LinearOutSlowInInterpolator();
                    break;
                case 2:
                    interpolator = new AnticipateInterpolator();
                    break;
                case 3:
                    interpolator = new AccelerateDecelerateInterpolator();
                    break;
                case 4:
                    interpolator = new AccelerateInterpolator();
                    break;
                case 5:
                    interpolator = new DecelerateInterpolator();
                    break;
                default:
                    interpolator = new FastOutLinearInInterpolator();

            }
            animate().setDuration(mTiempoAnimacion)
                    .rotation(360)
                    .scaleY(.8f)
                    .scaleX(.8f)
                    .setListener(new EscuchadorAccion())
                    .setInterpolator(interpolator);
            handler.sendMessageDelayed(Message.obtain(handler, 0, 0), mRetrasoMensaje);
        }
    }

    class ManejadorDado extends Handler {
        public void handleMessage(Message msg) {
            int numCara = rnd.nextInt(24);
            setImageLevel(numCara);
            Integer rollsLeft = (Integer) msg.obj;
            if (rollsLeft < mMensajes) {
                if (rollsLeft == mMensajePareja && mPareja != null) mPareja.hacerClick();
                handler.sendMessageDelayed(Message.obtain(handler, 0, ++rollsLeft), mRetrasoMensaje);
            } else {
                mValor = (numCara / 4) + 1;
                if (dadoTrucado){
                    mValor = valorTrucado;
                    //dadoTrucado=false;
                }
                setImageLevel(mValor + 23);
                girarDado();
                numTirada++;
                mEnClick = false;
            }
        }
    }

    class EscuchadorAccion extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            girarDado();
            escalarDado();
        }
    }
}
