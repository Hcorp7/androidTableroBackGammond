package com.example.tablerosdejuego.ui.main.controladores;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tablerosdejuego.R;
import com.example.tablerosdejuego.ui.main.Consola;
import com.example.tablerosdejuego.ui.main.enumerados.ColorFicha;
import com.example.tablerosdejuego.ui.main.grafica.Cubilete;
import com.example.tablerosdejuego.ui.main.grafica.FichaGrafica;
import com.example.tablerosdejuego.ui.main.logica.FichaLogica;
import com.example.tablerosdejuego.ui.main.logica.GestorCasillasLogicas;
import com.example.tablerosdejuego.ui.main.logica.LogicaJuego;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class MainFragment extends Fragment {
    private LogicaJuego mLogica;
    private HashMap<Integer, FichaGrafica> mFichasGraficas;
    private int mHashFichaCapturadora = 0;
    private final Vector<Integer> mFichasEnBarra = new Vector<>(); //Las blancas se añaden siempre al indice 0, las negras se añaden al final.
    private float mYfondo = 1108f;
    private RelativeLayout mEscenario;
    private final float mEjeXFuera = 1795f;
    private final float mEjeYFueraBlancas = 1045f;
    private final float mEjeYFueraNegras = 45f;
    private final Vector<JSONObject> mJugada = new Vector<>();
    // private Cubilete mCubilete;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.escenario_juego, container, false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLogica = new ViewModelProvider(this).get(LogicaJuego.class);
        mFichasGraficas = new HashMap();

        //Carga de tablero
        AppCompatImageView tablero = view.findViewById(R.id.tablero);
        tablero.setImageDrawable(getResources().getDrawable(mLogica.getIdRecursoTablero(), requireActivity().getTheme()));

        //
        mEscenario = view.findViewById(R.id.escenario);
        //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mEscenario.getLayoutParams();

        //Botón de retorno
        AppCompatImageView retorno = mEscenario.findViewById(R.id.restaurar);
        retorno.setX(33f);
        retorno.setY(550f);
        retorno.setBackgroundColor(getResources().getColor(R.color.teal_700, getContext().getTheme()));
        retorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jugadaAtras();
            }
        });

        //Fichas
        creacionFichasGraficas();
        mFichasGraficas.forEach((h, ficha) -> {
            mEscenario.addView((AppCompatImageView) ficha);
        });
        TextView contador = mEscenario.findViewById(R.id.fueraNegras);
        contador.setRotation(180);
        contador.setY(mEjeYFueraNegras + 75 + 25);
        contador.setX(mEjeXFuera + 75);
        contador.setText("");
        contador = mEscenario.findViewById(R.id.fueraBlancas);
        contador.setY(mEjeYFueraBlancas - 25);
        contador.setX(mEjeXFuera);
        contador.setText("");

        //Cubilete
        new Cubilete(mLogica, mEscenario, 510f, getActivity());

    }

    /**
     * Las fichas gráficas son creadas y estas contendrán la ficha lógica de las casillas lógicas que albergan la situación
     * a recrear. La única razón para hacer esto es evitar una doble ficha lógica pues la gráfica ha de tener una para identificar su
     * color y la casilla a la que pertenece y el contexto lógico necesita otra. Así se decide que, como la primera vez que nace una ficha
     * esta es lógica y esta ficha lógica siempre permanece, cuando la ficha gráfica se genera, guarde un puntero de la ficha lógica que
     * se encuentra en la casilla y lugar que la ficha gráfica ocupará en el tablero. Por otra parte, en cuanto al proceso de 'mover'
     * una ficha lógica de una casilla a otra se considera más rápido localizar el puntero de la ficha, borrarlo y agregarlo a la casilla
     * de destino, que borrar el puntero en la casilla origen e instanciár una nueva ficha lógica para guardar su puntero en la
     * casilla de destino.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void creacionFichasGraficas() {
        FichaGrafica fichaGrafica;
        HashMap<Integer, JSONObject> distribucion = mLogica.distribucionFichas();
        //En casillas
        for (int num_casilla = 1; num_casilla < 25; num_casilla++) {
            if (distribucion.containsKey(num_casilla)) {
                int total_fichas = 0;
                ColorFicha colorFicha = null;
                try {
                    total_fichas = (int) distribucion.get(num_casilla).get("total");
                    colorFicha = (ColorFicha) distribucion.get(num_casilla).get("color");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int lugar = 1; lugar <= total_fichas; lugar++) {
                    fichaGrafica = new FichaGrafica(getContext(), mLogica.gestorCasillas().getFichaLogica(num_casilla, lugar));
                    moverACasilla(fichaGrafica, lugar);
                    fichaGrafica.setOnTouchListener(new fichaOnTouch());
                    fichaGrafica.setImageResource(mLogica.getIdRecursoFicha(colorFicha));
                    mFichasGraficas.put(fichaGrafica.hashCode(), fichaGrafica);
                }
            }
        }
        //A Fuera
        for (ColorFicha colorFicha : ColorFicha.values()) {
            int tFuera = mLogica.totalFuera(colorFicha);
            if (tFuera > 0) {
                TextView contador = mEscenario.findViewById(colorFicha == ColorFicha.NEGRA ? R.id.fueraNegras : R.id.fueraBlancas);
                for (int i = 0; i < tFuera; i++) {
                    fichaGrafica = new FichaGrafica(getContext(), new FichaLogica(colorFicha, GestorCasillasLogicas.NUM_AREA_FUERA, 0));
                    fichaGrafica.setImageResource(mLogica.getIdRecursoFicha(colorFicha));
                    if (fichaGrafica.esNegra()) {
                        fichaGrafica.setY(mEjeYFueraNegras);
                        contador.setY(fichaGrafica.getY() + 75 + 25);
                        contador.setX(mEjeXFuera + 75);
                    } else {
                        fichaGrafica.setY(mEjeYFueraBlancas);
                        contador.setY(fichaGrafica.getY() - 25);
                        contador.setX(mEjeXFuera);
                    }
                    fichaGrafica.setX(mEjeXFuera);
                    fichaGrafica.ampliar(i);
                    mFichasGraficas.put(fichaGrafica.hashCode(), fichaGrafica);
                }
                contador.setText(String.valueOf(tFuera));
            }
        }
        //En Barra
        for (ColorFicha colorFicha : ColorFicha.values()) {
            for (int f = 0; f < mLogica.totalEnBarra(colorFicha); f++) {
                fichaGrafica = new FichaGrafica(getContext(), new FichaLogica(colorFicha, GestorCasillasLogicas.NUM_AREA_BARRA, 0));
                fichaGrafica.setOnTouchListener(new fichaOnTouch());
                fichaGrafica.setImageResource(mLogica.getIdRecursoFicha(colorFicha));
                if (fichaGrafica.esNegra()) mFichasEnBarra.add(0, fichaGrafica.hashCode());
                else mFichasEnBarra.add(fichaGrafica.hashCode());
                mFichasGraficas.put(fichaGrafica.hashCode(), fichaGrafica);
            }
        }
        int totBlancas = mLogica.totalEnBarra(ColorFicha.BLANCA);
        int totNegras = mLogica.totalEnBarra(ColorFicha.NEGRA);
        int longGrupo = (totBlancas + totNegras) * (75 + 15);
        float x = 939.5f - (75f / 2f);
        float y = ((mYfondo + 93) / 2f) - (longGrupo / 2f);
        Iterator<Integer> hash = mFichasEnBarra.iterator();
        while (hash.hasNext()) {
            FichaGrafica fichaBarra = mFichasGraficas.get(hash.next());
            fichaBarra.setX(x);
            fichaBarra.setY(y);
            fichaBarra.memorizarPosicion(x, y);
            y += 75 + 15;
        }
    }

    private void jugadaAtras() {
        if (!mJugada.isEmpty()) {
            JSONObject jsonObject = mJugada.lastElement();
            JSONArray jsonArray = null;
            String etiqueta = "";
            Integer codigoHash = 0;
            int valor = -1;
            try {
                Iterator<String> iterator = jsonObject.keys();
                etiqueta = iterator.next();
                jsonArray = (JSONArray) jsonObject.get(etiqueta);
                codigoHash = jsonArray.getInt(0);
                valor = jsonArray.getInt(1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (etiqueta == "ficha") {
                FichaGrafica ficha = mFichasGraficas.get(codigoHash);
                boolean fichaEnBarra = ficha.estaEnBarra();
                boolean esFichaCapturadora = mHashFichaCapturadora == ficha.hashCode();
                boolean esFichaFuera = ficha.estaFuera();
                boolean vueltaABarra = valor == GestorCasillasLogicas.NUM_AREA_BARRA;
                Consola.mostrar("jugadaAtras/ La ficha " + ficha.getColorFicha() + "  estaEnBarra: " + fichaEnBarra + "  esFichaCapturadora: " + esFichaCapturadora + "  vueltaABarra: " + vueltaABarra + "  esFichaFuera: " + esFichaFuera);
                if (vueltaABarra) Consola.mostrar("La ficha quiere volver a la barra.");
                if (esFichaCapturadora) mHashFichaCapturadora = 0;
                if (vueltaABarra) {
                    Consola.mostrar("Ficha de color " + ficha.getColorFicha() + "  regresa a barra.");
                    regresarABarra(ficha);
                } else {
                    Consola.mostrar("jugadaAtras/ Ficha de color " + ficha.getColorFicha() + "  fichaACasilla: " + valor);
                    mLogica.fichaACasilla(ficha, valor);
                    moverACasilla(ficha, mLogica.totalFichasCasilla(valor));
                    Consola.mostrar("jugadaAtras/ Jugada atrás a casilla: " + ficha.getNumCasilla());
                }
                mJugada.remove(jsonObject);
                if (esFichaFuera) {
                    ficha.setOnTouchListener(new fichaOnTouch());
                    actualizarContadorFuera(ficha.getColorFicha());
                } else if (fichaEnBarra) {
                    mFichasEnBarra.remove(codigoHash);
                    jugadaAtras();
                    ficha.setLugar(1);
                    ficha.memorizarPosicion(ficha.getX(), ficha.getY());
                    Consola.mostrar("jugadaAtras/ la casilla: " + valor + " tiene: " + mLogica.totalFichasCasilla(valor) + " fichas." + " la ficha tiene el lugar:" + ficha.getLugar());
                }
                distribuidorEspacio(ficha.getNumCasilla());
            }
        }
    }

    /**
     * Función principal para el movimiento de las fichas cuando las arrastra el usuario.
     *
     * @param hasCodeFicha
     */
    private void moverFicha(int hasCodeFicha) {
        boolean fichaMovida = false;
        FichaGrafica ficha = mFichasGraficas.get(hasCodeFicha);
        int numCasilla = ficha.getNumCasilla();

        if (!ficha.estaEnBarra()) {
            boolean esUltima = ficha.getLugar() == mLogica.totalFichasCasilla(numCasilla);
            boolean hayCaptura = mLogica.hayCasillasEnCaptura();
            boolean esFichaCapturada = (mLogica.casillaEnCaptura() == ficha.getNumCasilla()) && (ficha.getLugar() == 1);
            boolean presenciaEnBarra = mLogica.hayFichasEnBarra(ficha.getColorFicha());

            if (!hayCaptura && esUltima && !presenciaEnBarra) {
                //Establecer si se pretende sacar
                boolean quiereSacar = ficha.getX() > (1838 - FichaGrafica.AJUSTE_X);
                fichaMovida = quiereSacar ? sacarFicha(ficha) : intentarMoverPorFicha(ficha);
            } else if (hayCaptura && esFichaCapturada) {
                fichaMovida = moverCapturadaABarra(ficha);
            } else {
                ficha.recuperarPosicion();
                Consola.mostrar("casilla: " + numCasilla + " lugar: " + ficha.getLugar() + " hay captura: " + hayCaptura + "  esUltima: " + esUltima + "  esFichaCapturada: " + esFichaCapturada + "  presenciaEnbarra: " + presenciaEnBarra);
            }
        } else {
            fichaMovida = intentarMoverPorFicha(ficha);
        }
        if (fichaMovida) {
            if (ficha.getNumCasilla() != 0) {
                //mejoras-> Si la ficha movida no es una ficha capturada, hay que añadir los valores de los dados usados.
            }
            agregarJugada(ficha.getClass(), ficha.hashCode(), numCasilla);
            distribuidorEspacio(numCasilla);
            //if (numCasilla != ficha.getNumCasilla()) distribuidorEspacio(ficha.getNumCasilla());  //Se supone que si la ficha se ha movido, siempre se cumple.
            distribuidorEspacio(ficha.getNumCasilla());
        }
    }

    private void agregarJugada(Class claseObjeto, int codigoHash, int valor) {
        JSONArray jsonArray = new JSONArray();
        String etiqueta = claseObjeto == FichaGrafica.class ? "ficha" : "dado";
        jsonArray.put(codigoHash);
        jsonArray.put(valor);
        JSONObject jsonObject = new JSONObject();
        try {
            Consola.mostrar("Agregar jugada: " + etiqueta + " de color " + (mFichasGraficas.get(codigoHash)).getColorFicha() + " Casilla de retorno: " + valor);
            jsonObject.put(etiqueta, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mJugada.add(jsonObject);
    }

    /**
     * Mueve una ficha a una casilla y lugar en esta.
     * Esta función NO HACE ningún tipo de comprobaciones lógicas. Es una función totalmente imperativa.
     *
     * @param ficha
     * @param lugar Hace referencia al número que ocupa en la casilla comenzando desde la base.
     */
    private void moverACasilla(FichaGrafica ficha, int lugar) {
        float x = 0;
        float y = 0;
        int num_casilla;
        num_casilla = ficha.getNumCasilla();
        if (num_casilla == 0) {
            //Aquí no debería entrar nunca.
            String s = "Se ha entrado con número de casilla cero. Esta entrada no está implementada.";
            Log.d("Hcorp", "MainFragment.java/moverACasilla/ " +s);
            throw new IllegalStateException(s);
        } else {
            int factorX = num_casilla < 13 ? 13 - num_casilla : num_casilla - 12;
            x = 50 + (125 * factorX);
            if (num_casilla < 7 || num_casilla > 18) x += 80;
            y = num_casilla < 13 ? 40 + (lugar - 1) * 83 : 1055 - (lugar - 1) * 83;
        }
        ficha.setX(x);
        ficha.setY(y);
        ficha.memorizarPosicion(x, y);
    }

    /**
     * Establece el eje X para una ficha que se mueve a una casilla.
     * -Se calcula la casilla a la que agregarla (eje X y número de casilla de destino).
     * -Si la ficha proviene de la barra se revisa que las casillas sean las interiores y del color correspondiente.
     * -Si la ficha proviene de la barra se actualizan el vector de fichas en barra.
     * -Se verifica que el avance de la ficha sea en la dirección según su color.
     *
     * @param ficha
     * @return
     */
    private boolean intentarMoverPorFicha(FichaGrafica ficha) {
        float x;
        float y = 0;
        int num_casilla = ficha.getNumCasilla();
        int columna = num_casilla != 0 ? 0 : 6;
        do {
            columna++;
            x = 50 + (125 * columna);
            if (columna > 6) x += 80;
        } while (!(ficha.getX() + 62.5 > x && ficha.getX() - 62.5 < x) && columna < 13);
        boolean esOk = columna < 13;
        if (esOk) {
            int casilla = ficha.getY() < 525 ? 13 - columna : 12 + columna;
            esOk = casilla != num_casilla;
            if (esOk & ficha.getNumCasilla() == 0) {
                //Verificar salida correcta
                esOk = (ficha.esNegra() && casilla > 18)
                        || (ficha.esBlanca() && casilla < 7);
            } else {
                //Verificar avance correcto
                esOk = ficha.esBlanca() ? ficha.getNumCasilla() < casilla : ficha.getNumCasilla() > casilla;
            }
            if (esOk) {
                esOk = mLogica.fichaACasilla(ficha, casilla);
                if (esOk) {
                    int lugar = mLogica.totalFichasCasilla(casilla);
                    y = casilla < 13 ? 40 + (lugar - 1) * 83 : 1055 - (lugar - 1) * 83;
                    if (mFichasEnBarra.contains(ficha.hashCode()))
                        mFichasEnBarra.remove((Integer) ficha.hashCode());
                    if (mLogica.casillaEnCaptura() == casilla)
                        mHashFichaCapturadora = ficha.hashCode();
                } else Consola.mostrar("Se ha rechazado ficha_a_casilla");
            } else {
                Consola.mostrar("No se permite mover a la misma casilla o casilla de salida equivocada de color.");
            }
        }
        if (esOk) ficha.moverHastaXY(x, y, true);
        else ficha.recuperarPosicion();
        return esOk;
    }

    /**
     * Calcula las coordenadas que una ficha capturada ha de tener en la barra.
     * <p>
     * <p>
     * <p>
     * Es el método para la ficha capturada que se ha de mover a la barra.
     * Este método entra en funcionamiento cuando se suelta la ficha capturada.
     * <p>
     * -Agrega a la barra según el color; blancas abajo, negras arriba.
     * -Redistribulle el espacio que ocupan las fichas centrándolas en la barra
     * -Sitúa la ficha capturadora en la base de la casilla.
     *
     * @param ficha
     */
    private boolean moverCapturadaABarra(FichaGrafica ficha) {
        agregadorDeBarra(ficha);
        FichaGrafica fichaCapturadora = mFichasGraficas.get(mHashFichaCapturadora);
        if (fichaCapturadora == null) throw new Error("no hay hash de ficha.");
        fichaCapturadora.setLugar(1);
        distribuidorEspacio(fichaCapturadora.getNumCasilla());
        mHashFichaCapturadora = 0;
        return true;
    }

    /**
     * Gestiona la vuelta a la barra de una ficha cuando se rectifica el movimiento de salida.
     * Este caso puede darse porque se ha salido con un valor que se quiere cambiar por otro.
     *
     * @param ficha La ficha que ha salido de la barra y se desea corregir el valor de su salida.
     */
    private void regresarABarra(FichaGrafica ficha) {
        agregadorDeBarra(ficha);
    }

    /**
     * Funcionalidad común a los métodos que se encargan de agregar una ficha a la barra.
     * -Registra la ficha a nivel lógico.
     * -Gestiona el nivel gráfico.
     *
     * @param ficha
     */
    private void agregadorDeBarra(FichaGrafica ficha) {
        if (ficha.esNegra()) mFichasEnBarra.add(0, ficha.hashCode());
        else mFichasEnBarra.add(ficha.hashCode());
        mLogica.fichaABarra(ficha);
        int totBlancas = mLogica.totalEnBarra(ColorFicha.BLANCA);
        int totNegras = mLogica.totalEnBarra(ColorFicha.NEGRA);
        int longGrupo = (totBlancas + totNegras) * (75 + 15);
        float x = 939.5f - (75f / 2f);
        float y = ((mYfondo + 93) / 2f) - (longGrupo / 2f);
        Iterator<Integer> hash = mFichasEnBarra.iterator();
        while (hash.hasNext()) {
            FichaGrafica fichaBarra = mFichasGraficas.get(hash.next());
            fichaBarra.moverHastaXY(x, y, false);
            y += 75 + 15;
        }
    }

    private boolean sacarFicha(FichaGrafica ficha) {
        if (mLogica.fichaAFuera(ficha)) {
            ficha.setOnTouchListener(null);
            int tFuera = actualizarContadorFuera(ficha.getColorFicha());
            ficha.ampliar(tFuera);
            ficha.moverHastaXY(mEjeXFuera, ficha.esNegra() ? mEjeYFueraNegras : mEjeYFueraBlancas, false);
        } else ficha.recuperarPosicion();
        return true;
    }

    /**
     * Actualiza el marcador gráfico de fichas fuera según el color.
     *
     * @param colorFicha
     * @return Devuelve el número de fichas fuera que hay del color consultado.
     */
    private int actualizarContadorFuera(ColorFicha colorFicha) {
        int tFuera = mLogica.totalFuera(colorFicha);
        TextView contador = mEscenario.findViewById(colorFicha == ColorFicha.NEGRA ? R.id.fueraNegras : R.id.fueraBlancas);
        contador.setText(tFuera > 0 ? Integer.toString(tFuera) : "");
        return tFuera;
    }

    /**
     * Sitúa todas las fichas de una casilla para que no ocupen un espacio excesivo.
     * -Interviene en la actualización del eje Y.
     * -Establece la propiedad z para evitar solapamientos graficamente incorrectos.
     *
     * @param numCasilla
     */
    private void distribuidorEspacio(int numCasilla) {
        if (numCasilla > 0 && numCasilla < 25) {
            int totFichas = mLogica.totalFichasCasilla(numCasilla);
            for (FichaGrafica ficha : (Iterable<FichaGrafica>) mFichasGraficas.values()) {
                if (ficha.getNumCasilla() == numCasilla) {
                    int lugar = ficha.getLugar();
                    float s = (totFichas > 6) ? 83f * 6f / totFichas : 83f;
                    float y = numCasilla < 13 ? 40f + (lugar - 1) * s : 1055f - (lugar - 1f) * s;
                    ficha.moverHastaXY(ficha.getFichaLogica().getHogarX(), y, false);
                }
            }
        }
    }

    class fichaOnTouch implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Consola.mostrar(event.getRawY() + " " + event.getY() + " " + event.getYPrecision());
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float ex = event.getRawX();
                    float ey = event.getRawY();
                    //Consola.mostrar("ex: " + ex + ", eY: " + ey);
                    if (ex < 209) ex = 209;
                    if (ey < 93) ey = 93;
                    else if (ey > mYfondo) ey = mYfondo;
                    v.setX(ex - FichaGrafica.AJUSTE_X);
                    v.setY(ey - FichaGrafica.AJUSTE_Y);
                    break;
                case MotionEvent.ACTION_DOWN:
                    v.setScaleX(FichaGrafica.ESCALA_MAYOR - (FichaGrafica.ESCALA_MAYOR * FichaGrafica.CORRECCION_ESCALA));
                    v.setScaleY(FichaGrafica.ESCALA_MAYOR);
                    break;
                case MotionEvent.ACTION_UP:
                    //v.setScaleX(FichaGrafica.ESCALA_NORMAL - (FichaGrafica.ESCALA_NORMAL * FichaGrafica.CORRECCION_ESCALA));
                    //v.setScaleY(FichaGrafica.ESCALA_NORMAL);
                    moverFicha(v.hashCode());
                    break;
            }
            return true;
        }
    }
}