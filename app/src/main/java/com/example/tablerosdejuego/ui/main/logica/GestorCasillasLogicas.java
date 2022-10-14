package com.example.tablerosdejuego.ui.main.logica;

import androidx.annotation.NonNull;

import com.example.tablerosdejuego.ui.main.Consola;
import com.example.tablerosdejuego.ui.main.enumerados.ColorFicha;
import com.example.tablerosdejuego.ui.main.enumerados.EstadoCasilla;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;

public class GestorCasillasLogicas {
    private ArrayList<CasillaLogica> mCasillas;
    private ArrayList<Integer> mTotalEnBarra = new ArrayList<>(2);
    private ArrayList<Integer> mTotalFuera = new ArrayList<>(2);
    private int mCasillaEnCaptura = 0;
    public static final int NUM_AREA_BARRA = 0;
    public static final int NUM_AREA_FUERA = -1; // En número de casilla que tienen las fichas que salen.

    public GestorCasillasLogicas() {
        this.mCasillas = new ArrayList<>(24);
        IntStream.range(1, 25).forEach(i -> {
            mCasillas.add(new CasillaLogica(i));
        });
        switch (1) {
            case 1: // Posición de inicio
                agregarFichas(1, 2, ColorFicha.BLANCA);
                agregarFichas(6, 5, ColorFicha.NEGRA);
                agregarFichas(8, 3, ColorFicha.NEGRA);
                agregarFichas(12, 5, ColorFicha.BLANCA);
                agregarFichas(13, 5, ColorFicha.NEGRA);
                agregarFichas(17, 3, ColorFicha.BLANCA);
                agregarFichas(19, 5, ColorFicha.BLANCA);
                agregarFichas(24, 2, ColorFicha.NEGRA);
                break;
            case 2: //Negras y blancas pueden salir
                int filas = 3;
                for (int i = 19; i < 19 + filas; i++) agregarFichas(i, 2, ColorFicha.BLANCA);
                for (int i = 6; i > 6 - filas; i--) agregarFichas(i, 5, ColorFicha.NEGRA);
                break;
            case 3: //Medio juego
                agregarFichas(1, 2, ColorFicha.BLANCA);
                agregarFichas(6, 5, ColorFicha.NEGRA);
                //agregarFichas(8, 3, ColorFicha.NEGRA);
                agregarFichas(12, 5, ColorFicha.BLANCA);
                //agregarFichas(13, 5, ColorFicha.NEGRA);
                agregarFichas(17, 3, ColorFicha.BLANCA);
                agregarFichas(19, 5, ColorFicha.BLANCA);
                //agregarFichas(24, 2, ColorFicha.NEGRA);
                break;
            case 4: //Blancas tiran 4-6
                agregarFichas(1, 2, ColorFicha.BLANCA);
                agregarFichas(2, 2, ColorFicha.NEGRA);
                agregarFichas(3, 2, ColorFicha.NEGRA);
                agregarFichas(5, 2, ColorFicha.NEGRA);
                agregarFichas(6, 3, ColorFicha.NEGRA);
                agregarFichas(12, 5, ColorFicha.BLANCA);
                agregarFichas(16, 2, ColorFicha.NEGRA);
                agregarFichas(18, 3, ColorFicha.BLANCA);
                agregarFichas(19, 5, ColorFicha.BLANCA);
                agregarFichas(22, 2, ColorFicha.NEGRA);
                agregarFichas(23, 2, ColorFicha.NEGRA);
                break;
            case 5: //Blancas tiran 4-6 con fichas que pueden salir
                agregarFichas(1, 2, ColorFicha.NEGRA);
                agregarFichas(2, 2, ColorFicha.NEGRA);
                agregarFichas(3, 2, ColorFicha.NEGRA);
                agregarFichas(4, 2, ColorFicha.NEGRA);
                agregarFichas(5, 2, ColorFicha.NEGRA);
                agregarFichas(6, 3, ColorFicha.NEGRA);
                agregarFichas(19, 1, ColorFicha.BLANCA);
                agregarFichas(20, 2, ColorFicha.BLANCA);
                agregarFichas(22, 1, ColorFicha.BLANCA);
                agregarFichas(24, 2, ColorFicha.NEGRA);
                break;
            case 6: //Blancas tiran 4-6 -> Pueden ejecutar 4-6 5/1 3/0  o 6-4 5/0 3/0  o solo la última ?????
                agregarFichas(23, 2, ColorFicha.NEGRA);
                agregarFichas(22, 1, ColorFicha.BLANCA);
                agregarFichas(20, 1, ColorFicha.BLANCA);
                break;
            case 7: //Cuadrante iterior bloqueado por piezas contrarias
                for (int i = 1; i <7; i++) {
                    agregarFichas(25-i,3,ColorFicha.BLANCA);
                    agregarFichas(i,2,ColorFicha.NEGRA);
                }
                break;
            case 8:
                agregarFichas(1, 3, ColorFicha.NEGRA);
                agregarFichas(2, 4, ColorFicha.NEGRA);
                agregarFichas(5, 2, ColorFicha.NEGRA);
                agregarFichas(6, 2, ColorFicha.NEGRA);
                agregarFichas(7, 1, ColorFicha.BLANCA);
                agregarFichas(10, 2, ColorFicha.NEGRA);
                agregarFichas(12, 1, ColorFicha.BLANCA);
                agregarFichas(17, 2, ColorFicha.BLANCA);
                agregarFichas(18, 4, ColorFicha.BLANCA);
                agregarFichas(20, 2, ColorFicha.BLANCA);
                agregarFichas(22, 2, ColorFicha.BLANCA);
                agregarFichas(23, 2, ColorFicha.NEGRA);
                agregarFichas(24, 2, ColorFicha.BLANCA);
                break;
        }
        for (int i = 0; i < 2; i++) {
            mTotalFuera.add(0);
            mTotalEnBarra.add(0);
        }

    }

