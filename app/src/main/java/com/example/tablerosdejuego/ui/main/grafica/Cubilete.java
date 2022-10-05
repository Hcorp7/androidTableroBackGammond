package com.example.tablerosdejuego.ui.main.grafica;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentActivity;

import com.example.tablerosdejuego.R;
import com.example.tablerosdejuego.ui.main.Consola;
import com.example.tablerosdejuego.ui.main.enumerados.ColorFicha;
import com.example.tablerosdejuego.ui.main.enumerados.EstadoJuego;
import com.example.tablerosdejuego.ui.main.logica.LogicaJuego;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class Cubilete {
    private Dado mDado1;
    private Dado mDado2;
    private float mEjeY; //Eje de dados
    private float mSegmentoBlancoX1; //Los segmentos son los segmentos del eje de dados sobre los que se mostrarán los dados según color.
    private float mSegmentoNegroX1;
    private float mAjusteAnchoDado;
    private float mAnchoCasilla;
    private RelativeLayout mContenedor;
    private final Random mRnd = new Random();
    private LogicaJuego mLogica;
    private FragmentActivity mActivity;
    private int mMemoTirada;  //Memoriza el número de tirada de los dados.
    private final ArrayList<Dado> mDados = new ArrayList<Dado>(2);

    public Cubilete(LogicaJuego logica, RelativeLayout contenedor, float ejeY, FragmentActivity activity) {
        mLogica = logica;
        mContenedor = contenedor;
        mEjeY = ejeY;
        mActivity = activity;

        float margenIzquierdo = 15f;
        mAjusteAnchoDado = 27f;
        mAnchoCasilla = 125f;
        float anchoBarra = 80f;
        float columnaNegra = 2.5f; // Columna donde empieza el segmento blanco para posicionar los dados
        float columnaBlanca = 8.5f;
        mSegmentoNegroX1 = margenIzquierdo - mAjusteAnchoDado + (mAnchoCasilla * columnaNegra);
        mSegmentoBlancoX1 = margenIzquierdo - mAjusteAnchoDado + anchoBarra + (mAnchoCasilla * columnaBlanca);

        mDado1 = new Dado(mContenedor.getContext(),0);
        mDado1.setY(mEjeY);
        mContenedor.addView(mDado1);
        mDado2 = new Dado(mContenedor.getContext(),1);
        mDado2.setY(mEjeY);
        mContenedor.addView(mDado2);
        mDados.add(mDado1);
        mDados.add(mDado2);

        if (mLogica.getEstadoJuego() == EstadoJuego.ELECCION_TURNO) elegirTurno();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mLogica.getEstadoJuego() == EstadoJuego.EN_JUEGO) {
                    //Consola.mostrar("Hilo/OfrecerDados: " + mLogica.getOfrecerDados());
                    if (mLogica.ofrecerDados()) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ofrecerDados(mLogica.blancasMueven());
                            }
                        });
                    } else if (mDado1.tieneValor() && mDado2.tieneValor() && mDado1.numTirada() != mMemoTirada) {
                        //Consola.mostrar("MMMMMMMMMMMMMMMMEEEEEEEEEEEEEEEEk");
                        actualizarValoresEnLaLogica();
                    }
                }
                //Consola.mostrar("Hilo En juego");
            }
        }, 5000, 300);
    }

    private void elegirTurno() {
        mDado1.setImageResource(R.drawable.caras_dado_azul);
        mDado1.setImageLevel(mRnd.nextInt(6) + 24);
        mDado1.setX(calcularEjesX(ColorFicha.NEGRA, 1).firstElement());
        mDado2.setImageResource(R.drawable.caras_dado_rojo);
        mDado2.setImageLevel(mRnd.nextInt(6) + 24);
        mDado2.setX(calcularEjesX(ColorFicha.BLANCA, 1).firstElement());

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mDado1.tieneValor() && mDado2.tieneValor()) {
                    if (mDado1.getValor() != mDado2.getValor()) {
                        //mLogica.setValorDados(mDado1.getValor(), mDado2.getValor());
                        //Consola.mostrar("Timer/Los dados se han reseteado.");
                        actualizarValoresEnLaLogica();
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                               /* if (mLogica.getEstadoJuego() == EstadoJuego.ELECCION_TURNO)
                                    elegirTurno();
                                else primeraMano();*/
                                primeraMano();
                                //Consola.mostrar("Los dados tienen valor:" + mDado1.getValor() + " y " + mDado2.getValor());
                                timer.cancel();
                                timer.purge();
                            }
                        });
                        Consola.mostrar("Cubilete/ Los dados se han tirado");
                    } else {
                        mDado1.resetValor();
                        mDado2.resetValor();
                    }
                }
            }
        }, mDado1.getTiempoAnimacion(), 250);
    }

    private void actualizarValoresEnLaLogica(){
        mLogica.setValorDados(mDado1.getValor(), mDado2.getValor());
        mMemoTirada = mDado1.numTirada();
    }

    private void primeraMano() {
        Consola.mostrar("primeraMano/ se entra.");
        float escalaMedia = (FichaGrafica.ESCALA_MAYOR + FichaGrafica.ESCALA_NORMAL) / 2.85f;
        if (mLogica.blancasMueven()) {
            mDado1.setImageResource(mLogica.getIdRecursoDado(ColorFicha.BLANCA));
            Vector<Float> ejesX = calcularEjesX(ColorFicha.BLANCA, 2);
            mDado1.animate().scaleX(escalaMedia - FichaGrafica.CORRECCION_ESCALA).scaleY(escalaMedia).x(ejesX.firstElement()).setDuration(mDado1.getTiempoAnimacion());
            mDado2.animate().x(ejesX.lastElement()).setDuration(mDado2.getTiempoAnimacion());
        } else {
            mDado2.setImageResource(mLogica.getIdRecursoDado(ColorFicha.NEGRA));
            Vector<Float> ejesX = calcularEjesX(ColorFicha.NEGRA, 2);
            mDado1.animate().x(ejesX.firstElement()).setDuration(mDado1.getTiempoAnimacion());
            mDado2.animate().scaleX(escalaMedia - FichaGrafica.CORRECCION_ESCALA).scaleY(escalaMedia).x(ejesX.lastElement()).setDuration(mDado2.getTiempoAnimacion());
        }
        emparejarDados();
    }

    private void emparejarDados() {
        mDado1.setPareja(mDado2);
        mDado2.setPareja(mDado1);
    }

    private Vector<Float> calcularEjesX(ColorFicha colorFicha, int numDados) {
        float eje1 = 0;
        float segmento = colorFicha == ColorFicha.NEGRA ? mSegmentoNegroX1 : mSegmentoBlancoX1;
        float paso = 0;
        switch (numDados) {
            case 1:
                eje1 = segmento - mAjusteAnchoDado + mAnchoCasilla * 1.5f;
                break;
            case 2:
                eje1 = segmento - mAjusteAnchoDado + mAnchoCasilla * .5f;
                paso = mAnchoCasilla * 2;
                break;
            case 4:
                eje1 = segmento - mAjusteAnchoDado;
                paso = mAnchoCasilla;
        }
        Vector<Float> ejes = new Vector<>();
        ejes.add(eje1);
        int punto = 1;
        while (punto < numDados) ejes.add(eje1 + paso * punto++);
        return ejes;
    }

    private void ofrecerDados(Boolean blancasMueven) {
        ColorFicha colorFicha = blancasMueven ? ColorFicha.BLANCA : ColorFicha.NEGRA;
        Vector<Float> ejesX = calcularEjesX(colorFicha, 2);
        int rImagen = mLogica.getIdRecursoDado(colorFicha);
        float escala = mDado1.getEscalaOrigen();
        for (Dado dado:mDados) {
            dado.animate().setDuration(600).scaleX(0).scaleY(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dado.setImageResource(rImagen);
                    dado.setX(ejesX.get(dado.getId()));
                    dado.resetValor();
                    dado.animate().setDuration(600).scaleX(escala).scaleY(escala)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    dado.escalarDado();
                                }
                            });
                }
            });
        }
        mLogica.dadosOfrecidos();
        Consola.mostrar("Ofreciendo dados");
    }
}
