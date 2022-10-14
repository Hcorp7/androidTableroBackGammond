package com.example.tablerosdejuego.ui.main.logica;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.tablerosdejuego.R;
import com.example.tablerosdejuego.ui.main.Consola;
import com.example.tablerosdejuego.ui.main.enumerados.ColorFicha;
import com.example.tablerosdejuego.ui.main.enumerados.EstadoCasilla;
import com.example.tablerosdejuego.ui.main.enumerados.EstadoJuego;
import com.example.tablerosdejuego.ui.main.grafica.FichaGrafica;

import org.json.JSONObject;

import java.util.HashMap;

public class LogicaJuego extends AndroidViewModel {
    private int mIdRecursoTablero;
    private int mIdRecursoFichaBlanca;
    private int mIdRecursoFichaNegra;
    private int mIdRecursoDadoBlanco;
    private int mIdRecursoDadoNegro;
    private EstadoJuego mEstado;
    private GestorCasillasLogicas mGestorCasillasLogicas;
    private int mValorDado1;
    private int mValorDado2;
    private boolean mEsTiradaMultiple;
    private int mValorMultiple;
    private ColorFicha mColorTurno;
    private boolean mOfrecerDados;
    private MovimientosPosibles mMovimientos;

    public LogicaJuego(@NonNull Application application) {
        super(application);
        mIdRecursoTablero = R.drawable.tablero_madera_cuero_sobre_tela;
        mIdRecursoFichaBlanca = R.drawable.ficha_piedra_amarilla;
        mIdRecursoFichaNegra = R.drawable.ficha_negra_volcan;
        mIdRecursoDadoBlanco = R.drawable.caras_dado_rojo;
        mIdRecursoDadoNegro = R.drawable.caras_dado_azul;
        mGestorCasillasLogicas = new GestorCasillasLogicas();
        mEstado = EstadoJuego.ELECCION_TURNO;
    }

    private void establecerTurnoSalida() {
        mColorTurno = mValorDado1 > mValorDado2 ? ColorFicha.NEGRA : ColorFicha.BLANCA;
        mEstado = EstadoJuego.TURNO_SALIDA;
    }

    private void siguienteTurno() {
        mColorTurno = mColorTurno == ColorFicha.BLANCA ? ColorFicha.NEGRA : ColorFicha.BLANCA;
        mEstado = EstadoJuego.EN_JUEGO;
        Consola.mostrar("Se establece el turno: " + mColorTurno);
    }

    public boolean blancasMueven() {
        return mColorTurno == ColorFicha.BLANCA;
    }

    private boolean esTurnoCorrecto(FichaLogica ficha) {
        Consola.mostrar("Es turno correcto " + (ficha.color() == mColorTurno));
        return ficha.color() == mColorTurno;
    }

