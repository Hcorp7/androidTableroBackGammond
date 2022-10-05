package com.example.tablerosdejuego.ui.main.logica;

import androidx.annotation.NonNull;

import com.example.tablerosdejuego.ui.main.Consola;
import com.example.tablerosdejuego.ui.main.enumerados.ColorFicha;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Vector;
import java.util.stream.IntStream;

public class MovimientosPosibles {
    private int mPosA;
    private boolean mEsMultiple;
    private Vector<Vector<Movimiento>> mManos = new Vector();
    private Vector<Movimiento> mMovimientos = new Vector<>();


    ///   Clases anidadas           ********************************************
    private class InstantaneaJuego implements Cloneable {
        protected int mPos;
        protected int mBloqu2;
        protected int mBloqu3;
        protected int mBloqueadas;
        protected Vector<Integer> mDdados = new Vector<>();

        protected InstantaneaJuego(int dado1, int dado2) {
            mDdados.add(dado1);
            mDdados.add(dado2);
            for (int i = 0; i < 2 && mEsMultiple; i++) mDdados.add(dado1);
        }

        protected void suprimirDado(Integer valorDado) {
            if (!mDdados.remove(valorDado))
                throw new IllegalStateException("Se esperaba un objeto conocido");
        }

        protected boolean hayDados() {
            return !mDdados.isEmpty();
        }

        protected boolean puedenSalir() {
            return mPos < 64;
        }

        protected boolean puedeSalir(int casilla) {
            int bin = IntStream.range(0, casilla).reduce(0, (sub, i) -> sub += (int) Math.pow(2, i));
            return mPos <= bin;
        }

        @NonNull
        @Override
        protected InstantaneaJuego clone() throws CloneNotSupportedException {
            InstantaneaJuego ins = (InstantaneaJuego) super.clone();
            ins.mDdados = new Vector<>();
            mDdados.forEach(i -> {
                if (i > 0) ins.mDdados.add(new Integer(i));
            });
            return ins;
        }
    }

    private class Movimiento {
        private int mDesde;
        private int mHasta;
        private int mDado;

        protected Movimiento(int desdeCasilla, int hastaCasilla, int dado) {
            mDesde = desdeCasilla;
            mHasta = hastaCasilla;
            mDado = dado;
        }
    }

    /// Fin de clases anidadas      ******************************************