    public FichaLogica getFichaLogica(int numCasilla, int lugar) {
        return mCasillas.get(numCasilla - 1).getFichaLogica(lugar);
    }

    protected HashMap<Integer, JSONObject> posicionFichas() {
        HashMap distribucion = new HashMap();
        mCasillas.forEach(casilla -> {
            int total_fichas = casilla.getTotalFichas();
            if (total_fichas > 0) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("total", total_fichas);
                    jsonObject.put("color", casilla.getColorFichas());
                    distribucion.put(casilla.getNumCasilla(), jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("blancas", mTotalEnBarra.get(ColorFicha.BLANCA.ordinal()));
            jsonObject.put("negras", mTotalEnBarra.get(ColorFicha.NEGRA.ordinal()));
            distribucion.put(NUM_AREA_BARRA, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("blancas", mTotalFuera.get(ColorFicha.BLANCA.ordinal()));
            jsonObject.put("negras", mTotalFuera.get(ColorFicha.NEGRA.ordinal()));
            distribucion.put(NUM_AREA_FUERA, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return distribucion;
    }

    private void agregarFichas(int numCasilla, int numFichas, ColorFicha colorFicha) {
        Consola.mostrar("Agregando ficha con num casilla: " + numCasilla);
        for (int lugar = 1; lugar <= numFichas; lugar++) {
            mCasillas.get(numCasilla - 1).registrarFicha(new FichaLogica(colorFicha, numCasilla, lugar));
        }
    }

    /**
     * Mueve lógicamente una ficha a la barra.
     * <p>
     * Cuando la ficha se encuentra avanzando con normalidad, siempre que se produce esta llamada al
     * método se realiza desde una casilla en estado de CAPTURA.
     * Cuando la ficha se encuentra retrocediendo, el estado de la casilla nunca podrá ser BLOQUEADA
     * POR -color contrario-
     *
     * @param ficha
     */
    protected void fichaABarra(FichaLogica ficha) {
        int numCasilla = ficha.getNumCasilla();
        mCasillas.get(numCasilla - 1).suprimirFicha(ficha);
        int ordinal_color = ficha.color().ordinal();
        int total = mTotalEnBarra.get(ordinal_color);
        mTotalEnBarra.set(ordinal_color, ++total);
        ficha.setNumCasilla(NUM_AREA_BARRA);
        mCasillaEnCaptura = 0;
    }

    protected boolean hayCasillasEnCaptura() {
        return mCasillaEnCaptura > 0;
    }

    protected int casillaEnCaptura() {
        return mCasillaEnCaptura;
    }

    protected boolean fichaACasilla(@NonNull FichaLogica ficha, int numCasilla) {
        CasillaLogica cDestino = mCasillas.get(numCasilla - 1);
        boolean se_registra = cDestino.registrarFicha(ficha);
        if (se_registra) {
            int casillaOrigen = ficha.getNumCasilla();
            if (casillaOrigen > 0) {
                mCasillas.get(casillaOrigen - 1).suprimirFicha(ficha);
                if (mCasillaEnCaptura == casillaOrigen)
                    mCasillaEnCaptura = 0; //Esta línea tiene funcionalidad solo cuando se retrocede la jugada
                Consola.mostrar("fichaACasilla/ se suprime la ficha en la casilla: " + casillaOrigen);
            } else {
                int ordinal_color = ficha.color().ordinal();
                if (casillaOrigen == NUM_AREA_BARRA) {
                    int total = mTotalEnBarra.get(ordinal_color);
                    mTotalEnBarra.set(ordinal_color, --total);
                } else {
                    int total = mTotalFuera.get(ordinal_color);
                    mTotalFuera.set(ordinal_color, --total);
                }
            }
            if (estadoCasilla(numCasilla) == EstadoCasilla.CAPTURA) {
                mCasillaEnCaptura = numCasilla;
            }
            ficha.setLugar(cDestino.getTotalFichas());
            Consola.mostrar("fichaACasilla/ se registra la ficha de color: " + ficha.color() + " en la casilla: " + numCasilla);
            ficha.setNumCasilla(numCasilla);
        }
        return se_registra;
    }

    protected boolean fichaAFuera(@NonNull FichaLogica ficha) {
        boolean salen = puedenSalir(ficha.color());
        if (salen) {
            mCasillas.get(ficha.getNumCasilla() - 1).suprimirFicha(ficha);
            ficha.setNumCasilla(NUM_AREA_FUERA);
            int ordinal_color = ficha.color().ordinal();
            int total = mTotalFuera.get(ordinal_color);
            mTotalFuera.set(ordinal_color, ++total);
        }
        return salen;
    }

    protected boolean hayFichasEnBarra(@NonNull ColorFicha colorFicha) {
        return mTotalEnBarra.get(colorFicha.ordinal()) > 0;
    }

    protected boolean puedenSalir(ColorFicha colorFicha) {
        boolean encontradas = hayFichasEnBarra(colorFicha);
        if (!encontradas) {
            int inicial = colorFicha.equals(ColorFicha.BLANCA) ? 1 : 7;
            int ultima = inicial + 17;
            while (!encontradas && inicial <= ultima) {
                encontradas = mCasillas.get(inicial - 1).getTotalFichas(colorFicha) > 0;
                inicial++;
            }
        }
        return !encontradas;
    }

    protected int totalFichas(int numCasilla) {
        return mCasillas.get(numCasilla - 1).getTotalFichas();
    }

    protected EstadoCasilla estadoCasilla(int numCasilla) {
        return mCasillas.get(numCasilla - 1).getEstado();
    }

    protected int totalEnBarra(ColorFicha colorFicha) {
        return mTotalEnBarra.get(colorFicha.ordinal());
    }

    protected int totalFuera(ColorFicha colorFicha) {
        return mTotalFuera.get(colorFicha.ordinal());
    }
}