    /**
     * Supervisa que el avance de casillas sea acorde a los valores no consumidos de los dados y
     * pasando por casillas no bloqueadas por el color contrario.
     * <p>
     * En los avances dobles (con la suma de los dados) se inspecciona la casilla del primer avance
     * ya que la casilla final siempre es inspeccionada por el GestorDeCasillas. Debido a esta
     * circunstancia el método se menciona como 'Posible', ya que puede dar por válido el avance
     * <p>
     * Cuando en un salto doble sea inevitable pasar primeramente por una casilla con ficha capturable
     * se rechazará el movimiento para obligar a la captura de la ficha, pues no existe la captura
     * automática. Si la captura puede ser evitada se permitirá el movimiento asumiendo por defecto
     * que el jugador no desea capturar.
     * <p>
     * ATENCIÓN: Este método no puede modificar valores ni establecerlos porque es uno de los métodos
     * de consulta relacionados con el intento de movimiento de una ficha.
     *
     * @param ficha
     * @param numCasDestino
     * @return
     */
    private boolean esPosibleAvanceCorrecto(FichaLogica ficha, int numCasDestino, int avance, ColorFicha colorFicha) {
        boolean esOk;
        Consola.mostrar("Logica Juego/ avance: " + avance + " Dado1: " + mValorDado1 + " Dado2: " + mValorDado2);
        esOk = avance == mValorDado1 || avance == mValorDado2; //Comprobamos si el avance es sencillo o multiple (true = sencillo).

        if (!esOk) {
            if (mEsTiradaMultiple) {
                float totSaltos = avance / mValorDado1;
                esOk = avance % mValorDado1 == 0
                        && totSaltos <= 4
                        && mValorMultiple - totSaltos * mValorDado1 >= 0;
                Consola.mostrar("LogicaJuego/posibleAvance/Validación de avances multiples: " + esOk);
                if (esOk) { //Aquí no es necesario incluir ..&& totSaltos > 1 porque previamente ya comprobamos si el salto es sencillo o múltiple.
                    int saltoIntermedio = 1;
                    while (saltoIntermedio < totSaltos && esOk) {
                        esOk = esSaltoLegal(ficha.color(), ficha.getNumCasilla(), mValorDado1 * saltoIntermedio);
                        saltoIntermedio++;
                    }
                    Consola.mostrar("LogicaJuego/posibleAvance/Salto intermedio multiple legal:" + esOk);
                }
            } else {
                if (avance == mValorDado1 + mValorDado2) {
                    int saltosIlegales = 0;
                    for (int salto : new int[]{mValorDado1, mValorDado2}) {
                        if (!esSaltoLegal(ficha.color(), ficha.getNumCasilla(), salto))
                            saltosIlegales++;
                    }
                    esOk = saltosIlegales < 2;
                    Consola.mostrar("LogicaJuego/posibleAvance/Avance multiple con saltos malos: " + saltosIlegales);
                }
            }
        }
        Consola.mostrar("LogicaJuego/posibleAvance/Es posible avance: " + esOk);
        return esOk;
    }

    /**
     * Método auxiliar
     *
     * @param colorFicha
     * @param numCasilla
     * @param salto
     * @return
     */
    private boolean esSaltoLegal(ColorFicha colorFicha, int numCasilla, int salto) {
        boolean esLegal;
        int casillaSalto = colorFicha == ColorFicha.NEGRA ? numCasilla - salto : numCasilla + salto;
        esLegal = (casillaSalto > 0 && casillaSalto < 25);
        if (!esLegal) Consola.mostrar("esSaltoLegal/Casilla sobrepasada, salto: " + casillaSalto);
        else {
            esLegal = estadoCasilla(casillaSalto).esCompatible(colorFicha);
            Consola.mostrar("esSaltoLegal/Casilla de salto: " + casillaSalto + "  estado: " + estadoCasilla(casillaSalto).name() + " salto legal: " + esLegal);
        }
        return esLegal;
    }

    /**
     * ATENCION: Este método no comprueba nada. Ha de llamarse con posterioridad a un Verdadero por
     * parte de todos los métodos que comprueban que un movimiento de ficha es legal.
     *
     * @param consumo
     */
    private void consumoDados(int consumo) {
        Consola.mostrar("Consumo dados/Entrada/ Tirada multiple: " + mEsTiradaMultiple + " el consumo es: " + consumo + " Dado1: " + mValorDado1 + " Dado2: " + mValorDado2 + " valorMultiple: " + mValorMultiple);
        if (mEsTiradaMultiple) mValorMultiple -= consumo;
        else {
            if (consumo == mValorDado1) mValorDado1 = 0;
            else if (consumo == mValorDado2) mValorDado2 = 0;
            else {
                mValorDado1 = 0;
                mValorDado2 = 0;
            }
        }
        Consola.mostrar("Consumo dados/Salida/ Tirada multiple: " + mEsTiradaMultiple + " el consumo es: " + consumo + " Dado1: " + mValorDado1 + " Dado2: " + mValorDado2 + " valorMultiple: " + mValorMultiple);
    }

    public int getIdRecursoTablero() {
        return mIdRecursoTablero;
    }

    public int getIdRecursoFicha(ColorFicha colorFicha) {
        return colorFicha == ColorFicha.BLANCA ? mIdRecursoFichaBlanca : mIdRecursoFichaNegra;
    }

