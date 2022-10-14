package com.example.tablerosdejuego.ui.main.logica;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.example.tablerosdejuego.ui.main.Consola;
import com.example.tablerosdejuego.ui.main.enumerados.ColorFicha;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

public class MovimientosPosibles {
    private int mPosA;
    private List<String> mManos = new ArrayList<>();
    private int mExtnNotacion = 0;
    private boolean mDadosIguales;


    ///   Clases internas           ********************************************

    private static class InstantaneaJuego implements Cloneable {
        protected int sPos;
        protected int sBloqu2;
        protected int sBloqu3;
        protected int sBloqueadas;
        protected int sEnBarra;
        protected ArrayList<Integer> sDdados = new ArrayList<>(4);

        protected InstantaneaJuego(int dado1, int dado2, int enBarra) {
            sDdados.add(dado1);
            sDdados.add(dado2);
            for (int i = 0; i < 2 && (dado1 == dado2); i++) sDdados.add(dado1);
            sEnBarra = enBarra;
        }

        protected void suprimirDado(Integer valorDado) {
            if (!sDdados.remove(valorDado))
                throw new IllegalStateException("Se esperaba un objeto conocido");
        }

        protected boolean puedenSalir() {
            return sPos < 64;
        }

        protected boolean puedeSalir(int casilla) {
            int bin = IntStream.range(0, casilla).reduce(0, (sub, i) -> sub += (int) Math.pow(2, i));
            return sPos <= bin;
        }

        public boolean hayEnBarra() {
            return sEnBarra > 0;
        }

        @NonNull
        @Override
        protected InstantaneaJuego clone() {
            InstantaneaJuego ins = null;
            try {
                ins = (InstantaneaJuego) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            ins.sDdados = new ArrayList<>(sDdados);
            return ins;
        }
    }

    private static class Movimiento {
        private final int sDesde;
        private final int sHasta;
        private final int sDado;

        protected Movimiento(int desdeCasilla, int hastaCasilla, int dado) {
            sDesde = desdeCasilla;
            sHasta = hastaCasilla;
            sDado = dado;
        }
    }

    private class Nodo {
        private final InstantaneaJuego sJuego;
        private final HashMap<Movimiento, Nodo> sHijo = new HashMap<>();

        protected Nodo(InstantaneaJuego juego) {
            sJuego = juego;
            crearHijos();
        }

        protected HashMap<Movimiento, Nodo> hijos() {
            return sHijo;
        }

        private void crearHijos() {
            for (int dado: sJuego.sDdados){
                if (sJuego.hayEnBarra()) {
                    InstantaneaJuego nuevoJuego = sJuego.clone();
                    int mov = 25 - dado;
                    int fichaD = ficha(mov);
                    if ((mPosA & fichaD) != fichaD) {  //Comprobamos si la casilla de salida está o no bloqueado por fichas A.
                        //Introducir la ficha en las casillas del cuadrante interior del jugador contrario.
                        if ((nuevoJuego.sPos ^ fichaD) > nuevoJuego.sPos) { //Comprobamos si ocupamos una casilla vacía (o desbloqueada_A). En caso negativo estamos ante una bloqueada por fichas B
                            nuevoJuego.sPos ^= fichaD; //Agregar la nueva posición (al coincidir con un bit a 0 se agrega).
                        } else { //La casilla ya está ocupada por, al menos, una ficha B.
                            if ((nuevoJuego.sBloqueadas & fichaD) != fichaD) {//Si no coincide en Bloqueadas es una casilla desbloqueada_B que pasa a estar bloqueada. Hay que registrarla en Bloqueos y Bloqueo2.
                                nuevoJuego.sBloqueadas ^= fichaD;
                                nuevoJuego.sBloqu2 ^= fichaD;
                            } else if ((nuevoJuego.sBloqu2 & fichaD) == fichaD) { //Si coincide en Bloqueo2 hay que suprimir el bit en Bloqueo2 y agregarlo a Bloqueo3.
                                nuevoJuego.sBloqu2 ^= fichaD;
                                nuevoJuego.sBloqu3 ^= fichaD;
                            } else if ((nuevoJuego.sBloqu3 & fichaD) == fichaD) { //Si coincide en Bloqueo3 hay que suprimir el bit en Bloqueo3.
                                nuevoJuego.sBloqu3 ^= fichaD;
                            }
                        }
                        nuevoJuego.sEnBarra--;
                        nuevoJuego.suprimirDado(dado);
                        sHijo.put(new Movimiento(0, mov, dado), new Nodo(nuevoJuego));
                    }
                } else {
                    for (int casilla : listaCasillas(sJuego.sPos)) {
                        InstantaneaJuego nuevoJuego = sJuego.clone();
                        int mov = mueveFicha(nuevoJuego, casilla, dado);
                        if (mov > -1) {
                            nuevoJuego.suprimirDado(dado);
                            sHijo.put(new Movimiento(casilla, mov, dado), new Nodo(nuevoJuego));
                        }
                    }
                }
                if (mDadosIguales) break;
            }
        }
    }

