package com.uade.vidification.model.strategy;

/**
 * Enum que actúa como fábrica simple de estrategias. Se persiste en el hábito y permite
 * reconstruir la {@link EstrategiaPuntaje} correspondiente cuando se calcula el puntaje.
 */
public enum TipoEstrategia {
    BASE {
        @Override
        public EstrategiaPuntaje crear() {
            return new PuntajeBase();
        }
    },
    POR_RACHA {
        @Override
        public EstrategiaPuntaje crear() {
            return new PuntajePorRacha();
        }
    };

    public abstract EstrategiaPuntaje crear();
}