    public int getIdRecursoDado(ColorFicha colorFicha) {
        return colorFicha == ColorFicha.BLANCA ? mIdRecursoDadoBlanco : mIdRecursoDadoNegro;
    }

    public EstadoJuego getEstadoJuego() {
        return mEstado;
    }

    private EstadoCasilla estadoCasilla(int numCasilla) {
        return mGestorCasillasLogicas.estadoCasilla(numCasilla);
    }

    public boolean fichaACasilla(FichaGrafica fichaGrafica, int numCasilla) {
        FichaLogica ficha = fichaGrafica.getFichaLogica();
        ColorFicha colorFicha = ficha.color();
        int avance;
        if (colorFicha.equals(ColorFicha.NEGRA)) {
            avance = ficha.getNumCasilla() == 0 ? 25 - numCasilla : ficha.getNumCasilla() - numCasilla;
        } else avance = numCasilla - ficha.getNumCasilla();
        boolean esOk = esTurnoCorrecto(ficha)
                && esPosibleAvanceCorrecto(ficha, numCasilla, avance, colorFicha)
                && mGestorCasillasLogicas.fichaACasilla(ficha, numCasilla);
        if (esOk) revisionTurnos(avance);
        return esOk;
    }

    public boolean fichaAFuera(FichaGrafica fichaGrafica) {
        FichaLogica ficha = fichaGrafica.getFichaLogica();
        int avance = avanceParaSalir(ficha);
        boolean esOk = avance > 0
                && esTurnoCorrecto(ficha)
                && mGestorCasillasLogicas.fichaAFuera(ficha);
        if (esOk) revisionTurnos(avance);
        return esOk;
    }

    public void fichaABarra(FichaGrafica ficha) {
        mGestorCasillasLogicas.fichaABarra(ficha.getFichaLogica());
    }

    /**
     * Encuentra el avance optimo para una ficha que quiere salir.
     * En caso de poder hacerlo con cualquier dado, el avance tendrá el valor del dado más bajo.
     * Si la tirada es múltiple, hará uso del multiplo más económico.
     * <p>
     * Este método es auxiliar del método fichaAFuera().
     *
     * @param ficha
     * @return
     */
    public int avanceParaSalir(FichaLogica ficha) {
        int numCasilla = ficha.getNumCasilla();
        int avance = 0;
        //Comprobar si la casilla está a palo de algún valor de los dados
        int casillaAPalo = ficha.esNegra() ? numCasilla : 25 - numCasilla;
        int i = 0;
        int[] valoresDados = {mValorDado1, mValorDado2};
        do {
            if (valoresDados[i] == casillaAPalo) avance = valoresDados[i];
        } while (++i < 2 && avance == 0);
        Consola.mostrar("LogicaJuego/avanceParaSalir/La casilla está a palo de: " + avance);

        //Si no está a palo, comprobar que:
        if (avance == 0) {
            // que no hay fichas anteriores
            int casillasAComprobar = ficha.esNegra() ? 6 - numCasilla : numCasilla - 19;
            int paso = ficha.esNegra() ? 1 : -1;
            boolean fichasAnteriores = false;
            int numComprobar = numCasilla;
            while (casillasAComprobar-- > 0 && !fichasAnteriores) {
                numComprobar += paso;
                fichasAnteriores = estadoCasilla(numComprobar).coincideColor(ficha.color());
            }
            Consola.mostrar("LogicaJuego/avanceParaSalir/hay fichas anteriores: " + fichasAnteriores);

            if (!fichasAnteriores) {
                // que no habiendo fichas anteriores tiene dados para salir
                if (mEsTiradaMultiple) {
                    if (casillaAPalo <= mValorMultiple) {
                        int div = casillaAPalo / mValorDado1;
                        avance = casillaAPalo % mValorDado1 == 0 ? div : ++div; //Cuidado-> he cambiado div++ por ++div ya que avance no debería obtener
                    }
                    Consola.mostrar("LogicaJuego/avanceParaSalir/Es tirada multiple y tiene un avance de: " + avance);

                } else if (casillaAPalo <= (mValorDado1 + mValorDado2)) {
                    int dif1 = mValorDado1 - casillaAPalo;
                    int dif2 = mValorDado2 - casillaAPalo;
                    if (dif1 < 0 && dif2 < 0) avance = mValorDado1 + mValorDado2;
                    else if (dif1 > 0 && dif2 > 0)
                        avance = Math.min(mValorDado1, mValorDado2); //Seleccionamos el dado de menor valor.
                    else avance = dif1 > 0 ? mValorDado1 : mValorDado2;
                    Consola.mostrar("LogicaJuego/avanceParaSalir/Es tirada mixta y tiene un avance de: " + avance);

                }
            }
        }
        return avance;
    }