    //*****************************************************************************

    public MovimientosPosibles(HashMap<Integer, JSONObject> posiciones
            , ColorFicha colorTurno
            , int valorDado1
            , int valorDado2
            , int fichasEnBarra) {
        Consola.mostrar("MovimientosPosibles/MovimientosPosibles/ Juega: " + colorTurno.name() + " Con dado: " + valorDado1 + " y " + valorDado2);
        //Inicialización de valores
        InstantaneaJuego juego = new InstantaneaJuego(valorDado1, valorDado2, fichasEnBarra);
        posiciones.forEach((numCasilla, info) -> {
            int casilla = colorTurno.equals(ColorFicha.BLANCA) ? 25 - numCasilla : numCasilla;
            int ficha = ficha(casilla);
            try {
                ColorFicha colorFicha = (ColorFicha) info.get("color");
                if (!colorFicha.equals(colorTurno)) mPosA += ficha;
                else {
                    juego.sPos += ficha;
                    int total = info.getInt("total");
                    if (total > 1) juego.sBloqueadas += ficha;
                    if (total == 2) juego.sBloqu2 += ficha;
                    else if (total == 3) juego.sBloqu3 += ficha;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        mDadosIguales = valorDado1 == valorDado2;
        Nodo arbol = new Nodo(juego);
        extraerManos(arbol, "");
        if (!mManos.isEmpty()) {
            //Suprimir las manos con menor valor de dados cuando se trata de dados distintos y manos de un movimiento.
            if (mExtnNotacion == 6 && mManos.size() > 1 && !mDadosIguales) {
                Consola.mostrar("MovimientosPosibles/MovimientosPosibles/ un movimiento.");
                ArrayList<String> movLargos = new ArrayList<>();
                for (String mano : mManos) {
                    if (Integer.parseInt(mano.substring(0, 2))
                            - Integer.parseInt(mano.substring(3, 5))
                            == Math.max(valorDado1, valorDado2)) movLargos.add(mano);
                }
                if (!movLargos.isEmpty()) mManos = new ArrayList<>(movLargos);
            }
            //mManos.forEach(Consola::mostrar);
        }
        Consola.mostrar("MovimientosPosibles/MovimientosPosibles/ movimientos: " + mManos.size());
    }

    /**
     * Extrae todas las manos del arbol de movimientos
     * <p>
     * Carga las manos en un vector.
     * -Desechando las repetidas.
     * -Desechando las de menor movimiento.
     *
     * @param nodo
     * @param notacionRaiz
     * @return El contenido sólo es válido recursivamente. Cuando finaliza el método devuelve basura.
     */
    private String extraerManos(Nodo nodo, String notacionRaiz) {
        String notacion = "";
        for (Movimiento m : nodo.hijos().keySet()) {
            notacion = notacionRaiz + String.format("%1$02d/%2$02d ", m.sDesde, m.sHasta); //m.sDesde + "/" + m.sHasta + " ";
            Nodo nodoB = nodo.hijos().get(m);
            if (!nodoB.hijos().isEmpty()) notacion += extraerManos(nodoB, notacion);
            else if (!mManos.contains(notacion) && notacion.length() >= mExtnNotacion) {
                if (notacion.length() > mExtnNotacion) {
                    mExtnNotacion = notacion.length();
                    mManos.removeIf(pr -> pr.length() < mExtnNotacion);
                }
                mManos.add(notacion);
            }
        }
        return notacion;
    }

    /**
     * Esta función es operada por los objetos Node. Los objetos Node son objetos internos de esta clase. Este método está excluido para aligerar los objetos Node.
     * <p>
     * Con Xor, si el bit ficha coincide con un bite a 1 en mPosA se produce la resta entre los valores, si coincide con un bit a 0 en mPosA se produce la suma.
     *
     * @param juego   El objeto InstantaneaJuego que será procesado resultando en modificaciones por el movimiento.
     * @param casilla
     * @param dado
     * @return
     */
    private int mueveFicha(InstantaneaJuego juego, int casilla, int dado) {
        int fichaD = ficha(casilla - dado);
        int mov = -1;
        if (fichaD < 1 && juego.puedenSalir()) {
            if (casilla == dado || juego.puedeSalir(casilla)) { // Si la ficha está a palo o no tiene fichas anteriores.
                //Suprimir de la posicion original
                suprimirDeOrigen(juego, ficha(casilla));
                mov = 0;
            }
        } else {
            if ((mPosA & fichaD) != fichaD) {  //Comprobamos si el desplazamiento está o no bloqueado por fichas A.
                mov = casilla - dado;
                //Mover a
                if ((juego.sPos ^ fichaD) > juego.sPos) { //Comprobamos si ocupamos una casilla vacía (o desbloqueada_A). En caso negativo estamos ante una bloqueada por fichas B
                    //Establecer nueva posición
                    juego.sPos ^= fichaD; //Agregar la nueva posición (al coincidir con un bit a 0 se agrega).
                } else { //La casilla ya está ocupada por, al menos, una ficha B.
                    if ((juego.sBloqueadas & fichaD) != fichaD) {//Si no coincide en Bloqueadas es una casilla desbloqueada_B que pasa a estar bloqueada. Hay que registrarla en Bloqueos y Bloqueo2.
                        juego.sBloqueadas ^= fichaD;
                        juego.sBloqu2 ^= fichaD;
                    } else if ((juego.sBloqu2 & fichaD) == fichaD) { //Si coincide en Bloqueo2 hay que suprimir el bit en Bloqueo2 y agregarlo a Bloqueo3.
                        juego.sBloqu2 ^= fichaD;
                        juego.sBloqu3 ^= fichaD;
                    } else if ((juego.sBloqu3 & fichaD) == fichaD) { //Si coincide en Bloqueo3 hay que suprimir el bit en Bloqueo3.
                        juego.sBloqu3 ^= fichaD;
                    }
                }
                //Suprimir de la posicion original (Si no está bloqueada, caso de estarlo tratar bloqueadas)
                suprimirDeOrigen(juego, ficha(casilla));
            }
        }
        return mov;
    }

    public boolean estaBloqueado() {
        return mManos.isEmpty();
    }


    /**
     * Esta función es operada por los objetos Node. Los objetos Node son objetos internos de esta clase. Este método está excluido para aligerar los objetos Node.
     *
     * @param juego
     * @param ficha
     */
    private void suprimirDeOrigen(InstantaneaJuego juego, int ficha) {
        if ((juego.sBloqueadas & ficha) != ficha) { //Si no es una ficha bloqueada hay que suprimirla de la posición general
            juego.sPos ^= ficha;  //Suprimir de la posición general (al coincidir con un bit a 1 se suprime).
        } else if ((juego.sBloqu2 & ficha) == ficha) { //Suprimir de la posición de Bloqueo2 si existe y de Bloqueadas
            juego.sBloqu2 ^= ficha;
            juego.sBloqueadas ^= ficha;
        } else if ((juego.sBloqu3 & ficha) == ficha) { //Suprimir de la posición de Bloqueo3 si existe y agregar a (actualizar) Bloqueo2
            juego.sBloqu3 ^= ficha;
            juego.sBloqu2 ^= ficha;
        }
    }

    /**
     * Crea una lista ordenada con los números de las casillas que contienen fichasB
     * <p>
     * Esta función es operada por los objetos Node. Los objetos Node son objetos internos de esta clase. Este método está excluido para aligerar los objetos Node.
     *
     * @param bandera
     * @return
     */
    private List<Integer> listaCasillas(int bandera) {
        List<Integer> casillas = new ArrayList<>(24);
        int casilla = 1;
        do {
            if (bandera % 2 != 0) casillas.add(casilla);
            casilla++;
            bandera = bandera >> 1;
        } while (bandera > 0);
        return casillas;
    }

    /**
     * Esta función es operada por los objetos Node. Los objetos Node son objetos internos de esta clase. Este método está excluido para aligerar los objetos Node.
     *
     * @param casilla
     * @return
     */
    private int ficha(int casilla) {
        return (int) Math.pow(2, casilla - 1);
    }
}
