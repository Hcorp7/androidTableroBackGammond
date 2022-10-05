package com.example.tablerosdejuego.ui.main.logica;

import com.example.tablerosdejuego.ui.main.enumerados.ColorFicha;

public class FichaLogica {

    private ColorFicha mColorFicha = null;
    private int mNumCasilla = 0; //Número de la casilla donde se encuentran. 0 = en barra, -1 = fuera.
    private int mLugar;
    private float mHogarX; //El hogar es el lugar donde la ficha está o tiene que ir. Es 'su casa'
    private float mHogarY;

    public FichaLogica(ColorFicha colorFicha, int numCasilla, int lugar) {
        mColorFicha = colorFicha;
        mNumCasilla = numCasilla;
        mLugar = lugar;
    }

    public float getHogarX() {
        return mHogarX;
    }

    public void setHogarX(float hogarX) {
        mHogarX = hogarX;
    }

    public float getHogarY() {
        return mHogarY;
    }

    public void setHogarY(float hogarY) {
        mHogarY = hogarY;
    }

    public ColorFicha color() {
        return mColorFicha;
    }

    public boolean esBlanca() {
        return mColorFicha == ColorFicha.BLANCA;
    }

    public boolean esNegra() {
        return mColorFicha == ColorFicha.NEGRA;
    }

    public int getNumCasilla() {
        return mNumCasilla;
    }

    public void setNumCasilla(int numCasilla) {
        mNumCasilla = numCasilla;
    }

    public int getLugar() {
        return mLugar;
    }

    public void setLugar(int lugar) {
        mLugar = lugar;
    }

    public boolean estaEnBarra() {
        return mNumCasilla == GestorCasillasLogicas.NUM_AREA_BARRA;
    }

    public boolean estaFuera() {
        return mNumCasilla < 0;
    }
}
