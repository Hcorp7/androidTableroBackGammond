package com.example.tablerosdejuego.ui.main.logica;

import androidx.annotation.NonNull;

import com.example.tablerosdejuego.ui.main.Consola;
import com.example.tablerosdejuego.ui.main.enumerados.ColorFicha;
import com.example.tablerosdejuego.ui.main.enumerados.EstadoCasilla;

import java.util.Vector;

public class CasillaLogica {

    private EstadoCasilla mEstadoCasilla;
    private final int mNumCasilla;  //Las casillas van del 1 al 24. La barra es el 0 y es común a ambos colores.
    private Vector<FichaLogica> mFichas = new Vector<>();

    public CasillaLogica(int cardinal) {
        this.mNumCasilla = cardinal;
        this.mEstadoCasilla = EstadoCasilla.LIBRE;
    }

    public FichaLogica getFichaLogica(int lugar) {
        return mFichas.get(lugar - 1);
    }

    public boolean registrarFicha(@NonNull FichaLogica ficha_logica) {
        boolean es_agregable = this.mEstadoCasilla.esCompatible(ficha_logica.color());
        if (es_agregable) {
            this.mFichas.add(ficha_logica);
            actualizarEstado();
        }else Consola.mostrar("La ficha no es compatible");
        return es_agregable;
    }

    public void suprimirFicha(FichaLogica ficha) {
        mFichas.remove(ficha);
        actualizarEstado();
    }

    public int getTotalFichas() {
        return mFichas.size();
    }

    public EstadoCasilla getEstado() {
        return mEstadoCasilla;
    }

    public ColorFicha getColorFichas() {
        ColorFicha colorFicha = null;
   //     try {
            colorFicha = mFichas.firstElement().color();
   //     } catch (Exception e) {
   //         e.printStackTrace();
   //     }
        return colorFicha;
    }

    public int getTotalFichas(ColorFicha colorFicha) {
        int total;
        if (!mFichas.isEmpty() && mFichas.firstElement().color().equals(colorFicha)) total = mFichas.size();
        else total = 0;
        return total;
    }

    public int getNumCasilla() {
        return mNumCasilla;
    }

    private void actualizarEstado() {
        switch (mFichas.size()) {
            case 0:
                mEstadoCasilla = EstadoCasilla.LIBRE;
                break;
            case 1:
                mEstadoCasilla = mFichas.firstElement().esBlanca() ? EstadoCasilla.DESBLOQUEADA_BLANCAS : EstadoCasilla.DESBLOQUEADA_NEGRAS;
                break;
            case 2:
                if (mFichas.firstElement().color() != mFichas.get(1).color()){
                    mEstadoCasilla = EstadoCasilla.CAPTURA;
                    break;
                }
            default:
                mEstadoCasilla = mFichas.firstElement().esBlanca() ? EstadoCasilla.BLOQUEADA_BLANCAS : EstadoCasilla.BLOQUEADA_NEGRAS;
        }
        Consola.mostrar("La casilla " + mNumCasilla + " tiene el estado: " + mEstadoCasilla);
    }
}