    private void revisionTurnos(int avance) {
        Consola.mostrar("LogicaJuego/Revision de turnos");
        consumoDados(avance);
        if ((mEsTiradaMultiple && mValorMultiple == 0) || (!mEsTiradaMultiple && (mValorDado1 + mValorDado2 == 0))) {
            siguienteTurno();
            mOfrecerDados = true;
            Consola.mostrar("Logica Juego/ mOfrecerDados: " + mOfrecerDados);
        }
    }

    public GestorCasillasLogicas gestorCasillas() {
        return mGestorCasillasLogicas;
    }

    public HashMap<Integer, JSONObject> distribucionFichas() {
        return mGestorCasillasLogicas.posicionFichas();
    }

    public int casillaEnCaptura() {
        return mGestorCasillasLogicas.casillaEnCaptura();
    }

    public boolean hayCasillasEnCaptura() {
        return mGestorCasillasLogicas.hayCasillasEnCaptura();
    }

    public boolean hayFichasEnBarra(ColorFicha colorFicha) {
        return mGestorCasillasLogicas.hayFichasEnBarra(colorFicha);
    }

    public int totalFichasCasilla(int numCasilla) {
        return mGestorCasillasLogicas.totalFichas(numCasilla);
    }

    public int totalEnBarra(ColorFicha colorFicha) {
        return mGestorCasillasLogicas.totalEnBarra(colorFicha);
    }

    public int totalFuera(ColorFicha colorFicha) {
        return mGestorCasillasLogicas.totalFuera(colorFicha);
    }

    ///////  Estos métodos se utilizan para dialogar con el cubilete

    public void setValorDados(int valorDado1, int valorDado2) {
        //Consola.mostrar("Se actualiza el valor de los dados a: " + valorDado1 + " y " + valorDado2);
       /* if (mEstado == EstadoJuego.EN_JUEGO) {
            valorDado1 = 6;
            valorDado2 = 6;//valorDado1;

            mColorTurno = ColorFicha.NEGRA;
        }*/
        mValorDado1 = valorDado1;
        mValorDado2 = valorDado2;
        mEsTiradaMultiple = valorDado1 == valorDado2;
        mValorMultiple = mValorDado1 * 4;
        if (mEstado == EstadoJuego.ELECCION_TURNO){
            if( mValorDado1 != mValorDado2) {
                if (true){
                    mValorDado1 = 1;
                    mValorDado2 = 1;
                    mEsTiradaMultiple = mValorDado1 == mValorDado2;
                    mValorMultiple = mValorDado1 * 4;
                    mColorTurno = ColorFicha.NEGRA;
                    mEstado = EstadoJuego.TURNO_SALIDA;
                   // mMovimientos.MovimientosPosibles(mGestorCasillasLogicas.posicionFichas(), mColorTurno, mValorDado1, mValorDado2, totalEnBarra(mColorTurno));

                }else establecerTurnoSalida();
            }
            mOfrecerDados = true;
        }
        mMovimientos = new MovimientosPosibles(mGestorCasillasLogicas.posicionFichas(), mColorTurno, mValorDado1, mValorDado2, totalEnBarra(mColorTurno));
        if (mMovimientos.estaBloqueado()) {
            mValorDado1 = 0;
            mValorDado2 = 0;
            siguienteTurno();
        }
    }

    public boolean ofrecerDados() {
        return mOfrecerDados;
    }

    public void dadosOfrecidos() {
        mOfrecerDados = false;
    }

    ///////
}