    public void calcular(HashMap<Integer, JSONObject> posiciones
            , ColorFicha colorTurno
            , int valorDado1
            , int valorDado2
            , boolean esTiradaMultiple) {
        Consola.mostrar("Juega: " + colorTurno.name() + " Con dado: " + valorDado1 + " y: " + valorDado2);
        //Inicialización de valores
        InstantaneaJuego juego = new InstantaneaJuego(valorDado1, valorDado2);
        mEsMultiple = esTiradaMultiple;
        posiciones.forEach((numCasilla, info) -> {
            int casilla = colorTurno.equals(ColorFicha.BLANCA) ? 25 - numCasilla : numCasilla;
            int ficha = ficha(casilla);
            try {
                ColorFicha colorFicha = (ColorFicha) info.get("color");
                if (!colorFicha.equals(colorTurno)) mPosA += ficha;
                else {
                    juego.mPos += ficha;
                    int total = info.getInt("total");
                    if (total > 1) juego.mBloqueadas += ficha;
                    if (total == 2) juego.mBloqu2 += ficha;
                    else if (total == 3) juego.mBloqu3 += ficha;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        buscarMovimientos(juego);


        if (false) {
            Consola.mostrar("______Manos________");
            mManos.forEach(l -> {
                Consola.mostrar("Jugada:");
                l.forEach(j -> {
                    Consola.mostrar("Desde: " + j.mDesde + " Hasta: " + j.mHasta + " Dado:" + j.mDado);
                });
            });
        }else{
            Consola.mostrar("_______Mano_______");
            mMovimientos.forEach(j->{
                Consola.mostrar("Desde: " + j.mDesde + " Hasta: " + j.mHasta + " Dado:" + j.mDado);

            });
        }


    }

    /**
     * Carga el vector atributo <code>mMovimientos</code> con todos los movimientos posibles.
     * - El primer movimiento es una ficha con un primer dado.
     * - El resto de movimiventos son fichas con el resto de los dados disponibles.
     * - Al final de esta mano se incluye un movimiento a cero para marcar el final de la mano.
     *
     * @param juego
     */
    private void buscarMovimientos(InstantaneaJuego juego) {
        InstantaneaJuego respaldo = null;
        try {
            respaldo = juego.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        for (Integer dado : juego.mDdados) {
            for (int casilla : casillas(juego.mPos)) {
                int mov1 = mueveFicha(juego, casilla, dado);
                if (mov1 > -1) {
                    //Consola.mostrar("Ficha en " + casilla + " a " + mov1 + " Dado " + dado);
                    mMovimientos.add(new Movimiento(casilla, mov1, dado));
                    try {
                        InstantaneaJuego j2 = juego.clone();
                        j2.suprimirDado(dado);
                        if (j2.hayDados()) buscarMovimientos(j2);
                        juego = respaldo.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!mMovimientos.isEmpty() && (mMovimientos.lastElement().mDado !=0)) mMovimientos.add(new Movimiento(0, 0, 0));
        }
    }

    /**
     * Con Xor, si el bit ficha coincide con un bite a 1 en mPosA se produce la resta entre los valores, si coincide con un bit a 0 en mPosA se produce la suma.
     *
     * @param juego
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
                if ((juego.mPos ^ fichaD) > juego.mPos) { //Comprobamos si ocupamos una casilla vacía (o desbloqueada_A). En caso negativo estamos ante una bloqueada por fichas B
                    //Establecer nueva posición
                    juego.mPos ^= fichaD; //Agregar la nueva posición (al coincidir con un bit a 0 se agrega).

                    //Comprobación experimental  mejoras-> quitar esta comprobación, si todo funciona, es redundante.
                    int fichaC = ficha(casilla);
                    if ((juego.mPos & fichaC) != fichaC) {
                        throw new IllegalStateException("Se esperaba una casilla con ficha B.");
                    }

                    //Suprimir de la posicion original
                    suprimirDeOrigen(juego, fichaC);
                } else { //La casilla ya está ocupada por, al menos, una ficha B.
                    if ((juego.mBloqueadas & fichaD) != fichaD) {//Si no coincide en Bloqueadas es una casilla desbloqueada_B que pasa a estar bloqueada. Hay que registrarla en Bloqueos y Bloqueo2.
                        juego.mBloqueadas ^= fichaD;
                        juego.mBloqu2 ^= fichaD;
                    } else if ((juego.mBloqu2 & fichaD) == fichaD) { //Si coincide en Bloqueo2 hay que suprimir el bit en Bloqueo2 y agregarlo a Bloqueo3.
                        juego.mBloqu2 ^= fichaD;
                        juego.mBloqu3 ^= fichaD;
                    } else if ((juego.mBloqu3 & fichaD) == fichaD) { //Si coincide en Bloqueo3 hay que suprimir el bit en Bloqueo3.
                        juego.mBloqu3 ^= fichaD;
                    }
                }
            }
        }
        return mov;
    }

    private void suprimirDeOrigen(InstantaneaJuego juego, int ficha) {
        juego.mPos ^= ficha;  //Suprimir de la posición general (al coincidir con un bit a 1 se suprime).
        if ((juego.mBloqu2 & ficha) == ficha) { //Suprimir de la posición de Bloqueo2 si existe y de Bloqueadas
            juego.mBloqu2 ^= ficha;
            juego.mBloqueadas ^= ficha; //agregada a ultima hora!!!!!!!!!
        } else if ((juego.mBloqu3 & ficha) == ficha) { //Suprimir de la posición de Bloqueo3 si existe y agregar a (actualizar) Bloqueo2
            juego.mBloqu3 ^= ficha;
            juego.mBloqu2 ^= ficha;
        }
    }

    private Vector<Integer> casillas(int bandera) {
        Vector casillas = new Vector();
        int casilla = 1;
        do {
            if (bandera % 2 != 0) casillas.add(casilla);
            casilla++;
            bandera = bandera >> 1;
        } while (bandera > 0);
        return casillas;
    }

    private int ficha(int casilla) {
        return (int) Math.pow(2, casilla - 1);
    }
}
