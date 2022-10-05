package com.example.tablerosdejuego.ui.main.enumerados;

public enum EstadoCasilla {
    LIBRE,
    BLOQUEADA_NEGRAS, // Bloqueada por fichas negras
    BLOQUEADA_BLANCAS,
    DESBLOQUEADA_BLANCAS, // Hay una sola ficha blanca
    DESBLOQUEADA_NEGRAS,
    CAPTURA;

    /**
     * Saber si la ficha puede ir a una casilla.
     * @param colorFicha
     * @return
     */
    public boolean esCompatible(ColorFicha colorFicha) {
        boolean compatibilidad;
        if (this == LIBRE) compatibilidad = true;
        else
            compatibilidad = this != (colorFicha == ColorFicha.BLANCA ? BLOQUEADA_NEGRAS : BLOQUEADA_BLANCAS);
        return compatibilidad;
    }

    public boolean coincideColor(ColorFicha colorACoincidir) {
        boolean coincidencia;
        return this == (colorACoincidir == ColorFicha.BLANCA ? BLOQUEADA_BLANCAS : BLOQUEADA_NEGRAS)
                || this == (colorACoincidir == ColorFicha.BLANCA ? BLOQUEADA_BLANCAS : BLOQUEADA_NEGRAS);
    }
}
